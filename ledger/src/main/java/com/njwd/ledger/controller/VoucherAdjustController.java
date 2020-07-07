package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.ledger.VoucherAdjust;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.AccountBookPeriodReportDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodReportVo;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.ledger.service.VoucherAdjustService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/7 11:35
 */
@RestController
@RequestMapping("voucherAdjust")
public class VoucherAdjustController extends BaseController {

    @Resource
    private VoucherAdjustService voucherAdjustService;

    /**
     * @description: 整理凭证
     * @param: [ids]
     * @return: com.njwd.support.Result
     * @author: xdy
     * @create: 2019-08-08 11-44
     */
    @RequestMapping("adjust")
    public Result<List<VoucherAdjust>> adjust(@RequestBody List<Long> ids){
        return ok(voucherAdjustService.adjust(ids));
    }

    /**
     * @description: 获取待整理列表
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>> 
     * @author: xdy        
     * @create: 2019-08-07 14-19 
     */
    @RequestMapping("findToAdjustList")
    public Result<Page<AccountBookPeriodVo>> findToAdjustList(@RequestBody AccountBookPeriodDto accountBookPeriodDto){
        return ok(voucherAdjustService.findToAdjustList(accountBookPeriodDto));
    }
    

    /**
     * @description: 检测是否断号
     * @param: [ids]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.VoucherAdjust>> 
     * @author: xdy        
     * @create: 2019-08-08 11-43 
     */
    @RequestMapping("checkBroken")
    public Result<List<VoucherAdjust>> checkBroken(@RequestBody List<Long> ids){
        return ok(voucherAdjustService.checkBroken(ids, Constant.Is.YES));
    }
    
    /**
     * @description: 获取报告分页数据
     * @param: [accountBookPeriodReportDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodReportVo>> 
     * @author: xdy        
     * @create: 2019-08-09 15-50 
     */
    @RequestMapping("findReportPage")
    public Result<Page<AccountBookPeriodReportVo>> findReportPage(@RequestBody AccountBookPeriodReportDto accountBookPeriodReportDto){
        FastUtils.checkNull(accountBookPeriodReportDto.getPeriodId());
        return ok(voucherAdjustService.findReportPage(accountBookPeriodReportDto));
    }


}
