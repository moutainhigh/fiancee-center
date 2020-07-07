package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.AccountingItemValueService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.logger.SenderService;
import com.njwd.service.FileService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 自定义核算项目大区值
 * @Author 薛永利
 * @Date 2019/6/21 8:57
 */
@RestController
@RequestMapping("accountingItemValue")
public class AccountingItemValueController extends BaseController {
    @Resource
    private AccountingItemValueService accountingItemValueService;
    @Resource
    private FileService fileService;
    @Resource
    private SenderService senderService;

    /**
     * @Description 新增自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/6/20 18:11
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @RequestMapping("addAccountingItemValue")
    public Result<Long> addAccountingItemValue(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        SysUserVo operator = UserUtils.getUserVo();
        if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.ACCOUNTING_ITEM_UPDATE, accountingItemValueDto.getCompanyId());
        }
        accountingItemValueDto.setCreatorId(operator.getUserId());
        accountingItemValueDto.setCreatorName(operator.getName());
        accountingItemValueDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Long id = accountingItemValueService.addAccountingItemValue(accountingItemValueDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItemValue,
                LogConstant.operation.add,
                LogConstant.operation.add_type, id.toString()));
        return ok(id);
    }
    /**
     * @Description 查看项目值时 校验项目值是否被引用
     * @Author 薛永利
     * @Date 2019/8/29 16:51
     * @Param [accountingItemValueDto]
     * @return com.njwd.support.Result<java.lang.Integer>
     */
    @RequestMapping("findItemValueIsReference")
    public  Result<Integer> findItemValueIsReference(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        int flag = accountingItemValueService.findItemValueIsReference(accountingItemValueDto);
        return ok(flag);
    }
    /**
     * @Description 根据IDS批量删除自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:30
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @RequestMapping("deleteAccountingItemValueByIds")
    public Result<BatchResult> deleteAccountingItemValueByIds(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        if (accountingItemValueDto.getIds().isEmpty() || accountingItemValueDto.getItemId() == null) {
            return error(ResultCode.PARAMS_NOT);
        }
        BatchResult batchResult = new BatchResult();
        if (accountingItemValueDto.getIds().size() == Constant.Number.ONE) {
            accountingItemValueDto.setId(accountingItemValueDto.getIds().get(0));
            batchResult = accountingItemValueService.deleteAccountingItemValueByIds(accountingItemValueDto);
        } else {
            batchResult = accountingItemValueService.deleteAccountingItemValueByIds(accountingItemValueDto);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItemValue,
                LogConstant.operation.deleteBatch,
                LogConstant.operation.deleteBatch_type, accountingItemValueDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 修改自定义核算项目大区值信息
     * @Author 薛永利
     * @Date 2019/6/20 18:11
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @RequestMapping("updateAccountingItemValue")
    public Result<Integer> updateAccountingItemValue(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        SysUserVo operator = UserUtils.getUserVo();
        if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.ACCOUNTING_ITEM_UPDATE, accountingItemValueDto.getCompanyId());
        }
        FastUtils.checkParams(accountingItemValueDto.getId());
        accountingItemValueDto.setUpdatorId(operator.getUserId());
        accountingItemValueDto.setUpdatorName(operator.getName());
        int flag = accountingItemValueService.updateAccountingItemValue(accountingItemValueDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItemValue,
                LogConstant.operation.update,
                LogConstant.operation.update_type, accountingItemValueDto.getId().toString()));
        return ok(flag);
    }

    /**
     * @Description 禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/6/20 18:12
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("disableAccountingItemValueBatch")
    public Result<BatchResult> disableBatch(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        SysUserVo operator = UserUtils.getUserVo();
        if (accountingItemValueDto.getIds().isEmpty()) {
            return error(ResultCode.PARAMS_NOT);
        }
        accountingItemValueDto.setUpdatorId(operator.getUserId());
        accountingItemValueDto.setUpdatorName(operator.getName());
        accountingItemValueDto.setIsEnable(Constant.Is.NO);
        BatchResult batchResult = new BatchResult();
        if (accountingItemValueDto.getIds().size() == Constant.Number.ONE) {
            accountingItemValueDto.setId(accountingItemValueDto.getIds().get(0));
            batchResult = accountingItemValueService.updateById(accountingItemValueDto);
        } else {
            batchResult = accountingItemValueService.updateBatch(accountingItemValueDto);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItemValue,
                LogConstant.operation.forbidden,
                LogConstant.operation.forbidden_type, accountingItemValueDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 反禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/6/20 18:12
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("enableAccountingItemValueBatch")
    public Result<BatchResult> enableBatch(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        SysUserVo operator = UserUtils.getUserVo();
        if (accountingItemValueDto.getIds().isEmpty()) {
            return error(ResultCode.PARAMS_NOT);
        }
        accountingItemValueDto.setUpdatorId(operator.getUserId());
        accountingItemValueDto.setUpdatorName(operator.getName());
        accountingItemValueDto.setIsEnable(Constant.Is.YES);
        BatchResult batchResult = new BatchResult();
        if (accountingItemValueDto.getIds().size() == Constant.Number.ONE) {
            accountingItemValueDto.setId(accountingItemValueDto.getIds().get(0));
            batchResult = accountingItemValueService.updateById(accountingItemValueDto);
        } else {
            batchResult = accountingItemValueService.updateBatch(accountingItemValueDto);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItemValue,
                LogConstant.operation.antiForbidden,
                LogConstant.operation.antiForbidden_type, accountingItemValueDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 查询自定义核算项目大区值列表 分页
     * @Author 薛永利
     * @Date 2019/6/20 18:13
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("findAccountingItemValueList")
    public Result<Page<AccountingItemValueVo>> findAccountingItemValueList(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        SysUserVo operator = UserUtils.getUserVo();
        if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
            accountingItemValueDto.setCompanyIds(ShiroUtils.filterPerm(Constant.MenuDefine.ACCOUNTING_ITEM_FIND,accountingItemValueDto.getCompanyIds()));
            accountingItemValueDto.getCompanyIds().add(Constant.AccountSubjectData.GROUP_ID);
        }
        accountingItemValueDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountingItemValueService.findAccountingItemValueList(accountingItemValueDto));
    }

    /**
     * @Description 根据id查自定义核算项目大区值详情
     * @Author 薛永利
     * @Date 2019/6/20 18:13
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("findAccountingItemValueById")
    public Result<AccountingItemValueVo> findById(@RequestBody AccountingItemValueDto accountingItemValueDto) {
        FastUtils.checkParams(accountingItemValueDto.getId());
        if (Constant.Is.NO.equals(accountingItemValueDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.ACCOUNTING_ITEM_FIND, accountingItemValueDto.getCompanyId());
        }
        return ok(accountingItemValueService.findById(accountingItemValueDto));
    }

    /**
     * @Description 导出自定义核算项目大区值excel
     * @Author 薛永利
     * @Date 2019/6/20 18:14
     * @Param [accountingItemValueDto, response]
     * @return void
     */
    @RequestMapping("exportAccountingItemValueExcel")
    public void exportAccountingItemValueExcel(@RequestBody AccountingItemValueDto accountingItemValueDto, HttpServletResponse response) {
        if((Constant.Is.NO).equals(accountingItemValueDto.getIsEnterpriseAdmin())){
            accountingItemValueDto.setCompanyIds(ShiroUtils.filterPerm(Constant.MenuDefine.DEPT_EXPORT,accountingItemValueDto.getCompanyIds()));
        }
        accountingItemValueService.exportExcel(accountingItemValueDto, response);
    }

