package com.njwd.basedata.service.impl;


import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.mapper.DeptMapper;
import com.njwd.basedata.service.*;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.DeptVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.CompanyService;
import com.njwd.financeback.service.SysAuxDataService;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author jds
 * @Description 部门
 * @Date 2019/6/20 18:45
 */
@Service
public class DeptServiceImpl implements DeptService {

	@Resource
	private DeptMapper deptMapper;

	@Resource
	private DeptUseCompanyService deptUseCompanyService;

	@Resource
	private StaffService staffService;

	@Resource
	private BaseCustomService baseCustomService;


	@Resource
	private DeptService deptService;


	@Resource
	private CompanyService companyService;

	@Resource
	private SequenceService sequenceService;

	@Resource
	private FileService fileService;

	@Resource
	private SysAuxDataService sysAuxDataService;

	@Resource
	private ReferenceRelationService referenceRelationService;

	/**
	 * @return int
	 * @Description 新增部门
	 * @Author jds
	 * @Date 2019/6/20 18:45
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer add(DeptDto deptDto) {
		SysUserVo operator = UserUtils.getUserVo();
		//新增
		Dept dept = new Dept();
		FastUtils.copyProperties(deptDto, dept);
		deptDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		deptDto.setCreatorId(operator.getUserId());
		deptDto.setCreatorName(operator.getName());
		deptDto.setCreateTime(new Date());
		//验证上级部门
		deptService.findPre(deptDto);
		sentCodeAndLevel(deptDto);
		//校验部门编码和名称是否存在
		checkData(deptDto);
		Integer result = deptMapper.addDept(deptDto);
		deptService.addUseCompany(deptDto);
		return result;
	}


	/**
	 * @return int
	 * @Description 批量新增部门
	 * @Author jds
	 * @Date 2019/6/20 18:45
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer addDeptBatch(List<DeptDto> list) {
		List<DeptDto> dataList = new ArrayList<>();
		List<Long> parentIdList = new ArrayList<>();
		SysUserVo operator = UserUtils.getUserVo();
		Dept dept;
		for (DeptDto deptDto : list) {
			//新增
			dept = new Dept();
			FastUtils.copyProperties(deptDto, dept);
			deptDto.setRootEnterpriseId(operator.getRootEnterpriseId());
			deptDto.setCreatorId(operator.getUserId());
			deptDto.setCreatorName(operator.getName());
			if ((Constant.Is.NO).equals(deptDto.getCodeType())) {
				//系统编码
				if (deptDto.getPrarentId() == null) {
					//1级部门  （BM+公司编码）+2位流水号
					String prefix = Constant.BaseCodeRule.DEPT + deptDto.getCompanyCode();
					deptDto.setCode(sequenceService.getCode(
							prefix, Constant.BaseCodeRule.LENGTH_TWO
							, deptDto.getCompanyId()
							, Constant.BaseCodeRule.COMPANY));
				} else {
					//下级部门  上级部门编码+2位流水号
					deptDto.setCode(sequenceService.getCode(
							deptDto.getPrarentCode(), Constant.BaseCodeRule.LENGTH_TWO
							, deptDto.getCompanyId(), Constant.BaseCodeRule.COMPANY));
					parentIdList.add(deptDto.getPrarentId());
				}
			}
			//校验部门编码和名称是否存在
			checkData(deptDto);
			dataList.add(deptDto);
		}
		if (parentIdList.size() > Constant.Number.ZERO) {
			//更改上级部门是否是末級
			deptService.updateParentNoEnd(parentIdList, Constant.Is.NO);
		}
		Integer result = deptMapper.addDeptBatch(dataList);
		//新增记录
		deptService.addUseCompanyBatch(dataList);
		return result;
	}

	/**
	 * @return int
	 * @Description 批量新增记录
	 * @Author jds
	 * @Date 2019/6/23 18:53
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer addUseCompanyBatch(List<DeptDto> list) {
		List<DeptUseCompany> deptUseCompanyList = new ArrayList<>();
		DeptUseCompany deptUseCompany;
		for (DeptDto deptDto : list) {
			deptUseCompany = new DeptUseCompany();
			deptUseCompany.setDeptId(deptDto.getId());
			deptUseCompany.setUseCompanyId(deptDto.getUseCompanyId());
			deptUseCompany.setBusinessUnitId(deptDto.getBusinessUnitId());
			deptUseCompany.setCreatorId(deptDto.getCreatorId());
			deptUseCompany.setCreatorName(deptDto.getCreatorName());
			deptUseCompanyList.add(deptUseCompany);
		}
		return deptUseCompanyService.addUseCompanyBatch(deptUseCompanyList);
	}


	/**
	 * @Description //// 批量更改上级部门是否是末級
	 * @Author jds
	 * @Date 2019/9/12 15:54
	 * @Param [parentIdList, type]  type是否需要查詢上級部門是否有下級部門 0:不要 1：要
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateParentNoEnd(List<Long> parentIdList, Byte type) {
		Set<Long> set = new HashSet<>(parentIdList);
		List<Long> idList = new ArrayList<>(set);
		Dept dept = new Dept();
		dept.setIsEnd(type);
		if (type.equals(Constant.Is.YES)) {
			DeptDto pram = new DeptDto();
			pram.setIdList(idList);
			//查詢出還有下級部門的部門
			List<Long> prentIds = deptMapper.findChild(pram);
			//去除非末級部門ID
			idList.removeAll(prentIds);
		}
		deptMapper.update(dept, new QueryWrapper<Dept>().in(Constant.ColumnName.ID, idList));
		//清除缓存
		RedisUtils.removeBatch(Constant.RedisCache.DEPT, idList);
	}


	/**
	 * @return int
	 * @Description 删除部门
	 * @Author jds
	 * @Date 2019/6/20 18:47
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult deleteDeptBatch(DeptDto deptDto) {
		BatchResult batchResult = new BatchResult();
		if (Constant.Is.NO.equals(deptDto.getIsEnterpriseAdmin())) {
			//验证权限
			batchResult = deptService.batchVerifyPermission(deptDto);
			//重新获取有权限的版本号
			structuredData(deptDto, batchResult);
		}
		//验证版本号
		FastUtils.filterVersionIds(deptMapper, new QueryWrapper<>(), Constant.ColumnName.ID, deptDto.getIdList(), deptDto.getVersionList(), batchResult.getFailList());
		deptDto.setIsDel(Constant.Is.YES);
		//是否已删除校验
		checkIsDel(deptDto, batchResult);
		//是否是末吉校验
		checkIsEnd(deptDto, batchResult);
		//是否被引用校验
		List<Long> listIn = new ArrayList<>(deptDto.getIdList());
		ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.DEPT, listIn);
		//验证是否变更过使用公司
		changeUseCompany(referenceContext);
		//未被引用的ID
		if (!referenceContext.getNotReferences().isEmpty()) {
			//批量删除数据  不变更版本号
			FastUtils.updateBatch(deptMapper, deptDto, Constant.ColumnName.ID, referenceContext.getNotReferences(), null);
			//查詢上級部門
			DeptDto pram = new DeptDto();
			pram.setIdList(referenceContext.getNotReferences());
			List<Long> parentIds = deptMapper.findParentId(pram);
			if (parentIds.size() > Constant.Number.ZERO) {
				//更改上级部门是否是末級
				deptService.updateParentNoEnd(parentIds, Constant.Is.YES);
			}
			batchResult.setSuccessList(referenceContext.getNotReferences());
			//清除缓存
			RedisUtils.removeBatch(Constant.RedisCache.DEPT, referenceContext.getNotReferences());
		}
		//被引用的ID及说明
		if (!referenceContext.getReferences().isEmpty()) {
			List<ReferenceDescription> list = referenceContext.getReferences();
			list.addAll(batchResult.getFailList());
			batchResult.setFailList(list);
		}
		return batchResult;
	}

	/**
	 * @Description //查询是否有多个变更历史 有则被引用
	 * @Author jds
	 * @Date 2019/8/29 9:37
	 * @Param [deptDto, batchResult]
	 **/
	private void changeUseCompany(ReferenceContext referenceContext) {
		DeptDto deptDto = new DeptDto();
		//查询未引用的部门变更历史数量
		if (referenceContext.getNotReferences().size() > Constant.Number.ZERO) {
			deptDto.setIdList(referenceContext.getNotReferences());
			List<DeptVo> deptVos = deptMapper.findChangeCount(deptDto);
			List<ReferenceDescription> references = new LinkedList<>(referenceContext.getReferences());
			List<Long> ids = new LinkedList<>(referenceContext.getNotReferences());
			ReferenceDescription referenceDescription;
			for (DeptVo deptVo : deptVos) {
				if (deptVo.getChangeCount() > Constant.Number.ONE) {
					referenceDescription = new ReferenceDescription();
					referenceDescription.setBusinessId(deptVo.getId());
					referenceDescription.setReferenceDescription(ResultCode.IS_USED_BY_DEPT.message);
					ids.remove(deptVo.getId());
					references.add(referenceDescription);
				}
			}
			referenceContext.setNotReferences(ids);
			referenceContext.setReferences(references);
		}
	}

