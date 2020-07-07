package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountingCalendar;
import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author lj
 * @Description 会计日历
 * @Date:14:08 2019/6/26
 **/
public interface AccountingCalendarMapper extends BaseMapper<AccountingCalendar> {

    /**
     * @Description 根据会计准则id和账簿类型id查询会计日历列表
     * @Author lj
     * @Date:14:30 2019/6/26
     * @Param [accountingCalendarDto]
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingCalendarVo>
     **/
    List<AccountingCalendarVo> findAccCaListByAccTypeAndStand(AccountingCalendarDto accountingCalendarDto);

    /** 刘遵通
     * 查询页面 （分页）
     * @param accountingCalendarDto
     * @return
     */
    Page<AccountingCalendarVo> findPage(@Param("page") Page<AccountingCalendarVo> page, @Param("accountingCalendarDto") AccountingCalendarDto accountingCalendarDto);

    /** 刘遵通
     *  根据id集合查询list数据
     * @param accountingCalendarDto
     * @return
     */
    List<AccountingCalendarVo> findAccountingCalendarByIds(@Param("accountingCalendarDto") AccountingCalendarDto accountingCalendarDto);

    /**
     * 删除  审核  发布
     * @param accountingCalendarDto
     * @param AccountingCalendarList
     * @return
     */
    int delteOrCheckOrRelease(@Param("accountingCalendarDto") AccountingCalendarDto accountingCalendarDto, @Param("accountingCalendarList") List<AccountingCalendarVo> AccountingCalendarList);

}