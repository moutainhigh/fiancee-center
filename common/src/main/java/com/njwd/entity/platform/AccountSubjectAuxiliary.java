package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 会计科目辅助核算关系表
 * @Date:15:53 2019/6/25
 **/
@Data
public class AccountSubjectAuxiliary implements Serializable {
    private static final long serialVersionUID = 3763238184146582949L;
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会计科目ID 【会计科目】表ID
     */
    private Long accountId;

    /**
     * 辅助核算CODE
     */
    private String auxiliaryCode;

    /**
     * 辅助核算id
     */
    private Long auxiliaryId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 会计科目ID 【会计科目】表ID
     */
    private Long accountSubjectId;

    /**
     * 辅助核算名称
     */
    private String auxiliaryName;

    /**
     * 辅助核算值来源
     */
    private String auxiliarySource;

    /**
     * 辅助核算值来源表
     */
    private String auxiliarySourceTable;

    /**
     * 辅助核算值依赖
     */
    private String auxiliaryDependent;

    /**
     * 是否预置 0:否 1:是
     */
    private Byte isInit;

}
