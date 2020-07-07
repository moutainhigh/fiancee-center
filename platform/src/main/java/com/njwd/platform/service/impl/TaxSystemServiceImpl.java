package com.njwd.platform.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.TaxSystem;
import com.njwd.entity.platform.dto.AreaDto;
import com.njwd.entity.platform.dto.CurrencyDto;
import com.njwd.entity.platform.dto.TaxSystemDto;
import com.njwd.entity.platform.vo.AreaVo;
import com.njwd.entity.platform.vo.CurrencyVo;
import com.njwd.entity.platform.vo.TaxSystemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.TaxSystemMapper;
import com.njwd.platform.service.*;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 税收制度 service impl
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Service
public class TaxSystemServiceImpl implements TaxSystemService {

	@Resource
	private TaxSystemMapper taxSystemMapper;
	@Resource
	private BasePlatformMapper basePlatformMapper;
	@Resource
	private SequenceService sequenceService;
	@Resource
	private TaxSystemService taxSystemService;
	@Resource
	private AreaService areaService;
	@Resource
	private CurrencyService currencyService;
	@Resource
	private FileService fileService;
	@Resource
	private ReferenceRelationService referenceRelationService;
	@Resource
	private MessageService messageService;

	/**
	 * @Description 新增税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:43
	 * @Param [param]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addTaxSystem(TaxSystemDto param) {
		// todo 权限校验
		// 名称平台内不重复
		checkData(param);

		//国家信息
		AreaVo areaVo = findArea(param.getAreaId());
		param.setArea(areaVo.getAreaName());
		//币种信息
		CurrencyVo currencyVo = findCurrency(param.getCurrencyId());
		param.setPrecision(currencyVo.getPrecision());
		param.setRoundingType(currencyVo.getRoundingType());

		param.setVersion(Constant.Number.ZERO);
		// 编码规则为SZ+2位流水号
		param.setCode(sequenceService.getCode(PlatformConstant.PlatformCodeRule.TAX_SYSTEM, Constant.Number.TWO));
		TaxSystem insertData = new TaxSystem();
		FastUtils.copyProperties(param, insertData);
		return taxSystemMapper.insert(param);
	}

	/**
	 * @Description 更新税制
	 * @Author 郑勇浩
	 * @Data 2019/11/14 14:43
	 * @Param [param]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateTaxSystem(TaxSystemDto param) {
		// todo 权限校验
		//查询该数据状态
		TaxSystemDto findParam = new TaxSystemDto();
		findParam.setId(param.getId());
		TaxSystemVo taxSystem = taxSystemService.findTaxSystem(findParam);
		if (taxSystem == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}
		//如果版本号不同
		if (!taxSystem.getVersion().equals(param.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}
		//判断 删除状态
		if (taxSystem.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//判断 审核状态
		if (taxSystem.getIsApproved().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_APPROVED);
		}
		// 名称平台内不重复
		checkData(param);

		//国家信息
		AreaVo areaVo = findArea(param.getAreaId());
		param.setArea(areaVo.getAreaName());
		//币种信息
		CurrencyVo currencyVo = findCurrency(param.getCurrencyId());
		param.setPrecision(currencyVo.getPrecision());
		param.setRoundingType(currencyVo.getRoundingType());

		//更新
		TaxSystem sqlParam = new TaxSystem();
		sqlParam.setId(taxSystem.getId());

		param.setId(null);
		param.setVersion(taxSystem.getVersion());

		int result = taxSystemMapper.update(param, new QueryWrapper<>(sqlParam));
		if (result < 1) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}
		param.setId(sqlParam.getId());
		//清除缓存
		RedisUtils.remove(PlatformConstant.RedisCache.TAX_SYSTEM, param.getId());
		return result;
	}

	/**
	 * @Description 批量删除税制
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:12
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult deleteTaxSystem(TaxSystemDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DELETE);
	}

	/**
	 * @Description 批量审核税制
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:12
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult approvedTaxSystem(TaxSystemDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.APPROVED);
	}

	/**
	 * @Description 批量反审核税制
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:12
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult disapprovedTaxSystem(TaxSystemDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DISAPPROVED);
	}

	/**
	 * @Description 批量发布税制
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:13
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult releasedTaxSystem(TaxSystemDto param) {
		BatchResult batchResult = updateStatusBatch(param, PlatformConstant.OperateType.RELEASED);
		messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE, param.getMessageDto());
		return batchResult;
	}

	/**
	 * @Description 查询税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 18:03
	 * @Param [param]
	 * @return com.njwd.entity.platform.vo.TaxSystemVo
	 */
	@Override
	@Cacheable(value = PlatformConstant.RedisCache.TAX_SYSTEM, key = "#param.id", unless = "#result == null")
	public TaxSystemVo findTaxSystem(TaxSystemDto param) {
		return taxSystemMapper.findTaxSystem(param);
	}

