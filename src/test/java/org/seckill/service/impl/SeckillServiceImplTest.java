package org.seckill.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.ExposerDTO;
import org.seckill.dto.SeckillExecutionDTO;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yugi
 * @apiNote
 * @since 2017-06-12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-service.xml"})
@Slf4j
public class SeckillServiceImplTest {

    @Resource
    private SeckillService seckillServiceImpl;


    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckillList = seckillServiceImpl.getSeckillList();
        log.info("list:{}", seckillList);
    }

    @Test
    public void getById() throws Exception {
        Seckill seckill = seckillServiceImpl.getById(1000);
        log.info("seckill:{}", seckill);
    }


    //集成测试代码完整逻辑,注意可重复执行
    @Test
    public void testSeckillLogic() throws Exception {
        // 4acc607bc67f64352eddeb96e4b646fa
        long secKillId = 1000;
        ExposerDTO exposerDTO = seckillServiceImpl.exportSeckillUrl(secKillId);
        log.info("exposerDTO:{}", exposerDTO);
        if (!exposerDTO.isExposed()) {
            log.warn("秒杀未开始");
            return;
        }
        try {
            SeckillExecutionDTO seckillExecutionDTO = seckillServiceImpl.executeSeckill(secKillId, 13799999998L, exposerDTO.getMd5());
            log.info("seckillExecutionDTO:{}", seckillExecutionDTO);
        }
        catch (RepeatKillException | SeckillCloseException e) {
            log.error("{}", e.getMessage());
        }
    }

    @Test
    public void testExecuteSeckillProcedure() throws Exception {
        long secKillId = 1000;
        ExposerDTO exposerDTO = seckillServiceImpl.exportSeckillUrl(secKillId);
        if (exposerDTO.isExposed()){
            SeckillExecutionDTO seckillExecutionDTO = seckillServiceImpl.executeSeckillProcedure(secKillId, 13799999998L, exposerDTO.getMd5());
            log.info("seckillExecutionDTO:{}", seckillExecutionDTO);
        }
    }


}