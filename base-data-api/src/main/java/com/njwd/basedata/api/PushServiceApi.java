package com.njwd.basedata.api;

import com.njwd.entity.pushweb.dto.PushMessageDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 推送
 *
 * @author bjw
 * @create 2019-08-08
 */


@RequestMapping("push")
public interface PushServiceApi {
        //向openfire推送消息
        @RequestMapping("pushMessage")
        Result pushMessage(@RequestBody PushMessageDto pushMessageDto);

        //注册用户到openfire
        @RequestMapping("registUser")
        Result registUser(@RequestBody PushMessageDto pushMessageDto);
}
