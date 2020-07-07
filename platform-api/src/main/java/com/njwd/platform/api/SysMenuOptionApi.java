package com.njwd.platform.api;

import com.njwd.entity.basedata.SysMenuOptionTable;
import com.njwd.entity.platform.dto.SysMenuOptionDto;
import com.njwd.entity.platform.vo.SysMenuOptionVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 菜单选项公共Api
 * @Date:17:59 2019/6/13
 **/
@RequestMapping("platform/sysMenuOption")
public interface SysMenuOptionApi {

    /**
     * @Description 查询菜单选项列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findSysMenuOptionList")
    Result<List<SysMenuOptionVo>> findSysMenuOptionList(SysMenuOptionDto sysMenuOptionDto);

    /**
     * @Description 根据ID查询菜单选项
     * @Param [sysMenuOption]
     * @return java.lang.String
     **/
    @PostMapping("findSysMenuOptionById")
    Result<SysMenuOptionVo> findSysMenuOptionById(SysMenuOptionDto platformSysMenuOptionDto);

    /**
     * @description: 
     * @param: [sysMenuOptionDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.SysMenuOptionTable> 
     * @author: xdy        
     * @create: 2019-09-09 17:02
     */
    @PostMapping("findOptionTable")
    Result<SysMenuOptionTable> findOptionTable(@RequestBody SysMenuOptionDto sysMenuOptionDto);
}
