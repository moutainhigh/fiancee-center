package com.njwd.platform.api;

import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author lj
 * @Description 会计日历
 * @Date:14:33 2019/6/26
 **/
@RequestMapping("platform/accountingCalendar")
public interface AccountingCalendarApi {

    /**
     * @Description 根据会计准则id和账簿类型id查询会计日历列表
     * @Author lj
     * @Date:14:37 2019/6/26
     * @Param [accountingCalendarDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccCaListByAccTypeAndStand")
    Result<List<AccountingCalendarVo>> findAccCaListByAccTypeAndStand(AccountingCalendarDto accountingCalendarDto);
}
