package com.njwd.entity.pushweb.dto;

import com.njwd.entity.ledger.dto.VoucherEntryDto;
import com.njwd.entity.ledger.vo.VoucherVo;
import com.njwd.entity.pushweb.vo.PushMessageVo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bjw
 * @create 2019-08-11 下午 4:27
 */
@Getter
@Setter
public class PushMessageDto extends PushMessageVo {
    /**
     *消息发送人
     */
    private String from_user;

    /**
     *消息接收人
     */
    private String to_user;
}
