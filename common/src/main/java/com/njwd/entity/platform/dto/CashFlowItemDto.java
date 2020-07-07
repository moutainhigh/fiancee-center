package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 现金流量项目
 * @Date:15:38 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CashFlowItemDto extends CashFlowItemVo {
    private static final long serialVersionUID = -2590331792648713231L;
    private Page<CashFlowItemVo> page =new Page();

    /**
     * 租户ID
     */
    private Long rootEnterpriseId;

    private String codeOrName;
    /**
     * 基准现金流量表Id
     */
    private Long cashFlowId;

    /**
     * 基准现金流量表Id
     */
    private Long templateCashFlowId;
    /**
     * 现金流量模板名称
     */
    private String templateCashFlowName;
    /**
     * 现金流量项目基准名称
     */
    private String cashFlowName;
    /**
     * 状态
     */
    private String status;

    /**
     * Id集合
     */
    private List<Long> ids;

    /**
     *操作的ID列表
     **/
    private List<CashFlowItemVo> changeList;

    /**
     * code集合
     */
    private List<String> codes;


    /**
     * 版本号
     */
    private List<Integer> versions;


    /**
     * 更新时oldname
     */
    private String oldName;

    /**
     * 旧编码
     */
    private String oldCode;
    /**
     * 校验重复字段集合
     */
    private List<String> columnList;

    /**
     * 是否是管理员 0：否 1:是
     */
    private Byte isEnterpriseAdmin;

    /**
     * 账簿ID
     */
    private Long accountBookTypeId;

    /**
     * 账簿类型名称
     */
    private String accountBookTypeName;


    /**
     * 会计准则ID
     */
    private Long accStandardId;

    /**
     * 会计准则名称
     */
    private String accStandardName;


    /**
     * 旧上级编码
     */
    private String oldUpCode;


    /**
     * 是否包含已被引用数据 0：不包含  1：包含
     */
    private Byte isContainUsed;

    /**
     * 上级版本号
     */
    private Integer upVersion;

    /**
     * 上级ID
     */
    private Long upId;

    /**
     * 发布消息
     */
    private MessageDto messageDto;

    /**
     * 平台主键集合，初始化使用
     */
    private List<Long> platformIds;

    /**
     * 引入标识
     */
    private Boolean importFlag =false;

}
