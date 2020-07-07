package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.SysTabColumn;
import com.njwd.entity.platform.dto.SysTabColumnDto;
import com.njwd.entity.platform.vo.SysTabColumnVo;

import java.util.List;

/**
 * @author liuxiang
 * @Description 表格配置
 * @Date:10:52 2019/6/13
 */
public interface SysTabColumnMapper extends BaseMapper<SysTabColumn> {

    /**
     * @Description 查询表格配置列表
     * @Author liuxiang
     * @Date:15:36 2019/7/2
     * @Param [sysTabColumnVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysTabColumnVo>
     **/
    List<SysTabColumnVo> findSysTabColumnList(SysTabColumnDto sysTabColumnDto);


    /**
     * @Description 根据ID查询表格配置
     * @Author liuxiang
     * @Date:15:36 2019/7/2
     * @Param [sysTabColumnVo]
     * @return com.njwd.platform.entity.vo.SysTabColumnVo
     **/
    SysTabColumnVo findSysTabColumnById(SysTabColumnDto sysTabColumnDto);

}