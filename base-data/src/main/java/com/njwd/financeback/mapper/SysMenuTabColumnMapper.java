package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.SysMenuTabColumn;
import com.njwd.entity.basedata.dto.SysMenuTabColumnDto;
import com.njwd.entity.basedata.dto.query.TableConfigQueryDto;
import com.njwd.entity.basedata.vo.SysMenuTabColumnVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 表格配置mapper
 */
public interface SysMenuTabColumnMapper extends BaseMapper<SysMenuTabColumn> {
    /**
     * 根据菜单code和用户id查询
     * @param queryDto 查询实体
     * @return
     */
    List<SysMenuTabColumnVo> findList(@Param("queryDto") TableConfigQueryDto queryDto);

    /**
     * 批量删除用户对应的菜单选项
     * @param queryDto
     */
    void batchDelete(@Param("queryDto") TableConfigQueryDto queryDto);

    /**
     * 批量更新表格配置数据
     * @param tabColumnList 用于更新的list数据
     * @param isEnterpriseAdmin 当前菜单属性
     */
    void batchUpdate(@Param("tabColumnList") List<SysMenuTabColumnDto> tabColumnList, @Param("isEnterpriseAdmin") Byte isEnterpriseAdmin);

    /**
     * 用户设置的表格信息
     * @param queryDto
     * @return
     */
    List<SysMenuTabColumn> findUserList(TableConfigQueryDto queryDto);
}