package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

/**
 * @author yugi
 * @apiNote
 * @since 2017-06-07
 */
public interface SuccessKilledDao {

    /**
     * 插入购买明细,可以过滤重复
     *
     * @param seckillId 秒杀id
     * @param userPhone 用户电话
     * @return 插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据id查询SuccessKilled病携带秒杀产品对象实体
     *
     * @param seckillId 秒杀id
     * @return {@link SuccessKilled}
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId,@Param("userPhone") long userPhone);
}
