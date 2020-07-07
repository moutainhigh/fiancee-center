package com.njwd.platform.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.CommonAbstract;
import com.njwd.entity.platform.dto.CommonAbstractDto;
import com.njwd.entity.platform.vo.CommonAbstractVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.CommonAbstractMapper;
import com.njwd.platform.service.CommonAbstractService;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.StringUtil;
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
 * @Description 常用摘要 service impl
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Service
public class CommonAbstractServiceImpl implements CommonAbstractService {

	@Resource
	private CommonAbstractMapper commonAbstractMapper;
	@Resource
	private BasePlatformMapper basePlatformMapper;
	@Resource
	private CommonAbstractService commonAbstractService;
	@Resource
	private SequenceService sequenceService;
	@Resource
	private FileService fileService;
	@Resource
	private MessageService messageService;

	/**
	 * @Description 新增常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:45
	 * @Param [commonAbstractDto]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addCommonAbstract(CommonAbstractDto param) {
		// todo 权限校验
		// 重复性校验
		checkData(param);

		param.setRootEnterpriseId(Constant.Number.ZEROL);
		param.setCreateEnterpriseId(Constant.Number.ZEROL);
		param.setUseEnterpriseId(Constant.Number.ZEROL);
		param.setVersion(Constant.Number.ZERO);
		// 编码规则为ZY+3位流水号；
		param.setCode(sequenceService.getCode(PlatformConstant.PlatformCodeRule.COMMON_ABSTRACT, Constant.Number.THREE));
		CommonAbstract insertData = new CommonAbstract();
		FastUtils.copyProperties(param, insertData);
		return commonAbstractMapper.insert(param);
	}

	/**
	 * @Description 更新常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 13:55
	 * @Param [commonAbstractDto]
	 * @return long
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateCommonAbstract(CommonAbstractDto param) {
		// todo 权限校验
		//查询该数据状态
		CommonAbstractDto findParam = new CommonAbstractDto();
		findParam.setId(param.getId());
		CommonAbstractVo commonAbstract = commonAbstractService.findCommonAbstract(findParam);
		if (commonAbstract == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}
		//如果版本号不同
		if (!commonAbstract.getVersion().equals(param.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}
		//判断 删除状态
		if (commonAbstract.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//判断 发布状态
		if (commonAbstract.getIsReleased().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_RELEASED);
		}
		//判断 审核状态
		if (commonAbstract.getIsApproved().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_APPROVED);
		}
		// 重复性校验
		checkData(param);

		//更新
		CommonAbstract sqlParam = new CommonAbstract();
		sqlParam.setId(commonAbstract.getId());

		param.setId(null);
		param.setVersion(commonAbstract.getVersion());

		int result = commonAbstractMapper.update(param, new QueryWrapper<>(sqlParam));
		if (result < 1) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}
		param.setId(sqlParam.getId());
		//清除缓存
		RedisUtils.remove(PlatformConstant.RedisCache.COMMON_ABSTRACT, param.getId());
		return result;
	}

	/**
	 * @Description 批量删除常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:14
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult deleteCommonAbstract(CommonAbstractDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DELETE);
	}

	/**
	 * @Description 批量审核常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:10
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult approvedCommonAbstract(CommonAbstractDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.APPROVED);
	}

	/**
	 * @Description 批量反审核常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:10
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult disapprovedCommonAbstract(CommonAbstractDto param) {
		return updateStatusBatch(param, PlatformConstant.OperateType.DISAPPROVED);
	}

	/**
	 * @Description 批量发布常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:10
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public BatchResult releasedCommonAbstract(CommonAbstractDto param) {
		BatchResult batchResult = updateStatusBatch(param, PlatformConstant.OperateType.RELEASED);
		messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE, param.getMessageDto());
		return batchResult;
	}

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:25
	 * @Param [commonAbstractDto]
	 * @return com.njwd.entity.platform.vo.CommonAbstractVo
	 */
	@Override
	@Cacheable(value = PlatformConstant.RedisCache.COMMON_ABSTRACT, key = "#param.id", unless = "#result == null")
	public CommonAbstractVo findCommonAbstract(CommonAbstractDto param) {
		return commonAbstractMapper.findCommonAbstract(param);
	}

