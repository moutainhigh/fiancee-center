package com.njwd.basedata.controller;

import com.njwd.entity.pushweb.dto.PushMessageDto;
import com.njwd.basedata.service.PushService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author bjw
 * @create 2019-08-08 下午 1:48
 */
@RestController
@RequestMapping("push")
public class PushController extends BaseController {
    @Resource
    private PushService pushService;


    /**
     * @Description将消息推送至服务器
     * @Author bjw
     * @Date 2019/6/20 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("pushMessage")
    public Result pushMessage(@RequestBody PushMessageDto pushMessageDto) {
        FastUtils.checkParams(pushMessageDto.getUsername(), pushMessageDto.getContent(),pushMessageDto.getSubject());
        //向openfire推送消息
        boolean flag=true;
        flag=pushService.pushMsg(pushMessageDto);
        return ok(flag);
    }

    /**
     * @Description将消息推送至服务器
     * @Author bjw
     * @Date 2019/6/20 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("registUser")
    public Result registUser(@RequestBody PushMessageDto pushMessageDto) {
        FastUtils.checkParams(pushMessageDto.getUsername());
        //向openfire推送消息
        boolean flag=true;
        flag=pushService.regist(pushMessageDto);
        return ok(flag);
    }
}
