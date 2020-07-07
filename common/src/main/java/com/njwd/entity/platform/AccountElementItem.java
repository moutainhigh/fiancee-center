package com.njwd.entity.platform;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

/**
 * @Author lj
 * @Description 会计要素项
 * @Date:11:03 2019/8/22
 **/
@Data
public class AccountElementItem implements Serializable {
    private static final long serialVersionUID = 3763238184146582919L;
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 会计要素表id 【会计要素】表id
    */
    private Long elementId;

    /**
    * 编码
    */
    private String code;

    /**
    * 名称
    */
    private String name;

    /**
    * 方向 0：借 1：贷
    */
    private Byte direction;

    /**
    * 是否是损益科目 0：否 1：是
    */
    private Byte isProfitAndLoss;

    /**
    * 是否是表外科目 0：否 1：是
    */
    private Byte isOffBalance;

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
     * 是否中间科目 0否 1是
     */
    private Byte isMiddle;
    /**
     * 审核状态 0未审核 1已审核
     */
    private Byte isApproved;
    /**
     * 是否生效 0否 1是
     */
    private Byte isEnable;

    @TableField(exist = false)
    private ManagerInfo manageInfo;
}