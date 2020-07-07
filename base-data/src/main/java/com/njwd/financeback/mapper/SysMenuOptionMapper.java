package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.SysMenuOptionTable;
import com.njwd.entity.platform.SysMenuOption;
import com.njwd.entity.platform.vo.SysMenuOptionVo;

import java.util.List;

/**
 * <p>
 * 菜单选项设置 Mapper 接口
 * </p>
 *
 * @author xdy
 * @since 2019-06-19
 */
public interface SysMenuOptionMapper extends BaseMapper<SysMenuOption> {

    List<SysMenuOptionVo> findList(SysMenuOption sysMenuOption);

    Integer findTableDataCount(SysMenuOptionTable sysMenuOptionTable);

    int addBatch(List<? extends SysMenuOption> menuOptionList);

}
