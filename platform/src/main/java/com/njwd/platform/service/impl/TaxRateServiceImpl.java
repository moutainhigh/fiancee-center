package com.njwd.platform.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.TaxRate;
import com.njwd.entity.platform.dto.TaxCategoryDto;
import com.njwd.entity.platform.dto.TaxRateDto;
import com.njwd.entity.platform.dto.TaxSystemDto;
import com.njwd.entity.platform.vo.TaxCategoryVo;
import com.njwd.entity.platform.vo.TaxRateVo;
import com.njwd.entity.platform.vo.TaxSystemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.TaxRateMapper;
import com.njwd.platform.service.*;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 税率 service impl
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Service
public class TaxRateServiceImpl implements TaxRateService {

	@Resource
	private TaxRateMapper taxRateMapper;
	@Resource
	private BasePlatformMapper basePlatformMapper;
	@Resource
	private SequenceService sequenceService;
	@Resource
	private TaxRateService taxRateService;
	@Resource
	private TaxSystemService taxSystemService;
	@Resource
	private TaxCategoryService taxCategoryService;
	@Resource
	private FileService fileService;
	@Resource
	private MessageService messageService;

	/**
	 * @Description 新增税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:43
	 * @Param [param]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addTaxRate(TaxRateDto param) {
		// todo 权限校验
		//查询税收制度
		findTaxSystem(param.getTaxSystemId());
		//查询税种
		TaxCategoryVo taxCategoryVo = findTaxCategory(param.getTaxCategoryId());

		// 自动生成，等于税率（百分比）+ 税种的名称
		String taxRateName = String.format("%.2f", param.getTaxRate()) + PlatformConstant.TaxRate.PERCENT;
		param.setName(taxRateName + taxCategoryVo.getName());

		param.setVersion(Constant.Number.ZERO);
		// 自动编码；编码规则为税种的编码 + 2位流水号
		param.setCode(sequenceService.getCode(taxCategoryVo.getCode(), Constant.Number.TWO));
		param.setTaxpayerQual(Constant.Number.ZEROL);

		TaxRate insertData = new TaxRate();
		FastUtils.copyProperties(param, insertData);
		return taxRateMapper.insert(param);
	}

	/**
	 * @Description 更新税率
	 * @Author 郑勇浩
	 * @Data 2019/11/14 14:43
	 * @Param [param]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateTaxRate(TaxRateDto param) {
		// todo 权限校验
		//查询该数据状态
		TaxRateDto findParam = new TaxRateDto();
		findParam.setId(param.getId());
		TaxRateVo taxRate = taxRateService.findTaxRate(findParam);
		if (taxRate == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}
		//如果版本号不同
		if (!taxRate.getVersion().equals(param.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}
		//判断 删除状态
		if (taxRate.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//判断 发布状态
		if (taxRate.getIsReleased().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_RELEASED);
		}
		//判断 审核状态
		if (taxRate.getIsApproved().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_APPROVED);
		}

		//查询税收制度 税种
		findTaxSystem(param.getTaxSystemId());
		TaxCategoryVo taxCategoryVo = findTaxCategory(param.getTaxCategoryId());

		// 自动生成，等于税率（百分比）+ 税种的名称
		String taxRateName = String.format("%.2f", param.getTaxRate()) + PlatformConstant.TaxRate.PERCENT;
		param.setName(taxRateName + taxCategoryVo.getName());

		//更新
		TaxRate sqlParam = new TaxRate();
		sqlParam.setId(taxRate.getId());

		param.setId(null);
		param.setVersion(taxRate.getVersion());

		int result = taxRateMapper.update(param, new QueryWrapper<>(sqlParam));
		if (result < 1) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}
		param.setId(sqlParam.getId());
		//清除缓存
		RedisUtils.remove(PlatformConstant.RedisCache.TAX_RATE, param.getId());
		return result;
	}

	/**
	 * @Description 批量删除税率
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:12
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult deleteTaxRate(TaxRateDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DELETE);
	}

	/**
	 * @Description 批量审核税率
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:12
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult approvedTaxRate(TaxRateDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.APPROVED);
	}

	/**
	 * @Description 批量反审核税率
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:12
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult disapprovedTaxRate(TaxRateDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DISAPPROVED);
	}

	/**
	 * @Description 批量发布税率
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:13
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult releasedTaxRate(TaxRateDto param) {
		BatchResult batchResult = updateStatusBatch(param, PlatformConstant.OperateType.RELEASED);
		messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE, param.getMessageDto());
		return batchResult;
	}

	/**
	 * @Description 查询税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 18:03
	 * @Param [param]
	 * @return com.njwd.entity.platform.vo.TaxRateVo
	 */
	@Override
	@Cacheable(value = PlatformConstant.RedisCache.TAX_RATE, key = "#param.id", unless = "#result == null")
	public TaxRateVo findTaxRate(TaxRateDto param) {
		return taxRateMapper.findTaxRate(param);
	}

