package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.Message;
import com.njwd.entity.platform.dto.MessageDto;
import com.njwd.entity.platform.vo.MessageVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 10:57
 */
public interface MessageMapper extends BaseMapper<Message> {

    int addBatch(@Param("messageDto") MessageDto messageDto);

    /**
     * 更新消息
     * @Author lj
     * @Date:15:22 2019/12/4
     * @param messageDto
     * @return int
     **/
    int updateMessageBatch(@Param("messageDto") MessageDto messageDto);

    List<Long> findRootEnterpriseIds();

    /**
     * 查询消息列表分页
     * @Author lj
     * @Date:13:54 2019/12/4
     * @param page
     * @param messageDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.MessageVo>
     **/
    Page<MessageVo> findMessagePage(@Param("page") Page<MessageVo> page, @Param("messageDto") MessageDto messageDto);
}
