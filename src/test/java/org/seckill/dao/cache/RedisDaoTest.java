package org.seckill.dao.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author yugi
 * @apiNote
 * @since 2017-06-16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-dao.xml"})
@Slf4j
public class RedisDaoTest {

    private long id = 1000;

    @Resource
    private SeckillDao seckillDao;

    @Resource
    private RedisDao redisDao;


    @Test
    public void testSeckill() throws Exception {
        Seckill seckill = redisDao.getSeckill(id);
        if (seckill == null) {
            seckill = seckillDao.queryById(id);
            if (seckill != null) {
                String result = redisDao.putSeckill(seckill);
                log.info("结果result:{}", result);
                seckill = redisDao.getSeckill(id);
                log.info("结果seckill:{}", seckill);
            }
        }

    }


}