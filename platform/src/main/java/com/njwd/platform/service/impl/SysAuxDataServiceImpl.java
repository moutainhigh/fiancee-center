package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.platform.mapper.SysAuxDataMapper;
import com.njwd.platform.service.SysAuxDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 辅助资料
 * @Date:16:50 2019/7/2
 **/
@Service
public class SysAuxDataServiceImpl implements SysAuxDataService {

    @Autowired
    private SysAuxDataMapper sysAuxDataMapper;


    /**
     * @Description 根据NameOrCode和Type查询辅助资料分页
     * @Author liuxiang
     * @Date:16:49 2019/7/2
     * @Param [sysAuxDataDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    @Override
    @Cacheable(value = "auxDataPageByNameCodeType", key = "#sysAuxDataDto.codeOrName+'-'+#sysAuxDataDto.type+'-'+#sysAuxDataDto.page.current+'-'+#sysAuxDataDto.page.size")
    public Page<SysAuxDataVo> findAuxDataPageByNameCodeType(SysAuxDataDto sysAuxDataDto) {
        Page<SysAuxDataVo> page = sysAuxDataDto.getPage();
        page=sysAuxDataMapper.findAuxDataPageByNameCodeType(page,sysAuxDataDto);
        return page;
    }

    /**
     * @Description 根据Name和Type查询辅助资料列表
     * @Author liuxiang
     * @Date:16:50 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    @Override
    @Cacheable(value = "sysAuxDataListByNameType", key = "#sysAuxDataDto.name+'-'+#sysAuxDataDto.type")
    public List<SysAuxDataVo> findSysAuxDataListByNameType(SysAuxDataDto sysAuxDataDto) {
        return sysAuxDataMapper.findSysAuxDataListByNameType(sysAuxDataDto);
    }

    /**
     * @Description 根据Type查询辅助资料列表
     * @Author liuxiang
     * @Date:16:50 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    @Override
    //@Cacheable(value = "sysAuxDataListByType", key = "#sysAuxDataDto.type+''")
    public List<SysAuxDataVo> findSysAuxDataListByType(SysAuxDataDto sysAuxDataDto) {
        return sysAuxDataMapper.findSysAuxDataListByType(sysAuxDataDto);
    }

    /**
     * @Description 根据Names查询辅助资料列表
     * @Author liuxiang
     * @Date:16:51 2019/7/2
     * @Param [sysAuxDataVo]
     * @return java.util.List<com.njwd.platform.entity.vo.SysAuxDataVo>
     **/
    @Override
    @Cacheable(value = "sysAuxDataListByNames", key = "#sysAuxDataDto.names+''")
    public List<SysAuxDataVo> findSysAuxDataListByNames(SysAuxDataDto sysAuxDataDto) {
        return sysAuxDataMapper.findSysAuxDataListByNames(sysAuxDataDto);
    }

    /**
     * @Description 根据ID查询辅助资料
     * @Author liuxiang
     * @Date:16:51 2019/7/2
     * @Param [sysAuxDataVo]
     * @return com.njwd.platform.entity.vo.SysAuxDataVo
     **/
    @Override
    @Cacheable(value = "sysAuxDataById", key = "#sysAuxDataDto.id+''",unless="#result == null")
    public SysAuxDataVo findSysAuxDataById(SysAuxDataDto sysAuxDataDto) {
        return sysAuxDataMapper.findSysAuxDataById(sysAuxDataDto);
    }
}
