package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.AccountingItemService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.AccountingItemDto;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.logger.SenderService;
import com.njwd.service.FileService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
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
@RequestMapping("accountingItem")
public class AccountingItemController extends BaseController {

    @Resource
    private AccountingItemService accountingItemService;
    @Resource
    private FileService fileService;
    @Resource
    private SenderService senderService;

    /**
     * @Description 新增自定义核算项目
     * @Author 薛永利
     * @Date 2019/6/20 18:15
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @RequestMapping("addAccountingItem")
    public Result<Long> addAccountingItem(@RequestBody AccountingItemDto accountingItemDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemDto.setCreatorId(operator.getUserId());
        accountingItemDto.setCreatorName(operator.getName());
        accountingItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Long id = accountingItemService.addAccountingItem(accountingItemDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItem,
                LogConstant.operation.add,
                LogConstant.operation.add_type, id.toString()));
        return ok(id);
    }
    /**
     * @Description 查询项目是否有区值
     * @Author 薛永利
     * @Date 2019/8/21 9:37
     * @Param [accountingItemDto]
     * @return java.util.List<java.lang.Long>
     */
    @RequestMapping("findItemRelaValueById")
    public Result<List> findItemRelaValueById(@RequestBody AccountingItemDto accountingItemDto){
        return ok(accountingItemService.findItemRelaValueById(accountingItemDto));
    }
    /**
     * @Description 查看项目时 校验项目是否被引用
     * @Author 薛永利
     * @Date 2019/8/29 16:25
     * @Param [accountingItemDto]
     * @return int
     */
    @RequestMapping("findItemIsReference")
    public Result<Integer>  findItemIsReference(@RequestBody AccountingItemDto accountingItemDto) {
        int flag = accountingItemService.findItemIsReference(accountingItemDto);
        return ok(flag);
    }
    /**
     * @Description 根据IDS批量删除自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:29
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @RequestMapping("deleteAccountingItemByIds")
    public Result<BatchResult> deleteAccountingItemByIds(@RequestBody AccountingItemDto accountingItemDto) {
        if (accountingItemDto.getIds().isEmpty()) {
            return error(ResultCode.PARAMS_NOT);
        }
        BatchResult batchResult = new BatchResult();
        if (accountingItemDto.getIds().size() == Constant.Number.ONE) {
            accountingItemDto.setId(accountingItemDto.getIds().get(0));
            batchResult = accountingItemService.deleteAccountingItemById(accountingItemDto);
        } else {
            batchResult = accountingItemService.deleteAccountingItemByIds(accountingItemDto);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItem,
                LogConstant.operation.deleteBatch,
                LogConstant.operation.deleteBatch_type, accountingItemDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 修改自定义核算项目信息
     * @Author 薛永利
     * @Date 2019/7/9 10:29
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @RequestMapping("updateAccountingItem")
    public Result<Integer> updateAccountingItem(@RequestBody AccountingItemDto accountingItemDto) {
        FastUtils.checkParams(accountingItemDto.getId());
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemDto.setUpdatorId(operator.getUserId());
        accountingItemDto.setUpdatorName(operator.getName());
        int flag = accountingItemService.updateAccountingItem(accountingItemDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItem,
                LogConstant.operation.update,
                LogConstant.operation.update_type, accountingItemDto.getId().toString()));
        return ok(flag);
    }

    /**
     * @Description 禁用自定义核算项目
     * @Author 薛永利
     * @Date 2019/6/21 8:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @PostMapping("disableAccountingItemBatch")
    public Result<BatchResult> disableBatch(@RequestBody AccountingItemDto accountingItemDto) {
        if (accountingItemDto.getIds().isEmpty()) {
            return error(ResultCode.PARAMS_NOT);
        }
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemDto.setUpdatorId(operator.getUserId());
        accountingItemDto.setUpdatorName(operator.getName());
        accountingItemDto.setIsEnable(Constant.Is.NO);
        BatchResult batchResult = new BatchResult();
        if (accountingItemDto.getIds().size() == Constant.Number.ONE) {
            accountingItemDto.setId(accountingItemDto.getIds().get(0));
            batchResult = accountingItemService.updateById(accountingItemDto);
        } else {
            batchResult = accountingItemService.updateBatch(accountingItemDto);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItem,
                LogConstant.operation.forbidden,
                LogConstant.operation.forbidden_type, accountingItemDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 反禁用自定义核算项目
     * @Author 薛永利
     * @Date 2019/6/21 8:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @PostMapping("enableAccountingItemBatch")
    public Result<BatchResult> enableBatch(@RequestBody AccountingItemDto accountingItemDto) {
        if (accountingItemDto.getIds().isEmpty()) {
            return error(ResultCode.PARAMS_NOT);
        }
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemDto.setUpdatorId(operator.getUserId());
        accountingItemDto.setUpdatorName(operator.getName());
        accountingItemDto.setIsEnable(Constant.Is.YES);
        BatchResult batchResult = new BatchResult();
        if (accountingItemDto.getIds().size() == Constant.Number.ONE) {
            accountingItemDto.setId(accountingItemDto.getIds().get(0));
            batchResult = accountingItemService.updateById(accountingItemDto);
        } else {
            batchResult = accountingItemService.updateBatch(accountingItemDto);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.customerAccoutingItem,
                LogConstant.operation.antiForbidden,
                LogConstant.operation.antiForbidden_type, accountingItemDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 查询自定义核算项目列表 分页
     * @Author 薛永利
     * @Date 2019/6/21 8:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @PostMapping("findAccountingItemList")
    public Result<Page<AccountingItemVo>> findAccountingItemList(@RequestBody AccountingItemDto accountingItemDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountingItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountingItemService.findAccountingItemList(accountingItemDto));
    }

    /**
     * @Description 根据id查自定义核算项目详情
     * @Author 薛永利
     * @Date 2019/6/20 17:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @RequestMapping("findAccountingItemById")
    public Result<AccountingItemVo> findById(@RequestBody AccountingItemDto accountingItemDto) {
        FastUtils.checkParams(accountingItemDto.getId());
        return ok(accountingItemService.findById(accountingItemDto));
    }

    /**
     * @Description 导出自定义核算项目excel
     * @Author 薛永利          ``
     * @Date 2019/6/21 8:54
     * @Param [accountingItemDto, response]
     * @return void
     */
    @RequestMapping("exportAccountingItemExcel")
    public void exportAccountingItemExcel(@RequestBody AccountingItemDto accountingItemDto, HttpServletResponse response) {
        accountingItemService.exportExcel(accountingItemDto, response);
    }

    /**
     * @Description 下载自定义核算项目模板
     * @Author 薛永利
     * @Date 2019/6/21 8:54
     * @Param []
     * @return org.springframework.http.ResponseEntity
     */
    @RequestMapping("downloadAccountingItemTemplate")
    public ResponseEntity downloadAccountingItemTemplate() throws Exception {
        return fileService.downloadExcelTemplate("accounting_item");
    }

}
