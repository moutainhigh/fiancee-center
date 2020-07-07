package com.njwd.ledger.controller;

import com.njwd.entity.ledger.dto.BalanceSubjectCashJournalQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectCashJournalVo;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.ledger.service.CashJournalService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 现金日记账
 *
 * @author wuweiming
 * @since 2019/08/13
 */
@RestController
@RequestMapping("cashJournal")
public class CashJournalController extends BaseController {
    @Resource
    private CashJournalService cashJournalService;

    /**
     * 根据条件查询科目信息
     * @param dto
     * @return Result<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/09/18
     */
    @PostMapping("findSubjectInfoByParamWithData")
    public Result<AccountSubjectVo> findSubjectInfoByParamWithData(@RequestBody BalanceSubjectCashJournalQueryDto dto) {

        //校验必传字段是否为空
        FastUtils.checkParams(dto.getAccountBookId(),dto.getAccountBookEntityIds(),dto.getSubjectList(),
                dto.getSubjectId(),dto.getAccountBookEntityIdOperator(),dto.getAccountBookEntityIds());
        //会计期间 和 制单日期  必传一个
        FastUtils.checkParamsForOr(dto.getVoucherDate(),dto.getPeriodYears());

        return ok(cashJournalService.findSubjectInfoByParamWithData(dto));
    }


    /**
     * @Description 查询 现金日记账/银行日记账
     * @Author wuweiming
     * @Data 2019/08/07 17:02
     * @Param BalanceSubjectCashJournalQueryDto
     * @return Result<List<BalanceSubjectCashJournalVo>>
     */
    @RequestMapping("findCashJournalList")
    public Result<List<BalanceSubjectCashJournalVo>> findCashJournalList(@RequestBody BalanceSubjectCashJournalQueryDto dto) {
        //校验必传字段是否为空
        FastUtils.checkParams(dto.getAccountBookId(),dto.getAccountBookEntityList(),dto.getSubjectId(),
                dto.getAccountSubjectId(),dto.getSubjectName(),dto.getSubjectCode(),
                dto.getAccountBookEntityIdOperator());
        //会计期间 和 制单日期  必传一个
        FastUtils.checkParamsForOr(dto.getVoucherDate(),dto.getPeriodYears());
        return ok(cashJournalService.findCashJournalList(dto));
    }


    /**
     * Excel 导出
     *
     * @param dto
     * @param response
     * @return
     * @author wuweiming
     * @date 2019/8/29
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody BalanceSubjectCashJournalQueryDto dto, HttpServletResponse response) {
        ///校验必传字段是否为空
        FastUtils.checkParams(dto.getAccountBookId(),dto.getAccountBookEntityList(),dto.getSubjectId(),
                dto.getAccountSubjectId(),dto.getSubjectName(),dto.getSubjectCode(),
                dto.getAccountBookEntityIdOperator());
        //会计期间 和 制单日期  必传一个
        FastUtils.checkParamsForOr(dto.getVoucherDate(),dto.getPeriodYears());
        cashJournalService.exportExcel(dto, response);
    }

    /**
     * Excel 导出全部
     * @param dto
     * @param response
     * @return
     * @author wuweiming
     * @date 2019/9/30
     */
    @RequestMapping("exportExcelAll")
    public void exportExcelAll(@RequestBody BalanceSubjectCashJournalQueryDto dto, HttpServletResponse response) {
        ///校验必传字段是否为空
        FastUtils.checkParams(dto.getAccountBookId(),dto.getAccountBookEntityList(),dto.getSubjectId(),
                dto.getSubjectOperator(), dto.getIsFinal(),dto.getAccountBookEntityIdOperator());
        //会计期间 和 制单日期  必传一个
        FastUtils.checkParamsForOr(dto.getVoucherDate(),dto.getPeriodYears());
        cashJournalService.exportExcelAll(dto, response);
    }

}
