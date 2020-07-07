package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 会计科目币种关系表
 * @Date:15:53 2019/6/25
 **/
@Data
public class AccountSubjectCurrency implements Serializable {
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
        private Long id;

    /**
     * 会计科目编码 【会计科目】表ID
     */
    @TableField(value = "account_id")
        private Long accountId;

    /**
     * 币种编码 【币种】表ID
     */
    @TableField(value = "currency_id")
        private Long currencyId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
        private Date createTime;

    /**
     * 会计科目ID 【会计科目】表ID
     */
    private Long accountSubjectId;

    /**
     * 币种名称 【币种】表NAME
     */
    private String currencyName;



}