    /**
     * @Description 下载自定义核算项目大区值模板
     * @Author 薛永利
     * @Date 2019/6/20 18:14
     * @Param []
     * @return org.springframework.http.ResponseEntity
     */
    @RequestMapping("downloadAccountingItemValueTemplate")
    public ResponseEntity downloadAccountingItemValueTemplate() throws Exception {
        return fileService.downloadExcelTemplate("accounting_item_value");
    }

    /**
     * @Description 查询所有未删除的自定义核算
     * @Author wuweiming
     * @Param [AccountingItemValueDto]
     * @return Result<List<AccountingItemValueVo>>
     */
    @RequestMapping("findAllAccountItemValueByItemId")
    public Result<List<AccountingItemValueVo>> findAllAccountItemValueByItemId(@RequestBody AccountingItemValueDto dto) {
        return ok(accountingItemValueService.findAllAccountItemValueByItemId(dto));
    }

    /**
     * @Description 查询所有未删除的辅助核算
     * @Author wuweiming
     * @Param [AccountingItemValueDto]
     * @return Result<List<AccountingItemValueVo>>
     **/
    @PostMapping("findAllAuxiliaryItemValue")
    public Result<List<AccountingItemValueVo>> findAllAuxiliaryItemValue(@RequestBody AccountingItemValueDto dto){
        return ok(accountingItemValueService.findAllAuxiliaryItemValue(dto));
    }
}
