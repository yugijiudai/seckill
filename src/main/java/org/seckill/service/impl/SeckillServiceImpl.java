package org.seckill.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.ExposerDTO;
import org.seckill.dto.SeckillExecutionDTO;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这里发生的异常控制层就算抓住不抛出去,控制层不管有没有配置事务的情况下,这里的事务照样会回滚,在这个类的实现方法中，无论事务传播类型填什么,
 * 例如:有a方法,b方法,c方法,他们都配置了事务,a方法调b方法和c方法,无论这三个方法哪一个失败,事务都会回滚!要想事务不回滚，只能在a,b,c对应的一个方法
 * 改成调用另一个接口声明了事务的方法,而且传播属性还要是Propagation.REQUIRES_NEW
 *
 * @author yugi
 * @apiNote
 * @since 2017-06-09
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {


    @Resource
    private SeckillDao seckillDao;

    @Resource
    private SuccessKilledDao successKilledDao;

    @Resource
    private RedisDao redisDao;

    /**
     * md5盐值字符串,用于混淆MD5,随便写的
     */
    private final String salt = "fuck you";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public ExposerDTO exportSeckillUrl(long seckillId) {
        // 优化点:缓存优化,一致性维护:超时的基础上
        // 1:访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            // 2:访问数据库
            seckill = this.getById(seckillId);
            if (seckill == null) {
                return new ExposerDTO(false, seckillId);
            }
            // 3:放入redis
            String putRedis = redisDao.putSeckill(seckill);
            log.info("插入{}到redis:{}", seckill, putRedis);
        }
        long startTime = seckill.getStartTime().getTime();
        long endTime = seckill.getEndTime().getTime();
        //系统当前时间
        long now = new Date().getTime();
        if (now < startTime || now > endTime) {
            return new ExposerDTO(false, seckillId, now, startTime, endTime);
        }
        // 转化特定字符串的过程,不可逆
        String md5 = this.getMD5(seckillId);
        return new ExposerDTO(true, md5, seckillId);
    }


    @Override
    @Transactional
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    /*
     * 使用注解控制事务方法的优点:
     * 1:开发团队达成一致约定,明确标注事务方法的编程风格
     * 2:保证事务方法的执行时间尽可能短,不要穿插其他网络操作,RPC/HTTP请求或者剥离到事务方法外部
     * 3:不是所有的方法都需要事务,如只有一条修改操作,只读操作不需要事务控制
     *
     */
    public SeckillExecutionDTO executeSeckill(long seckillId, long userPhone, String md5) {
        try {
            if (md5 == null || !this.getMD5(seckillId).equals(md5)) {
                // throw new SeckillException("seckill data rewrite");
                return new SeckillExecutionDTO(seckillId, SeckillStateEnum.DATA_REWRITE);
            }
            // 记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一:seckillId, userPhone
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }
            // 减库存,热点商品竞争
            int updateCount = seckillDao.reduceNumber(seckillId, new Date());
            if (updateCount <= 0) {
                // 没有更新到记录,秒杀结束,rollback
                throw new SeckillCloseException("seckill is closed");
            }
            // commit
            SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
            return new SeckillExecutionDTO(seckillId, SeckillStateEnum.SUCCESS, successKilled);

        }
        catch (SeckillCloseException | RepeatKillException e1) {
            throw e1;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            //所有编译器异常转化成运行期异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    @Override
    public SeckillExecutionDTO executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !this.getMD5(seckillId).equals(md5)) {
            return new SeckillExecutionDTO(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", new Date());
        map.put("result", null);
        // 执行存储过程后,result被赋值
        try {
            seckillDao.killByProcedure(map);
            // 获取result
            Integer result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecutionDTO(seckillId, SeckillStateEnum.SUCCESS, successKilled);
            }
            return new SeckillExecutionDTO(seckillId, SeckillStateEnum.stateOf(result));
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return new SeckillExecutionDTO(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

}
