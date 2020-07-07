package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.mapper.MessageMapper;
import com.njwd.basedata.service.MessageService;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.MessageDto;
import com.njwd.entity.platform.vo.MessageVo;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 10:52
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Override
    public void addMessage(MessageDto messageDto) {
        List<Long> rootEnterpriseIds = messageMapper.findRootEnterpriseIds();
        if(rootEnterpriseIds!=null&&!rootEnterpriseIds.isEmpty()){
            messageDto.setRootEnterpriseIds(rootEnterpriseIds);
            messageMapper.addBatch(messageDto);
        }
    }

    /**
     * 更新消息
     *
     * @param messageDto
     * @return int
     * @Author lj
     * @Date:15:02 2019/12/4
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMessageBatch(MessageDto messageDto) {
        SysUserVo operator = UserUtils.getUserVo();
        messageDto.setUpdateTime(new Date());
        messageDto.setUpdatorId(operator.getUserId());
        messageDto.setUpdatorName(operator.getName());
        return messageMapper.updateMessageBatch(messageDto);
    }

    /**
     * 查询消息列表分页
     *
     * @param messageDto
     * @return Page<MessageVo>
     * @Author lj
     * @Date:14:40 2019/12/4
     **/
    @Override
    public Page<MessageVo> findMessagePage(MessageDto messageDto) {
        Page<MessageVo> page = messageDto.getPage();
        page=messageMapper.findMessagePage(page,messageDto);
        return page;
    }
}
