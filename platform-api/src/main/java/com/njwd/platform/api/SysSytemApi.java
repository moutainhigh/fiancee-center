package com.njwd.platform.api;

import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 子系统说明公共Api
 * @Date:17:59 2019/6/13
 **/
@RequestMapping("platform/sysSystem")
public interface SysSytemApi {

    /**
     * @Description 查询子系统说明列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findSysSystemList")
    Result<List<SysSystemVo>> findSysSytemList(SysSystemDto platformSysSystemDto);

    @RequestMapping("initData")
    Result initData(@RequestBody SysSystemDto sysSystemDto);
}
