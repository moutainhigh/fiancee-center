package com.njwd.platform.api;

import com.njwd.entity.platform.dto.SysTabColumnDto;
import com.njwd.entity.platform.vo.SysTabColumnVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 表格配置项公共Api
 * @Date:18:00 2019/6/13
 **/
@RequestMapping("platform/sysTabColumn")
public interface SysTabColumnApi {

    /**
     * @Description 查询表格配置项列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findSysTabColumnList")
    Result<List<SysTabColumnVo>> findSysTabColumnList(SysTabColumnDto sysTabColumnDto);

    /**
     * @Description 根据ID查询表格配置项
     * @Param [sysTabColumn]
     * @return java.lang.String
     **/
    @PostMapping("findSysTabColumnById")
    Result<SysTabColumnVo> findSysTabColumnById(SysTabColumnDto platformSysTabColumnDto);

}