	/**
	 * @Description 查询税率[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:13
	 * @Param [param]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	@Override
	public Page<TaxRateVo> findTaxRatePage(TaxRateDto param) {
		Page<TaxRateVo> page = param.getPage();
		page = taxRateMapper.findTaxRatePage(page, param);
		return page;
	}

	/**
	 * @Description 查询税率[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:18
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	@Override
	public List<TaxRateVo> findTaxRateList(TaxRateDto param) {
		return taxRateMapper.findTaxRateList(param);
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:08
	 * @Param [response, param]
	 */
	@Override
	public void exportExcel(HttpServletResponse response, TaxRateDto param) {
		List<TaxRateVo> data = taxRateMapper.findTaxRateList(param);

		if (CollectionUtils.isEmpty(data)) {
			fileService.exportExcel(response, new ArrayList<>());
			return;
		}
		fileService.exportExcel(response, data,
				new ExcelColumn("code", "编码"),
				new ExcelColumn("name", "名称"),
				new ExcelColumn("taxRateStr", "税率"),
				new ExcelColumn("taxSystemName", "税收制度"),
				new ExcelColumn("taxCategoryName", "税种"),
				new ExcelColumn("isApprovedStr", "审核状态"),
				new ExcelColumn("isReleasedStr", "发布状态")
		);
	}

	/**
	 * @Description 查询税收制度
	 * @Author 郑勇浩
	 * @Data 2019/11/15 11:51
	 * @Param [id]
	 */
	private void findTaxSystem(Long id) {
		TaxSystemDto param = new TaxSystemDto();
		param.setId(id);
		TaxSystemVo taxSystemVo = taxSystemService.findTaxSystem(param);
		if (taxSystemVo == null) {
			throw new ServiceException(ResultCode.TAX_SYSTEM_NOT_EXISTS);
		}
		//未审核
		if (taxSystemVo.getIsApproved().equals(Constant.Is.NO)) {
			throw new ServiceException(ResultCode.TAX_SYSTEM_IS_UNAPPROVED);
		}
	}

	/**
	 * @Description 查询税种
	 * @Author 郑勇浩
	 * @Data 2019/11/15 11:51
	 * @Param [id]
	 * @return com.njwd.entity.platform.vo.TaxCategoryVo
	 */
	private TaxCategoryVo findTaxCategory(Long id) {
		TaxCategoryDto param = new TaxCategoryDto();
		param.setId(id);
		TaxCategoryVo taxCategoryVo = taxCategoryService.findTaxCategory(param);
		if (taxCategoryVo == null) {
			throw new ServiceException(ResultCode.TAX_CATEGORY_NOT_EXISTS);
		}
		//未发布
		if (taxCategoryVo.getIsReleased().equals(Constant.Is.NO)) {
			throw new ServiceException(ResultCode.TAX_CATEGORY_IS_UNRELEASED);
		}
		return taxCategoryVo;
	}

	/**
	 * @Description 批量操作
	 * @Author 郑勇浩
	 * @Data 2019/11/14 16:02
	 * @Param [param, type]
	 * @return com.njwd.support.BatchResult
	 */
	private BatchResult updateStatusBatch(TaxRateDto param, int type) {
		//初始化
		BatchResult result = new BatchResult();
		result.setFailList(new LinkedList<>());
		result.setSuccessList(new ArrayList<>());

		//查询待查询的所有数据的状态
		List<TaxRateVo> statusList = taxRateMapper.findTaxRateListStatus(param);
		//如果查询结果没有数据
		if (CollectionUtils.isEmpty(statusList)) {
			for (TaxRateVo data : param.getChangeList()) {
				addFailResult(result, data.getId(), ResultCode.RECORD_NOT_EXIST.message);
			}
			return result;
		}
		//转化为id为key的状态map
		Map<Long, TaxRateVo> statusMap = statusList.stream().collect(Collectors.toMap(TaxRateVo::getId, o -> o));

		//循环判断当前数据是否能添加
		TaxRateVo statusData;
		for (TaxRateVo changeData : param.getChangeList()) {
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

		//批量操作
		basePlatformMapper.batchProcess(result.getSuccessList(), type, UserUtil.getUserVo(), PlatformConstant.TableName.TAX_RATE);
		//清除成功修改的redis缓存
		RedisUtils.removeBatch(PlatformConstant.RedisCache.TAX_RATE, result.getSuccessList());
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
