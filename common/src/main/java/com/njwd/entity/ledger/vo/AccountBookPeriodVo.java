package com.njwd.entity.ledger.vo;

import com.njwd.entity.base.ManagerInfo;
import com.njwd.entity.ledger.AccountBookPeriod;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 账簿期间表
 * @Date:16:46 2019/7/26
 **/
@Getter
@Setter
public class AccountBookPeriodVo extends AccountBookPeriod {
    private static final long serialVersionUID = -5650370778765644186L;


    /**
     * 账簿类型名称
     */
    private String accountBookType;

    /**
     * 会计期间全名
     */
    private String accountBookPeriodName;

    /**
     * 有用,勿删除
     */
    @SuppressWarnings("unused")
    public String getAccountBookPeriodName() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.getPeriodYear());
        sb.append("年第");
        sb.append(super.getPeriodNum());
        sb.append("期");
        return sb.toString();
    }


    //过账时使用的几个字段
    //账簿期间id
    private Long accountBookPeriodId;
    //过账状态,true为过账成功 ,为false时,具体错误检查信息在messageList中
    private Boolean transferFlag;
    //检查项列表
    private List<TransferDetail> messageList = new ArrayList<>();
    //过账信息
    private ManagerInfo manageInfo;


    /**
     * 开始期间号
     */
    private Byte beginNumber;

    /**
     * 结束期间号
     */
    private Byte endNumber;

    /**
     *　已打开的年度要求没有凭证数据
     */
    private Integer count;


}
