package com.njwd.platform.service;

import com.njwd.entity.platform.dto.MessageDto;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 10:19
 */
public interface MessageService {

    void sendMessage(Byte messageType,MessageDto messageDto);

}
