package org.seckill.exception;

/**
 * @author yugi
 * @apiNote 秒杀相关业务异常
 * @since 2017-06-09
 */
public class SeckillException extends RuntimeException{

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
