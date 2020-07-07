package com.njwd.platform.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 辅助资料
 * @Date:10:38 2019/6/24
 **/
@RequestMapping("platform/sysAuxData")
public interface SysAuxDataApi {

    /**
     * @Description 根据Name和Type查询辅助资料列表
     * @Param [accountSubject]
     * @return java.lang.String
     **/
    @PostMapping("findSysAuxDataListByNameType")
    Result<List<SysAuxDataVo>> findSysAuxDataListByNameType(SysAuxDataDto platformSysAuxDataDto);

    /**
     * @Description 根据NameOrCode和Type查询辅助资料分页
     * @Param [accountSubject]
     * @return java.lang.String
     **/
    @PostMapping("findAuxDataPageByNameCodeType")
    Result<Page<SysAuxDataVo>> findAuxDataPageByNameCodeType(SysAuxDataDto platformSysAuxDataDto);

    /**
     * @Description 根据Type查询辅助资料列表
     * @Param [accountSubject]
     * @return java.lang.String
     **/
    @PostMapping("findSysAuxDataListByType")
    Result<List<SysAuxDataVo>> findSysAuxDataListByType(SysAuxDataDto platformSysAuxDataDto);

    /**
     * @Description 根据Names查询辅助资料列表
     * @Param [accountSubject]
     * @return java.lang.String
     **/
    @PostMapping("findSysAuxDataListByNames")
    Result<List<SysAuxDataVo>> findSysAuxDataListByNames(SysAuxDataDto platformSysAuxDataDto);
}
