package com.njwd.entity.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *@description: 监控实体bean
 *@author: fancl
 *@create: 2019-09-17 
 */
@Getter
@Setter
public class Monitor implements Serializable {

    //列表序号
    private int id;
    //监控类型 http ls
    private String type;
    //应用标识 base-data等
    private String appCode;
    //应用名称 总账等
    private String appName;
    //应用访问列表
    private String url;
    //IP
    private String ip;
    //主机名
    private String hostName;


    //应用状态 1 OK   0 不OK
    private Byte isOk;
    //状态更新时间
    private Date updateTime;

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("id:").append(id).append(",type:").append(type).append(",appCode:").append(appCode).append(",appName:").append(appName).append(",url:").append(url).append(",isOk:").append(isOk);
        return sb.toString();
    }

}
