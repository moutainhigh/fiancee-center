package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import com.njwd.entity.base.ManagerInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/10/25
 */
@Getter
@Setter
public class SubjectSynergy {
    /**
    * 主键 默认自动递增
    */
    private Long id;

    /**
    * 租户ID
    */
    private Long rootEnterpriseId;

    /**
    * 归属科目表【wd_subject】ID
    */
    private Long subjectId;

    private String code;

    /**
    * 协同关系名
    */
    private String name;

    /**
    * 方向 0双向
    */
    private Byte direction;

    /**
    * 发送方 会计科目表【wd_account_subject】ID
    */
    private Long srcAccountSubjectId;

    /**
    * 发送方 辅助核算名称 【辅助核算】表NAME
    */
    private String srcAuxiliaryName;

    /**
    * 发送方 辅助核算值来源
    */
    private String srcAuxiliarySource;

    /**
    * 接收方 会计科目表【wd_account_subject】ID
    */
    private Long destAccountSubjectId;

    /**
    * 接收方 辅助核算名称 【辅助核算】表NAME
    */
    private String destAuxiliaryName;

    /**
    * 接收方 辅助核算值来源
    */
    private String destAuxiliarySource;

    /**
    * 启用期间年度
    */
    private Integer periodYear;

    /**
    * 启用期间号
    */
    private Byte periodNum;

    /**
    * 启用期间年号
    */
    private Integer periodYearNum;

    /**
    * 0待审核 1已审核
    */
    private Byte isApproved;

    /**
    * 0待发布 1已发布
    */
    private Byte isReleased;

    /**
    * 0未启用 1已启用
    */
    private Byte isEnable;

    /**
    * 删除标识 0：未删除、1：已删除
    */
    private Byte isDel;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 创建者ID
    */
    private Long creatorId;

    /**
    * 创建者
    */
    private String creatorName;

    /**
    * 更新时间
    */
    private Date updateTime;

    /**
    * 更新者ID
    */
    private Long updatorId;

    /**
    * 更新者
    */
    private String updatorName;

    /**
    * 管理信息:禁用人等信息
    */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

    /**
    * 版本号
    */
    @Version
    private Integer version;

    /**
     * 平台的协同配置ID
     **/
    private Long platformId;
}