	/**
	 * @Description 校验已删除
	 * @Author jds
	 * @Date 2019/7/4 11:05
	 * @Param [deptDto]
	 **/
	private void checkIsDel(DeptDto deptDto, BatchResult batchResult) {
		if (deptDto.getIdList().size() > Constant.Number.ZERO) {
			List<ReferenceDescription> listR = new ArrayList<>();
			List<Long> ids = new ArrayList<>(deptDto.getIdList());
			FastUtils.filterIds(ResultCode.IS_DEL, deptMapper, new QueryWrapper<Dept>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, ids, listR);
			deptDto.setIdList(ids);
			listR.addAll(batchResult.getFailList());
			//替换返回数据
			batchResult.setFailList(listR);
		}
	}

	/**
	 * @Description 是否是末吉
	 * @Author jds
	 * @Date 2019/7/4 11:05
	 * @Param [deptDto]
	 **/
	private void checkIsEnd(DeptDto deptDto, BatchResult batchResult) {
		if (deptDto.getIdList().size() > Constant.Number.ZERO) {
			List<ReferenceDescription> listR = new ArrayList<>();
			List<Long> ids = new ArrayList<>(deptDto.getIdList());
			List<Long> idsEnd = deptMapper.findChild(deptDto);
			ReferenceDescription refer;
			if (idsEnd.size() > Constant.Number.ZERO) {
				ResultCode resultCode = ResultCode.DEL_NO_END_LEVEL;
				for (Long id : idsEnd) {
					refer = new ReferenceDescription();
					refer.setReferenceDescription(resultCode.message);
					refer.setBusinessId(id);
					listR.add(refer);
					ids.remove(id);
				}
			}
			deptDto.setIdList(ids);
			listR.addAll(batchResult.getFailList());
			//替换返回数据
			batchResult.setFailList(listR);
		}
	}

