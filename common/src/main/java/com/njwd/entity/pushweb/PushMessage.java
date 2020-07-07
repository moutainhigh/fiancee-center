package com.njwd.entity.pushweb;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author bjw
 * @create 2019-08-11 下午 4:23
 */
@Getter
@Setter
public class PushMessage implements Serializable {
    private static final long serialVersionUID = -5487206731394373194L;
    /**
     * 发送目标用户
     */
    private String username;

    /**
     *发送内容
     */
    private String content;

    /**
     *发送主题
     */
    private String subject;


}
