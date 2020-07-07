package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.ledger.vo.BalanceSubjectInitVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 期初余额-科目期初
 * @Date:10:03 2019/7/29
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceSubjectInitDto extends BalanceSubjectInitVo {

    Page<AccountBookEntityVo> page = new Page<>();
    /**
     * 公司id
     */
    private Long companyId;

    /**
     * 余额方向 0：借方、1：贷方
     */
    private Byte balanceDirection;

    /**
     * 记录标示
     */
    private Byte recordFlag;

    /**
     * 科目分类CODE A01：现金科目 A02：银行科目 A03：现金等价物 B01：一般科目（A表示现金科目 B表示非现金科目）
     */
    private String accountCategory;

    /**
     * 会计要素项名称 【会计要素项】表NAME
     */
    private String accountElementItemName;

    /**
     * 是否为末级科目 0：否、1：是
     */
    private Byte isFinal;
    
    /**
     *批量插入科目信息
     **/
    private List<BalanceSubjectInitDto> balanceSubjectInitList;

    /**
     *批量插入科目辅助核算项目余额
     **/
    private List<BalanceSubjectInitAuxiliaryDto> balanceSubInitAuxiliaryList;

    /**
     * 辅助核算数量
     */
    private Integer auxiliaryNum;

    /**
     * 编码或名称
     */
    private String codeOrName;

    /**
     * 已选择核算主体ID List
     */
    private List<Long> selectedIdList;
    /**
     * 并发版本号
     */
    private Integer version;
}