	/**
	 * @Description 查询税制[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:13
	 * @Param [param]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	@Override
	public Page<TaxSystemVo> findTaxSystemPage(TaxSystemDto param) {
		Page<TaxSystemVo> page = param.getPage();
		page = taxSystemMapper.findTaxSystemPage(page, param);
		return page;
	}

	/**
	 * @Description 查询税制[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:18
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	@Override
	public List<TaxSystemVo> findTaxSystemList(TaxSystemDto param) {
		return taxSystemMapper.findTaxSystemList(param);
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:08
	 * @Param [response, param]
	 */
	@Override
	public void exportExcel(HttpServletResponse response, TaxSystemDto param) {
		List<TaxSystemVo> data = taxSystemMapper.findTaxSystemList(param);

		if (CollectionUtils.isEmpty(data)) {
			fileService.exportExcel(response, new ArrayList<>());
			return;
		}
		fileService.exportExcel(response, data,
				new ExcelColumn("code", "编码"),
				new ExcelColumn("name", "名称"),
				new ExcelColumn("areaName", "地区"),
				new ExcelColumn("currencyName", "税收币种"),
				new ExcelColumn("isApprovedStr", "审核状态")
		);
	}

	/**
	 * @Description 重复性校验
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:33
	 * @Param [param]
	 */
	private void checkData(TaxSystemDto param) {
		// 名称平台内不重复
		TaxSystemDto checkParam = new TaxSystemDto();
		checkParam.setName(param.getName());
		checkParam.setIsDel(Constant.Number.ANTI_INITLIZED);
		TaxSystemVo data = taxSystemMapper.findTaxSystem(checkParam);
		//如果存在数据
		if (data != null && !data.getId().equals(param.getId())) {
			throw new ServiceException(ResultCode.NAME_EXIST);
		}
	}

	/**
	 * @Description 查询国家地区
	 * @Author 郑勇浩
	 * @Data 2019/11/14 18:07
	 * @Param [id]
	 */
	private AreaVo findArea(Long id) {
		AreaDto param = new AreaDto();
		param.setId(id);
		AreaVo area = areaService.findArea(param);
		//不存在
		if (area == null) {
			throw new ServiceException(ResultCode.TAX_SYSTEM_NOT_EXISTS);
		}
		//未审核
		if (area.getIsApproved().equals(Constant.Is.NO)) {
			throw new ServiceException(ResultCode.AREA_IS_UNAPPROVED);
		}
		return area;
	}

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 18:07
	 * @Param [id]
	 */
	private CurrencyVo findCurrency(Long id) {
		CurrencyDto param = new CurrencyDto();
		param.setId(id);
		CurrencyVo currency = currencyService.findCurrency(param);
		//不存在
		if (currency == null) {
			throw new ServiceException(ResultCode.TAX_SYSTEM_NOT_EXISTS);
		}
		//未发布
		if (currency.getIsReleased().equals(Constant.Is.NO)) {
			throw new ServiceException(ResultCode.CURRENCY_IS_UNRELEASED);
		}
		return currency;
	}

