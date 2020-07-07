package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.CurrencyFeignClient;
import com.njwd.basedata.mapper.CurrencyMapper;
import com.njwd.basedata.service.CurrencyService;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.Currency;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.CurrencyDto;
import com.njwd.entity.basedata.vo.CurrencyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 币种 service Impl.
 * @Date 2019/11/27 15:51
 * @Author 郑勇浩
 */
@Service
public class CurrencyServiceImpl implements CurrencyService {

	@Resource
	private CurrencyMapper currencyMapper;
	@Resource
	private FileService fileService;
	@Resource
	private CurrencyService currencyService;
	@Resource
	private CurrencyFeignClient currencyFeignClient;

	/**
	 * @Description 批量添加币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 15:52
	 * @Param [param]
	 * @return int
	 */
	@Override
	public int addCurrencyBatch(CurrencyDto param) {
		//用户信息
		SysUserVo operator = UserUtils.getUserVo();
		for (CurrencyVo currencyVo : param.getChangeList()) {
			FastUtils.checkParams(currencyVo.getId(), currencyVo.getCode(), currencyVo.getName(), currencyVo.getIsoCode(), currencyVo.getSymbol(),
					currencyVo.getPrecision(), currencyVo.getUnitPrecision(), currencyVo.getRoundingType());
			currencyVo.setPlatformId(currencyVo.getId());
			currencyVo.setId(null);
			currencyVo.setCreatorName(operator.getCreatorName());
			currencyVo.setCreateTime(operator.getCreateTime());
			currencyVo.setCreatorId(operator.getCreatorId());
			currencyVo.setRootEnterpriseId(operator.getRootEnterpriseId());
			currencyVo.setIsApproved(Constant.Is.YES);
			currencyVo.setIsReleased(Constant.Is.YES);
			currencyVo.setVersion(Constant.Number.ZERO);

		}
		return currencyMapper.addBatch(param.getChangeList());
	}

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/27 16:38
	 * @Param [param]
	 * @return com.njwd.entity.basedata.vo.CurrencyVo
	 */
	@Override
	public int updateCurrency(CurrencyDto param) {
		CurrencyVo currency = currencyService.findCurrency(param);
		if (currency == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}
		//如果版本号不同
		if (!currency.getVersion().equals(param.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}
		//判断 删除状态
		if (currency.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//todo 检查当前币种有无期初数据或凭证数据

		//更新
		Currency sqlParam = new Currency();
		sqlParam.setId(currency.getId());

		param.setId(null);
		param.setVersion(currency.getVersion());

		int result = currencyMapper.update(param, new QueryWrapper<>(sqlParam));
		if (result < 1) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}
		param.setId(sqlParam.getId());
		//todo 清除缓存
		return result;
	}

	/**
	 * @Description 批量删除币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 16:35
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	public BatchResult deleteCurrencyBatch(CurrencyDto param) {
		return updateStatusBatch(Constant.OperateType.DELETE, param);
	}

	/**
	 * @Description 批量启用币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 16:35
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	public BatchResult enableCurrencyBatch(CurrencyDto param) {
		return updateStatusBatch(Constant.OperateType.ENABLE, param);
	}

	/**
	 * @Description 批量禁用币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 16:35
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	public BatchResult disableCurrencyBatch(CurrencyDto param) {
		return updateStatusBatch(Constant.OperateType.DISABLE, param);
	}

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/27 16:38
	 * @Param [param]
	 * @return com.njwd.entity.basedata.vo.CurrencyVo
	 */
	@Override
	public CurrencyVo findCurrency(CurrencyDto param) {
		return currencyMapper.findCurrency(param);
	}

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/27 17:32
	 * @Param [param]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CurrencyVo>
	 */
	@Override
	public Page<CurrencyVo> findCurrencyPage(CurrencyDto param) {
		Page<CurrencyVo> page = param.getPage();
		page = currencyMapper.findCurrencyPage(page, param);
		return page;
	}

	/**
	 * @Description 查询平台币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/28 11:12
	 * @Param [param]
	 * @return com.njwd.support.Result
	 */
	@Override
	public Result findPlatformCurrencyList(CurrencyDto param) {
		//查询已有的平台id列表
		SysUserVo operator = UserUtils.getUserVo();
		param.setRootEnterpriseId(operator.getRootEnterpriseId());
		List<Long> platformIds = currencyMapper.findCurrencyPlatformIds(param);
		if (CollectionUtils.isEmpty(platformIds)) {
			platformIds = null;
		}
		//查询平台币种列表
		com.njwd.entity.platform.dto.CurrencyDto platformParam = new com.njwd.entity.platform.dto.CurrencyDto();
		platformParam.setCodeOrName(param.getCodeOrName());
		platformParam.setPlatformIds(platformIds);
		return currencyFeignClient.findCurrencyList(platformParam);
	}

	/**
	 * @Description Excel 导出
	 * @Author 郑勇浩
	 * @Data 2019/12/3 15:59
	 * @Param [response, currencyDto]
	 */
	@Override
	public void exportExcel(HttpServletResponse response, CurrencyDto param) {
		Page<CurrencyVo> page = new Page<>();
		fileService.resetPage(page);
		Page<CurrencyVo> currencyVoPage = currencyMapper.findCurrencyPage(page, param);
		if (CollectionUtils.isEmpty(currencyVoPage.getRecords())) {
			fileService.exportExcel(response, currencyVoPage.getRecords());
		}
		//不同身份导出不同表格
		fileService.exportExcel(response, currencyVoPage.getRecords(), MenuCodeConstant.CURRENCY);
	}

	/**
	 * @Description 批量更新状态
	 * @Author 郑勇浩
	 * @Data 2019/11/29 9:25
	 * @Param [param, type]
	 * @return com.njwd.support.BatchResult
	 */
	private BatchResult updateStatusBatch(int type, CurrencyDto param) {
		//初始化
		BatchResult result = new BatchResult();
		result.setFailList(new LinkedList<>());
		result.setSuccessList(new ArrayList<>());

		//查询待查询的所有数据的状态
		List<CurrencyVo> statusList = currencyMapper.findCurrencyListStatus(param);
		//如果查询结果没有数据
		if (CollectionUtils.isEmpty(statusList)) {
			for (CurrencyVo data : param.getChangeList()) {
				addFailResult(result, data.getId(), ResultCode.RECORD_NOT_EXIST.message);
			}
			return result;
		}
		//转化为id为key的状态map
		Map<Long, CurrencyVo> statusMap = statusList.stream().collect(Collectors.toMap(CurrencyVo::getId, o -> o));

		//循环判断当前数据是否能添加
		CurrencyVo statusData;
		for (CurrencyVo changeData : param.getChangeList()) {
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

			//判断 删除状态
			if (statusData.getIsDel().equals(Constant.Is.YES)) {
				addFailResult(result, changeData.getId(), ResultCode.IS_DEL.message);
				continue;
			}

			//判断反禁用 禁用状态
			if (type == Constant.OperateType.ENABLE && Constant.Is.YES.equals(statusData.getIsEnable())) {
				addFailResult(result, statusData.getId(), ResultCode.IS_ENABLE.message);
				continue;
			} else if (type == Constant.OperateType.DISABLE && Constant.Is.NO.equals(statusData.getIsEnable())) {
				addFailResult(result, statusData.getId(), ResultCode.IS_DISABLE.message);
				continue;
			}

			//判断当前币种是否被核算主体作为记账本位币使用】
			if (type == Constant.OperateType.DELETE || type == Constant.OperateType.DISABLE) {
				if (statusData.getAccountBookCount() > Constant.Number.ZERO || statusData.getAccountSubjectCount() > Constant.Number.ZERO) {
					addFailResult(result, statusData.getId(), ResultCode.DELETE_FAIL_DATA_USED.message);
					continue;
				}
			}
			result.getSuccessList().add(statusData.getId());
		}

		if (result.getSuccessList().size() == 0) {
			return result;
		}

		//批量操作
		currencyMapper.batchProcess(result.getSuccessList(), type, UserUtils.getUserVo());
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
