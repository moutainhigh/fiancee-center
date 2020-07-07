package com.njwd.platform.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.TaxCategory;
import com.njwd.entity.platform.TaxSystem;
import com.njwd.entity.platform.dto.TaxCategoryDto;
import com.njwd.entity.platform.dto.TaxSystemDto;
import com.njwd.entity.platform.vo.TaxCategoryVo;
import com.njwd.entity.platform.vo.TaxSystemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.TaxCategoryMapper;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.service.TaxCategoryService;
import com.njwd.platform.service.TaxSystemService;
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
 * @Description 税种 service impl
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Service
public class TaxCategoryServiceImpl implements TaxCategoryService {

	@Resource
	private TaxCategoryMapper taxCategoryMapper;
	@Resource
	private BasePlatformMapper basePlatformMapper;
	@Resource
	private SequenceService sequenceService;
	@Resource
	private TaxCategoryService taxCategoryService;
	@Resource
	private TaxSystemService taxSystemService;
	@Resource
	private FileService fileService;
	@Resource
	private ReferenceRelationService referenceRelationService;
	@Resource
	private MessageService messageService;

	/**
	 * @Description 新增税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:55
	 * @Param [param]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addTaxCategory(TaxCategoryDto param) {
		// todo 权限校验
		// 同一个税收制度内不可重复
		checkData(param);
		//查询税收制度信息
		TaxSystemVo taxSystemVo = findTaxSystem(param.getTaxSystemId());

		param.setVersion(Constant.Number.ZERO);
		// 编码规则为税收制度的编码+3位流水号
		param.setCode(sequenceService.getCode(taxSystemVo.getCode(), Constant.Number.THREE));

		TaxSystem insertData = new TaxSystem();
		FastUtils.copyProperties(param, insertData);
		return taxCategoryMapper.insert(param);
	}

	/**
	 * @Description 更新税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:55
	 * @Param [param]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateTaxCategory(TaxCategoryDto param) {
		// todo 权限校验
		//查询该数据状态
		TaxCategoryDto findParam = new TaxCategoryDto();
		findParam.setId(param.getId());
		TaxCategoryVo taxCategory = taxCategoryService.findTaxCategory(findParam);
		if (taxCategory == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}
		//如果版本号不同
		if (!taxCategory.getVersion().equals(param.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}
		//判断 删除状态
		if (taxCategory.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//判断 发布状态
		if (taxCategory.getIsReleased().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_RELEASED);
		}
		//判断 审核状态
		if (taxCategory.getIsApproved().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_APPROVED);
		}

		// 同一个税收制度内不可重复
		checkData(param);
		//税收制度信息
		findTaxSystem(param.getTaxSystemId());

		//更新
		TaxCategory sqlParam = new TaxCategory();
		sqlParam.setId(taxCategory.getId());

		param.setId(null);
		param.setVersion(taxCategory.getVersion());

		int result = taxCategoryMapper.update(param, new QueryWrapper<>(sqlParam));
		if (result < 1) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}
		param.setId(sqlParam.getId());
		//清除缓存
		RedisUtils.remove(PlatformConstant.RedisCache.TAX_CATEGORY, param.getId());
		return result;
	}

	/**
	 * @Description 批量删除税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult deleteTaxCategory(TaxCategoryDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DELETE);
	}

	/**
	 * @Description 批量审核税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult approvedTaxCategory(TaxCategoryDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.APPROVED);
	}

	/**
	 * @Description 批量反审核税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult disapprovedTaxCategory(TaxCategoryDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DISAPPROVED);
	}

	/**
	 * @Description 批量发布税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult releasedTaxCategory(TaxCategoryDto param) {
		BatchResult batchResult = updateStatusBatch(param, PlatformConstant.OperateType.RELEASED);
		messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE, param.getMessageDto());
		return batchResult;
	}

	/**
	 * @Description 查询税种
	 * @Author 郑勇浩
	 * @Data 2019/11/15 9:33
	 * @Param [param]
	 * @return com.njwd.entity.platform.vo.TaxCategoryVo
	 */
	@Override
	@Cacheable(value = PlatformConstant.RedisCache.TAX_CATEGORY, key = "#param.id", unless = "#result == null")
	public TaxCategoryVo findTaxCategory(TaxCategoryDto param) {
		return taxCategoryMapper.findTaxCategory(param);
	}