	/**
	 * @return int
	 * @Description 禁用、反禁用部门
	 * @Author jds
	 * @Date 2019/6/20 18:46
	 * @Param [deptDto]
	 **/
	@Override
	public BatchResult updateDeptStatusBatch(DeptDto deptDto) {
		BatchResult batchResult = new BatchResult();
		if (Constant.Is.NO.equals(deptDto.getIsEnterpriseAdmin())) {
			//验证权限
			batchResult = deptService.batchVerifyPermission(deptDto);
			//重新获取有权限的版本号
			structuredData(deptDto, batchResult);
		}
		//验证版本号
		FastUtils.filterVersionIds(deptMapper, new QueryWrapper<>(), Constant.ColumnName.ID, deptDto.getIdList(), deptDto.getVersionList(), batchResult.getFailList());
		//是否已删除校验
		checkIsDel(deptDto, batchResult);
		//是否已启用或禁用
		checkIsEnable(deptDto, batchResult);
		if ((Constant.Is.YES).equals(deptDto.getIsEnable()) && deptDto.getIdList().size() > Constant.Number.ZERO) {
			//启用则  查询所有上级
			findAssociatedId(deptDto, Constant.Is.NO);
		} else if ((Constant.Is.NO).equals(deptDto.getIsEnable()) && deptDto.getIdList().size() > Constant.Number.ZERO) {
			//禁用则 查询所有下级
			findAssociatedId(deptDto, Constant.Is.YES);
		}
		List<Long> list = new ArrayList<>(deptDto.getIdList());

		if (list.size() > Constant.Number.ZERO) {
			if (deptDto.getIdList().size() > Constant.Number.ZERO) {
				deptDto.setBatchIds(deptDto.getIdList());
				baseCustomService.batchEnable(deptDto, deptDto.getIsEnable(), deptMapper, batchResult.getSuccessDetailsList());
				batchResult.setSuccessList(deptDto.getIdList());
				//清除缓存
				RedisUtils.removeBatch(Constant.RedisCache.DEPT, deptDto.getIdList());
			}
		}
		return batchResult;
	}

