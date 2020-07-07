package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.pushweb.PushMessage;
import com.njwd.entity.pushweb.dto.PushMessageDto;
import com.njwd.entity.pushweb.vo.PushServerVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author bjw
 * @create 2019-08-08 下午 2:15
 */
@Repository
public interface PushMapper extends BaseMapper<PushMessage> {
    /**
     * 记录openfire发送信息记录
     */
    int addSendMessageInfo(@Param("pushMessageDto") PushMessageDto pushMessageDto);

    /**
     * 查询服务器信息
     * @param
     */
    List<PushServerVo> findServerListByServerName();
}


