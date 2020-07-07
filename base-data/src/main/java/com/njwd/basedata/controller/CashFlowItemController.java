package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.*;
import com.njwd.basedata.service.AccountSubjectService;
import com.njwd.basedata.service.CashFlowItemService;
import com.njwd.basedata.service.CashFlowItemTemplateService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.excel.ExcelResult;
import com.njwd.entity.basedata.vo.CashFlowItemTempleteVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.*;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.service.FileService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description 现金流量项目Controller类
 * @Author admin
 * @Date 2019/6/11 17:38
 */
@RestController
@RequestMapping("cashFlowItem")
public class CashFlowItemController extends BaseController {
    @Autowired
    private CashFlowItemService cashFlowItemService;

    @Autowired
    private CashFlowItemTemplateService cashFlowItemTemplateService;
    @Autowired
    private SenderService senderService;

    @Autowired
    private FileService fileService;

    @Autowired
    private CashFlowFeignClient cashFlowFeignClient;
    @Autowired
    private CashFlowItemFeignClient cashFlowItemFeignClient;

    @Autowired
    private AccountingStandardFeignClient accountingStandardFeignClient;

    @Autowired
    private AccountBookTypeFeignClient accountBookTypeFeignClient;

    @Autowired
	private CashFlowItemLedgerFeignClient cashFlowItemLedgerFeignClient;

	@Autowired
	private AccountSubjectService accountSubjectService;