	/**
	 * @return int
	 * @Description 保存
	 * @Author jds
	 * @Date 2019/6/28 17:44
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer saveDeptChange(DeptDto deptDto) {
		//判断是否已失效和是否已被修改是否已删除
		DeptVo deptVo = deptService.findDeptByIdForMapper(deptDto);
		//判断权限
		if (Constant.Is.NO.equals(deptDto.getIsEnterpriseAdmin())) {
			ShiroUtils.checkPerm(Constant.MenuDefine.DEPT_EDIT, deptVo.getCompanyId());
		}
		if (!equality(deptDto.getVersion(), deptVo.getVersion())) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		} else if ((Constant.Is.NO).equals(deptVo.getIsEnable())) {
			throw new ServiceException(ResultCode.IS_DISABLE);
		} else if ((Constant.Is.YES).equals(deptVo.getIsDel())) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//判断是否是末吉
		List<Long> idsend = new ArrayList<>();
		idsend.add(deptDto.getId());
		deptDto.setIdList(idsend);
		List<Long> end = deptMapper.findChild(deptDto);
		if (end.size() > Constant.Number.ZERO) {
			throw new ServiceException(ResultCode.IS_NOT_FINAL);
		}
		//判断是否被引用
		isUsed(deptDto, deptVo);
		//验证上级部门
		deptService.findPre(deptDto);
		if (!equality(deptDto.getCompanyId(), deptVo.getCompanyId()) || !equality(deptDto.getPrarentId(), deptVo.getPrarentId())) {
			//创建公司变更 或 上级部门变更 重新编码
			sentCodeAndLevel(deptDto);
			if (!equality(deptDto.getCompanyId(), deptVo.getCompanyId())) {
				//创建公司变更   变更（重定义）主表和历史表
				deptService.updateUseCompany(deptDto);
			}
		} else {
			//查看页面不改变创建公司时 不对使用公司产生影响
			deptDto.setCompanyId(null);
			deptDto.setUseCompanyId(null);
			deptDto.setBusinessUnitId(null);
			deptDto.setPrarentId(null);
		}
		//重复性验证
		checkData(deptDto);
		SysUserVo operator = UserUtils.getUserVo();
		Dept sqlParam = new Dept();
		sqlParam.setId(deptDto.getId());
		deptDto.setUpdatorId(operator.getUserId());
		deptDto.setUpdatorName(operator.getName());
		deptDto.setUpdateTime(new Date());
		deptMapper.update(deptDto, new QueryWrapper<>(sqlParam));
		if (deptVo.getPrarentId() != null) {
			//验证原上级部门
			deptService.findOldPre(deptVo.getPrarentId());
		}
		//清除缓存
		RedisUtils.remove(Constant.RedisCache.DEPT, deptDto.getId());
		return deptDto.getVersion();
	}

	/**
	 * @Description //变更历史重定义 并重定义主表数据
	 * @Author jds
	 * @Date 2019/8/28 18:02
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUseCompany(DeptDto deptDto) {
		//并重定义主表数据
		deptDto.setUpdateTime(new Date());
		deptDto.setUpdatorId(UserUtils.getUserVo().getCreatorId());
		deptDto.setUpdatorName(UserUtils.getUserVo().getCreatorName());
		//重定义历史表并修改
		DeptUseCompany useCompany = new DeptUseCompany();
		useCompany.setCreateTime(new Date());
		useCompany.setCreatorId(UserUtils.getUserVo().getCreatorId());
		useCompany.setCreatorName(UserUtils.getUserVo().getCreatorName());
		useCompany.setDeptId(deptDto.getId());
		useCompany.setUseCompanyId(deptDto.getUseCompanyId());
		useCompany.setBusinessUnitId(deptDto.getBusinessUnitId());
		deptUseCompanyService.update(useCompany);
	}

	/**
	 * @Description //编码并设置等级
	 * @Author jds
	 * @Date 2019/8/28 11:41
	 * @Param [deptDto]
	 **/
	private void sentCodeAndLevel(DeptDto deptDto) {
		//上级部门切换 重新编码
		if (deptDto.getCodeType() == Constant.CodeType.SYSTEMCODE) {
			//系统编码
			if (deptDto.getPrarentId() == null) {
				//1级部门  （BM+公司编码）+2位流水号
				CompanyDto companyDto = new CompanyDto();
				companyDto.setId(deptDto.getCompanyId());
				CompanyVo company = companyService.findCompanyByIdOrCodeOrName(companyDto);
				if (null == company || StringUtil.isBlank(company.getCode())) {
					throw new ServiceException(ResultCode.INVAILD_COMPANY_ID);
				}
				String prefix = Constant.BaseCodeRule.DEPT + company.getCode();
				deptDto.setCode(sequenceService.getCode(
						prefix, Constant.BaseCodeRule.LENGTH_TWO
						, deptDto.getCompanyId()
						, Constant.BaseCodeRule.COMPANY));
				deptDto.setDeptLevel(Constant.Level.ONE);
			} else {
				//非1级部门  上级部门编码+2位流水号
				deptDto.setCode(sequenceService.getCode(
						deptDto.getPrarentCode(), Constant.BaseCodeRule.LENGTH_TWO
						, deptDto.getCompanyId(), Constant.BaseCodeRule.COMPANY));
				if (Constant.Level.ONE.equals(deptDto.getPrarentLevel())) {
					deptDto.setDeptLevel(Constant.Level.TWO);
				} else if (Constant.Level.TWO.equals(deptDto.getPrarentLevel())) {
					deptDto.setDeptLevel(Constant.Level.THREE);
				} else if (Constant.Level.THREE.equals(deptDto.getPrarentLevel())) {
					deptDto.setDeptLevel(Constant.Level.FOUR);
				}
			}
		}
	}


	/**
	 * @Description //  //判断是否被引用   名称、属性和描述可以编辑，其余所有字段都不可编辑；
	 * @Author jds
	 * @Date 2019/8/28 11:11
	 * @Param [deptDto, deptVo]
	 **/
	private void isUsed(DeptDto deptDto, DeptVo deptVo) {
		//查找变更记录 变更过则为算被引用
		DeptUseCompany deptUseCompany = new DeptUseCompany();
		deptUseCompany.setDeptId(deptDto.getId());
		List<DeptUseCompany> list = deptUseCompanyService.findUseCompanyList(deptUseCompany);
		ReferenceResult reference = referenceRelationService.isReference(Constant.Reference.DEPT, deptDto.getId());
		//判断是否被引用   名称、属性和描述可以编辑，其余所有字段都不可编辑；
		if (reference.isReference()) {
			if (!equality(deptDto.getPrarentId(), deptVo.getPrarentId()) || !equality(deptDto.getAttrBusinessUnitId(), deptVo.getAttrBusinessUnitId())) {
				throw new ServiceException(ResultCode.IS_REFERENCED);
			}
		} else if ((list.size() > Constant.Number.ONE) && !equality(deptDto.getAttrBusinessUnitId(), deptVo.getAttrBusinessUnitId())) {
			throw new ServiceException(ResultCode.IS_REFERENCED);
		}
	}

	/**
	 * @Description 判断是否相等 null==null
	 * @Author jds
	 * @Date 2019/8/27 18:01
	 * @Param [object1, object2]
	 * @return java.lang.Boolean
	 **/
	private Boolean equality(Object object1, Object object2) {
		boolean bool = false;
		if (object1 != null && object2 != null) {
			if (object1.equals(object2)) {
				bool = true;
			}
		} else if (object1 == null && object2 == null) {
			bool = true;
		}
		return bool;
	}


