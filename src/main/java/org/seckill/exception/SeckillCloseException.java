package org.seckill.exception;

/**
 * @author yugi
 * @apiNote 秒杀关闭异常
 * @since 2017-06-09
 */
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
