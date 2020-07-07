package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 多栏账方案
 * @Date:17:16 2019/7/29
 **/
@Getter
@Setter
public class MultiColumnScheme implements Serializable {
    private static final long serialVersionUID = 1487627074724020937L;
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(value = "root_enterprise_id")
    private Long rootEnterpriseId;

    /**
     * 账簿ID
     */
    @TableField(value = "account_book_id")
    private Long accountBookId;

    /**
     * 账簿名称
     */
    @TableField(value = "account_book_name")
    private String accountBookName;

    /**
     * 方案编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 方案名称
     */
    @TableField(value = "scheme_name")
    private String schemeName;

    /**
     * 查询此类型 0：科目、1：辅助核算
     */
    @TableField(value = "scheme_type")
    private Byte schemeType;

    /**
     * 会计科目id
     */
    @TableField(value = "account_subject_id")
    private Long accountSubjectId;

    /**
     * 会计科目名称
     */
    private String accountSubjectName;

    /**
     * 是否自定义核算项目 0：否、1：是
     */
    @TableField(value = "is_custom")
    private Byte isCustom;

    /**
     * 辅助核算项ID
     */
    @TableField(value = "auxiliary_item_id")
    private Long auxiliaryItemId;

    /**
     * 辅助核算项名称
     */
    @TableField(value = "auxiliary_item_name")
    private String auxiliaryItemName;

    /**
     * 是否删除 0：否、1：是
     */
    @TableField(value = "is_del")
    private Byte isDel;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建人ID
     */
    @TableField(value = "creator_id")
    private Long creatorId;

    /**
     * 创建人
     */
    @TableField(value = "creator_name")
    private String creatorName;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 修改人ID
     */
    @TableField(value = "updator_id")
    private Long updatorId;

    /**
     * 修改人
     */
    @TableField(value = "updator_name")
    private String updatorName;

    /**
     * 公司主键
     */
    @TableField(value = "company_id")
    private Long companyId;

}