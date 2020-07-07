package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 科目表
 *
 * @author zhuzs
 * @date 2019-07-02 17:58
 */
@Data
public class AccountingSubject {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 账簿类型ID
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
     * 基准科目表ID
     */
    private  Long subjectId;

    /**
     * 基准科目表
     */
    private  String subjectName;

    /**
     * 科目表模板ID
     */
    private Long templateSubjectId;

    /**
     * 科目表模板
     */
    private String templateSubjectName;

    /**
     * 科目表模板最大级次
     */
    private String maxLevel;

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

}

