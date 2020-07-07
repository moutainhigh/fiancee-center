package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author liuxiang
 * @Description 科目表
 * @Date:15:56 2019/6/25
 **/
@Getter
@Setter
public class Subject extends BaseModel implements Serializable {
    private static final long serialVersionUID = -2126930463962938546L;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 会计准则 【会计准则】表ID
     */
    private Long accStandardId;

    /**
     * 是否基准 0：否、1：是
     */
    private Byte isBase;

    /**
     * 账簿类型 【账簿类型】表ID
     */
    private Long accountTypeId;

    /**
     * 上级科目表 关联本表
     */
    private Long parentId;

    /**
     * 科目最大级次
     */
    private String maxLevel;

    /**
     * 最大级次
     */
    private Byte maximumLevel;
    /**
     * 会计要素id
     */
    private Long elementId;

    /**
     * 数据类型 1：共享、2：分配、3：私有
     */
    private Byte dataType;

    /**
     * 是否预置 0：否、1：是
     */
    private Byte isInit;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 审核状态 0：未审核、1：已审核
     */
    private Byte isApproved;

    /**
     * 发布状态 0：未发布、1：已发布
     */
    private Byte isReleased;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 账簿类型ID 对应【账簿类型】表ID
     */
    private String accountBookTypeId;

    /**
     * 账簿类型名称 对应【账簿类型】表NAME
     */
    private String accountBookTypeName;

    /**
     * 会计准则名称 对应【会计准则】表NAME
     */
    private String accStandardName;

    /**
     * 基准科目表ID
     */
    private Long subjectId;

    /**
     * 基准科目表名称
     */
    private String subjectName;

    /**
     * 科目表模板ID
     */
    private Long templateSubjectId;

    /**
     * 科目表模板名称
     */
    private String templateSubjectName;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

    /**
     * 平台ID 初始化
     */
    private Long platformId;

}