	/**
	 * @Description ////验证上级部门信息
	 * @Author jds
	 * @Date 2019/8/26 14:42
	 * @Param [deptDto]
	 * @return void
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer findPre(DeptDto deptDto) {
		//查询上级部门等级
		if (deptDto.getPrarentId() != null && !deptDto.getPrarentId().toString().isEmpty()) {
			DeptDto deptDtoFindPre = new DeptDto();
			deptDtoFindPre.setId(deptDto.getPrarentId());
			DeptVo deptPre = deptService.findDeptByIdForMapper(deptDtoFindPre);
			//判断上级部门等级
			if (Constant.Level.FOUR.equals(deptPre.getDeptLevel())) {
				throw new ServiceException(ResultCode.MAX_LEVEL);
			}
			//判断上级数据状态
			if (Constant.Is.NO.equals(deptPre.getIsEnable())) {
				throw new ServiceException(ResultCode.PARENT_DEPT_IS_DISABLE);
			}
			//判断上级部门是否是末级部门 是则改为非末级部门
			if (Constant.Is.YES.equals(deptPre.getIsEnd()) && deptDto.getName() != null && !deptDto.getName().isEmpty()) {
				deptPre.setIsEnd(Constant.Is.NO);
				DeptDto sqlParam = new DeptDto();
				sqlParam.setId(deptPre.getId());
				deptMapper.update(deptPre, new QueryWrapper<>(sqlParam));
				//清除上级部门缓存
				RedisUtils.remove(Constant.RedisCache.DEPT, deptPre.getId());
			}
			//判断上级否被引用
			ReferenceResult result = referenceRelationService.isReference(Constant.Reference.DEPT, deptPre.getId());
			if (!result.isReference()) {
				DeptDto deptParent = new DeptDto();
				FastUtils.copyProperties(deptPre, deptParent);
				List<Long> ids = new ArrayList<>();
				ids.add(deptPre.getId());
				//查询未引用的部门变更历史数量
				deptParent.setIdList(ids);
				List<DeptVo> deptVos = deptMapper.findChangeCount(deptParent);
				if (deptVos.get(Constant.Number.ZERO).getChangeCount() > Constant.Number.ONE) {
					//变更历史多于1  设定为已经被引用
					result.setReference(true);
					result.setReferenceDescription(ResultCode.IS_USED_BY_DEPT.message);
				}
			}
			if (result.isReference()) {
				throw new ServiceException(ResultCode.PARENT_DEPT_IS_USED);
			}
			//判断上级部门是否被删除
			if (Constant.Is.YES.equals(deptPre.getIsDel())) {
				throw new ServiceException(ResultCode.PARENT_DEPT_IS_DEL);
			}
			deptDto.setPrarentCode(deptPre.getCode());
			deptDto.setPrarentLevel(deptPre.getDeptLevel());
		}
		return Constant.Number.ONE;
	}


	/**
	 * @Description ////验证原上级部门信息
	 * @Author jds
	 * @Date 2019/8/26 14:42
	 * @Param [id]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void findOldPre(Long id) {
		List<Long> list = new LinkedList<>();
		list.add(id);
		DeptDto dto = new DeptDto();
		dto.setIdList(list);
		List<Long> parentIds = deptMapper.findChild(dto);
		if (parentIds.size() == Constant.Number.ZERO) {
			Dept dept = new Dept();
			dept.setId(id);
			dto.setIsEnd(Constant.Is.YES);
			deptMapper.update(dto, new QueryWrapper<>(dept));
			//清除缓存
			RedisUtils.remove(Constant.RedisCache.DEPT, id);
		}
	}


	/**
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.DeptVo>
	 * @Description 分页查询部门列表
	 * @Author jds
	 * @Date 2019/6/20 17:58
	 * @Param [deptDto]
	 **/
	@Override
	public Page<DeptVo> findDeptPage(DeptDto deptDto) {
		return deptMapper.findPage(deptDto.getPage(), deptDto);
	}


	/**
	 * @Description //查询id关联的所有上级或下级
	 * @Author jds
	 * @Date 2019/8/30 16:58
	 * @Param [deptDto, type] type:0查上级   1：查下及
	 **/
	private void findAssociatedId(DeptDto deptDto, Byte type) {
		List<Long> ids = new ArrayList<>(deptDto.getIdList());
		List<Long> idList = new ArrayList<>(deptDto.getIdList());
		deptDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
		//查询公司下的部门  （不包括IdList的值）
		DeptDto deptDto1 = new DeptDto();
		//查上级只查被禁用的上级  查下级只查启用的下级
		deptDto1.setIsEnable(type);
		deptDto1.setRootEnterpriseId(deptDto.getRootEnterpriseId());
		List<DeptVo> deptVoList = deptMapper.findDeptList(deptDto1);
		if (deptVoList.size() == Constant.Number.ZERO) {
			return;
		}
		for (int i = 0; i < idList.size(); i++) {
			for (DeptVo deptVo : deptVoList) {
				//判断是否存在上级部门
				if (deptVo.getId().equals(idList.get(i)) && deptVo.getPrarentId() != null && type.equals(Constant.Is.NO) && type.equals(deptVo.getParentIsEnable())) {
					idList.add(deptVo.getPrarentId());
					ids.add(deptVo.getPrarentId());
				}
				//判断是否存在下级部门
				if (idList.get(i).equals(deptVo.getPrarentId()) && type.equals(Constant.Is.YES) && type.equals(deptVo.getIsEnable())) {
					idList.add(deptVo.getId());
					ids.add(deptVo.getId());
				}
			}
		}
		deptDto.setIdList(ids);
	}


