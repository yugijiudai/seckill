package org.seckill.dto;

import lombok.Data;

/**
 * @author yugi
 * @apiNote 暴露秒杀地址DTO
 * @since 2017-06-09
 */
@Data
public class ExposerDTO {

    /**
     * 是否开启秒杀
     */
    private boolean exposed;

    /**
     * 一种加密措施
     */
    private String md5;

    /**
     * 秒杀id
     */
    private long seckillId;

    /**
     * 系统当前时间(毫秒)
     */
    private long now;

    /**
     * 秒杀开启时间
     */
    private long start;

    /**
     * 秒杀结束时间
     */
    private long end;

    public ExposerDTO() {
    }

    public ExposerDTO(boolean exposed, String md5, long seckillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public ExposerDTO(boolean exposed, long seckillId, long now, long start, long end) {
        this.exposed = exposed;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public ExposerDTO(boolean exposed, long seckillId) {
        this.exposed = exposed;
        this.seckillId = seckillId;
    }

}
