package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.dto.UserRoleDto;
import com.njwd.entity.basedata.vo.SysMenuVo;
import com.njwd.entity.basedata.vo.SysRoleVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserRoleVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.mapper.SysRoleMapper;
import com.njwd.financeback.mapper.SysUserEnterpriseMapper;
import com.njwd.financeback.mapper.SysUserRoleMapper;
import com.njwd.financeback.mapper.UserAccountBookEntityMapper;
import com.njwd.financeback.service.SysMenuService;
import com.njwd.financeback.service.SysRoleService;
import com.njwd.handler.UserRealm;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/21
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysRoleServiceImpl implements SysRoleService {
	@Resource
	private SysRoleMapper sysRoleMapper;
	@Resource
	private SysUserRoleMapper sysUserRoleMapper;
	@Resource
	private UserRealm userRealm;
	@Resource
	private SysMenuService sysMenuService;
	@Resource
	private SysUserEnterpriseMapper sysUserEnterpriseMapper;
	@Resource
	private UserAccountBookEntityMapper userAccountBookEntityMapper;
	@Resource
	private ReferenceRelationService referenceRelationService;
	@Resource
	private SequenceService sequenceService;

	@Override
	public Long add(SysRole sysRole) {
		checkDuplication(sysRole);
		sysRoleMapper.insert(sysRole);
		return sysRole.getRoleId();
	}

	@Override
	public Long addEnterpriseWithPerm(SysRoleDto sysRoleDto, SysUserVo operator) {
		checkDuplication(sysRoleDto);
		SysRole sysRole = new SysRole();
		FastUtils.copyProperties(sysRoleDto, sysRole);
		sysRole.setCreatorId(operator.getUserId());
		sysRole.setCreatorName(operator.getName());
		if (StringUtils.isEmpty(sysRoleDto.getCode())) {
			// 生成code
			sysRole.setCode(sequenceService.getCode(Constant.BaseCodeRule.ROLE, Constant.BaseCodeRule.LENGTH_FIVE, operator.getRootEnterpriseId(), Constant.BaseCodeRule.ENTERPRISE));
		}
		sysRoleMapper.insert(sysRole);
		Long roleId = sysRole.getRoleId();
		if (sysRoleDto.getSysMenuList() != null) {
			sysRoleDto.setRoleId(roleId);
			sysMenuService.assignPerm(sysRoleDto, operator);
		}
		return roleId;
	}

	@Override
	public void checkCode(SysRole sysRole) {
		if (StringUtils.isEmpty(sysRole.getCode())) {
			return;
		}
		QueryWrapper<SysRole> queryWrapper = new QueryWrapper<SysRole>().eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, sysRole.getRootEnterpriseId()).eq(Constant.ColumnName.CODE, sysRole.getCode()).eq(Constant.ColumnName.IS_DEL, Constant.Is.NO).last(Constant.ConcatSql.LIMIT_1);
		if (sysRole.getRoleId() != null) {
			queryWrapper.ne(Constant.ColumnName.ROLE_ID, sysRole.getRoleId());
		}
		SysRole existSysRole = sysRoleMapper.selectOne(queryWrapper);
		if (existSysRole != null) {
			throw new ServiceException(ResultCode.CODE_EXIST);
		}
	}

	@Override
	public Long update(SysRole sysRole) {
		FastUtils.checkParams(sysRole.getRoleId());
		if (sysRole.getIsDel() == null || !Constant.Is.YES.equals(sysRole.getIsDel())) {
			checkDuplication(sysRole);
		}
		sysRole.setCreateTime(null);
		sysRole.setCreatorId(null);
		sysRole.setRootEnterpriseId(null);
		sysRoleMapper.updateById(sysRole);
		return sysRole.getRoleId();
	}

	@Override
	public Page<SysRoleVo> findPage(SysRoleDto sysRoleDto) {
		Page<SysRoleVo> page = sysRoleDto.getPage();
		sysRoleDto.getCommParams().setOrColumn(Constant.OrMatchColumn.SYS_ROLE);
		return page.setRecords(sysRoleMapper.findPage(sysRoleDto, page, sysRoleDto.getCommParams()));
	}

	@Override
	public List<SysRoleVo> findList(SysUserDto sysUserDto) {
		FastUtils.checkParams(sysUserDto.getRootEnterpriseId());
		return sysRoleMapper.findList(sysUserDto.getUserId(), sysUserDto.getRootEnterpriseId());
	}

	@Override
	public void assign(SysUserDto sysUserDto, SysUserVo operator) {
		FastUtils.checkParams(sysUserDto.getUserId(), sysUserDto.getRootEnterpriseId());
		if (sysUserDto.getSysRoleAssignMap() != null && sysUserDto.getSysRoleAssignMap().size() > 0) {
			//删除原公司绑定的关联角色
			SysUserRole sysUserRoleQuery = new SysUserRole();
			sysUserRoleQuery.setUserId(sysUserDto.getUserId());
			sysUserRoleQuery.setRootEnterpriseId(sysUserDto.getRootEnterpriseId());
			sysUserRoleMapper.delete(new QueryWrapper<>(sysUserRoleQuery).in(Constant.ColumnName.COMPANY_ID, sysUserDto.getSysRoleAssignMap().keySet()));
			for (Map.Entry<Long, Long[]> entry : sysUserDto.getSysRoleAssignMap().entrySet()) {
				if (entry.getKey() != 0 && entry.getValue() != null && entry.getValue().length != 0) {
					// 未提交公司对应的角色id时,仅做删除原关联角色,不插入绑定关系
					sysUserRoleMapper.insertBatch(entry.getKey(), entry.getValue(), sysUserDto, operator);
				}
			}
			// 刷新被修改用户的权限
			userRealm.clearCachedAuthorizationInfo(operator.getRootEnterpriseId(), sysUserDto.getUserId());
		}
		if (sysUserDto.getAccountBookEntityMap() != null && !sysUserDto.getAccountBookEntityMap().isEmpty()) {
			// 删除原公司关联的核算账簿主体
			UserAccountBookEntity userAccountBookEntityQuery = new UserAccountBookEntity();
			userAccountBookEntityQuery.setUserId(sysUserDto.getUserId());
			userAccountBookEntityQuery.setRootEnterpriseId(sysUserDto.getRootEnterpriseId());
			userAccountBookEntityMapper.delete(new QueryWrapper<>(userAccountBookEntityQuery).in(Constant.ColumnName.COMPANY_ID, sysUserDto.getAccountBookEntityMap().keySet()));
			assignAccountBookEntity(sysUserDto, operator);
		}
	}

	/**
	 * 批量设置用户的账簿主体
	 * @param sysUserDto accountBookEntityMap
	 *  	根据公司id(key)对核算账簿主体分类,value为公司下的accountBookEntityId数组：{"1":[1,2,3],"2":[4,5,6]}
	 * @param operator operator
	 */
	private void assignAccountBookEntity(SysUserDto sysUserDto, SysUserVo operator) {
		for (Map.Entry<Long, Long[]> entry : sysUserDto.getAccountBookEntityMap().entrySet()) {
			if (entry.getKey() != 0 && entry.getValue() != null && entry.getValue().length != 0) {
				// 未提交对应的账簿主体id时,仅做删除原关联主体,不插入绑定关系
				userAccountBookEntityMapper.insertBatch(entry.getKey(), entry.getValue(), sysUserDto, operator);
			}
		}
	}

	@Override
	public List<Long> assignBatchAdd(SysUserDto sysUserDto, SysUserVo operator) {
		if (sysUserDto.getCompanyIds() == null || sysUserDto.getCompanyIds().length == 0) {
			// 未传公司id不处理
			return Collections.emptyList();
		}
		List<Long> editUserIds = new LinkedList<>();
		//删除原公司绑定的关联角色
		List<Long> editRoleUserIds = new LinkedList<>();
		List<Long> editBookEntityUserIds = new LinkedList<>();
		sysUserDto.getSysUserDtoList().forEach(userDto -> {
			if (userDto.getAssignRoleIds() != null) {
				// 修改岗位
				editRoleUserIds.add(userDto.getUserId());
			}
			if (userDto.getAccountBookEntityMap() != null) {
				// 修改账簿主体
				editBookEntityUserIds.add(userDto.getUserId());
			}
			editUserIds.add(userDto.getUserId());
		});
		if (editRoleUserIds.size() > 0) {
			// 删除原绑定公司岗位
			assignBatchDelete(sysUserDto, operator.getRootEnterpriseId(), editRoleUserIds);
		}
		if (editBookEntityUserIds.size() > 0) {
			// 删除原绑定的账簿主体
			UserAccountBookEntity bookEntityQuery = new UserAccountBookEntity();
			bookEntityQuery.setRootEnterpriseId(operator.getRootEnterpriseId());
			userAccountBookEntityMapper.delete(new QueryWrapper<>(bookEntityQuery).in(Constant.ColumnName.USER_ID, editBookEntityUserIds).in(Constant.ColumnName.COMPANY_ID, (Object[]) sysUserDto.getCompanyIds()));
		}
		// 可能值:  {"companyIds":[7],"sysUserDtoList":[{"userId":316}]}
		for (SysUserDto userDto : sysUserDto.getSysUserDtoList()) {
			if (userDto.getAssignRoleIds() != null && userDto.getAssignRoleIds().length != 0) {
				sysUserRoleMapper.assignBatchAdd(userDto, sysUserDto.getCompanyIds(), operator);
			}
			if (userDto.getAccountBookEntityMap() != null && !userDto.getAccountBookEntityMap().isEmpty()) {
				assignAccountBookEntity(userDto, operator);
			}
		}
		// 刷新被修改用户的权限
		editUserIds.forEach(userId -> userRealm.clearCachedAuthorizationInfo(operator.getRootEnterpriseId(), userId));
		return editUserIds;
	}

	@Override
	public void assignDelete(SysUserDto sysUserDto, SysUserVo operator) {
		FastUtils.checkParams(sysUserDto.getUserId());
		SysUserRole sysUserRoleQuery = new SysUserRole();
		sysUserRoleQuery.setRootEnterpriseId(operator.getRootEnterpriseId());
		sysUserRoleQuery.setUserId(sysUserDto.getUserId());
		sysUserRoleQuery.setRoleId(sysUserDto.getRoleId());
		UserAccountBookEntity bookEntityQuery = new UserAccountBookEntity();
		bookEntityQuery.setRootEnterpriseId(operator.getRootEnterpriseId());
		bookEntityQuery.setUserId(sysUserDto.getUserId());
		bookEntityQuery.setAccountBookEntityId(sysUserDto.getAccountBookEntityId());
		QueryWrapper<SysUserRole> userRoleWrapper = new QueryWrapper<>(sysUserRoleQuery);
		QueryWrapper<UserAccountBookEntity> bookEntityWrapper = new QueryWrapper<>(bookEntityQuery);
		if (sysUserDto.getCompanyIds() != null) {
			userRoleWrapper.in(Constant.ColumnName.COMPANY_ID, (Object[]) sysUserDto.getCompanyIds());
			bookEntityWrapper.in(Constant.ColumnName.COMPANY_ID, (Object[]) sysUserDto.getCompanyIds());
		}
		sysUserRoleMapper.delete(userRoleWrapper);
		userAccountBookEntityMapper.delete(bookEntityWrapper);
		// 刷新被修改用户的权限
		userRealm.clearCachedAuthorizationInfo(operator.getRootEnterpriseId(), sysUserDto.getUserId());
	}

	@Override
	public SysRoleVo findById(SysRoleDto sysRoleDto) {
		FastUtils.checkParams(sysRoleDto.getRoleId());
		SysRole sysRole = sysRoleMapper.selectById(sysRoleDto.getRoleId());
		FastUtils.checkNull(sysRole);
		return getSysRoleVoAndMenuList(sysRole);
	}

	@Override
	public int updateBatch(SysRoleDto sysRoleDto, SysUserVo operator) {
		SysRole sysRole = new SysRole();
		sysRole.setIsDel(sysRoleDto.getIsDel());
		sysRole.setIsEnable(sysRoleDto.getIsEnable());
		sysRole.setUpdatorId(operator.getUserId());
		sysRole.setUpdatorName(operator.getName());
		return sysRoleMapper.update(sysRole, new QueryWrapper<SysRole>().in(Constant.ColumnName.ROLE_ID, sysRoleDto.getRoleIds()).eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, operator.getRootEnterpriseId()));
	}

	@Override
	public void assignBusinessAdmin(SysUserDto sysUserDto, SysUserVo operator) {
		Long rootEnterpriseId = sysUserDto.getRootEnterpriseId();
		FastUtils.checkParams(sysUserDto.getUserId(), rootEnterpriseId);
		SysUserEnterprise sysUserEnterprise = new SysUserEnterprise();
		sysUserEnterprise.setUserId(sysUserDto.getUserId());
		sysUserEnterprise.setRootEnterpriseId(rootEnterpriseId);
		SysUserEnterprise updateSysUserEnterprise = new SysUserEnterprise();
		updateSysUserEnterprise.setIsAdmin(sysUserDto.getIsAdmin());
		updateSysUserEnterprise.setUpdatorId(operator.getUserId());
		updateSysUserEnterprise.setUpdatorName(operator.getName());
		updateSysUserEnterprise.setUpdateTime(new Date());
		sysUserEnterpriseMapper.update(updateSysUserEnterprise, new QueryWrapper<>(sysUserEnterprise));
		// 刷新被修改用户权限
		userRealm.clearCachedAuthorizationInfo(operator.getRootEnterpriseId(), sysUserDto.getUserId());
	}

	@Override
	public boolean checkRefer(Long roleId) {
		List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectList(new QueryWrapper<SysUserRole>().eq(Constant.ColumnName.ROLE_ID, roleId).last(Constant.ConcatSql.LIMIT_1));
		return sysUserRoles.size() == 0;
	}

	/**
	 * 用户权限列表 分页
	 * @param userRoleDto
	 * @return
	 */
	@Override
	public Page<UserRoleVo> findUserRolePage(UserRoleDto userRoleDto) {
		Page<UserRoleVo> page = userRoleDto.getPage();
		List<UserRoleVo> userRoleVoList = sysUserRoleMapper.selectUserRolePage(userRoleDto,page);
		return page.setRecords(userRoleVoList);
	}

	/**
	 * 查询可用岗位/角色列表 分页
	 * @param sysRoleDto
	 * @return
	 */
	@Override
	public Page<SysRoleVo> findEnableList(SysRoleDto sysRoleDto) {
		Page<SysRoleVo> page = sysRoleDto.getPage();
		List<SysRoleVo> sysRoleVoList = sysRoleMapper.selectEnableList(sysRoleDto,page);
		return page.setRecords(sysRoleVoList);
	}

	@Override
	public void assignBatchDelete(SysUserDto sysUserDto, Long rootEnterpriseId, List<Long> userIds) {
		SysUserRole sysUserRoleQuery = new SysUserRole();
		sysUserRoleQuery.setRootEnterpriseId(rootEnterpriseId == null ? sysUserDto.getRootEnterpriseId() : rootEnterpriseId);
		// 删除岗位
		QueryWrapper<SysUserRole> wrapper = new QueryWrapper<>(sysUserRoleQuery).in(Constant.ColumnName.USER_ID, userIds == null ? sysUserDto.getUserIds() : userIds);
		if (sysUserDto.getCompanyIds() != null && sysUserDto.getCompanyIds().length != 0) {
			wrapper.in(Constant.ColumnName.COMPANY_ID, (Object[]) sysUserDto.getCompanyIds());
		}
		sysUserRoleMapper.delete(wrapper);
	}

	@Override
	public BatchResult updateBatchDelete(SysRoleDto sysRoleDto) {
		BatchResult batchResult = new BatchResult();
		List<ReferenceDescription> failList = new ArrayList<>();
		batchResult.setFailList(failList);
		List<Long> idList = sysRoleDto.getRoleIds();
		if (CollectionUtils.isNotEmpty(idList)) {
			// 查询已删除的记录id,筛选将被操作的id中
			FastUtils.filterIds(ResultCode.IS_DEL, sysRoleMapper, new QueryWrapper<SysRole>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ROLE_ID, idList, failList);
			// 查询被引用的记录,有则放入操作失败集合
			ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ROLE, idList);
			failList.addAll(referenceContext.getReferences());
			if (!referenceContext.getNotReferences().isEmpty()) {
				// 操作未被引用的数据
				SysRole sysRole = new SysRole();
				sysRole.setIsDel(Constant.Is.YES);
				FastUtils.updateBatch(sysRoleMapper, sysRole, Constant.ColumnName.ROLE_ID, referenceContext.getNotReferences(),null);
				batchResult.setSuccessList(referenceContext.getNotReferences());
				if (!referenceContext.getNotReferences().isEmpty()) {
					// 刷新权限
					userRealm.clearCachedAuthorizationInfo(UserUtils.getUserVo().getRootEnterpriseId(), null);
				}
			}
		}
		return batchResult;
	}

	@Override
	public BatchResult updateBatch(SysRoleDto sysRoleDto) {
		BatchResult batchResult = new BatchResult();
		List<ReferenceDescription> failList = new ArrayList<>();
		batchResult.setFailList(failList);
		List<Long> idList = sysRoleDto.getRoleIds();
		if (CollectionUtils.isNotEmpty(idList)) {
			//查询已删除的记录id
			FastUtils.filterIds(ResultCode.IS_DEL, sysRoleMapper, new QueryWrapper<SysRole>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ROLE_ID, idList, failList);
			//查询启用状态已变更成功的记录,筛选
			FastUtils.filterIds(Constant.Is.NO.equals(sysRoleDto.getIsEnable()) ? ResultCode.IS_DISABLE : ResultCode.IS_ENABLE, sysRoleMapper, new QueryWrapper<SysRole>().eq(Constant.ColumnName.IS_ENABLE, sysRoleDto.getIsEnable()), Constant.ColumnName.ROLE_ID, idList, failList);
			//更新状态
			SysRole sysRole = new SysRole();
			sysRole.setIsEnable(sysRoleDto.getIsEnable());
			FastUtils.updateBatch(sysRoleMapper, sysRole, Constant.ColumnName.ROLE_ID, idList,null);
			batchResult.setSuccessList(idList);
			if (!idList.isEmpty()) {
				// 刷新权限
				userRealm.clearCachedAuthorizationInfo(UserUtils.getUserVo().getRootEnterpriseId(), null);
			}
		}
		return batchResult;
	}

	private SysRoleVo getSysRoleVoAndMenuList(@NotNull SysRole existSysRole) {
		SysRoleVo sysRoleVo;
		if (existSysRole instanceof SysRoleVo) {
			sysRoleVo = (SysRoleVo) existSysRole;
		} else {
			sysRoleVo = new SysRoleVo();
			FastUtils.copyProperties(existSysRole, sysRoleVo);
		}
		// 查询岗位下的权限树
		List<SysMenuVo> sysMenuVos = sysMenuService.getList(existSysRole);
		sysRoleVo.setSysMenuList(sysMenuVos);
		return sysRoleVo;
	}

	/**
	 * 校验角色名和角色定义是否重复
	 */
	private void checkDuplication(SysRole sysRole) {
		int count = sysRoleMapper.checkDuplication(sysRole);
		if (count > 0) {
			throw new ServiceException(ResultCode.ROLE_EXIST);
		}
	}
}