	/**
	 * @Description 查询税种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/15 9:33
	 * @Param [param]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	@Override
	public Page<TaxCategoryVo> findTaxCategoryPage(TaxCategoryDto param) {
		Page<TaxCategoryVo> page = param.getPage();
		page = taxCategoryMapper.findTaxCategoryPage(page, param);
		return page;
	}

	/**
	 * @Description 查询税种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/15 9:34
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	@Override
	public List<TaxCategoryVo> findTaxCategoryList(TaxCategoryDto param) {
		return taxCategoryMapper.findTaxCategoryList(param);
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:08
	 * @Param [response, param]
	 */
	@Override
	public void exportExcel(HttpServletResponse response, TaxCategoryDto param) {
		List<TaxCategoryVo> data = taxCategoryMapper.findTaxCategoryList(param);

		if (CollectionUtils.isEmpty(data)) {
			fileService.exportExcel(response, new ArrayList<>());
			return;
		}
		fileService.exportExcel(response, data,
				new ExcelColumn("code", "编码"),
				new ExcelColumn("name", "名称"),
				new ExcelColumn("taxSystemName", "税收制度"),
				new ExcelColumn("precision", "税额精度"),
				new ExcelColumn("roundingTypeStr", "舍入规则"),
				new ExcelColumn("isVatStr", "增值税"),
				new ExcelColumn("isApprovedStr", "审核状态"),
				new ExcelColumn("isReleasedStr", "发布状态")
		);
	}

	/**
	 * @Description 重复性校验
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:33
	 * @Param [param]
	 */
	private void checkData(TaxCategoryDto param) {
		// 同一个税收制度内不可重复
		TaxCategoryDto checkParam = new TaxCategoryDto();
		checkParam.setName(param.getName());
		checkParam.setTaxSystemId(param.getTaxSystemId());
		checkParam.setIsDel(Constant.Number.ANTI_INITLIZED);
		TaxCategoryVo data = taxCategoryMapper.findTaxCategory(checkParam);
		//如果存在数据
		if (data != null && !data.getId().equals(param.getId())) {
			throw new ServiceException(ResultCode.NAME_EXIST);
		}
	}

	/**
	 * @Description 查询税收制度
	 * @Author 郑勇浩
	 * @Data 2019/11/14 18:07
	 * @Param [id]
	 */
	private TaxSystemVo findTaxSystem(Long id) {
		TaxSystemDto param = new TaxSystemDto();
		param.setId(id);
		TaxSystemVo taxSystemVo = taxSystemService.findTaxSystem(param);
		//不存在
		if (taxSystemVo == null) {
			throw new ServiceException(ResultCode.TAX_SYSTEM_NOT_EXISTS);
		}
		//未审核
		if (taxSystemVo.getIsApproved().equals(Constant.Is.NO)) {
			throw new ServiceException(ResultCode.TAX_SYSTEM_IS_UNAPPROVED);
		}
		return taxSystemVo;
	}

	/**
	 * @Description 批量操作
	 * @Author 郑勇浩
	 * @Data 2019/11/15 9:44
	 * @Param [param, type]
	 * @return com.njwd.support.BatchResult
	 */
	private BatchResult updateStatusBatch(TaxCategoryDto param, int type) {
		//初始化
		BatchResult result = new BatchResult();
		result.setFailList(new LinkedList<>());
		result.setSuccessList(new ArrayList<>());

		//查询待查询的所有数据的状态
		List<TaxCategoryVo> statusList = taxCategoryMapper.findTaxCategoryListStatus(param);
		//如果查询结果没有数据
		if (CollectionUtils.isEmpty(statusList)) {
			for (TaxCategoryVo data : param.getChangeList()) {
				addFailResult(result, data.getId(), ResultCode.RECORD_NOT_EXIST.message);
			}
			return result;
		}
		//转化为id为key的状态map
		Map<Long, TaxCategoryVo> statusMap = statusList.stream().collect(Collectors.toMap(TaxCategoryVo::getId, o -> o));

		//循环判断当前数据是否能添加
		TaxCategoryVo statusData;
		for (TaxCategoryVo changeData : param.getChangeList()) {
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

		//删除 判断引用
		if (type == PlatformConstant.OperateType.DELETE) {
			ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_TAX_CATEGORY, result.getSuccessList());
			result.getFailList().addAll(referenceContext.getReferences());
			result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
			if (result.getSuccessList().size() == 0) {
				return result;
			}
		}

		//批量操作
		basePlatformMapper.batchProcess(result.getSuccessList(), type, UserUtil.getUserVo(), PlatformConstant.TableName.TAX_CATEGORY);
		//清除成功修改的redis缓存
		RedisUtils.removeBatch(PlatformConstant.RedisCache.TAX_CATEGORY, result.getSuccessList());
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
