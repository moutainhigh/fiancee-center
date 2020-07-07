package com.njwd.entity.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 *@description: 监控外部类
 *@author: fancl
 *@create: 2019-09-18 
 */
@Getter
@Setter
public class MonitorBean implements Serializable {
    private List<Monitor> monitorList;
}
