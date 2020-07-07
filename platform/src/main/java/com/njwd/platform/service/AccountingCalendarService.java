package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountingCalendar;
import com.njwd.entity.platform.AccountingPeriod;
import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 会计日历
 * @Author lj
 * @Date:14:31 2019/6/26
 * @Param
 * @return
 **/
public interface AccountingCalendarService {

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingCalendarVo>
     * @Description 根据会计准则id和账簿类型id查询会计日历列表
     * @Author lj
     * @Date:14:30 2019/6/26
     * @Param [accountingCalendarDto]
     **/
    List<AccountingCalendarVo> findAccCaListByAccTypeAndStand(AccountingCalendarDto accountingCalendarDto);
    /**
     * 刘遵通
     * 增加会计日历
     * @param accountingCalendarDto
     * @return
     */
    Long addAccountingCalendar(AccountingCalendarDto accountingCalendarDto);

    /**
     * 刘遵通
     * 生成会计期间
     * @param accountingCalendarDto
     * @return
     */
    List<AccountingPeriod> generateAccountingPeriod(AccountingCalendarDto accountingCalendarDto);

    /** 刘遵通
     * 查询页面 （分页）
     * @param accountingCalendarDto
     * @return
     */
    Page<AccountingCalendarVo> findPage(AccountingCalendarDto accountingCalendarDto);

    /** 刘遵通
     * 删除会计日历
     * @param accountingCalendarDto
     * @return
     */
    BatchResult deleteAccountingCalendar(AccountingCalendarDto accountingCalendarDto);
    /**刘遵通
     * 审核
     * @param accountingCalendarDto
     * @return
     */
    BatchResult checkApprove(AccountingCalendarDto accountingCalendarDto);
    /**刘遵通
     * 反审核
     * @param accountingCalendarDto
     * @return
     */
    BatchResult reversalApprove(AccountingCalendarDto accountingCalendarDto);
    /**
     * 发布
     * @param accountingCalendarDto
     * @return
     */
    BatchResult release(AccountingCalendarDto accountingCalendarDto);
    /**
     * 编辑中的查看
     * @param accountingCalendarDto
     * @return
     */
    AccountingCalendarVo selectById(AccountingCalendarDto accountingCalendarDto);

    /**
     * 根据名称查出数量
     * @param accountingCalendarDto
     * @return
     */
    Integer selectByName(AccountingCalendarDto accountingCalendarDto);
    /**
     * 修改
     * @param accountingCalendarDto
     * @return
     */
    Long updateById(AccountingCalendarDto accountingCalendarDto);

    /**
     * 追加
     * @param accountingCalendarDto
     * @return
     */
    List<AccountingPeriod> appendById(AccountingCalendarDto accountingCalendarDto);
    /**
     * 导出
     * @param accountingCalendarDto
     * @param response
     */
    void exportExcel(AccountingCalendarDto accountingCalendarDto, HttpServletResponse response);

}
