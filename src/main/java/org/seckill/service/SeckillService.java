package org.seckill.service;

import org.seckill.dto.ExposerDTO;
import org.seckill.dto.SeckillExecutionDTO;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 * @author yugi
 * @apiNote
 * @since 2017-06-09
 */
public interface SeckillService {

    /**
     * 查询所有秒杀记录
     *
     * @return {@link Seckill}
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId 秒杀id
     * @return {@link Seckill}
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址,否则输出系统时间和秒杀时间
     *
     * @param seckillId 秒杀id
     */
    ExposerDTO exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     *
     * @param seckillId 秒杀id
     * @param userPhone 用户的电话
     * @param md5       md5校验
     */
    SeckillExecutionDTO executeSeckill(long seckillId, long userPhone, String md5);
    // SeckillExecutionDTO executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException;

    /**
     * 执行秒杀操作by存储过程
     *
     * @param seckillId 秒杀id
     * @param userPhone 用户的电话
     * @param md5       md5校验
     * @return {@link SeckillExecutionDTO}
     */
    SeckillExecutionDTO executeSeckillProcedure(long seckillId, long userPhone, String md5);

}
