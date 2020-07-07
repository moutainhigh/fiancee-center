package com.njwd.entity.base;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description 共通MODEL父类
 * @Date 2019/7/4 17:44
 * @Author 朱小明
 */
@Getter
@Setter
public class BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 默认自动递增
     */
    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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
     * 删除标识 0：未删除、1：删除
     */
    private Byte isDel;

    /**
     * 版本号 并发版本
     */
    @Version
    private Integer version;

    /**
     * 批量处理id集合
     */
    @TableField(exist = false)
    private List<Long> batchIds;

    /**
     * 数据库表名
     */
    @TableField(exist = false)
    private String tableName;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = false)
    private ManagerInfo manageInfos;
}