	/**
	 * @Description 批量操作
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:02
	 * @Param [param, type]
	 * @return com.njwd.support.BatchResult
	 */
	private BatchResult updateStatusBatch(TaxSystemDto param, int type) {
		//初始化
		BatchResult result = new BatchResult();
		result.setFailList(new LinkedList<>());
		result.setSuccessList(new ArrayList<>());

		//查询待查询的所有数据的状态
		List<TaxSystemVo> statusList = taxSystemMapper.findTaxSystemListStatus(param);
		//如果查询结果没有数据
		if (CollectionUtils.isEmpty(statusList)) {
			for (TaxSystemVo data : param.getChangeList()) {
				addFailResult(result, data.getId(), ResultCode.RECORD_NOT_EXIST.message);
			}
			return result;
		}
		//转化为id为key的状态map
		Map<Long, TaxSystemVo> statusMap = statusList.stream().collect(Collectors.toMap(TaxSystemVo::getId, o -> o));

		//循环判断当前数据是否能添加
		TaxSystemVo statusData;
		for (TaxSystemVo changeData : param.getChangeList()) {
			//获取当前数据对应的状态数据
			statusData = statusMap.get(changeData.getId());
			if (statusData == null) {
				addFailResult(result, changeData.getId(), ResultCode.RECORD_NOT_EXIST.message);
				continue;
			}

			//判断版本号
			if (!statusData.getVersion().equals(changeData.getVersion())) {
				addFailResult(result, statusData.getId(), ResultCode.VERSION_ERROR.message);
				continue;
			}

			//判断删除 判断审核 判断反审核
			if (type == PlatformConstant.OperateType.DELETE) {
				if (statusData.getIsDel().equals(Constant.Is.YES)) {
					addFailResult(result, changeData.getId(), ResultCode.DELETE_FAIL.message);
					continue;
				}
				if (statusData.getIsApproved().equals(Constant.Is.YES)) {
					addFailResult(result, changeData.getId(), ResultCode.DEL_CHECK_APPROVED.message);
					continue;
				}
			} else if (type == PlatformConstant.OperateType.APPROVED) {
				if (statusData.getIsApproved().equals(Constant.Is.YES)) {
					addFailResult(result, changeData.getId(), ResultCode.APPROVE_CHECK_APPROVED.message);
					continue;
				}
			} else if (type == PlatformConstant.OperateType.DISAPPROVED) {
				if (statusData.getIsReleased().equals(Constant.Is.YES)) {
					addFailResult(result, changeData.getId(), ResultCode.DISAPPROVE_CHECK_RELEASED.message);
					continue;
				} else if (statusData.getIsApproved().equals(Constant.Is.NO)) {
					addFailResult(result, changeData.getId(), ResultCode.DISAPPROVE_CHECK_DISAPPROVED.message);
					continue;
				}
			} else if (type == PlatformConstant.OperateType.RELEASED) {
				if (statusData.getIsReleased().equals(Constant.Is.YES)) {
					addFailResult(result, changeData.getId(), ResultCode.RELEASE_CHECK_RELEASED.message);
					continue;
				} else if (statusData.getIsApproved().equals(Constant.Is.NO)) {
					addFailResult(result, changeData.getId(), ResultCode.RELEASE_CHECK_NO_APPROVED.message);
					continue;
				}
			} else {
				return new BatchResult();
			}
			result.getSuccessList().add(statusData.getId());
		}

		if (result.getSuccessList().size() == 0) {
			return result;
		}

		//反审核 删除 判断引用
		if (type == PlatformConstant.OperateType.DELETE) {
			ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_TAX_SYSTEM, result.getSuccessList());
			result.getFailList().addAll(referenceContext.getReferences());
			result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
			if (result.getSuccessList().size() == 0) {
				return result;
			}
		}

		//批量操作
		basePlatformMapper.batchProcess(result.getSuccessList(), type, UserUtil.getUserVo(), PlatformConstant.TableName.TAX_SYSTEM);
		//清除成功修改的redis缓存
		RedisUtils.removeBatch(PlatformConstant.RedisCache.TAX_SYSTEM, result.getSuccessList());
		return result;
	}


	/**
	 * @Description 添加失败原因
	 * @Author 郑勇浩
	 * @Data 2019/11/12 17:24
	 * @Param [result, id, failMessage]
	 */
	private void addFailResult(BatchResult result, Long id, String failMessage) {
		ReferenceDescription fd = new ReferenceDescription();
		fd.setBusinessId(id);
		fd.setReferenceDescription(failMessage);
		result.getFailList().add(fd);
	}

}
