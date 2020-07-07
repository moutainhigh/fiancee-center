package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.CashFlowItemFeignClient;
import com.njwd.basedata.mapper.CashFlowItemMapper;
import com.njwd.basedata.service.BaseCustomService;
import com.njwd.basedata.service.CashFlowItemService;
import com.njwd.basedata.service.CashFlowItemTemplateService;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.CashFlowItem;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.entity.platform.vo.CashFlowVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Libao
 * @Description 现金流量项目Service层实现类
 * @Date 2019/6/11 17:40
 **/
@Service
public class CashFlowItemServiceImpl implements CashFlowItemService {

	@Resource
	private CashFlowItemMapper cashFlowItemMapper;

	@Resource
	private CashFlowItemTemplateService cashFlowItemTemplateService;

	@Resource
	private ReferenceRelationService referenceRelationService;

	@Resource
	private FileService fileService;

	@Resource
	BaseCustomService baseCustomService;

	@Autowired
	private CashFlowItemFeignClient cashFlowItemFeignClient;

	/**
	 * @return int
	 * @Author Libao
	 * @Description 导入现金流量模板数据
	 * @Date 2019/6/19 11:45
	 * @Param temleteList
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = "cashFlowItem", allEntries = true)
	public int addCashFlowItemBatch(List<CashFlowItemDto> list) {
		int result = 0;
		if (CollectionUtils.isNotEmpty(list)) {
			//根据cashFlowId分组
			Map<String, List<CashFlowItemDto>> collect = list.stream().collect(Collectors.groupingBy(e -> e.getCashFlowId().toString()));
			List<CashFlowItemDto> cashFlowItemList;
			for (String key : collect.keySet()) {
				cashFlowItemList = collect.get(key);

				SysUserVo operator = UserUtils.getUserVo();
				CashFlowItemDto info = cashFlowItemList.get(0);
				info.setRootEnterpriseId(operator.getRootEnterpriseId());
				if(!info.getImportFlag()){
					// 1、判断当前所有现金流量数据是否有被引用，被引用，则抛出已被引用异常，没有被引用，则先删除当前基准现金流量的表信息
					Boolean flag = checkIsUsed(info);
					if (!flag) {
						// 已被引用
						throw new ServiceException(ResultCode.IS_CITED);
					}
				}
				CashFlow cashFlow = new CashFlow();
				FastUtils.copyProperties(info, cashFlow);
				cashFlow.setCreatorId(operator.getUserId());
				cashFlow.setCreatorName(operator.getName());
				// 2.跟新现金流量表模板数据
				// 查询模板是否存在
				CashFlow cashFlowInfo = cashFlowItemTemplateService.findCashFlowTemplate(cashFlow);

				if (cashFlowInfo != null) {
					cashFlow.setId(cashFlowInfo.getId());
					cashFlowItemTemplateService.updateCashFlowTemplate(cashFlow);
					info.setCashFlowId(cashFlowInfo.getId());
					if(!info.getImportFlag()){
						// 3.删除现金流量项目相关数据
						cashFlowItemMapper.delCashFlowByCashFlowId(info);
					}
				} else {
					//初始化
					cashFlow.setPlatformId(info.getCashFlowId());
					cashFlowItemTemplateService.addCashFlowTemplate(cashFlow);
				}

				// 4.新增现金流量项目数据
				CashFlowItem cashFlowItem = new CashFlowItem();
				for (CashFlowItemDto cashFlowItemDto : cashFlowItemList) {
					cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
					cashFlowItemDto.setCashFlowId(cashFlow.getId());
					// 检验现金流量项目编码和名称是否重复
					Boolean flag = checkCashFlowItem(cashFlowItemDto);
					if (!flag) {
						// 校验不通过,返回校验结果（覆盖校验重复提示并返回重复字段）
						ResultCode.COLUMN_EXIST.message = cashFlowItemDto.getResultMessage();
						throw new ServiceException(ResultCode.COLUMN_EXIST, cashFlowItemDto.getColumnList());
					}

					cashFlowItemDto.setCreatorId(operator.getUserId());
					cashFlowItemDto.setCreatorName(operator.getName());
					result = addCashFlowItemInit(cashFlowItem, cashFlowItemDto);
				}
				if (result == 0) {
					throw new ServiceException(ResultCode.OPERATION_FAILURE);
				}
			}
		}
		return result;
	}


	/**
	 * @return int
	 * @Author Libao
	 * @Description 现金流量项目新增下级
	 * @Date 2019/6/12 14:10
	 * @Param [cashFlowItemDto]
	 */
	@Override
	@CacheEvict(value = "cashFlowItem", allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public CashFlowItemVo addCashFlowItem(CashFlowItemDto cashFlowItemDto) {
		//校验唯一性
		checkCashFlowItemUniqueness(cashFlowItemDto);
		//校验编码不能以00结尾
		String code = cashFlowItemDto.getCode();
		String newCode = code.substring(code.length() -2);
		if (Constant.Character.ZERO.equals(newCode)){
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CHECK_CODE);
		}
		//查询上级现金流量项目
		CashFlowItemVo cashFlowItemVoUp = cashFlowItemMapper.findUpCashFlowItemByCode(cashFlowItemDto);
		if (cashFlowItemVoUp == null){
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_UP_NOT_EXIST);
		}
		//检验是否超过最大级次
		checkMaxLevel(cashFlowItemDto);
		//校验是否被禁用
		if (Constant.Is.NO.equals(cashFlowItemVoUp.getIsEnable())) {
			//抛出数据已禁用
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_DISABLE);
		}
		//校验是否被引用
		ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CASH_FLOW_ITEM, cashFlowItemVoUp.getId());
		if (referenceResult.isReference()){
			//已被引用
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_CITED);
		}
		//校验系统预置的内部往来项目为三级项目，且是末级项目，不能新增下级
		checkInteriorAndInit(cashFlowItemDto, cashFlowItemVoUp);

		// 设置基础数据
		CashFlowItem cashFlowItem = new CashFlowItem();
		FastUtils.copyProperties(cashFlowItemDto, cashFlowItem);
		setCashFlowItemInfo(cashFlowItem, Constant.CashFlowItemData.CASHFLOWITEMADD);
		//TODO 目前按照集团创建,集团共享的控制策略，如后面有新的策略，则按新的策略设置值
		cashFlowItemDto.setCompanyId(Constant.CashFlowItemData.GROUP_ID);
		cashFlowItemDto.setUseCompanyId(Constant.CashFlowItemData.GROUP_ID);
		cashFlowItemMapper.insert(cashFlowItem);
		//将上级末级字段设置为0（非末级）
		CashFlowItem item = new CashFlowItem();
		item.setIsFinal(Constant.Is.NO);
		//版本号加1
		item.setVersion(cashFlowItemVoUp.getVersion());
		//特殊处理
		cashFlowItemMapper.update(item, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemVoUp.getId()));
		//返回上级和新增下级的Id
		CashFlowItemVo cashFlowItemVo = new CashFlowItemVo();
		cashFlowItemVo.setUpId(cashFlowItemVoUp.getId());
		cashFlowItemVo.setId(cashFlowItem.getId());
		return cashFlowItemVo;
        //ID互换逻辑，暂时保存
		/*//返回日志Id
		cashFlowItemDto.setId(cashFlowItemVoUp.getId());
		CashFlowItemVo cashFlowItemVo = new CashFlowItemVo();
		//判断是否是末级数据
		if (Constant.Is.YES.equals(cashFlowItemVoUp.getIsFinal())) {
			//重新组装现金流量项目
			addAndUpdateReassembleData(cashFlowItemDto, cashFlowItem, cashFlowItemVoUp);
			cashFlowItemVo.setId(cashFlowItemDto.getId());
			cashFlowItemVo.setUpId(cashFlowItem.getId());
			return cashFlowItemVo;
		}
		cashFlowItemVo.setId(cashFlowItem.getId());
		cashFlowItemVo.setUpId(cashFlowItemVoUp.getId());
		return cashFlowItemVo;*/
	}



	/**
	 * @return int
	 * @Author Libao
	 * @Description 模板数据导入现金流量项目
	 * @Date 2019/7/4 9:44
	 * @Param [cashFlowItem, cashFlowItemDto]
	 */

	public int addCashFlowItemInit(CashFlowItem cashFlowItem, CashFlowItemDto cashFlowItemDto) {
		int result = 0;
		// 目前按照集团创建,集团共享的控制策略，如后面有新的策略，则按新的策略设置值
		cashFlowItemDto.setCompanyId(Constant.CashFlowItemData.GROUP_ID);
		cashFlowItemDto.setUseCompanyId(Constant.CashFlowItemData.GROUP_ID);
		if (Constant.Is.NO.equals(cashFlowItemDto.getIsFinal())){
			cashFlowItemDto.setIsExistNextInit(Constant.Is.YES);
		}
		cashFlowItemDto.setIsInit(Constant.Is.YES);
		FastUtils.copyProperties(cashFlowItemDto, cashFlowItem);
		cashFlowItem.setPlatformId(cashFlowItemDto.getId());
		result = cashFlowItemMapper.insert(cashFlowItem);
		return result;
	}

	@Override
	public int findCount(Long rootEnterpriseId) {
		return cashFlowItemMapper.findCount(rootEnterpriseId);
	}

	/**
	 * @param cashFlowItemDto
	 * @return java.lang.String
	 * @Author Libao
	 * @Description 批量删除现金流量项目
	 * @Date 2019/6/13 14:28
	 */
	@Override
	@CacheEvict(value = "cashFlowItem", allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public BatchResult delBatch(CashFlowItemDto cashFlowItemDto) {
		//结果集对象
		BatchResult batchResult = new BatchResult();
		//删除失败集合
		List<ReferenceDescription> failList = new ArrayList<>();
		//校验版本号
		FastUtils.filterVersionIds(cashFlowItemMapper,new QueryWrapper<>(),Constant.ColumnName.ID,cashFlowItemDto.getIds(),cashFlowItemDto.getVersions(),failList);
		//如果版本号过滤之后不存在满足条件的Id则直接返回结果
		if (CollectionUtils.isEmpty(cashFlowItemDto.getIds())){
			batchResult.setFailList(failList);
			return batchResult;
		}
		//记录操作成功集合
		List<Long> successList = new ArrayList<>();
		//记录操作结果对象
		ReferenceDescription referenceDescription;
		//创建对象itemDto用于查询删除对象
		CashFlowItemDto itemDto ;
		//创建对象updateUp用于修改上级为末级
		CashFlowItemDto updateUp;
		for (Long id : cashFlowItemDto.getIds()) {
			itemDto = new CashFlowItemDto();
			itemDto.setId(id);
			itemDto.setCashFlowId(cashFlowItemDto.getCashFlowId());
			//创建操作结果实例
			referenceDescription = new ReferenceDescription();
			//1.查询是否已经被删除
			CashFlowItemVo cashFlowItemVo = findCashFlowItemById(itemDto);
			if (Constant.Is.YES.equals(cashFlowItemVo.getIsDel())) {
				referenceDescription.setBusinessId(cashFlowItemVo.getId());
				referenceDescription.setReferenceDescription(ResultCode.CASH_FLOW_ITEM_HASDEL.message);
				failList.add(referenceDescription);
			}else if (Constant.Is.YES.equals(cashFlowItemVo.getIsInit())){
				//预置数据不能删除
				referenceDescription.setBusinessId(cashFlowItemVo.getId());
				referenceDescription.setReferenceDescription(ResultCode.CASH_FLOW_ITEM_IS_INIT_DISABLE_DEL.message);
				failList.add(referenceDescription);
			}else if (Constant.Is.NO.equals(cashFlowItemVo.getIsFinal())) {
				//存在下级项目不能删除
				referenceDescription.setBusinessId(cashFlowItemVo.getId());
				referenceDescription.setReferenceDescription(ResultCode.CASH_FLOW_ITEM_IS_FINAL.message);
				failList.add(referenceDescription);
			}else if (Constant.Is.YES.equals(cashFlowItemVo.getIsInteriorContact())){
				//内部往来数据禁止删除
				referenceDescription.setBusinessId(cashFlowItemVo.getId());
				referenceDescription.setReferenceDescription(ResultCode.CASH_FLOW_ITEM_IS_FINAL_INTERIOR_INIT.message);
				failList.add(referenceDescription);
			}else {
				//2、判断是否被引用
				ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CASH_FLOW_ITEM, cashFlowItemVo.getId());
				if (referenceResult.isReference()) {
					referenceDescription.setBusinessId(cashFlowItemVo.getId());
					referenceDescription.setReferenceDescription(referenceResult.getReferenceDescription());
					failList.add(referenceDescription);
				} else {
					try {
						//执行删除操作
						CashFlowItem cashFlowItem = new CashFlowItem();
						cashFlowItem.setIsDel(Constant.Is.YES);
						cashFlowItem.setId(cashFlowItemVo.getId());
						cashFlowItem.setVersion(cashFlowItemVo.getVersion() + Constant.Number.ONE);
						cashFlowItemMapper.delCashFlowItemById(cashFlowItem);
						successList.add(cashFlowItem.getId());
						//删除成功判断将上级设置为末级
						updateUp = new CashFlowItemDto();
						if (cashFlowItemVo.getUpCode() != null && !Constant.Character.STRING_ZERO.equals(cashFlowItemVo.getUpCode())){
							//查询上级
							CashFlowItemDto cashFlowItemDto1 = new CashFlowItemDto();
							cashFlowItemDto1.setCashFlowId(cashFlowItemVo.getCashFlowId());
							cashFlowItemDto1.setUpCode(cashFlowItemVo.getUpCode());
							CashFlowItemVo cashFlowItemVo1 = cashFlowItemMapper.findUpCashFlowItemByCode(cashFlowItemDto1);
							updateUp.setCashFlowId(cashFlowItemVo.getCashFlowId());
							updateUp.setUpCode(cashFlowItemVo.getUpCode());
							updateUp.setVersion(cashFlowItemVo1.getVersion());
							checkIsNeedUpdateFinal(updateUp);
						}
					} catch (Exception e) {
						//删除失败，将失败说明添加到失败结果集
						referenceDescription.setBusinessId(cashFlowItemVo.getId());
						referenceDescription.setReferenceDescription(ResultCode.CASH_FLOW_ITEM_DEL_FAILED.message);
						failList.add(referenceDescription);
					}
				}
			}
		}
		batchResult.setFailList(failList);
		batchResult.setSuccessList(successList);
		return batchResult;
	}

	/**
	 * @return com.njwd.entity.basedata.vo.CashFlowItemVo
	 * @Author Libao
	 * @Description 根据Id逻辑删除现金流量项目
	 * @Date 2019/6/12 17:01
	 * @Param [cashFlowItemDto]
	 */
	@Override
	@CacheEvict(value = "cashFlowItem", allEntries = true)
		public int delCashFlowItemById(CashFlowItemDto cashFlowItemDto) {
		//校验版本号
		checkVersion(cashFlowItemDto);
		//执行逻辑删除
		int result = 0;
		//校验是否是末级
		checkIsFinal(cashFlowItemDto);
		//校验预置数据
		if (Constant.Is.YES.equals(cashFlowItemDto.getIsInit())){
			//系统预置项目不可删除
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_INIT_DISABLE_DEL);
		}
		//校验内部往来
		if (Constant.Is.YES.equals(cashFlowItemDto.getIsInteriorContact()) && Constant.Is.YES.equals(cashFlowItemDto.getIsInit())){
			throw  new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_FINAL_INTERIOR_INIT);
		}
		//校验是否被引用
		ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CASH_FLOW_ITEM, cashFlowItemDto.getId());
		if (referenceResult.isReference()){
			//已被引用
			throw new ServiceException(ResultCode.IS_CITED);
		}else{
			//未被引用
			//设置基础数据
			CashFlowItem cashFlowItem = new CashFlowItem();
			cashFlowItem.setIsDel(Constant.Is.YES);
			cashFlowItem.setVersion(cashFlowItemDto.getVersion() + Constant.Number.ONE);
			cashFlowItem.setId(cashFlowItemDto.getId());
			result = cashFlowItemMapper.delCashFlowItemById(cashFlowItem);
			//删除成功判断将上级设置为末级
			checkIsNeedUpdateFinal(cashFlowItemDto);
		}
		return result;
	}

	/**
	 * @Author Libao
	 * @Description 根据Id修改或批量修改现流量项目数据状态（禁用、反禁用、批量禁用、批量反禁用）
	 * @Date 2019/6/13 10:33
	 * @Param [cashFlowItemDto]
	 * @Return int
	 */
	@Override
	@CacheEvict(value = "cashFlowItem", allEntries = true)
	public BatchResult updateOrBatch(CashFlowItemDto cashFlowItemDto, byte flag) {
		BatchResult batchResult = new BatchResult();
		List<ReferenceDescription> failList = new ArrayList<>();
		cashFlowItemDto.setIsEnable(flag);
		FastUtils.filterVersionIds(cashFlowItemMapper,new QueryWrapper<>(),Constant.ColumnName.ID,cashFlowItemDto.getIds(),cashFlowItemDto.getVersions(),failList);
		//如果版本号过滤之后不存在满足条件的Id则直接返回结果
		if (CollectionUtils.isNotEmpty(cashFlowItemDto.getIds())){
			//根据Id查询Code
			List codesByIds = cashFlowItemMapper.findCodesByIds(cashFlowItemDto);
			cashFlowItemDto.setCodes(codesByIds);
		}else{
			batchResult.setFailList(failList);
			return batchResult;
		}
		//查询所有内部往来数据code
		List<String> codes = cashFlowItemMapper.findInteriorContactCode(cashFlowItemDto);
		CashFlowItemDto interiorCodesItem = new CashFlowItemDto();
		interiorCodesItem.setCodes(codes);
		interiorCodesItem.setCashFlowId(cashFlowItemDto.getCashFlowId());
		if (Constant.IsEnable.DISABLE.equals(flag)){
			//内部往来和其上级Code
			List<String> interiorCodes = cashFlowItemMapper.findInteriorContactAndUp(interiorCodesItem);
			//禁用/反禁用编码交集
			if (getNeedDealCodes(cashFlowItemDto, batchResult, failList, interiorCodes)){ return batchResult;}
		}else{
			if (getNeedDealCodes(cashFlowItemDto, batchResult, failList, codes)){ return batchResult;}
		}

       //最终需要处理的会计科目id集合
		List<Long> idList = cashFlowItemMapper.findOperateIdsByParam(cashFlowItemDto);
		idList = idList.stream().distinct().collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(idList)) {
			// 查询已删除的记录id,有则放入操作失败集合
			FastUtils.filterIds(ResultCode.IS_DEL, cashFlowItemMapper, new QueryWrapper<CashFlowItem>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, failList);
			//查询启用状态已变更成功的记录,有则放入操作失败集合
			FastUtils.filterIds(Constant.Is.NO.equals(cashFlowItemDto.getIsEnable()) ? ResultCode.IS_DISABLE : ResultCode.IS_ENABLE, cashFlowItemMapper, new QueryWrapper<CashFlowItem>().eq(Constant.ColumnName.IS_ENABLE, cashFlowItemDto.getIsEnable()), Constant.ColumnName.ID, idList, failList);
			batchResult.setFailList(failList);
			if (idList.size() > 0) {
				//更新状态,并返回成功详情
				CashFlowItem cashFlowItem = new CashFlowItem();
				cashFlowItem.setBatchIds(idList);
				baseCustomService.batchEnable(cashFlowItem, cashFlowItemDto.getIsEnable(), cashFlowItemMapper, batchResult.getSuccessDetailsList());
				cashFlowItemDto.setIds(idList);
			}
		}
		return batchResult;
	}


	/**
	 * @return int
	 * @Author Libao
	 * @Description 根据Id修改现金流量项目
	 * @Date 2019/6/26 11:08
	 * @Param cashFlowItemDto
	 */
	@Override
	@CacheEvict(value = "cashFlowItem", allEntries = true)
	public Long updateCashFlowItemById(CashFlowItemDto cashFlowItemDto) {
		//校验版本号
		 checkVersion(cashFlowItemDto);
		//查询此记录是否被引用
		ReferenceResult referenceContext = referenceRelationService.isReference(Constant.Reference.CASH_FLOW_ITEM, cashFlowItemDto.getId());
        //判断是否被引用
		if (referenceContext.isReference()){
			//TODO 下个迭代修改，备注信息可以修改
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_CITED_FORBID_UPDATE);
		 }else {
			//系统预置禁止更新
			if (Constant.Is.YES.equals(cashFlowItemDto.getIsInit())){
				//TODO 下个迭代修改，备注信息可以修改
				throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_INIT);
			}
			//检验编码和名称是否重复
			checkCashFlowItemForUpdate(cashFlowItemDto);
			//校验数据是否被禁用
			checkIsForbid(cashFlowItemDto);
			//设置基础数据
			CashFlowItem cashFlowItem = new CashFlowItem();
			FastUtils.copyProperties(cashFlowItemDto, cashFlowItem);
			setCashFlowItemInfo(cashFlowItem, Constant.CashFlowItemData.CASHFLOWITEMUPDATE);

			//如果老的upCode等于新的upCode，则没有切换上级，否则为切换上级，走新增流程
			if (!cashFlowItemDto.getUpCode().equals(cashFlowItemDto.getOldUpCode())){
				//查询上级项目根据upCode
				CashFlowItemVo cashFlowItemVoUp = cashFlowItemMapper.findUpCashFlowItemByCode(cashFlowItemDto);
				//校验上级是否被引用
				ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CASH_FLOW_ITEM, cashFlowItemVoUp.getId());
				if (referenceResult.isReference()){
					//已被引用
					throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_CITED);
				}
				//校验系统预置的内部往来项目为三级项目，且是末级项目，不能新增下级,存在下级预置数据禁止新增
				checkInteriorAndInit(cashFlowItemDto, cashFlowItemVoUp);
				//开始跟新
				cashFlowItemMapper.update(cashFlowItem, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemDto.getId()));
				//跟新上级为非末级
				cashFlowItemVoUp.setIsFinal(Constant.Is.NO);
				cashFlowItemVoUp.setVersion(cashFlowItemDto.getVersion());
				cashFlowItemMapper.update(cashFlowItemVoUp, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemVoUp.getId()));
				//新建老的Dto用于校验老的上级项目是否存在下级
				CashFlowItemDto cashFlowItemDtoOld = new CashFlowItemDto();
				cashFlowItemDtoOld.setCashFlowId(cashFlowItemDto.getCashFlowId());
				cashFlowItemDtoOld.setUpCode(cashFlowItemDto.getOldUpCode());
				//判断原上级项目是否存在其他下级，不存在则跟新末级为是
				int downCount = cashFlowItemMapper.findIsExistNextCashFlowItem(cashFlowItemDtoOld);
				//不存在下级则跟新老的上级为末级
				if (downCount == 0) {
					CashFlowItem cashFlowItemOld = new CashFlowItem();
					cashFlowItemOld.setIsFinal(Constant.Is.YES);
					cashFlowItemOld.setCode(cashFlowItemDto.getOldUpCode());
					cashFlowItemOld.setCashFlowId(cashFlowItemDto.getCashFlowId());
					cashFlowItemOld.setVersion(cashFlowItemDto.getVersion());
					cashFlowItemMapper.update(cashFlowItemOld,new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getCode, cashFlowItemOld.getCode()).eq(CashFlowItem::getCashFlowId,cashFlowItemOld.getCashFlowId()).eq(CashFlowItem::getIsDel,Constant.Number.ZERO));
				}
				 /*//id互换流程，暂时保留
				 //返回日志Id
				cashFlowItemDto.setId(cashFlowItemVoUp.getId());
				//判断上级是否是末级
				if (Constant.Is.YES.equals(cashFlowItemVoUp.getIsFinal())){
					//末级项目需要重新组装数据
					addAndUpdateReassembleData(cashFlowItemDto, cashFlowItem, cashFlowItemVoUp);
					//新建老的Dto用于校验老的上级项目是否存在下级
					CashFlowItemDto cashFlowItemDtoOld = new CashFlowItemDto();
					cashFlowItemDtoOld.setCashFlowId(cashFlowItemDto.getCashFlowId());
					cashFlowItemDtoOld.setUpCode(cashFlowItemDto.getOldUpCode());
					//判断原上级项目是否存在其他下级，不存在则跟新末级为是
					int downCount = cashFlowItemMapper.findIsExistNextCashFlowItem(cashFlowItemDtoOld);
					//不存在下级则跟新老的上级为末级
					if (downCount == 0) {
						CashFlowItem cashFlowItemOld = new CashFlowItem();
						cashFlowItemOld.setIsFinal(Constant.Is.YES);
						cashFlowItemOld.setCode(cashFlowItemDto.getOldUpCode());
						cashFlowItemOld.setCashFlowId(cashFlowItemDto.getCashFlowId());
						cashFlowItemMapper.update(cashFlowItemOld,new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getCode, cashFlowItemOld.getCode()).eq(CashFlowItem::getCashFlowId,cashFlowItemOld.getCashFlowId()).eq(CashFlowItem::getIsDel,Constant.Number.ZERO));
					}
					return cashFlowItemDto.getId();
				}*/
			}else{
				//没有更换上级，直接更新
				cashFlowItemMapper.update(cashFlowItem, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemDto.getId()));
			}
			return cashFlowItem.getId();
		}
	}

	/**
	 * @param cashFlowItemDtos
	 * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
	 * @Author Libao
	 * @Description 查询现金流量项目信息，校验是否存在禁用上级
	 * @Date 2019/8/6 16:27
	 * @Param []
	 */
	@Override
	public List<CashFlowItemVo> findCashFlowItemForEnable(List<CashFlowItemDto> cashFlowItemDtos){
		 List<CashFlowItemVo> voList = new ArrayList<>();
		for (CashFlowItemDto cashFlowItemDto:cashFlowItemDtos){
			List<CashFlowItemVo> list = cashFlowItemMapper.findCashFlowItemByCode(cashFlowItemDto);
			voList.addAll(list);
		}
		return  voList;
	}

	/**
	 * @param cashFlowItemDto
	 * @return Integer
	 * @Author Libao
	 * @Description 根据 编码 查询现金流量项目
	 * @Date 2019/6/12 14:10
	 */
	@Override
	public Integer findCashFlowItemByCode(CashFlowItemDto cashFlowItemDto) {
		return cashFlowItemMapper.selectCount(new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getCode, cashFlowItemDto.getCode()).eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId()).eq(CashFlowItem::getIsDel,Constant.Number.ZERO));
	}

	/**
	 * @param cashFlowItemDto
	 * @return Integer
	 * @Author Libao
	 * @Description 根据 名称 查询现金流量项目
	 * @Date 2019/6/12 14:10
	 */
	@Override
	public Integer findCashFlowItemByName(CashFlowItemDto cashFlowItemDto) {
		return cashFlowItemMapper.selectCount(new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getName, cashFlowItemDto.getName()).eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId()).eq(CashFlowItem::getIsDel,Constant.Number.ZERO));
	}

	/**
	 * @return com.njwd.entity.basedata.CashFlowItem
	 * @Author Libao
	 * @Description 根据Id查询现金流量项目
	 * @Date 2019/6/12 15:58
	 * @Param [cashFlowItemDto]
	 */
	@Override
	@Cacheable(value = "cashFlowItem", key = "#cashFlowItemDto.id", unless = "#result==null")
	public CashFlowItemVo findCashFlowItemById(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		CashFlowItemVo cashFlowItemVo = cashFlowItemMapper.findCashFlowItemById(cashFlowItemDto);
		return cashFlowItemVo;
	}

	/**
	 * @return com.njwd.entity.basedata.vo.CashFlowItemVo
	 * @Author Libao
	 * @Description 查询分组
	 * @Date 2019/6/18 13:57
	 * @Param []
	 */
	@Override
	public List<CashFlowItemVo> findGroup(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		List<CashFlowItemVo> groupList = cashFlowItemMapper.findGroup(cashFlowItemDto);
		return groupList;
	}

	/**
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.basedata.entity.CashFlowItemVo>
	 * @Author Libao
	 * @Description 分页查询现金流量项目
	 * @Date 2019/6/11 17:54
	 * @Param [cashFlowItemDto]
	 **/
	@Override
	public Page<CashFlowItemVo> findPage(CashFlowItemDto cashFlowItemDto) {
		Page<CashFlowItemVo> page = cashFlowItemDto.getPage();
		Page<CashFlowItemVo> cashFlowItemVoList = cashFlowItemMapper.findPage(page, cashFlowItemDto);
		return cashFlowItemVoList;

	}

	/**
	 * @param cashFlowItemDto
	 * @return list
	 * @Author Libao
	 * @Description 根据租户Id 和 现金流量项目表Id查询现金流量项目
	 * @Date 2019/7/2 10:32
	 * @ParamcashFlowItemDto
	 */
	@Override
	public List<CashFlowItemVo> findCashFlowItemList(CashFlowItemDto cashFlowItemDto) {

		return cashFlowItemMapper.findCashFlowItemList(cashFlowItemDto);
	}

	/**
	 * @param cashFlowItemDto
	 * @return CashFlowItemVo
	 * @Author Libao
	 * @Description 查询上级id（用于报表）
	 * @Date 2019/7/5 9:07
	 * @Param [cashFlowItemDto]
	 */
	@Override
	public List<CashFlowItemVo> findCashFlowItemIdForReport(CashFlowItemDto cashFlowItemDto) {
		List<CashFlowItemVo> list = cashFlowItemMapper.findCashFlowItemIdsByCode(cashFlowItemDto);
		//集合对象去重
		 List<CashFlowItemVo> voList = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o->o.getId()))),ArrayList::new));
		return voList;
	}


	/**
	 * @param cashFlowItemDto
	 * @return com.njwd.entity.platform.vo.CashFlowItemVo
	 * @Author Libao
	 * @Description 根据Id查询下级最大Code
	 * @Date 2019/9/4 11:27
	 * @Param [cashFlowItemDto]
	 */
	@Override
	public CashFlowItemVo findCashFlowItemCodeForAdd(CashFlowItemDto cashFlowItemDto) {
		//获取前端穿过来的编码
		String code = cashFlowItemDto.getCode();
		//创建对象，用于返回前端
		CashFlowItemVo cashFlowItemVoReturn = new CashFlowItemVo();
		//拼接code
		String contactCode;
		//返回的code
		String returnCode = "";
		//查询出所有下级编码
		List<CashFlowItemVo> list = cashFlowItemMapper.findCashFlowItemCodeForAdd(cashFlowItemDto);
		if (CollectionUtils.isEmpty(list)){
			returnCode = code + Constant.CashFlowItemData.CASH_CODE;
			cashFlowItemVoReturn.setCode(returnCode);
			return cashFlowItemVoReturn;
		}

		for (int i = 1;i<=99;i++){
			if (i<10){
				contactCode = code+"0"+i;
			}else{
				contactCode = code + i;
			}
			//创建标识，用于判断是否存在
			Boolean flag = false;
				for (CashFlowItemVo cashFlowItemVo : list){
					if ((contactCode).equals(cashFlowItemVo.getCode())){
						flag = true;
						break;
					}
				}
				if (!flag){
					returnCode = contactCode;
					break;
				}
		}
		cashFlowItemVoReturn.setCode(returnCode);
		return cashFlowItemVoReturn;
	}


	/**
	 * @param cashFlowItemDto
	 * @return com.njwd.entity.platform.vo.CashFlowItemVo
	 * @Author Libao
	 * @Description 根据上级code查询上级Id
	 * @Date 2019/9/6 9:37
	 * @Param [cashFlowItemDto]
	 */
	@Override
	public CashFlowItemVo findUpCashFlowItemByCode(CashFlowItemDto cashFlowItemDto) {
		//查询上级现金流量项目
		return cashFlowItemMapper.findUpCashFlowItemByCode(cashFlowItemDto);
	}

	/**
	 * 查询现金流量项目列表-平台增量数据
	 *
	 * @param cashFlowItemDto
	 * @return com.njwd.support.Result
	 * @Author lj
	 * @Date:16:56 2019/12/2
	 **/
	@Override
	public List<CashFlowItemVo> findPlatformCashFlowItemList(CashFlowItemDto cashFlowItemDto) {
		//查询基础资料已有的现金流量项目platformId
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		List<Long> platformIds=cashFlowItemMapper.findCashFlowItemPlatformIds(cashFlowItemDto);
		if(CollectionUtils.isEmpty(platformIds)){
			platformIds=null;
		}
		CashFlow cashFlow = new CashFlow();
		FastUtils.copyProperties(cashFlowItemDto, cashFlow);
		// 2.跟新现金流量表模板数据
		// 查询模板是否存在
		CashFlow cashFlowInfo = cashFlowItemTemplateService.findCashFlowTemplate(cashFlow);
		Long cashFlowId=0L;
		if(cashFlowInfo!=null){
			cashFlowId=cashFlowInfo.getId();
		}
		//查询租户下已存在的现金流量数据
		List<CashFlowItem> cashFlowItemList = cashFlowItemMapper.selectList(new LambdaQueryWrapper<CashFlowItem>()
		.eq(CashFlowItem::getIsDel,Constant.Number.ZERO)
		.eq(CashFlowItem::getRootEnterpriseId,operator.getRootEnterpriseId())
		.eq(CashFlowItem::getCashFlowId,cashFlowId));
		Map<String,CashFlowItem> cashFlowItemMap = cashFlowItemList.stream().collect(Collectors.toMap(t->t.getCode(),t->t));
		//查询平台现金流量项目列表
		cashFlowItemDto.setPlatformIds(platformIds);
		cashFlowItemDto.setRootEnterpriseId(null);
		List<CashFlowItemVo> cashFlowItemVoList = cashFlowItemFeignClient.findCashFlowItemList(cashFlowItemDto).getData();
		//去除已存在的数据
		Iterator<CashFlowItemVo> iterator = cashFlowItemVoList.iterator();
		while (iterator.hasNext()) {
			if(cashFlowItemMap.containsKey(iterator.next().getCode())){
				iterator.remove();
			}
		}
		return cashFlowItemVoList;
	}

	/**
	 * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
	 * @Author Libao
	 * @Description 查询现金流量项目信息，用于总账报表拼接
	 * @Date 2019/8/6 16:27
	 * @Param []
	 */
	@Override
	public List<CashFlowItemVo> findCashFlowItemForReport(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		return cashFlowItemMapper.findCashFlowItemForReport(cashFlowItemDto);
	}

	/**
	 * @param cashFlowItemDto,response
	 * @return
	 * @Author Libao
	 * @Description 导出excel
	 * @Date 2019/7/3 15:54
	 */
	@Override
	public void exportExcel(CashFlowItemDto cashFlowItemDto, HttpServletResponse response) {
		Page<CashFlowItemVo> page = cashFlowItemDto.getPage();
		fileService.resetPage(page);
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		Page<CashFlowItemVo> cashFlowItemVoList = cashFlowItemMapper.findPage(page, cashFlowItemDto);
		//user多使用公司字段
		if (Constant.Number.ONE.equals(cashFlowItemDto.getIsEnterpriseAdmin())) {
			fileService.exportExcel(
					response,
					cashFlowItemVoList.getRecords(),
					MenuCodeConstant.CASH_FLOW_ITEM,
					(byte) cashFlowItemDto.getIsEnterpriseAdmin());
		} else {
			fileService.exportExcel(
					response,
					cashFlowItemVoList.getRecords(),
					MenuCodeConstant.CASH_FLOW_ITEM,
					(byte) cashFlowItemDto.getIsEnterpriseAdmin());
		}
	}

	/**
	 * 根据场景设置基础信息
	 *
	 * @param obj
	 * @param scenes 场景
	 */
	public void setCashFlowItemInfo(Object obj, String scenes) {
		SysUserVo userVo = UserUtils.getUserVo();
		CashFlowItem cashFlowItem = null;
		CashFlowItemDto cashFlowItemDto = null;
		switch (scenes) {
			case Constant.CashFlowItemData.CASHFLOWITEMADD:
				cashFlowItem = (CashFlowItem) obj;
				cashFlowItem.setCreatorId(userVo.getUserId());
				cashFlowItem.setCreatorName(userVo.getName());
				cashFlowItem.setRootEnterpriseId(userVo.getRootEnterpriseId());
				cashFlowItem.setCreateTime(new Date());
				cashFlowItem.setUpdateTime(null);
				break;
			case Constant.CashFlowItemData.CASHFLOWITEMUPDATE:
				cashFlowItem = (CashFlowItem) obj;
				cashFlowItem.setUpdatorId(userVo.getUserId());
				cashFlowItem.setUpdatorName(userVo.getName());
				cashFlowItem.setUpdateTime(new Date());
				break;
			case Constant.CashFlowItemData.CASHFLOWITEMDELBATCH:
				cashFlowItemDto = (CashFlowItemDto) obj;
				cashFlowItemDto.setUpdatorId(userVo.getUserId());
				cashFlowItemDto.setUpdatorName(userVo.getName());
				//cashFlowItemDto.setUpdateTime(new Date());
                break;
			case Constant.CashFlowItemData.CASHFLOWITEMUPDATEBATCH:
				cashFlowItemDto = (CashFlowItemDto) obj;
				cashFlowItemDto.setUpdatorId(userVo.getUserId());
				cashFlowItemDto.setUpdatorName(userVo.getName());
				cashFlowItemDto.setUpdateTime(new Date());
				break;
			default:
				cashFlowItem = (CashFlowItem) obj;
				cashFlowItem.setUpdateTime(new Date());
		}

	}

	/**
	 * @Author Libao
	 * @Description 校验现金流量项目编码/名称是否重复
	 * @Date 2019/6/12 14:10
	 * @Param [cashFlowItemDto]
	 */
	private void checkCashFlowItemUniqueness(CashFlowItemDto cashFlowItemDto) {
		//设置租户
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		Integer row = findCashFlowItemByCode(cashFlowItemDto);
		if (row != 0) {
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CODE_EXIST);
		}
		row = findCashFlowItemByName(cashFlowItemDto);
		if (row != 0) {
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_NAME_EXIST);
		}
	}

	/**
	 * @Author Libao
	 * @Description 校验现金流量项目编码/名称是否重复
	 * @Date 2019/6/12 14:10
	 * @Param [cashFlowItemDto]
	 */
	private void checkCashFlowItemForUpdate(CashFlowItemDto cashFlowItemDto) {
		int row = 0;
		if (!cashFlowItemDto.getOldName().equals(cashFlowItemDto.getName())) {
			row = findCashFlowItemByName(cashFlowItemDto);
			if (row != 0) {
				throw new ServiceException(ResultCode.NAME_EXIST);
			}
		}
		if (!cashFlowItemDto.getOldCode().equals(cashFlowItemDto.getCode())) {
			row = findCashFlowItemByCode(cashFlowItemDto);
			if (row != 0) {
				throw new ServiceException(ResultCode.CODE_EXIST);
			}
		}
	}

	/**
	 * @return java.lang.Boolean
	 * @Author Libao
	 * @Description 判断数据是否被引用
	 * @Date 2019/7/3 15:54
	 * @Param [cashFlowItemDto]
	 */
	@Override
	public Boolean checkIsUsed(CashFlowItemDto cashFlowItemDto) {
		Boolean flag = true;
		List<Long> ids = cashFlowItemMapper.findIdsByCashFlowId(cashFlowItemDto);
		if (ids != null && ids.size() > 0) {
			ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.CASH_FLOW_ITEM, ids);
			if (!referenceContext.getReferences().isEmpty()) {
				//已被引用
				flag = false;
			}
		}
		return flag;
	}

	/**
	 * @Author Libao
	 * @Description 判断是否引用，前端调用
	 * @Date  2019/8/29 17:03
	 * @Param [cashFlowItemDto]
	 * @return com.njwd.entity.basedata.ReferenceResult
	 */
	@Override
	public ReferenceResult checkIsUsedSingle(CashFlowItemDto cashFlowItemDto) {
		ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.CASH_FLOW_ITEM, cashFlowItemDto.getId());
		return referenceResult;
	}

    /**
     * @Author Libao
     * @Description 校验现金流量项目是否重复
     * @Date  2019/8/29 17:07
     * @Param [cashFlowItemDto]
     * @return java.lang.Boolean
     */
	public Boolean checkCashFlowItem(CashFlowItemDto cashFlowItemDto) {
		Boolean flag = true;
		List<String> columnList = new ArrayList<>();
		int count;
		StringBuilder message = new StringBuilder();
		if (StringUtils.isNotBlank(cashFlowItemDto.getCode())) {
			count = findCashFlowItemByCode(cashFlowItemDto);
			if (count > 0) {
				flag = false;
				columnList.add(Constant.EntityName.CODE);
				message.append(ResultCode.CODE_EXIST.message);
			}
		}
		if (StringUtils.isNotBlank(cashFlowItemDto.getName())) {
			count = findCashFlowItemByName(cashFlowItemDto);
			if (count > 0) {
				flag = false;
				columnList.add(Constant.EntityName.NAME);
				message = StringUtils.isBlank(message.toString()) ? message.append(ResultCode.NAME_EXIST.message) : message.append(",").append(ResultCode.NAME_EXIST.message);
			}
		}
		cashFlowItemDto.setResultMessage(message.toString());
		cashFlowItemDto.setColumnList(columnList);
		return flag;
	}

	/**
	 * @param cashFlowItemDto
	 * @return
	 * @Author Libao
	 * @Description 校验最大级次
	 * @Date 2019/7/5 9:07
	 */
	private void checkMaxLevel(CashFlowItemDto cashFlowItemDto) {
		String maxLevel = cashFlowItemDto.getMaxLevel();
		String[] maxLevelArr = maxLevel.split("-");
		if (cashFlowItemDto.getLevel() > maxLevelArr.length) {
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_MAX_LEVEL);
		}
	}

	/**
	 * @return boolean
	 * @Author Libao
	 * @Description 导入参数code校验
	 * @Date 2019/7/5 9:07
	 * @param cashFlowItemDto
	 */
	@Override
	public int checkCode(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		return findCashFlowItemByCode(cashFlowItemDto);
	}

	/**
	 * @param cashFlowItemDto
	 * @return boolean
	 * @Author Libao
	 * @Description 导入参数name校验
	 * @Date 2019/7/5 9:07
	 * @Param [cashFlowItemDto]
	 */
	@Override
	public int checkName(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		return findCashFlowItemByName(cashFlowItemDto);
	}



	/**
	 * @param cashFlowItemDto
	 * @return CashFlowItemVo
	 * @Author Libao
	 * @Description 查询上级项目（用于校验）
	 * @Date 2019/7/5 9:07
	 * @Param [cashFlowItemDto]
	 */
	@Override
	public CashFlowItemVo checkUpCashFlowItem(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		return cashFlowItemMapper.findUpCashFlowItemByCode(cashFlowItemDto);
	}

	/**
	 * @return void
	 * @Author Libao
	 * @Description 校验是否被禁用
	 * @Date 2019/7/23 10:11
	 * @Param [cashFlowItemDto]
	 */
	public void checkIsForbid(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		CashFlowItemVo cashFlowItemVo = cashFlowItemMapper.findUpCashFlowItemByCode(cashFlowItemDto);
		if (cashFlowItemVo == null){
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_UP_NOT_EXIST);
		}
		//判断数据是否被禁用
		if (Constant.Is.NO.equals(cashFlowItemVo.getIsEnable())) {
			//抛出数据已禁用
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_DISABLE);
		}
	}

	/**
	 * @return void
	 * @Author Libao
	 * @Description 校验是否是末级
	 * @Date 2019/7/23 10:11
	 * @Param [cashFlowItemDto]
	 */
	public void checkIsFinal(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		CashFlowItemVo cashFlowItemVo = cashFlowItemMapper.findCashFlowItemById(cashFlowItemDto);
		//判断数据是否是末级
		if (Constant.Is.NO.equals(cashFlowItemVo.getIsFinal())) {
			//抛出数据非末级
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_FINAL);
		}
	}

	/**
	 * @Author Libao
	 * @Description 校验是否需要跟新末级
	 * @Date  2019/9/6 9:43
	 * @Param [cashFlowItemDto]
	 * @return void
	 */
	public void checkIsNeedUpdateFinal(CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		List<CashFlowItemVo> cashFlowItemVo = cashFlowItemMapper.findCashFlowItemByUpCode(cashFlowItemDto);
		//判断上级是否存在下级
		if (cashFlowItemVo == null || cashFlowItemVo.isEmpty()) {
			//不存在下级则跟新上级为末级
			CashFlowItem cashFlowItemUp = new CashFlowItem();
			cashFlowItemUp.setIsFinal(Constant.Is.YES);
			cashFlowItemUp.setVersion(cashFlowItemDto.getVersion());
			cashFlowItemMapper.update(cashFlowItemUp, new LambdaQueryWrapper<CashFlowItem>()
					.eq(CashFlowItem::getCode, cashFlowItemDto.getUpCode())
					.eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId())
					.eq(CashFlowItem::getIsDel,Constant.Number.ZERO)
			 );
		}
	}

	//校验系统预置的内部往来项目为三级项目，且是末级项目，不能新增下级
	private void checkInteriorAndInit(CashFlowItemDto cashFlowItemDto, CashFlowItemVo cashFlowItemVoUp) {
		//系统预置的内部往来项目为三级项目，且是末级项目，不能新增下级
		if (Constant.Is.YES.equals(cashFlowItemVoUp.getIsInteriorContact()) && Constant.Is.YES.equals(cashFlowItemVoUp.getIsInit()) && Constant.Is.YES.equals(cashFlowItemVoUp.getIsFinal())) {
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_FINAL_INTERIOR_INIT);
		}
		//查询预置数据是否存在下级预置数据，存在则不可以新增
		int downCount = cashFlowItemMapper.findCashFlowItemCountByUpCode(cashFlowItemDto);
		if (downCount > 0) {
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_INIT_AND_NOT_FINAL);
		}
	}


	/**
	 * @Author Libao
	 * @Description 获取需要处理的codes
	 * @Date  2019/9/10 13:43
	 * @Param [cashFlowItemDto, batchResult, failList, codes]
	 * @return boolean
	 */
	private boolean getNeedDealCodes(CashFlowItemDto cashFlowItemDto, BatchResult batchResult, List<ReferenceDescription> failList, List<String> codes) {
		ReferenceDescription failDescription;
		codes.retainAll(cashFlowItemDto.getCodes());
		if (CollectionUtils.isNotEmpty(codes)) {
			for (String code : codes) {
				failDescription = new ReferenceDescription();
				CashFlowItem cashFlowItem = cashFlowItemMapper.selectOne(new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getCode, code).eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId()));
				failDescription.setBusinessId(cashFlowItem.getId());
				failDescription.setBusinessCode(cashFlowItem.getCode());
				failDescription.setInfo(cashFlowItem);
				failDescription.setReferenceDescription(ResultCode.EXIST_DOWN_INTERIOR_OR_IS_INTERIOR.message);
				failList.add(failDescription);
			}
			cashFlowItemDto.getCodes().removeAll(codes);
		}
		if (CollectionUtils.isEmpty(cashFlowItemDto.getCodes())) {
			batchResult.setFailList(failList);
			return true;
		}
		return false;
	}
    /**
     * @Author Libao
     * @Description 校验版本号
     * @Date  2019/9/10 17:03
     * @Param [cashFlowItemDto]
     * @return void
     */
    @Override
	public void checkVersion(CashFlowItemDto cashFlowItemDto){
		Long id = cashFlowItemMapper.checkVersion(cashFlowItemDto);
		if (id != null){
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CHECK_VERSION);
		}
	}


	/**
	 * @Author Libao
	 * @Description 末级项目数据重新组装
	 * @Date  2019/9/6 9:41
	 * @Param [cashFlowItemDto, cashFlowItem, cashFlowItemVoUp]
	 * @return void
	 */
	private void addAndUpdateReassembleData(CashFlowItemDto cashFlowItemDto, CashFlowItem cashFlowItem, CashFlowItemVo cashFlowItemVoUp) {
		//重新组装现金流量项目
		CashFlowItem cashFlowItemUp = new CashFlowItem();
		FastUtils.copyProperties(cashFlowItemDto, cashFlowItemUp);
		//更新上级为非预置
		cashFlowItemUp.setIsInit(Constant.CashFlowItemData.IS_INIT_NO);
		cashFlowItemMapper.update(cashFlowItemUp, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemVoUp.getId()));
		CashFlowItem cashFlowItemDown = new CashFlowItem();
		FastUtils.copyProperties(cashFlowItemVoUp, cashFlowItemDown);
		//将上级末级字段设置为0（非末级）
		cashFlowItemDown.setIsFinal(Constant.Is.NO);
		//特殊处理
		cashFlowItemDown.setRemark(cashFlowItemDown.getRemark() == null ? "" : cashFlowItemDown.getRemark());
		cashFlowItemDown.setUpCode(cashFlowItemDown.getUpCode() == null ? "" : cashFlowItemDown.getUpCode());
		cashFlowItemMapper.update(cashFlowItemDown, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItem.getId()));
	}

	/**
	 * @Author 周鹏
	 * @Description 查询现金流量项目信息，用于会计科目初始化和切换模板时将平台的现金流量信息更新成基础资料的信息
	 * @Date  2019/9/20 16:27
	 * @Param [cashFlowItemDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
	 */
	@Override
	public List<CashFlowItemVo> findCashFlowItemInfoList(CashFlowItemDto cashFlowItemDto){
		return cashFlowItemMapper.findCashFlowItemInfoList(cashFlowItemDto);
	}

}

