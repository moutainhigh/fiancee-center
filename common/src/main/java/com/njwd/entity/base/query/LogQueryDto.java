package com.njwd.entity.base.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.CommParams;
import com.njwd.entity.base.vo.SysLogVo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @description: 日志查询用户端Dto
 * @author: fancl
 * @create: 2019-06-10
 */
@Setter
@Getter
public class LogQueryDto implements Serializable {
    private Long rootEnterpriseId;
    //分页对象
    Page<SysLogVo> page = new Page<>();
    //公共匹配参数
    private CommParams commParams = new CommParams();
    //当前年月
    private String currentYearMonth;
    //系统名称
    private String sysName;
    //开始日期
    private String beginDay;
    //结束日期
    private String endDay;


}
