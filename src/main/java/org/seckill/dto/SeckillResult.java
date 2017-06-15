package org.seckill.dto;

import lombok.Data;

/**
 * @author yugi
 * @apiNote 所有ajax请求返回类型, 封装json结果
 * @since 2017-06-13
 */

@Data
public class SeckillResult<T> {

    private boolean success;

    private T data;

    private String errorTip;

    private String exception;


    public SeckillResult(boolean success, String errorTip) {
        this.success = success;
        this.errorTip = errorTip;
    }

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String errorTip, String exception, T data) {
        this.success = success;
        this.data = data;
        this.errorTip = errorTip;
        this.exception = exception;
    }
}