    /**
     * @Author Libao
     * @Description 现金流量项目新增下级
     * @Date  2019/6/12 17:46
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("addCashFlowItem")
    public Result<CashFlowItemVo> addCashFlowItem(@RequestBody CashFlowItemDto cashFlowItemDto) {
		FastUtils.checkParams(cashFlowItemDto.getCashFlowId(),cashFlowItemDto.getCode(),cashFlowItemDto.getName(),cashFlowItemDto.getLevel(),
				             cashFlowItemDto.getMaxLevel(),cashFlowItemDto.getUpCode(),cashFlowItemDto.getUpVersion(),cashFlowItemDto.getUpId());
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		//校验版本号(因为service有别的地方调用，这里校验写在controller层)
		CashFlowItemDto cashFlowItemDtoUp = new CashFlowItemDto();
		cashFlowItemDtoUp.setRootEnterpriseId(operator.getRootEnterpriseId());
		cashFlowItemDtoUp.setId(cashFlowItemDto.getUpId());
		cashFlowItemDtoUp.setVersion(cashFlowItemDto.getUpVersion());
		cashFlowItemService.checkVersion(cashFlowItemDtoUp);
		/*if (Constant.Is.NO.equals(cashFlowItemDto.getIsEnterpriseAdmin())) {
			ShiroUtils.checkPerm(Constant.MenuDefine.CASH_FLOW_ITEM_ADD, cashFlowItemDto.getCompanyId());
		} else {
			ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
		}*/
        //addFlag新增结果标识
        CashFlowItemVo resultVo = cashFlowItemService.addCashFlowItem(cashFlowItemDto);
        /*//将新增成功的Id加到Dto
        cashFlowItemDto.setId(resultVo.getId());
        CashFlowItemVo cashFlowItemVo = cashFlowItemService.findCashFlowItemById(cashFlowItemDto);
		cashFlowItemVo.setUpId(resultVo.getUpId());*/
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.cashFlowItems,
                LogConstant.operation.add,
                LogConstant.operation.add_type,resultVo.getId().toString()));
        return ok(resultVo);

    }


    /**
     * @Author Libao
     * @Description 导入平台获取现金流量数据
     * @Date  2019/6/19 10:38
     * @Param
     * @return
     */
    @RequestMapping("addCashFlowItemTempleteData")
    public Result<Boolean> addCashFlowItemTempleteData(@RequestBody com.njwd.entity.platform.dto.CashFlowItemDto platformCashFlowItemDto) {
        FastUtils.checkParams(platformCashFlowItemDto.getCashFlowId());
        int result = addCashFlowItemTempleteDataInfo(platformCashFlowItemDto);
        return confirm(result);


    }
    /**
     * @Author Libao
     * @Description 预置数据，模板导入
     * @Date  2019/8/29 16:17
     * @Param [platformCashFlowItemDto]
     * @return int
     */
    public int addCashFlowItemTempleteDataInfo(com.njwd.entity.platform.dto.CashFlowItemDto platformCashFlowItemDto) {
        //获取运营平台现金流量项目模板
        Result<List<CashFlowItemVo>> cashFlowItemString = cashFlowItemFeignClient.findCashFlowItemList(platformCashFlowItemDto);


        List<CashFlowItemVo> list = cashFlowItemString.getData();
        List<CashFlowItemDto> cashFlowItemDtos = new ArrayList<>();
        for (CashFlowItemVo  cashFlowItemVo: list) {
        	CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
            FastUtils.copyProperties(cashFlowItemVo,cashFlowItemDto);
			cashFlowItemDtos.add(cashFlowItemDto);

        }
        if (list == null || list.isEmpty()) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_NO_DATA);
        }
        //flag:平台模板数据导入结果标识
        int flag = cashFlowItemService.addCashFlowItemBatch(cashFlowItemDtos);
        senderService.sendLog(UserUtils.getUserLogInfo(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.cashFlowItems,
                LogConstant.operation.addBatch,
                LogConstant.operation.addBatch_type));
        return flag;
    }
    /**
     * @Author Libao
     * @Description 预置数据接口
     * @Date  2019/8/29 16:18
     * @Param []
     * @return com.njwd.support.Result<java.lang.Boolean>
     */
    @RequestMapping("addInitData")
    public Result<Boolean> addInitData() {
        SysUserVo operator = UserUtils.getUserVo();
        FastUtils.checkParams(operator.getRootEnterpriseId());
        int count = cashFlowItemService.findCount(operator.getRootEnterpriseId());
        if (count > 0) {
            return ok();
        }
        com.njwd.entity.platform.dto.CashFlowItemDto platformCashFlowItemDto = new com.njwd.entity.platform.dto.CashFlowItemDto();
        platformCashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        int result = addCashFlowItemTempleteDataInfo(platformCashFlowItemDto);
        return confirm(result);

    }

    /**
     * @Author Libao
     * @Description 根据Id 删除现金流量项目（逻辑删除）
     * @Date  2019/6/12 17:53
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("delCashFlowItemById")
    public Result<Boolean> delCashFlowItemById(@RequestBody CashFlowItemDto cashFlowItemDto){
        //判断Id是否为空
        FastUtils.checkParams(cashFlowItemDto.getId(),
				              cashFlowItemDto.getVersion(),
				              cashFlowItemDto.getCashFlowId(),
							  cashFlowItemDto.getIsInit(),
				 			  cashFlowItemDto.getIsInteriorContact(),
				              cashFlowItemDto.getUpCode());
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
       //delFlag删除结果标识
        int delFlag = cashFlowItemService.delCashFlowItemById(cashFlowItemDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.cashFlowItems,
                LogConstant.operation.delete,
                LogConstant.operation.delete_type,cashFlowItemDto.getId().toString()));
     return confirm(delFlag);
    }

    /**
     * @Author Libao
     * @Description 批量删除（逻辑删除）
     * @Date  2019/6/14 17:26
     */
    @RequestMapping("delBatch")
    public  Result<BatchResult> delBatch(@RequestBody CashFlowItemDto cashFlowItemDto){
        //判断必传参数是否为空
       FastUtils.checkParams(cashFlowItemDto.getIds(),cashFlowItemDto.getCashFlowId(),cashFlowItemDto.getVersions());
        //delResult：批量删除结果描述
        BatchResult delResult = cashFlowItemService.delBatch(cashFlowItemDto);
        //获取日志Id
		if (delResult.getSuccessList() != null && delResult.getSuccessList().size() > 0){
        String logId = delResult.getSuccessList().toString();
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.cashFlowItems,
                LogConstant.operation.delete,
                LogConstant.operation.delete_type,logId));
		}
        return ok(delResult);
    }

   /**
    * @Author Libao
    * @Description 根据Id修改现金流量项目数据状态，禁用 (批量禁用)、已失效
    * @Date  2019/6/13 10:50
    * @Param [cashFlowItemDto]
    * @return java.lang.String
    */
    @RequestMapping("updateOrBatchDisable")
    public Result<BatchResult> updateOrBatchDisable(@RequestBody CashFlowItemDto cashFlowItemDto){
        //判断必传参数是否为空
		FastUtils.checkParams(cashFlowItemDto.getIds(),cashFlowItemDto.getCashFlowId(),cashFlowItemDto.getVersions());
        //flag：禁用标识
         byte flag = Constant.Is.NO;
        //禁用、批量禁用、反禁用、批量反禁用，操作结果cashFlowItemVo
		BatchResult batchResult = cashFlowItemService.updateOrBatch(cashFlowItemDto,flag);
		if (batchResult.getSuccessDetailsList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
					LogConstant.sysName.FinanceBackSys,
					LogConstant.menuName.cashFlowItems,
					LogConstant.operation.forbidden,
					LogConstant.operation.forbidden_type, cashFlowItemDto.getIds().toString()));
		}
        return ok(batchResult);
    }

    /**
     * @Author Libao
     * @Description 根据Id修改现金流量项目
     * @Date  2019/6/26 10:52
     * @Param
     * @return
     */

    @RequestMapping("updateCashFlowItemById")
    public Result<CashFlowItemVo> updateCashFlowItemById(@RequestBody CashFlowItemDto cashFlowItemDto){
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        //判断Id是否为空
        FastUtils.checkParams(cashFlowItemDto.getId(),
				              cashFlowItemDto.getIsInit(),
				              cashFlowItemDto.getOldName(),
				              cashFlowItemDto.getName(),
				              cashFlowItemDto.getOldCode(),
				              cashFlowItemDto.getCode(),
				              cashFlowItemDto.getVersion(),
				              cashFlowItemDto.getUpCode(),
				              cashFlowItemDto.getOldUpCode());
		Long  logId = cashFlowItemService.updateCashFlowItemById(cashFlowItemDto);
        //将新增成功的Id加到Dto
        cashFlowItemDto.setId(logId);
        CashFlowItemVo cashFlowItemVo = cashFlowItemService.findCashFlowItemById(cashFlowItemDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.cashFlowItems,
                LogConstant.operation.update,
                LogConstant.operation.update_type,logId.toString()));
		return ok(cashFlowItemVo);
    }

    /**
     * @Author Libao
     * @Description 根据Id修改现金流量项目数据状态，反禁用 （批量反禁用）、已生效
     * @Date  2019/6/13 10:50
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("updateOrBatchEnable")
    public Result<BatchResult> updateOrBatchEnable(@RequestBody CashFlowItemDto cashFlowItemDto){
        //判断必传参数是否为空
		FastUtils.checkParams(cashFlowItemDto.getIds(),cashFlowItemDto.getCashFlowId(),cashFlowItemDto.getVersions());
        //修改数据状态,  0：禁用，1：启用
        byte flag = Constant.Is.YES;
        //反禁用、批量反禁用操作结果标识
		BatchResult batchResult = cashFlowItemService.updateOrBatch(cashFlowItemDto,flag);
		if (batchResult.getSuccessDetailsList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
					LogConstant.sysName.FinanceBackSys,
					LogConstant.menuName.cashFlowItems,
					LogConstant.operation.antiForbidden,
					LogConstant.operation.antiForbidden_type, cashFlowItemDto.getIds().toString()));
		}
		return ok(batchResult);
	}

    /**
     * @Author Libao
     * @Description 查询分组
     * @Date  2019/6/18 13:53
     * @return
     */
    @RequestMapping("findGroup")
    public Result<List<CashFlowItemVo>> findGroup(@RequestBody CashFlowItemDto cashFlowItemDto) {
        List<CashFlowItemVo> cashFlowItemVoList = cashFlowItemService.findGroup(cashFlowItemDto);
        return ok(cashFlowItemVoList);
    }

    /**
     * @Author Libao
     * @Description 根据Id查询现金流量项目
     * @Date  2019/6/12 17:50
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("findCashFlowItemById")
    public Result<CashFlowItemVo> findCashFlowItemById(@RequestBody CashFlowItemDto cashFlowItemDto){
        //判断Id是否为空
        FastUtils.checkParams(cashFlowItemDto.getId());
        return ok(cashFlowItemService.findCashFlowItemById(cashFlowItemDto));
    }



    /**
     * @Author Libao
     * @Description 从平台获取现金流量模板集合
     * @Date  2019/6/19 10:38
     * @Param
     * @return
     */
    @RequestMapping("findCashFlowItemSelection")
    public Result<List<CashFlowItemTempleteVo>> findCashFlowItemSelect(@RequestBody CashFlowDto platformCashFlowDto ) {
        //获取运营平台现金流量项目模板
        Result<List<CashFlowVo>> result = cashFlowFeignClient.findCashFlowList(platformCashFlowDto);
        List<CashFlowVo> cashFlowItemTempleteVos = result.getData();
		List<CashFlowItemTempleteVo> cashFlowItemDtos = new ArrayList<>();
		for (CashFlowVo  cashFlowVo: cashFlowItemTempleteVos) {
			CashFlowItemTempleteVo cashFlowItemTempleteVo = new CashFlowItemTempleteVo();
			FastUtils.copyProperties(cashFlowVo,cashFlowItemTempleteVo);
			cashFlowItemDtos.add(cashFlowItemTempleteVo);
		}
        return ok(cashFlowItemDtos);
    }


    /**
     * 账簿类型下拉框.
     *
     * @param
     * @return java.lang.String
     * @author 李宝
     * @date 2019/6/25
     */
    @PostMapping("findAccountBookTypeSelection")
    public Result<List<AccountBookTypeVo>> findAccountBookTypeSelection() {
        //获取运营平台账簿类型选项
        return accountBookTypeFeignClient.findAccountBookTypeList();
    }

    /**
     * 会计准则下拉框.
     *
     * @param
     * @return java.lang.String
     * @author 李宝
     * @date 2019/6/25
     */
    @PostMapping("findAccountingStandardSelection")
    public Result<List<AccountingStandardVo>> findAccountingStandardSelection() {
        //获取运营平台会计准则选项
        return accountingStandardFeignClient.findAccountingList();
    }

    /**
     * @Author Libao
     * @Description 分页展示平台获取现金流量数据
     * @Date  2019/6/19 10:38
     * @Param platformCashFlowItemDto
     * @return jsonstr
     */
    @RequestMapping("findCashFlowItemTempleteByPage")
    public Result findCashFlowItemTempleteByPage(@RequestBody CashFlowItemDto platformCashFlowItemDto) {
        //获取运营平台现金流量项目分页数据
        return cashFlowItemFeignClient.findCashFlowItemPage(platformCashFlowItemDto);
    }

    /**
     * @Author Libao
     * @Description 获取现金流量项目列表，分页
     * @Date  2019/6/11 17:57
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     **/
    @RequestMapping("findCashFlowItemByPage")
    public Result<Page<CashFlowItemVo>> findCashFlowItemByPage(@RequestBody CashFlowItemDto cashFlowItemDto) {
		SysUserVo operator = UserUtils.getUserVo();
		cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		//校验user端用户权限
		if (cashFlowItemDto.getIsEnterpriseAdmin() != null && Constant.Is.NO.equals(cashFlowItemDto.getIsEnterpriseAdmin()) &&
			cashFlowItemDto.getCompanyId() != null && !Constant.CashFlowItemData.GROUP_ID.equals(cashFlowItemDto.getCompanyId())) {
			Boolean perm = ShiroUtils.hasPerm(Constant.MenuDefine.CASH_FLOW_ITEM_EXPORT, cashFlowItemDto.getCompanyId());
			if (!perm){
				return ok();
			}
		}
		if (cashFlowItemDto.getAccountBookTypeId() != null && cashFlowItemDto.getAccStandardId() != null) {
			CashFlow cashFlow = cashFlowItemTemplateService.findCashFlowItemTemplateIdByParam(cashFlowItemDto);
			if (cashFlow != null) {
				cashFlowItemDto.setCashFlowId(cashFlow.getId());
			}
		}
		return ok(cashFlowItemService.findPage(cashFlowItemDto));
    }
    /**
     * @Author Libao
     * @Description 查询现金流量项目数据（提供外部）
     * @Date 2019/6/11 17:57
     * @param cashFlowItemDto
     * @return java.lang.String
     */
    @RequestMapping("findCashFlowItemListByTemplateCashFlowId")
    public Result<List<CashFlowItemVo>> findCashFlowItemList(@RequestBody CashFlowItemDto cashFlowItemDto) {
        SysUserVo operator = UserUtils.getUserVo();
        cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        //判断cashFlowId是否为空
        FastUtils.checkParams(cashFlowItemDto.getCashFlowId());
        List<CashFlowItemVo> cashFlowItemVoList = cashFlowItemService.findCashFlowItemList(cashFlowItemDto);
        return ok(cashFlowItemVoList);
    }

    /**
     * @param cashFlowItemDto
     * @return java.lang.String
     * @Author Libao
	 * @Description 查询现金流量表Id
     * @Date 2019/6/11 17:57
     */
    @RequestMapping("findCashFlowItemTemplateId")
    public Result<CashFlow> findCashFlowItemTemplateId(@RequestBody CashFlowItemDto cashFlowItemDto) {
        SysUserVo operator = UserUtils.getUserVo();
        cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        CashFlow cashFlow = cashFlowItemTemplateService.findCashFlowItemTemplateId(cashFlowItemDto);
        return ok(cashFlow);
    }
    /**
     * @Author Libao
     * @Description 查询现金流量项目信息，用于总账报表拼接
     * @Date  2019/8/6 16:50
     * @Param []
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>>
     */
    @RequestMapping("findCashFlowItemForReport")
    public Result<List<CashFlowItemVo>> findCashFlowItemForReport(@RequestBody CashFlowItemDto cashFlowItemDto){
        List<CashFlowItemVo> list = cashFlowItemService.findCashFlowItemForReport(cashFlowItemDto);
        return ok(list);
    }

    /**
     * @Author Libao
     * @Description 查询现金流量项目信息，用于查询上级是否有禁用数据
     * @Date  2019/8/6 16:50
     * @Param []
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>>
     */
    @RequestMapping("findCashFlowItemForEnable")
    public Result<List<CashFlowItemVo>> findCashFlowItemForEnable(@RequestBody List<CashFlowItemDto> cashFlowItemDtos){
        List<CashFlowItemVo> list = cashFlowItemService.findCashFlowItemForEnable(cashFlowItemDtos);
        return ok(list);
    }

	/**
	 * @Author Libao
	 * @Description 根据code查询所有下级现金流量信息，包含本身
	 * @Date  2019/8/6 16:50
	 * @Param [cashFlowItemDto]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CashFlowItemVo>
	 */
	@RequestMapping("findCashFlowItemIdForReport")
	public Result<List<CashFlowItemVo>> findCashFlowItemIdForReport(@RequestBody CashFlowItemDto cashFlowItemDto){
		List<CashFlowItemVo> cashFlowItemVoList = cashFlowItemService.findCashFlowItemIdForReport(cashFlowItemDto);
		return ok(cashFlowItemVoList);
	}

	/**
	 * @Author Libao
	 * @Description 根据Id查询未使用下级
	 * @Date  2019/9/4 11:44
	 * @Param [cashFlowItemDto]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CashFlowItemVo>
	 */
	@RequestMapping("findCashFlowItemCodeForAdd")
    public Result<CashFlowItemVo> findCashFlowItemCodeForAdd(@RequestBody CashFlowItemDto cashFlowItemDto){
		//判断cashFlowId是否为空
		FastUtils.checkParams(cashFlowItemDto.getId());
	    CashFlowItemVo cashFlowItemVo = cashFlowItemService.findCashFlowItemCodeForAdd(cashFlowItemDto);
	    return ok(cashFlowItemVo);
    }

    /**
     * @Author Libao
     * @Description 查询被使用现金流量项目Id
     * @Date  2019/9/6 9:30
     * @Param [accountSubjectDto]
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CashFlowItemVo>
     */
	@RequestMapping("findCashFlowItemHasUsed")
    public Result<CashFlowItemVo> findCashFlowItemHasUsed(@RequestBody AccountSubjectDto accountSubjectDto){
		SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());

		//判断cashFlowId是否为空
		FastUtils.checkParams(accountSubjectDto.getCashFlowId());
        List<AccountSubjectVo> accountSubjectVos = accountSubjectService.findSubjectInfoByParam(accountSubjectDto);
        List<Long> itemIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(accountSubjectVos)){
             for (AccountSubjectVo accountSubjectVo : accountSubjectVos){
             	if (accountSubjectVo.getCashInflowId() != null){
					itemIds.add(accountSubjectVo.getCashInflowId());
				}
			 }
			for (AccountSubjectVo accountSubjectVo : accountSubjectVos){
				if (accountSubjectVo.getCashOutflowId() != null){
					itemIds.add(accountSubjectVo.getCashInflowId());
				}
			}
		}

         CashFlowItemVo cashFlowItemVo = new CashFlowItemVo();
         Result<BalanceCashFlowVo> result =  cashFlowItemLedgerFeignClient.findCashFlowItemUsed();
		 BalanceCashFlowVo balanceCashFlowVo = result.getData();

        if (CollectionUtils.isEmpty(itemIds) && balanceCashFlowVo == null){
			cashFlowItemVo.setBatchIds(itemIds);
        	return ok(cashFlowItemVo);
		}
        if (CollectionUtils.isNotEmpty(itemIds) && balanceCashFlowVo != null){
        	itemIds.addAll(balanceCashFlowVo.getIds());
			itemIds = itemIds.stream().distinct().collect(Collectors.toList());
			cashFlowItemVo.setBatchIds(itemIds);
			return ok(cashFlowItemVo);
		}
        if (CollectionUtils.isNotEmpty(itemIds) ){
			itemIds = itemIds.stream().distinct().collect(Collectors.toList());
			cashFlowItemVo.setBatchIds(itemIds);
			return ok(cashFlowItemVo);
		}
		cashFlowItemVo.setBatchIds(balanceCashFlowVo.getIds());
        return ok(cashFlowItemVo);
	}

	/**
	 * @Author Libao
	 * @Description 根据upcode查询上级Id
	 * @Date  2019/9/6 9:33
	 * @Param [cashFlowItemDto]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CashFlowItemVo>
	 */
	@RequestMapping("findUpCashFlowItemByCode")
	public Result<CashFlowItemVo> findUpCashFlowItemByCode(@RequestBody CashFlowItemDto cashFlowItemDto){
		CashFlowItemVo cashFlowItemVo = cashFlowItemService.findUpCashFlowItemByCode(cashFlowItemDto);
		return ok(cashFlowItemVo);
	}

    /**
     * @Author Libao
     * @Description导出现金流量项目数据
     * @Date 2019/6/11 17:57
     * @param cashFlowItemDto
     * @param response
     */
    @RequestMapping("exportCashFlowItemExcel")
    public Result<Boolean> exportCashFlowItemExcel(@RequestBody CashFlowItemDto cashFlowItemDto, HttpServletResponse response) {
		Boolean perm = true;
		//校验用户是否登录
		UserUtils.getUserVo();
		//校验user端用户权限
		if (cashFlowItemDto.getIsEnterpriseAdmin() != null && Constant.Is.NO.equals(cashFlowItemDto.getIsEnterpriseAdmin()) &&
		   cashFlowItemDto.getCompanyId() != null && !Constant.CashFlowItemData.GROUP_ID.equals(cashFlowItemDto.getCompanyId())) {
			 perm = ShiroUtils.hasPerm(Constant.MenuDefine.CASH_FLOW_ITEM_EXPORT, cashFlowItemDto.getCompanyId());
		}
		if (perm){
			cashFlowItemService.exportExcel(cashFlowItemDto,response);
		}
		return ok(perm);
    }
    /**
     * @Author Libao
     * @Description 判断数据是否被引用
     * @Date  2019/7/9 11:51
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("checkIsUsed")
    public Result checkIsUsed(@RequestBody CashFlowItemDto cashFlowItemDto){
        SysUserVo operator = UserUtils.getUserVo();
            cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Boolean flag = cashFlowItemService.checkIsUsed(cashFlowItemDto);
        if (!flag) {
            return ok(ResultCode.CASH_FLOW_ITEM_IS_CITED);
        }
        return ok();
    }

    /**
     * @Author Libao
     * @Description 判断项目是否被引用
     * @Date  2019/9/6 9:30
     * @Param [cashFlowItemDto]
     * @return com.njwd.support.Result<com.njwd.entity.basedata.ReferenceResult>
     */
    @RequestMapping("checkIsUsedSingle")
    public Result<ReferenceResult> checkIsUsedSingle(@RequestBody CashFlowItemDto cashFlowItemDto) {
        SysUserVo operator = UserUtils.getUserVo();
        cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        ReferenceResult referenceResult = cashFlowItemService.checkIsUsedSingle(cashFlowItemDto);
        return ok(referenceResult);
    }


    /**
     *@Description 下载基础资料模板
     *@Author Libao
     *@Date 2019/6/17
     *@Param []
     *@return ResponseEntity
     */
    @RequestMapping("downloadTemplate")
    public ResponseEntity downloadTemplate()throws Exception{
        return fileService.downloadExcelTemplate(Constant.Reference.CASH_FLOW_ITEM);
    }

    /**
     *@Author Libao
     *@Description 上传并校验excel
     *@Param [MultipartFile]
     *@Date 2019/6/17
     *@return String
     */
    @RequestMapping("uploadAndCheckExcelData")
    public Result<ExcelResult> uploadAndCheckExcelData(@RequestParam(value = "file") MultipartFile file, String cashFlowId) {
        Map paramMap = new HashMap();
        paramMap.put("cashFlowId", cashFlowId);
        return ok(fileService.uploadAndCheckExcel(file, Constant.Reference.CASH_FLOW_ITEM, paramMap));
    }

    /**
     *@Description 导入数据
     *@Author Libao
     *@Date 2019/6/17
     *@Param [Map]
     *@return String
     */
    @RequestMapping("importCashFlowItemData")
    public Result<ExcelResult> importCashFlowItemData(@RequestBody Map paramMap){
        String uuid = "";
        if (paramMap.get(Constant.CashFlowItemData.UUID) != null) {
            uuid = paramMap.get(Constant.CashFlowItemData.UUID).toString();
        }else {
            throw  new ServiceException(ResultCode.PARAMS_NOT);
        }
        return ok(fileService.importExcel(uuid));
    }

    /**
     * 查询现金流量项目列表-平台增量数据
     * @Author lj
     * @Date:16:56 2019/12/2
     * @param cashFlowItemDto
     * @return com.njwd.support.Result
     **/
    @RequestMapping("findPlatformCashFlowItemList")
    public Result<List<CashFlowItemVo>> findPlatformCashFlowItemList(@RequestBody CashFlowItemDto cashFlowItemDto) {
        return ok(cashFlowItemService.findPlatformCashFlowItemList(cashFlowItemDto));
    }

   /**
    * 引入现金流量
    * @Author lj
    * @Date:16:50 2019/12/3
    * @param cashFlowItemDto
    * @return com.njwd.support.Result<java.lang.Boolean>
    **/
    @RequestMapping("addCashFlowItemData")
    public Result<Integer> addCashFlowItemData(@RequestBody CashFlowItemDto cashFlowItemDto) {
        List<CashFlowItemVo> list= cashFlowItemService.findPlatformCashFlowItemList(cashFlowItemDto);
        List<CashFlowItemDto> cashFlowItemDtos = new ArrayList<>();
        for (CashFlowItemVo  cashFlowItemVo: list) {
            CashFlowItemDto itemDto = new CashFlowItemDto();
            FastUtils.copyProperties(cashFlowItemVo,itemDto);
            itemDto.setImportFlag(true);
            cashFlowItemDtos.add(itemDto);
        }
        //引入现金流量项目必须将现金流量项目的上级和下级一起引入
        //去除已存在的数据
        Iterator<CashFlowItemDto> iterator = cashFlowItemDtos.iterator();
        while (iterator.hasNext()) {
            String tempCode = iterator.next().getCode();
            for(String code:cashFlowItemDto.getCodes()) {
                if(!tempCode.startsWith(code)&&!code.startsWith(tempCode)){
                    iterator.remove();
                }
            }
        }
        return ok(cashFlowItemService.addCashFlowItemBatch(cashFlowItemDtos));
    }

    /**
     * @Author Libao
     * @Description 获取日志Id
     * @Date  2019/7/1 9:41
     * @Param [cashFlowItemDtos]
     * @return java.lang.String
     */
    private String getLogId(List<CashFlowItemDto> cashFlowItemDtos){
        List<Long> logIdList = new ArrayList<>();
        if (null != cashFlowItemDtos && cashFlowItemDtos.size() > 0){
            for (CashFlowItemDto cashFlowItemDto:cashFlowItemDtos) {
                logIdList.add(cashFlowItemDto.getId());
            }
        }else{
            return "";
        }
      return logIdList.toString();
    }

    /**
     * @Author 李宝
     * @Description 校验 必传参数
     * @Date 2019/7/1 9:41
     * @param cashFlowItemDtos 必传参数
     */
    private void checkCashFlowItemParams(List<CashFlowItemDto> cashFlowItemDtos) {
        if (cashFlowItemDtos == null || cashFlowItemDtos.isEmpty()) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        for (CashFlowItemDto cashFlowItemDto : cashFlowItemDtos) {
            if (cashFlowItemDto == null || cashFlowItemDto.getId() == null) {
                throw new ServiceException(ResultCode.PARAMS_NOT);
            }
        }
    }
}
