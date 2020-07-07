package com.njwd.ledger.controller;

import com.njwd.common.LedgerConstant;
import com.njwd.entity.ledger.dto.ItemJournalQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo;
import com.njwd.ledger.service.ItemJournalBackService;
import com.njwd.ledger.service.ItemJournalService;
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
 * @Description 科目日记账
 * @Author: ZhuHC
 * @Date: 2019/8/6 17:09
 */
@RestController
@RequestMapping("itemJournal")
public class ItemJournalController extends BaseController {

    @Resource
    private ItemJournalService itemJournalService;
    @Resource
    private ItemJournalBackService itemJournalBackService;

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:27
     * @Param [itemJournalQueryDto]
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo>>
     * @Description 科目日记账  查询
     */
    @PostMapping("getItemJournalDetails")
    public Result<List<GeneralReturnItemJournalVo>> getItemJournalDetails(@RequestBody ItemJournalQueryDto itemJournalQueryDto) {
        FastUtils.checkParams(itemJournalQueryDto.getAccountBookIds(),itemJournalQueryDto.getAccountBookEntityIds(),
                                     itemJournalQueryDto.getPeriodOperator(),itemJournalQueryDto.getCodes(),
                                     itemJournalQueryDto.getAccountBookEntityList());
        if(LedgerConstant.PeriodOperator.PERIOD_YEAR_AND_NUM.equals(itemJournalQueryDto.getPeriodOperator())){
            FastUtils.checkParams(itemJournalQueryDto.getPeriodYearNum());
        }else{
            FastUtils.checkParams(itemJournalQueryDto.getVoucherDates());
        }
      /*  return ok(itemJournalService.getItemJournalDetails(itemJournalQueryDto));*/
        return ok(itemJournalBackService.findGeneralReturnItemJournalList(itemJournalQueryDto));
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 15:52
     * @Param [itemJournalQueryDto, response]
     * @return void
     * @Description 导出
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody ItemJournalQueryDto itemJournalQueryDto, HttpServletResponse response){
        FastUtils.checkParams(itemJournalQueryDto.getAccountBookIds(),itemJournalQueryDto.getAccountBookEntityIds(),
                             itemJournalQueryDto.getPeriodOperator(),itemJournalQueryDto.getCodes(),itemJournalQueryDto.getAccountBookEntityList());
        if(LedgerConstant.PeriodOperator.PERIOD_YEAR_AND_NUM.equals(itemJournalQueryDto.getPeriodOperator())){
            FastUtils.checkParams(itemJournalQueryDto.getPeriodYearNum());
        }
        if(LedgerConstant.PeriodOperator.VOUCHER_DATE.equals(itemJournalQueryDto.getPeriodOperator())){
            FastUtils.checkParams(itemJournalQueryDto.getVoucherDates());
        }
        /*itemJournalService.exportExcel(itemJournalQueryDto,response);*/
        itemJournalBackService.exportExcel(itemJournalQueryDto,response);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 15:52
     * @Param [itemJournalQueryDto, response]
     * @return void
     * @Description 导出
     */
    @RequestMapping("exportAllExcel")
    public void exportAllExcel(@RequestBody ItemJournalQueryDto itemJournalQueryDto, HttpServletResponse response){
        FastUtils.checkParams(itemJournalQueryDto.getAccountBookIds(),itemJournalQueryDto.getAccountBookEntityIds(),
                itemJournalQueryDto.getPeriodOperator(),itemJournalQueryDto.getCodes(),itemJournalQueryDto.getAccountBookEntityList());
        if(LedgerConstant.PeriodOperator.PERIOD_YEAR_AND_NUM.equals(itemJournalQueryDto.getPeriodOperator())){
            FastUtils.checkParams(itemJournalQueryDto.getPeriodYearNum());
        }
        if(LedgerConstant.PeriodOperator.VOUCHER_DATE.equals(itemJournalQueryDto.getPeriodOperator())){
            FastUtils.checkParams(itemJournalQueryDto.getVoucherDates());
        }
        itemJournalService.exportAllExcel(itemJournalQueryDto,response);
    }
}
