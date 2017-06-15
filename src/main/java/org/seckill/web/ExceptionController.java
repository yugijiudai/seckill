package org.seckill.web;

import org.seckill.constants.WebConstant;
import org.seckill.dto.SeckillResult;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yugi
 * @apiNote 异常controller处理器
 * @since 2017-06-13
 */
public class ExceptionController {


    @ExceptionHandler
    @ResponseBody
    // public SeckillResult handleAndReturnData(Exception ex) {
    public SeckillResult handleAndReturnData(HttpServletRequest request, Exception ex) {
        Object attribute = request.getAttribute(WebConstant.ERROR_DATA);
        boolean success = ex instanceof RepeatKillException || ex instanceof SeckillCloseException;
        SeckillResult seckillResult = new SeckillResult<>(success, ex.getMessage(), ex.getClass().getSimpleName(), attribute);
        if (attribute != null) {
            request.removeAttribute(WebConstant.ERROR_DATA);
        }
        return seckillResult;
    }

}