	/**
	 * @return java.util.List<com.njwd.entity.basedata.vo.DeptVo>
	 * @Description 获取部门下拉列表
	 * @Author jds
	 * @Date 2019/6/28 10:01
	 * @Param [deptDto]
	 **/
	@Override
	public List<DeptVo> findDeptList(DeptDto deptDto) {
		return deptMapper.findDeptList(deptDto);
	}

	/**
	 * @return java.util.List<com.njwd.entity.basedata.vo.DeptVo>
	 * @Description 上级部门下拉列表
	 * @Author jds
	 * @Date 2019/6/28 10:01
	 * @Param [deptDto]
	 **/
	@Override
	public Page<DeptVo> findParentDeptList(DeptDto deptDto) {
		//查询公司下所有部门
		List<DeptVo> usedList = deptMapper.findDeptListByCompany(deptDto);
		if (usedList.size() > Constant.Number.ZERO) {
			List<Long> idList = new ArrayList<>();
			for (DeptVo deptVo : usedList) {
				idList.add(deptVo.getId());
			}
			ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.DEPT, idList);
			//验证是否变更过使用公司
			changeUseCompany(referenceContext);
			List<ReferenceDescription> rList = referenceContext.getReferences();
			List<Long> ids = new ArrayList<>();
			//获取被引用的ID
			if (rList.size() > Constant.Number.ZERO) {
				for (ReferenceDescription refer : rList) {
					ids.add(refer.getBusinessId());
				}
				deptDto.setIdList(ids);
			}
		}
		return deptMapper.findParentDeptList(deptDto.getPage(), deptDto);
	}


	/**
	 * @return int
	 * @Description 更新使用公司
	 * @Author jds
	 * @Date 2019/6/20 18:46
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(DeptDto deptDto) {
		//判断是否已失效
		DeptVo deptVo = deptService.findDeptByIdForMapper(deptDto);
		if ((Constant.Is.NO).equals(deptVo.getIsEnable())) {
			throw new ServiceException(ResultCode.IS_DISABLE);
		}
		//判断是否被他人变更
		if (!deptDto.getUsedId().equals(deptVo.getUsedId())) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}
		//判断是否是管理员
		if ((Constant.Is.NO).equals(deptDto.getIsEnterpriseAdmin())) {
			throw new ServiceException(ResultCode.USER_NOT_EXIST);
		}
		//判断是否是末吉
		List<Long> idsend = new ArrayList<>();
		idsend.add(deptDto.getId());
		deptDto.setIdList(idsend);
		List<Long> end = deptMapper.findChild(deptDto);
		if (end.size() > Constant.Number.ZERO) {
			throw new ServiceException(ResultCode.IS_END_LEVEL);
		}
		//判断使用公司是否一致
		if (deptVo.getUseCompanyId().equals(deptDto.getUseCompanyId())) {
			throw new ServiceException(ResultCode.USE_COMPANY_IS_SAME);
		}
		//更新
		deptMapper.updatUseCompanyId(deptDto);
		//新增新使用公司记录
		int result2 = addUseCompany(deptDto);
		//查询是否存在关联的员工
		List<Long> staffIds = staffService.findStaffByDeptId(deptDto);
		if (staffIds.size() > Constant.Number.ZERO) {
			deptDto.setStaffIdList(staffIds);
			staffService.updateStaffInfoFromDept(deptDto);
		}
		//清除缓存
		RedisUtils.remove(Constant.RedisCache.DEPT, deptDto.getId());
		return result2;
	}


	/**
	 * @return int
	 * @Description 新增变更记录
	 * @Author jds
	 * @Date 2019/6/23 18:53
	 * @Param [deptDto]
	 **/
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer addUseCompany(DeptDto deptDto) {
		SysUserVo operator = UserUtils.getUserVo();
		DeptUseCompany deptUseCompany = new DeptUseCompany();
		deptUseCompany.setDeptId(deptDto.getId());
		deptUseCompany.setUseCompanyId(deptDto.getUseCompanyId());
		deptUseCompany.setBusinessUnitId(deptDto.getBusinessUnitId());
		deptUseCompany.setCreateTime(new Date());
		deptUseCompany.setCreatorId(operator.getUserId());
		deptUseCompany.setCreatorName(operator.getName());
		return deptUseCompanyService.addUseCompany(deptUseCompany);
	}


	/**
	 * @return java.util.List<com.njwd.entity.basedata.DeptUseCompany>
	 * @Description 查询历史记录
	 * @Author jds
	 * @Date 2019/6/23 21:38
	 * @Param [deptDto]
	 **/
	@Override
	public DeptVo findCompanyList(DeptDto deptDto) {
		DeptUseCompany deptUseCompany = new DeptUseCompany();
		deptUseCompany.setDeptId(deptDto.getId());
		List<DeptUseCompany> list = deptUseCompanyService.findUseCompanyList(deptUseCompany);
		DeptVo deptVo = findDeptById(deptDto);
		deptVo.setDeptUseCompanyList(list);
		return deptVo;
	}


	/**
	 * @Description //查询变更历史记录数量
	 * @Author jds
	 * @Date 2019/9/25 17:37
	 * @Param [deptDto]
	 * @return java.util.List<com.njwd.entity.basedata.vo.DeptVo>
	 **/
	@Override
	public List<DeptVo> findChangeCount(DeptDto deptDto) {
		return deptMapper.findChangeCount(deptDto);
	}


	/**
	 * @Description 校验已禁用/已启用
	 * @Author jds
	 * @Date 2019/7/4 11:05
	 * @Param [deptDto]
	 **/
	private void checkIsEnable(DeptDto deptDto, BatchResult batchResult) {
		if (deptDto.getIdList().size() > Constant.Number.ZERO) {
			List<ReferenceDescription> listR = new ArrayList<>(batchResult.getFailList());
			List<Long> ids = new ArrayList<>(deptDto.getIdList());
			FastUtils.filterIds((Constant.Is.NO).equals(deptDto.getIsEnable()) ? ResultCode.IS_DISABLE : ResultCode.IS_ENABLE, deptMapper
					, new QueryWrapper<Dept>().eq(Constant.ColumnName.IS_ENABLE, deptDto.getIsEnable()), Constant.ColumnName.ID, ids, listR);
			deptDto.setIdList(ids);
			//替换返回数据
			batchResult.setSuccessList(ids);
			batchResult.setFailList(listR);
		}
	}

	/**
	 * @Description 导出
	 * @Author jds
	 * @Date 2019/6/20 18:48
	 * @Param [DeptDto, response]
	 **/
	@Override
	public void exportExcel(DeptDto deptDto, HttpServletResponse response) {
		SysUserVo userVo = UserUtils.getUserVo();
		deptDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
		Page<DeptVo> page = new Page<>();
		fileService.resetPage(page);
		Page<DeptVo> deptVoList = deptMapper.findPage(page, deptDto);
		//不同身份导出不同表格
		fileService.exportExcel(response, deptVoList.getRecords(), MenuCodeConstant.DEPT, deptDto.getIsEnterpriseAdmin());
	}


	/**
	 * @Description 数据验证
	 * @Author jds
	 * @Date 2019/6/20 18:49
	 * @Param [deptDto]
	 **/
	private void checkData(DeptDto deptDto) {
		//校验 部门编码和名称唯一性
		if (deptDto.getCode() != null && !deptDto.getCode().isEmpty()) {
			int code = deptMapper.checkDuplicateCode(deptDto);
			if (code > Constant.Number.ZERO) {
				throw new ServiceException(ResultCode.CODE_EXIST, deptDto.getCode());
			}
		}
		if (deptDto.getName() != null && !deptDto.getName().isEmpty()) {
			int name = deptMapper.checkDuplicateName(deptDto);
			if (name > Constant.Number.ZERO) {
				throw new ServiceException(ResultCode.NAME_EXIST, deptDto.getName());
			}
		}
	}

	/**
	 * @return java.lang.Integer
	 * @Description 验证部门名称是否存在
	 * @Author jds
	 * @Date 2019/7/5 17:00
	 * @Param [deptDto]
	 **/
	@Override
	public Integer checkName(DeptDto deptDto) {
		return deptMapper.checkDuplicateName(deptDto);
	}

	/**
	 * @return com.njwd.entity.basedata.Dept
	 * @Description 根据ID查询单个部门信息
	 * @Author jds
	 * @Date 2019/6/20 18:49
	 * @Param [deptDto]
	 **/
	@Override
	public DeptVo findDeptById(DeptDto deptDto) {
		DeptVo dept = deptService.findDeptByIdForMapper(deptDto);
		if (Constant.Is.NO.equals(deptDto.getIsEnterpriseAdmin())) {
			List<Long> companyList = new ArrayList<>();
			companyList.add(dept.getCompanyId());
			companyList.add(dept.getUseCompanyId());
			ShiroUtils.filterPerm(Constant.MenuDefine.DEPT_FIND, companyList);
			if (CollectionUtils.isEmpty(companyList)) {
				throw new ServiceException(ResultCode.PERMISSION_NOT);
			}
		}
		//查询是否被引用
		ReferenceResult result = referenceRelationService.isReference(Constant.Reference.DEPT, deptDto.getId());
		if (!result.isReference()) {
			List<Long> list = new ArrayList<>();
			list.add(deptDto.getId());
			deptDto.setIdList(list);
			//查询未引用的部门变更历史数量
			List<DeptVo> deptVos = deptMapper.findChangeCount(deptDto);
			if (deptVos.get(Constant.Number.ZERO).getChangeCount() > Constant.Number.ONE) {
				//变更历史多于1  设定为已经被引用
				result.setReference(true);
				result.setReferenceDescription(ResultCode.IS_USED_BY_DEPT.message);
			}
		}
		if (result.isReference()) {
			dept.setIsUsed(Constant.Is.YES);
		} else {
			dept.setIsUsed(Constant.Is.NO);
		}
		return dept;
	}


	/**
	 * @Description //根据ID从数据库或缓存查取数据
	 * @Author jds
	 * @Date 2019/9/9 10:10
	 * @Param [deptDto]
	 * @return com.njwd.entity.basedata.vo.DeptVo
	 **/
	@Override
	@Cacheable(value = Constant.RedisCache.DEPT, key = "#deptDto.id", unless = "#result==null")
	public DeptVo findDeptByIdForMapper(DeptDto deptDto) {
		return deptMapper.findById(deptDto);
	}

	/**
	 * @Description //根据编码查询部门
	 * @Author jds
	 * @Date 2019/8/21 16:38
	 * @Param [deptDto]
	 * @return com.njwd.entity.basedata.vo.DeptVo
	 **/
	@Override
	public DeptVo findByCode(DeptDto deptDto) {
		return deptMapper.findByCode(deptDto);
	}

	/**
	 * 查询  部门属性  辅助资料列表
	 *
	 * @return Result
	 */
	@Override
	public Result findDpetTypeList(SysAuxDataDto sysAuxDataDto) {
		sysAuxDataDto.setType(Constant.PropertyName.DEPT_TYPE);
		return sysAuxDataService.findAuxDataListByCodeOrName(sysAuxDataDto);
	}

	/**
	 * 根据name查询  部门属性  辅助资料表
	 *
	 * @return Result
	 */
	@Override
	public Result findDpetTypeByName(SysAuxDataDto platformSysAuxDataDto) {
		platformSysAuxDataDto.setType(Constant.PropertyName.DEPT_TYPE);
		return sysAuxDataService.findAuxDataByName(platformSysAuxDataDto);
	}

	/**
	 * 查询  部门属性  辅助资料列表
	 *
	 * @return List
	 */
	@Override
	public List<SysAuxDataVo> findTypeList(SysAuxDataDto platformSysAuxDataDto) {
		platformSysAuxDataDto.setType(Constant.PropertyName.DEPT_TYPE);
		return sysAuxDataService.findAuxDataList(platformSysAuxDataDto);
	}

	/**
	 * @return java.lang.String
	 * @Author ZhuHC
	 * @Date 2019/7/2 13:42
	 * @Param [deptDto]
	 * @Description 根据公司ID和部门名称查询部门ID
	 */
	@Override
	public String findIdByCompanyIdAndDeptName(DeptDto deptDto) {
		return deptMapper.findIdByCompanyIdAndDeptName(deptDto);
	}

	/**
	 * @return java.util.List<com.njwd.entity.basedata.Dept>
	 * @Description 根据单位编码查询部门列表
	 * @Author LuoY
	 * @Date 2019/7/2 17:53
	 * @Param [companyId]
	 */
	@Override
	public List<Dept> findDeptListByCompanyId(Long companyId) {
		return deptMapper.selectList(new LambdaQueryWrapper<Dept>().eq(Dept::getCompanyId, companyId).eq(Dept::getIsDel, Constant.Is.NO));
	}

	/**
	 * @Description //批量验证数据权限
	 * @Author jds
	 * @Date 2019/9/18 9:40
	 * @Param [deptDto]
	 * @return com.njwd.support.BatchResult
	 **/
	@Override
	public BatchResult batchVerifyPermission(DeptDto deptDto) {
		List<DeptVo> deptVoList = deptMapper.findDeptByIdList(deptDto);
		List<DeptVo> compareList = new ArrayList<>(deptVoList);
		BatchResult batchResult = ShiroUtils.filterNotPermData(deptVoList, deptDto.getMenuDefine(), new ShiroUtils.CheckPermSupport<DeptVo>() {
			@Override
			public Long getBusinessId(DeptVo deptVo) {
				return deptVo.getId();
			}

			@Override
			public Long getCompanyId(DeptVo deptVo) {
				return deptVo.getCompanyId();
			}
		});
		//返回id相同的数据
		List<ReferenceDescription> failList = batchResult.getFailList();
		if (CollectionUtils.isEmpty(failList)) {
			return batchResult;
		}

		//否则返回错误信息
		for (ReferenceDescription description : failList) {
			for (DeptVo deptVo : compareList) {
				if (description.getBusinessId().equals(deptVo.getId())) {
					description.setInfo(deptVo);
				}
			}
		}
		return batchResult;
	}

	/**
	 * @Description //整理被过滤后的数据
	 * @Author jds
	 * @Date 2019/9/18 14:23
	 * @Param [deptDto, batchResult]
	 **/
	private void structuredData(DeptDto deptDto, BatchResult batchResult) {
		if (batchResult.getFailList().size() > Constant.Number.ZERO) {
			//ID集合
			List<Long> arrayList = new ArrayList<>(deptDto.getIdList());
			//版本号集合
			List<Integer> verList = new ArrayList<>(deptDto.getVersionList());
			//有权限的ID集合
			List<Long> list = new ArrayList<>(deptDto.getIdList());
			List<ReferenceDescription> fail = new ArrayList<>(batchResult.getFailList());
			for (ReferenceDescription res : fail) {
				list.remove(res.getBusinessId());
			}
			//有权限的版本号集合
			List<Integer> vlist = new ArrayList<>();
			//部门ID唯一取下标 获取版本号
			if (deptDto.getIdList().size() > Constant.Number.ZERO) {
				for (Long value : list) {
					//获取下表
					arrayList.indexOf(value);
					vlist.add(verList.get(arrayList.indexOf(value)));
				}
			}
			deptDto.setIdList(list);
			deptDto.setVersionList(vlist);
		}
	}

}
