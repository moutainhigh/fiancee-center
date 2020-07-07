package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.VoucherAdjust;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.AccountBookPeriodReportDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodReportVo;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/7 11:29
 */
public interface VoucherAdjustService {

    /**
     * @description: 凭证整理
     * @param: [ids]
     * @return: java.util.List<com.njwd.entity.ledger.VoucherAdjust>
     * @author: xdy
     * @create: 2019-08-08 17-00
     */
    List<VoucherAdjust> adjust(List<Long> ids);

    /**
     * @Description
     * @Author 朱小明
     * @Date 2019/9/23
     * @param accountBookPeriods
     * @return java.lang.Object
     **/
    List<VoucherAdjust> adjustExcute(List<AccountBookPeriodVo> accountBookPeriods);

    /**
     * @description: 获取待整理账簿期间
     * @param: [accountBookPeriodDto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo> 
     * @author: xdy        
     * @create: 2019-08-08 17-00 
     */
    Page<AccountBookPeriodVo> findToAdjustList(AccountBookPeriodDto accountBookPeriodDto);

    /**
     * @description: 断号检测
     * @param: [ids]
     * @return: java.util.List<com.njwd.entity.ledger.VoucherAdjust> 
     * @author: xdy        
     * @create: 2019-08-08 17-01 
     */
    List<VoucherAdjust> checkBroken(List<Long> ids);

    List<VoucherAdjust> checkBroken(List<Long> ids, Byte isLock);

    /**
     * @description: 获取报告数据
     * @param: [accountBookPeriodReportDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodReportVo> 
     * @author: xdy        
     * @create: 2019-08-09 15-50 
     */
    Page<AccountBookPeriodReportVo> findReportPage(AccountBookPeriodReportDto accountBookPeriodReportDto);
}