	/**
	 * @Description 查询常用摘要[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:24
	 * @Param [commonAbstractDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CommonAbstractVo>
	 */
	@Override
	public Page<CommonAbstractVo> findCommonAbstractPage(CommonAbstractDto param) {
		Page<CommonAbstractVo> page = param.getPage();
		page = commonAbstractMapper.findCommonAbstractPage(page, param);
		return page;
	}

	/**
	 * @Description 查询常用摘要[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:32
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.CommonAbstractVo>
	 */
	@Override
	public List<CommonAbstractVo> findCommonAbstractList(CommonAbstractDto param) {
		return commonAbstractMapper.findCommonAbstractList(param);
	}

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:08
	 * @Param [response, param]
	 */
	@Override
	public void exportExcel(HttpServletResponse response, CommonAbstractDto param) {
		List<CommonAbstractVo> data = commonAbstractMapper.findCommonAbstractList(param);

		if (CollectionUtils.isEmpty(data)) {
			fileService.exportExcel(response, new ArrayList<>());
			return;
		}
		fileService.exportExcel(response, data,
				new ExcelColumn("code", "编码"),
				new ExcelColumn("abstractContent", "摘要内容"),
				new ExcelColumn("abstractCode", "助记码"),
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
	private void checkData(CommonAbstractDto param) {
		// 重复性校验
		CommonAbstractDto checkParam = new CommonAbstractDto();
		checkParam.setAbstractContent(param.getAbstractContent());
		checkParam.setAbstractCode(param.getAbstractCode());
		if (param.getId() != null) {
			checkParam.setId(param.getId());
		}
		CommonAbstractVo data = commonAbstractMapper.findExistCommonAbstract(checkParam);
		//如果存在数据
		if (data == null) {
			return;
		}
		//校验摘要内容
		if (data.getAbstractContent().equals(checkParam.getAbstractContent())) {
			throw new ServiceException(ResultCode.ABSTRACT_CONTENT_EXISTS);
		}
		//校验助记码
		if (StringUtil.isNotEmpty(data.getAbstractCode()) && data.getAbstractCode().equals(checkParam.getAbstractCode())) {
			throw new ServiceException(ResultCode.ABSTRACT_CODE_EXISTS);
		}

	}

	/**
	 * @Description 批量操作
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:16
	 * @Param [type, commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	private BatchResult updateStatusBatch(CommonAbstractDto param, int type) {
		//初始化
		BatchResult result = new BatchResult();
		result.setFailList(new LinkedList<>());
		result.setSuccessList(new ArrayList<>());

		//查询待查询的所有数据的状态
		List<CommonAbstractVo> statusList = commonAbstractMapper.findCommonAbstractListStatus(param);
		//如果查询结果没有数据
		if (CollectionUtils.isEmpty(statusList)) {
			for (CommonAbstractVo data : param.getChangeList()) {
				addFailResult(result, data.getId(), ResultCode.RECORD_NOT_EXIST.message);
			}
			return result;
		}
		//转化为id为key的状态map
		Map<Long, CommonAbstractVo> statusMap = statusList.stream().collect(Collectors.toMap(CommonAbstractVo::getId, o -> o));

		//循环判断当前数据是否能添加
		CommonAbstractVo statusData;
		for (CommonAbstractVo changeData : param.getChangeList()) {
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
		basePlatformMapper.batchProcess(result.getSuccessList(), type, UserUtil.getUserVo(), PlatformConstant.TableName.COMMON_ABSTRACT);
		//清除成功修改的redis缓存
		RedisUtils.removeBatch(PlatformConstant.RedisCache.COMMON_ABSTRACT, result.getSuccessList());
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
