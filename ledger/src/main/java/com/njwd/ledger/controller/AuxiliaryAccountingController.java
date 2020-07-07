package com.njwd.ledger.controller;

import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.entity.ledger.dto.AuxiliaryAccountingQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import com.njwd.ledger.service.AuxiliaryAccountingBackService;
import com.njwd.ledger.service.AuxiliaryAccountingService;
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
 * @Description 辅助核算明细账
 * @Author: ZhuHC
 * @Date: 2019/7/30 10:12
 */
@RestController
@RequestMapping("auxiliaryAccounting")
public class AuxiliaryAccountingController extends BaseController {

    @Autowired
    private AuxiliaryAccountingService auxiliaryAccountingService;
    @Autowired
    private AuxiliaryAccountingBackService auxiliaryAccountingBackService;

    /**
     * @Author ZhuHC
     * @Date  2019/7/30 11:20
     * @Param [querySchemeDetail]
     * @return java.lang.String
     * @Description 根据辅助核算项目  查询明细
     */
    @PostMapping("findAuxiliaryDetails")
    public Result<List<GeneralReturnAuxiliaryVo>> findAuxiliaryDetails(@RequestBody AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto) {
        FastUtils.checkParams(auxiliaryAccountingQueryDto.getAccountBookIds(),auxiliaryAccountingQueryDto.getAccountBookEntityIds(),
                auxiliaryAccountingQueryDto.getSourceTableAndValueList(),auxiliaryAccountingQueryDto.getPeriodOperator(),
                auxiliaryAccountingQueryDto.getAccountBookEntityList());

        if(LedgerConstant.PeriodOperator.PERIOD_YEAR_AND_NUM.equals(auxiliaryAccountingQueryDto.getPeriodOperator())){
            FastUtils.checkParams(auxiliaryAccountingQueryDto.getPeriodYearNum());
        }else {
            FastUtils.checkParams(auxiliaryAccountingQueryDto.getVoucherDates());
        }
       /* return ok(auxiliaryAccountingService.findAuxiliaryDetails(auxiliaryAccountingQueryDto));*/
        return ok(auxiliaryAccountingBackService.findAuxiliaryDetailList(auxiliaryAccountingQueryDto));
    }

    /**
     * @Author wuweiming
     * @Date  2019/08/07 09:59
     * @Param []
     * @return java.lang.String
     * @Description 查询所有辅助核算（包含自定义）类型
     */
    @PostMapping("findAuxiliaryItemList")
    public Result<List<AuxiliaryItemVo>> findAuxiliaryItemList(){
        return ok(auxiliaryAccountingService.findAuxiliaryItemList());
    }

    /**
     * @Author wuweiming
     * @Date  2019/08/07 14:19
     * @Param [AccountingItemValueDto]
     * @return java.lang.String
     * @Description 查询所有辅助核算（包含自定义）
     */
    @PostMapping("findAuxiliaryItemValues")
    public Result<List<AccountingItemValueVo>> findAuxiliaryItemValues(@RequestBody AccountingItemValueDto dto){
        return ok(auxiliaryAccountingService.findAuxiliaryItemValues(dto));
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 15:52
     * @Param [auxiliaryAccountingQueryDto, response]
     * @return void
     * @Description 导出
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto, HttpServletResponse response){
        FastUtils.checkParams(auxiliaryAccountingQueryDto.getAccountBookIds(),auxiliaryAccountingQueryDto.getAccountBookEntityIds(),
                auxiliaryAccountingQueryDto.getSourceTableAndValueList(),auxiliaryAccountingQueryDto.getPeriodOperator(),
                auxiliaryAccountingQueryDto.getAccountBookEntityList());
        if(LedgerConstant.PeriodOperator.PERIOD_YEAR_AND_NUM.equals(auxiliaryAccountingQueryDto.getPeriodOperator())){
            FastUtils.checkParams(auxiliaryAccountingQueryDto.getPeriodYearNum());
        }
        if(LedgerConstant.PeriodOperator.VOUCHER_DATE.equals(auxiliaryAccountingQueryDto.getPeriodOperator())){
            FastUtils.checkParams(auxiliaryAccountingQueryDto.getVoucherDates());
        }
        /*auxiliaryAccountingService.exportExcel(auxiliaryAccountingQueryDto,response);*/
        auxiliaryAccountingBackService.exportExcel(auxiliaryAccountingQueryDto,response);
    }
}
