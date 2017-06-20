package org.seckill.web;

import lombok.extern.slf4j.Slf4j;
import org.seckill.constants.WebConstant;
import org.seckill.dto.ExposerDTO;
import org.seckill.dto.SeckillExecutionDTO;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * url:/模块/资源/{id}/细分  /seckill/list
 *
 * @author yugi
 * @apiNote
 * @since 2017-06-13
 */
@Controller
@RequestMapping("/seckill")
@Slf4j
public class SeckillController extends ExceptionController {

    @Resource
    private SeckillService seckillServiceImpl;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        //获取列表页
        List<Seckill> seckillList = seckillServiceImpl.getSeckillList();
        model.addAttribute("list", seckillList);
        return "list";
    }


    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillServiceImpl.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    @ResponseBody
    @RequestMapping(value = "/{seckillId}/exposer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public SeckillResult<ExposerDTO> exposer(@PathVariable("seckillId") Long seckillId) {
        SeckillResult<ExposerDTO> result;
        try {
            ExposerDTO exposerDTO = seckillServiceImpl.exportSeckillUrl(seckillId);
            result = new SeckillResult<>(true, exposerDTO);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            result = new SeckillResult<>(false, e.getMessage());
        }
        return result;
    }


    // 如果要在控制层加事务,这里也要加注解,而且要注意web.xml的加载顺序,应该先加载spring-service.xml再加载spring=web.xml,这样才保证事务能在控制层起效,
    // 如果在服务层出现的异常,这里不管有没有配置事务,控制层不抛出去事务也会回滚
    @RequestMapping(value = "/{seckillId}/{md5}/execution", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Transactional
    public SeckillResult<SeckillExecutionDTO> execution(@PathVariable("seckillId") Long seckillId, @PathVariable("md5") String md5,
                                                        @CookieValue(value = "killPhone", required = false) Long userPhone, HttpServletRequest request) {
        // @RequestParam(value = "killPhone", required = false) Long userPhone, HttpServletRequest request) {
        if (userPhone == null) {
            return new SeckillResult<>(false, "没有手机号");
        }
        try {
            SeckillExecutionDTO executionDTO = seckillServiceImpl.executeSeckill(seckillId, userPhone, md5);
            // 执行存储过程
            // SeckillExecutionDTO executionDTO = seckillServiceImpl.executeSeckillProcedure(seckillId, userPhone, md5);
            // Integer.parseInt("cece"); //测试控制层事务回滚
            return new SeckillResult<>(true, executionDTO);
        }
        catch (SeckillCloseException e1) {
            // return new SeckillResult<>(false, new SeckillExecutionDTO(seckillId, SeckillStateEnum.END));
            request.setAttribute(WebConstant.ERROR_DATA, new SeckillExecutionDTO(seckillId, SeckillStateEnum.END));
            throw e1;
        }
        catch (RepeatKillException e2) {
            // return new SeckillResult<>(false, new SeckillExecutionDTO(seckillId, SeckillStateEnum.REPEAT_KILL));
            request.setAttribute(WebConstant.ERROR_DATA, new SeckillExecutionDTO(seckillId, SeckillStateEnum.REPEAT_KILL));
            throw e2;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            //如果配置了ExceptionController,事务层出现异常,这里return也没用, 仍然会进入ExceptionController中
            // return new SeckillResult<>(false, new SeckillExecutionDTO(seckillId, SeckillStateEnum.INNER_ERROR));
            //遇到异常不能用finally返回，不然controller的事务不能回滚
            request.setAttribute(WebConstant.ERROR_DATA, new SeckillExecutionDTO(seckillId, SeckillStateEnum.INNER_ERROR));
            throw new RuntimeException(SeckillStateEnum.INNER_ERROR.getStateInfo());
        }
    }


    @RequestMapping(value = "/time/now", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date date = new Date();
        return new SeckillResult<>(true, date.getTime());
    }


}
