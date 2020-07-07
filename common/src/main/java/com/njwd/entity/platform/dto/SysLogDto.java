package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SysLog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/20 15:47
 */
@Getter
@Setter
public class SysLogDto extends SysLog {

    private Page<SysLog> page = new Page<>();

    private String beginTime;

    private String endTime;

    private List<String> sysNames;

    private List<String> menuNames;

    private List<Long> userIds;

}
