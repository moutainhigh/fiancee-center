package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.SysMenu;
import com.njwd.entity.basedata.SysRole;
import com.njwd.entity.basedata.SysRoleMenu;
import com.njwd.entity.basedata.SysUserRole;
import com.njwd.entity.basedata.dto.SysMenuDto;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.vo.SysMenuVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.mapper.SysMenuMapper;
import com.njwd.financeback.mapper.SysRoleMapper;
import com.njwd.financeback.mapper.SysRoleMenuMapper;
import com.njwd.financeback.service.SysMenuService;
import com.njwd.handler.UserRealm;
import com.njwd.utils.FastUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysMenuServiceImpl implements SysMenuService {
	@Resource
	private SysMenuMapper sysMenuMapper;
	@Resource
	private SysRoleMenuMapper sysRoleMenuMapper;
	@Resource
	private SysRoleMapper sysRoleMapper;
	@Resource
	private UserRealm userRealm;
	@Override
	@CacheEvict(cacheNames = Constant.RedisCache.MENU_PERM, allEntries = true)
	public Long add(SysMenu sysMenu) {
		checkDuplication(sysMenu);
		if (sysMenu.getParentId() != null && 0 != sysMenu.getParentId()) {
			// 将父权限is_child设为parent
			SysMenu parentSysMenu = new SysMenu();
			parentSysMenu.setIsChild(Constant.Is.NO);
			parentSysMenu.setUpdatorId(sysMenu.getCreatorId());
			sysMenuMapper.update(parentSysMenu, new QueryWrapper<SysMenu>().eq(Constant.ColumnName.MENU_ID, sysMenu.getParentId()).eq(Constant.ColumnName.IS_CHILD, Constant.Is.YES));
			if (Constant.MenuType.MENU == sysMenu.getType()) {
				String catalogName = sysMenuMapper.findNameByMenuParentId(sysMenu.getParentId());
				// 设置目录名
				sysMenu.setTypeName(catalogName);
			}
		}
		sysMenuMapper.insert(sysMenu);
		return sysMenu.getMenuId();
	}

	@Override
	@CacheEvict(cacheNames = Constant.RedisCache.MENU_PERM, allEntries = true)
	public Long update(SysMenu sysMenu) {
		FastUtils.checkParams(sysMenu.getMenuId());
		checkDuplication(sysMenu);
		sysMenu.setCreateTime(null);
		sysMenu.setCreatorId(null);
		// 该接口不可修改shiro权限定义 不可修改父权限,类型
		sysMenu.setPermission(null);
		sysMenu.setParentId(null);
		sysMenu.setType(null);
		sysMenuMapper.updateById(sysMenu);
		return sysMenu.getMenuId();
	}

	@Override
	public Set<String> getPermissionDefinitionsByUserId(SysUserVo sysUserVo) {
		return sysMenuMapper.getPermissionDefinitionsByUserId(sysUserVo.getUserId(), sysUserVo.getRootEnterpriseId());
	}

	@Override
	public Page<SysMenuVo> findPage(SysMenuDto sysMenuDto) {
		Page<SysMenuVo> page = sysMenuDto.getPage();
		return page.setRecords(sysMenuMapper.findPage(sysMenuDto, page));
	}

	@Override
	@Cacheable(cacheNames = Constant.RedisCache.MENU_PERM, key = "targetClass.simpleName+':'+methodName", condition = "#sysRoleDto.menuParentId == null")
	public List<SysMenuVo> findList(SysRoleDto sysRoleDto) {
		if (sysRoleDto.getRoleId() != null) {
			// 提供roleId时查询所有
			return getList(sysRoleDto);
		}
		// 未提供roleId时返回权限树:先查所有,在java中通过栈遍历分层
		LinkedList<SysMenuVo> allList = sysMenuMapper.findListByUserId(null, null);
		// 取出根级权限列表
		List<SysMenuVo> rootMenuList = new ArrayList<>();
		Iterator<SysMenuVo> iterator = allList.iterator();
		while (iterator.hasNext()) {
			SysMenuVo sysMenuVo = iterator.next();
			if (sysRoleDto.getMenuParentId() != null) {
				// 取局部树
				if (sysRoleDto.getMenuParentId().equals(sysMenuVo.getParentId())) {
					rootMenuList.add(sysMenuVo);
					iterator.remove();
				}
			} else if (sysMenuVo.getParentId() == null || 0 == sysMenuVo.getParentId()) {
				rootMenuList.add(sysMenuVo);
				iterator.remove();
			}
		}
		// 通过栈遍历分层
		LinkedList<SysMenuVo> stack = new LinkedList<>(rootMenuList);
		List<SysMenuVo> emptyList = Collections.emptyList();
		while (!stack.isEmpty()) {
			SysMenuVo vo = stack.pop();
			List<SysMenuVo> subList = new LinkedList<>();
			vo.setSysMenuList(subList);
			iterator = allList.iterator();
			while (iterator.hasNext()) {
				SysMenuVo sysMenuVo = iterator.next();
				if (vo.getMenuId().equals(sysMenuVo.getParentId())) {
					subList.add(sysMenuVo);
					if (Constant.Is.NO.equals(sysMenuVo.getIsChild())) {
						stack.push(sysMenuVo);
					} else {
						sysMenuVo.setSysMenuList(emptyList);
					}
					// 从待分配list中删除该元素(一个子权限只会有一个父权限)
					iterator.remove();
				}
			}
		}
		return rootMenuList;
	}
	@Override
	public List<SysMenuVo> getList(SysRole sysRole) {
		return sysMenuMapper.findList(sysRole.getRoleId(), null);
	}

	@Override
	public void assign(SysRoleDto sysRoleDto, SysUserVo operator) {
		SysRole existSysRole = sysRoleMapper.selectById(sysRoleDto.getRoleId());
		FastUtils.checkNull(existSysRole);
		if (!existSysRole.getRootEnterpriseId().equals(sysRoleDto.getRootEnterpriseId())) {
			throw new ServiceException(ResultCode.BAD_REQUEST);
		}
		assignPerm(sysRoleDto, operator);
	}

	@Override
	public void assignPerm(SysRoleDto sysRoleDto, SysUserVo operator) {
		// 先删除原关联菜单
		SysRoleMenu sysRoleMenuQuery = new SysRoleMenu();
		sysRoleMenuQuery.setRoleId(sysRoleDto.getRoleId());
		sysRoleMenuMapper.delete(new QueryWrapper<>(sysRoleMenuQuery));
		if (sysRoleDto.getSysMenuList() != null && sysRoleDto.getSysMenuList().size() != 0) {
			sysRoleMenuMapper.insertBatch(sysRoleDto, operator);
		}
		// 清空所有权限缓存
		userRealm.clearCachedAuthorizationInfo(operator.getRootEnterpriseId(), null);
	}

	@Override
	@CacheEvict(cacheNames = Constant.RedisCache.MENU_PERM, allEntries = true)
	public int updateBatch(SysMenuDto sysMenuDto, SysUserVo operator) {
		SysMenu sysMenu = new SysMenu();
		sysMenu.setIsDel(sysMenuDto.getIsDel());
		sysMenu.setUpdatorId(operator.getUserId());
		sysMenu.setUpdatorName(operator.getName());
		return sysMenuMapper.update(sysMenu, new QueryWrapper<SysMenu>().in(Constant.ColumnName.MENU_ID, sysMenuDto.getMenuIds()));
	}

	@Override
	public List<Long> findListByCompanyAndUser(SysUserRole sysUserRole) {
		return sysMenuMapper.findListByCompanyAndUser(sysUserRole);
	}

	/**
	 * 可选择子系统列表
	 * @param sysMenuDto
	 * @return
	 */
	@Override
	public Page<SysMenuVo> findEnableSysList(SysMenuDto sysMenuDto) {
		Page<SysMenuVo> page = sysMenuDto.getPage();
		List<SysMenuVo> sysMenuVos = sysMenuMapper.findEnableSysList(sysMenuDto,page);
		return page.setRecords(sysMenuVos);
	}

	/**
	 * 可选择菜单列表
	 * @param sysMenuDto
	 * @return
	 */
	@Override
	public Page<SysMenuVo> findEnableMenuList(SysMenuDto sysMenuDto) {
		Page<SysMenuVo> page = sysMenuDto.getPage();
		List<SysMenuVo> sysMenuVos = sysMenuMapper.findEnableMenuList(sysMenuDto,page);
		return page.setRecords(sysMenuVos);
	}

	/**
	 * 校验权限名是否重复
	 */
	private void checkDuplication(SysMenu sysMenu) {
		int count = sysMenuMapper.checkDuplication(sysMenu);
		if (count > 0) {
			throw new ServiceException(ResultCode.MENU_EXIST);
		}
	}
}
