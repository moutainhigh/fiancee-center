package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/6/19
 */
@Data
public class UserAccountBookEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 用户id
    */
    private Long userId;

    /**
     * 租户id
     */
    private Long rootEnterpriseId;

    /**
    * 公司id
    */
    private Long companyId;

    /**
    * 核算主体id
    */
    private Long accountBookEntityId;
    /**
     * 是否默认核算主体  1是 0否
     */
    private Byte isDefault;

    /**
    * 创建人编码
    */
    private Long creatorId;

    /**
    * 创建人
    */
    private String creatorName;

    /**
    * 创建时间
    */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}