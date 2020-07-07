package com.njwd.entity.pushweb;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author bjw
 * @create 2019-08-11 下午 4:23
 */
@Getter
@Setter
@TableName("wd_system_push_server")
public class PushServer implements Serializable {

    private static final long serialVersionUID = 5311564153663116097L;
    /**
     * 主键 默认自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *服务器名称
     */
    private String serverName;

    /**
     *连接地址
     */
    private String domainUrl;

    /**
     *账号信息
     */
    private String account;


}
