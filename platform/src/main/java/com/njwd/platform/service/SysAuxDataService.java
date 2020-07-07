package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 辅助资料
 * @Date:9:18 2019/6/14
 **/
public interface SysAuxDataService {


    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.SysAuxDataVo>
     * @Description 根据NameOrCode和Type查询辅助资料分页
     * @Author liuxiang
     * @Date:16:52 2019/7/2
     * @Param [sysAuxDataDto]
     **/
    Page<SysAuxDataVo> findAuxDataPageByNameCodeType(SysAuxDataDto sysAuxDataDto);

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     * @Description 根据Name和Type查询辅助资料列表
     * @Author liuxiang
     * @Date:16:52 2019/7/2
     * @Param [sysAuxDataVo]
     **/
    List<SysAuxDataVo> findSysAuxDataListByNameType(SysAuxDataDto sysAuxDataDto);


    /**
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     * @Description 根据Type查询辅助资料列表
     * @Author liuxiang
     * @Date:16:52 2019/7/2
     * @Param [sysAuxDataVo]
     **/
    List<SysAuxDataVo> findSysAuxDataListByType(SysAuxDataDto sysAuxDataDto);

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     * @Description 根据Names查询辅助资料列表
     * @Author liuxiang
     * @Date:16:51 2019/7/2
     * @Param [sysAuxDataVo]
     **/
    List<SysAuxDataVo> findSysAuxDataListByNames(SysAuxDataDto sysAuxDataDto);

    /**
     * @return com.njwd.platform.entity.vo.SysAuxDataVo
     * @Description 根据ID查询辅助资料
     * @Author liuxiang
     * @Date:16:51 2019/7/2
     * @Param [sysAuxDataVo]
     **/
    SysAuxDataVo findSysAuxDataById(SysAuxDataDto sysAuxDataDto);

}
