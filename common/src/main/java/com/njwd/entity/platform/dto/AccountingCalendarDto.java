package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountingPeriod;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 会计日历
 * @Date:14:06 2019/6/26
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingCalendarDto extends AccountingCalendarVo {
    /** 用于分页 **/
    private Page<AccountingCalendarVo> page = new Page<>();
    private Long accountTypeId;
    private Long accStandardId;
    /**
     * AccountingCalendarDto集合   用于删除  修改  审核  发布
     */
    private List<AccountingCalendarDto> editList;
    //追加年数
    private Integer appendYear;
    //期间类型集合
    private List<Long> typeIds;
    /**
     * 发布消息
     */
    private MessageDto messageDto;
}
