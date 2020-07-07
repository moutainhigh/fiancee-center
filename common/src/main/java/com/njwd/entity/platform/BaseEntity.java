package com.njwd.entity.platform;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhuzs
 * @date 2019-11-21 14:56
 */
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 2306634866155453802L;
    private Long timestamp;
    private String sign;
}

