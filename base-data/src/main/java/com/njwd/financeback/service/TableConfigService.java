package com.njwd.financeback.service;

import com.njwd.entity.basedata.SysMenuTabColumn;
import com.njwd.entity.basedata.dto.SysMenuTabColumnDto;
import com.njwd.entity.basedata.dto.query.TableConfigQueryDto;
import com.njwd.entity.basedata.vo.SysMenuTabColumnVo;

import java.util.List;

/**
 * 表格配置接口
 */
public interface TableConfigService {

    /**
     * 查询菜单列  的列表
     * @param queryDto
     * @return
     */
    List<SysMenuTabColumnVo> findList(TableConfigQueryDto queryDto);
    /**
     * 查询菜单列  的列表
     * @param queryDto
     * @return
     */
    List<SysMenuTabColumn> findUserList(TableConfigQueryDto queryDto);

    /**
     * 批量更新
     * @param queryDto
     * @param tabColumnList
     */
    void batchUpdate(TableConfigQueryDto queryDto, List<SysMenuTabColumnDto> tabColumnList);


    }
