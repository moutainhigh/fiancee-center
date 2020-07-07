package com.njwd.basedata.service;

import com.njwd.entity.pushweb.dto.PushMessageDto;

public interface PushService {
    /**
     * 推送消息
     * @param
     * @return
     */
    boolean pushMsg(PushMessageDto pushMessageDto);

    boolean regist(PushMessageDto pushMessageDto);

    int addPushMessage(PushMessageDto pushMessageDto);
}
