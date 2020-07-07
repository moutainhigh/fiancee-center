package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SysAuxData;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 辅助资料
 * @Date:17:49 2019/6/13
 **/
public interface SysAuxDataMapper extends BaseMapper<SysAuxData> {

    /**
     * @Description 根据NameOrCode和Type查询辅助资料分页
     * @Author liuxiang
     * @Date:15:33 2019/7/2
     * @Param [sysAuxDataDto, page]
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    Page<SysAuxDataVo> findAuxDataPageByNameCodeType(Page<SysAuxDataVo> page, @Param("sysAuxDataDto") SysAuxDataDto sysAuxDataDto);

    /**
     * @Description 根据Name和Type查询辅助资料列表
     * @Author liuxiang
     * @Date:15:33 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    List<SysAuxDataVo> findSysAuxDataListByNameType(SysAuxDataDto sysAuxDataDto);

    /**
     * @Description 根据Type查询辅助资料列表
     * @Author liuxiang
     * @Date:15:33 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    List<SysAuxDataVo> findSysAuxDataListByType(SysAuxDataDto sysAuxDataDto);

    /**
     * @Description 根据Name和Type查询辅助资料列表
     * @Author liuxiang
     * @Date:15:33 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    List<SysAuxDataVo> findSysAuxDataListByNames(SysAuxDataDto sysAuxDataDto);

    /**
     * @Description 根据ID查询辅助资料
     * @Author liuxiang
     * @Date:15:34 2019/7/2
     * @Param [sysAuxDataVo]
     * @return com.njwd.platform.entity.vo.SysAuxDataVo
     **/
    SysAuxDataVo findSysAuxDataById(SysAuxDataDto sysAuxDataDto);

}