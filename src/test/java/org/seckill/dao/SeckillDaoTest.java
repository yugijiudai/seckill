package org.seckill.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author yugi
 * @apiNote
 * @since 2017-06-08
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-dao.xml"})
@Slf4j
public class SeckillDaoTest {


    @Resource
    private SeckillDao seckillDao;

    @Test
    public void reduceNumber() throws Exception {
        int insert = seckillDao.reduceNumber(1001, new Date());
        log.info("insert:{}", insert);
    }

    @Test
    public void queryById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        log.info("{}", seckill);
    }

    @Test
    public void queryAll() throws Exception {
        // java没有保存形参的记录  queryAll(int offset, int limit) ->  queryAll(int arg0, int arg1);
        List<Seckill> seckills = seckillDao.queryAll(0, 100);
        log.info("{}", seckills);
    }

}