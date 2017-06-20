package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import org.seckill.entity.Seckill;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Resource;

/**
 * @author yugi
 * @apiNote
 * @since 2017-06-16
 */
@Slf4j
@Repository
public class RedisDao {

    @Resource
    private JedisPool jedisPool;

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    // public RedisDao(String ip, int port, int timeOut) {
    //     this.jedisPool = new JedisPool(new JedisPoolConfig(), ip, port, timeOut);
    // }


    public Seckill getSeckill(long seckillId) {
        // redis操作逻辑
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "seckill:" + seckillId;
            // 并没有实现内部序列化操作 get->byte[]->反序列化->Object(Seckill)
            // 采用自定义序列化
            byte[] bytes = jedis.get(key.getBytes());
            // 缓存中获取到
            if (bytes != null) {
                // 空对象
                Seckill seckill = schema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                // seckill被反序列化,这种反序列化比实现Serializable接口效率高很多
                return seckill;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            this.returnBrokenResource(jedis);
        }
        return null;
    }

    public String putSeckill(Seckill seckill) {
        Jedis jedis = null;
        // set Object(Seckill) -> 序列化 ->byte[]
        try {
            jedis = jedisPool.getResource();
            String key = "seckill:" + seckill.getSeckillId();
            byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            // 超时缓存,1小时
            int timeout = 60 * 60;
            return jedis.setex(key.getBytes(), timeout, bytes);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            this.returnBrokenResource(jedis);
        }
        return null;
    }


    private void returnBrokenResource(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.close();
            }
            catch (Exception e) {
                log.error("Caught Redis Exception when returnBrokenResource: {}", e.getMessage(), e);
            }
        }
    }
}
