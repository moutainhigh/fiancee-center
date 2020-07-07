package com.njwd.ledger.controller;

import com.njwd.entity.ledger.dto.BalanceSubjectQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.ledger.service.BalanceSubjectService;
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
 * 科目余额表
 *
 * @author 周鹏
 * @date 2019/8/5
 */
@RestController
@RequestMapping("balanceSubject")
public class BalanceSubjectController extends BaseController {
    @Autowired
    private BalanceSubjectService balanceSubjectService;

    /**
     * 根据条件统计科目余额表
     *
     * @param balanceSubjectQueryDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/5
     */
    @PostMapping("findListByParam")
    public Result<List<BalanceSubjectVo>> findListByParam(@RequestBody BalanceSubjectQueryDto balanceSubjectQueryDto) {
        FastUtils.checkParams(balanceSubjectQueryDto.getAccountBookEntityIdOperator(),
                balanceSubjectQueryDto.getAccountBookEntityList(), balanceSubjectQueryDto.getPeriodOperator(),
                balanceSubjectQueryDto.getPeriodYears(), balanceSubjectQueryDto.getPeriodNumbers(),
                balanceSubjectQueryDto.getSubjectLevelOperator(), balanceSubjectQueryDto.getSubjectLevels(),
                balanceSubjectQueryDto.getIsShowFullName(), balanceSubjectQueryDto.getIsIncludeProfitAndLoss(),
                balanceSubjectQueryDto.getIsIncludeUnbooked(), balanceSubjectQueryDto.getIsShowAuxiliaryDetail(),
                balanceSubjectQueryDto.getShowCondition(), balanceSubjectQueryDto.getIsIncludeEnable(),
                balanceSubjectQueryDto.getSubjectId());
        return ok(balanceSubjectService.findListByParam(balanceSubjectQueryDto));
    }

    /**
     * 根据条件查询凭证号
     *
     * @param balanceSubjectQueryDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/17
     */
    @PostMapping("findVoucherNumberByParam")
    public Result<String> findVoucherNumberByParam(@RequestBody BalanceSubjectQueryDto balanceSubjectQueryDto) {
        FastUtils.checkParams(balanceSubjectQueryDto.getAccountBookEntityIdOperator(), balanceSubjectQueryDto.getAccountBookEntityIds());
        return ok(balanceSubjectService.findVoucherNumberByParam(balanceSubjectQueryDto));
    }

    /**
     * 根据条件统计科目汇总表
     *
     * @param balanceSubjectQueryDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/16
     */
    @PostMapping("findCollectListByParam")
    public Result<List<BalanceSubjectVo>> findCollectListByParam(@RequestBody BalanceSubjectQueryDto balanceSubjectQueryDto) {
        FastUtils.checkParams(balanceSubjectQueryDto.getAccountBookEntityIdOperator(),
                balanceSubjectQueryDto.getAccountBookEntityList(),
                balanceSubjectQueryDto.getSubjectLevelOperator(), balanceSubjectQueryDto.getSubjectLevels(),
                balanceSubjectQueryDto.getIsShowFullName(), balanceSubjectQueryDto.getIsIncludeProfitAndLoss(),
                balanceSubjectQueryDto.getIsIncludeUnbooked(), balanceSubjectQueryDto.getIsIncludeEnable(),
                balanceSubjectQueryDto.getSubjectId());
        return ok(balanceSubjectService.findCollectListByParam(balanceSubjectQueryDto));
    }

    /**
     * Excel 导出科目余额表
     *
     * @param balanceSubjectQueryDto
     * @param response
     * @return
     * @author 周鹏
     * @date 2019/8/29
     */
    @RequestMapping("exportListExcel")
    public void exportListExcel(@RequestBody BalanceSubjectQueryDto balanceSubjectQueryDto, HttpServletResponse response) {
        FastUtils.checkParams(balanceSubjectQueryDto.getAccountBookEntityIdOperator(),
                balanceSubjectQueryDto.getAccountBookEntityList(), balanceSubjectQueryDto.getPeriodOperator(),
                balanceSubjectQueryDto.getPeriodYears(), balanceSubjectQueryDto.getPeriodNumbers(),
                balanceSubjectQueryDto.getSubjectLevelOperator(), balanceSubjectQueryDto.getSubjectLevels(),
                balanceSubjectQueryDto.getIsShowFullName(), balanceSubjectQueryDto.getIsIncludeProfitAndLoss(),
                balanceSubjectQueryDto.getIsIncludeUnbooked(), balanceSubjectQueryDto.getIsShowAuxiliaryDetail(),
                balanceSubjectQueryDto.getShowCondition(), balanceSubjectQueryDto.getIsIncludeEnable(),
                balanceSubjectQueryDto.getSubjectId());
        balanceSubjectService.exportListExcel(balanceSubjectQueryDto, response);
    }

    /**
     * Excel 导出科目汇总表
     *
     * @param balanceSubjectQueryDto
     * @param response
     * @return
     * @author 周鹏
     * @date 2019/8/29
     */
    @RequestMapping("exportCollectListExcel")
    public void exportCollectListExcel(@RequestBody BalanceSubjectQueryDto balanceSubjectQueryDto, HttpServletResponse response) {
        FastUtils.checkParams(balanceSubjectQueryDto.getAccountBookEntityIdOperator(),
                balanceSubjectQueryDto.getAccountBookEntityList(),
                balanceSubjectQueryDto.getSubjectLevelOperator(), balanceSubjectQueryDto.getSubjectLevels(),
                balanceSubjectQueryDto.getIsShowFullName(), balanceSubjectQueryDto.getIsIncludeProfitAndLoss(),
                balanceSubjectQueryDto.getIsIncludeUnbooked(), balanceSubjectQueryDto.getIsIncludeEnable(),
                balanceSubjectQueryDto.getSubjectId());
        balanceSubjectService.exportCollectListExcel(balanceSubjectQueryDto, response);
    }

}
