package com.njwd.ledger.controller;

import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo;
import com.njwd.ledger.service.BalanceSubjectAuxiliaryItemService;
import com.njwd.ledger.service.BalanceSubjectAuxiliaryService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 辅助核算余额表
 *
 * @author 周鹏
 * @date 2019/8/16
 */
@RestController
@RequestMapping("balanceSubjectAuxiliary")
public class BalanceSubjectAuxiliaryController extends BaseController {
    @Autowired
    private BalanceSubjectAuxiliaryService balanceSubjectAuxiliaryService;

    @Autowired
    private BalanceSubjectAuxiliaryItemService balanceSubjectAuxiliaryItemService;

    /**
     * 根据条件统计辅助核算余额表
     *
     * @param queryDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/16
     */
    @PostMapping("findListByParam")
    public Result<List<BalanceSubjectAuxiliaryVo>> findListByParam(@RequestBody BalanceSubjectAuxiliaryItemQueryDto queryDto) {
        FastUtils.checkParams(queryDto.getAccountBookEntityIdOperator(),
                queryDto.getAccountBookEntityList(), queryDto.getPeriodOperator(),
                queryDto.getPeriodYears(), queryDto.getPeriodNumbers(),
                queryDto.getIsShowFullName(), queryDto.getIsIncludeProfitAndLoss(),
                queryDto.getIsIncludeUnbooked(), queryDto.getShowCondition(),
                queryDto.getSourceTableAndIdList(), queryDto.getSubjectId());
        return ok(balanceSubjectAuxiliaryService.findListByParam(queryDto));
    }

    /**
     * @return com.njwd.support.Result<com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem>
     * @description: 根据账簿id和值来源表和值id查询
     * @Param [queryDto]
     * @author LuoY
     * @date 2019/8/23 16:57
     */
    @PostMapping("findByAccountBookIdAndItemValueId")
    public Result<BalanceSubjectAuxiliaryItem> findByAccountBookIdAndItemValueId(@RequestBody BalanceSubjectAuxiliaryItemQueryDto queryDto) {
        return ok(balanceSubjectAuxiliaryItemService.findByAccountBookIdAndItemId(queryDto));
    }

    /**
     * Excel 导出辅助核算余额表
     *
     * @param queryDto
     * @param response
     * @return
     * @author 周鹏
     * @date 2019/8/29
     */
    @RequestMapping("exportListExcel")
    public void exportListExcel(@RequestBody BalanceSubjectAuxiliaryItemQueryDto queryDto, HttpServletResponse response) {
        FastUtils.checkParams(queryDto.getAccountBookEntityIdOperator(),
                queryDto.getAccountBookEntityList(), queryDto.getPeriodOperator(),
                queryDto.getPeriodYears(), queryDto.getPeriodNumbers(),
                queryDto.getIsShowFullName(), queryDto.getIsIncludeProfitAndLoss(),
                queryDto.getIsIncludeUnbooked(), queryDto.getShowCondition(),
                queryDto.getSourceTableAndIdList(), queryDto.getSubjectId());
        balanceSubjectAuxiliaryService.exportListExcel(queryDto, response);
    }

}
