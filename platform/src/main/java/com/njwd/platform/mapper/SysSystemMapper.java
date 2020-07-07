package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.SysSystem;
import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.SysSystemVo;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 子系统
 * @Date:17:52 2019/6/13
 **/
public interface SysSystemMapper extends BaseMapper<SysSystem> {

    /**
     * @Description 查询子系统列表
     * @Author liuxiang
     * @Date:15:35 2019/7/2
     * @Param [sysSystemDto]
     * @return java.util.List<com.njwd.platform.entity.vo.SysSystemVo>
     **/
    List<SysSystemVo> findSysSystemList(SysSystemDto sysSystemDto);

    /**
     * @Description 根据ID查询子系统
     * @Author liuxiang
     * @Date:15:35 2019/7/2
     * @Param [sysSystemDto]
     * @return com.njwd.platform.entity.vo.SysSystemVo
     **/
    SysSystemVo findSysSystemById(SysSystemDto sysSystemDto);
}