package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 周鹏
 * @Description 科目表辅助核算项
 * @Date:15:52 2019/8/23
 **/
@Data
@TableName(value = "wd_subject_auxiliary")
public class SubjectAuxiliary implements Serializable {
    private static final long serialVersionUID = -3288204783642982826L;

    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 科目表ID
     */
    private Long subjectId;

    /**
     * 科目编码
     */
    private String code;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 辅助核算来源 0：平台 1：自定义辅助核算
     */
    private Byte source;

    /**
     * 辅助核算来源表名
     */
    private String sourceTable;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者
     */
    private String creatorName;

    /**
     * 是否预置 0:否 1:是
     */
    private Byte isInit;

}