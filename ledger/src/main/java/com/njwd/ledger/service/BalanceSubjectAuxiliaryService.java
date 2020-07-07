package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import com.njwd.entity.ledger.dto.*;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.entity.platform.vo.AccountSubjectVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

/**
 * 科目辅助核算项目余额
 *
 * @author zhuzs
 * @date 2019-08-09 13:55
 */
public interface BalanceSubjectAuxiliaryService extends IService<BalanceSubjectAuxiliary> {

    /**
     * 更新余额
     *
     * @param balanceSubjectAuxiliaries balanceSubjectAuxiliaries
     * @param voucherDto                voucherDto
     * @param updateType
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 13:48
     **/
    void updateBatch(Collection<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaries, VoucherDto voucherDto, byte updateType);

    /**
     * 重分类 包含未过账
     * @return
     */
    BalanceSubjectVo findBySubjectIdList(List<AccountSubjectVo> accountSubjectVoList, BalanceDto balanceDto);

    /**
     *重分类 不包含未过账
     * @return
     */
    BalanceSubjectVo findPostingBySubjectIdList(List<AccountSubjectVo> accountSubjectVoList,BalanceDto balanceDto);

    /**
     * 根据条件统计辅助核算余额表
     *
     * @param queryDto
     * @return BalanceSubjectAuxiliaryVo
     * @author: 周鹏
     * @create: 2019/8/16
     */
    List<BalanceSubjectAuxiliaryVo> findListByParam(BalanceSubjectAuxiliaryItemQueryDto queryDto);

    /**
     * 初始化
     *
     * @param balanceSubjectAuxiliary balanceSubjectAuxiliary
     * @param voucherDto              voucherDto
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 12:18
     **/
    void initBatch(List<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliary, VoucherDto voucherDto);

    /**
     * Excel 导出辅助核算余额表
     *
     * @param queryDto
     * @param response
     * @author: 周鹏
     * @create: 2019/8/29
     */
    void exportListExcel(BalanceSubjectAuxiliaryItemQueryDto queryDto, HttpServletResponse response);
}
