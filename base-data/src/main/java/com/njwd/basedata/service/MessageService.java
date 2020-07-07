package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.MessageDto;
import com.njwd.entity.platform.vo.MessageVo;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 10:52
 */
public interface MessageService {

    void addMessage(MessageDto messageDto);

    /**
     * 更新消息
     * @Author lj
     * @Date:15:02 2019/12/4
     * @param messageDto
     * @return int
     **/
    int updateMessageBatch(MessageDto messageDto);

    /**
     * 查询消息列表分页
     * @Author lj
     * @Date:14:40 2019/12/4
     * @param messageDto
     * @return Page<MessageVo>
     **/
    Page<MessageVo> findMessagePage(MessageDto messageDto);
}
