package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.CommParams;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author liuxiang
 * @Description 账簿期间表
 * @Date:16:46 2019/7/26
 **/
@Getter
@Setter
public class AccountBookPeriodDto extends AccountBookPeriodVo {
    private static final long serialVersionUID = -5650370778765644186L;

    /**
     * 账簿期间ID List
     */
    private List<Long> idList;

    /**
     * 账簿列表
     */
    private List<Long> accountBookIds;

    /**
     * 分页信息
     */
    private Page<AccountBookPeriodVo> page = new Page<>();

    //公共匹配参数
    private CommParams commParams = new CommParams();

    /**
     * 查询类型(0:查询开始期间后最近的已结账期间 1：查询结束期间前最近的已结账期间)
     */
    private Integer type;

    /**
     * 首条或者末条 0：首条，1：末条
     */
    private Byte beginOrEnd;

    /**
     * 期间年
     */
    private List<Integer> periodYears;

    /**
     * 期间号
     */
    private List<Byte> periodNums;

    /**
     * 是否启用分账核算 0:否 1:是
     */
    private Byte hasSubAccount;

    /**
     * 日期区间
     */
    private List<String> dateList;

    /**
     * 子系统标识 a：总帐，b：资产，c：应收
     */
    private String systemSign;

    /**
     * 是否已启用 0:否 1:是
     */
    private Byte status;

    /**
     * 是否已结账 0:否 1:是
     */
    private Byte isSettle;

    /**
     * 损益科目
     */
    private List<Long> profitLossList;

    /**
     *  以前期间或未来期间 -1:以前 1:未来
     */
    private Byte beforeOrFuture;

    /**
     * 画面code
     */
    private String menuCode;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 未来期间数
     */
    private Byte futurePeriodNum;

    /**
     * 核算主体Set
     */
    private Set<Long> abEntitySet;

    /**
     * 根据账簿ID集合和期间数集合
     */
    List<AccountBookPeriodVo> accountBookPeriodVos;

    /**
     * 是否未来期间
     */
    private Byte isFuture;

    /**
     * 制单日期
     */
    private Date voucherDate;

    /**
     * 是否查询最小期间 0：否，1：是
     */
    private Byte isLeast;

    /**
     * 是否查询最大期间 0：否，1：是
     */
    private Byte isMax;

    @Override
    public Integer getPeriodYearNum() {
        if (getPeriodYear() != null && getPeriodNum() != null) {
            return getPeriodYear() * 100 + getPeriodNum();
        } else {
            return super.getPeriodYearNum();
        }
    }
}
