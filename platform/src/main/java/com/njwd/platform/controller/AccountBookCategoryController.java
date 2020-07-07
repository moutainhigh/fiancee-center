package com.njwd.platform.controller;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.AccountBookCategory;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.vo.AccountBookCatVo;
import com.njwd.entity.platform.vo.AccountBookCategoryVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.AccountBookCategoryService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 账簿分类
 * @Date:16:50 2019/6/24
 **/
@RestController
@RequestMapping("accountbookcategory")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class AccountBookCategoryController extends BaseController {

    @Autowired
    private AccountBookCategoryService accountBookCategoryService;
    @Resource
    private SenderService senderService;

    /**
     * @Description 根据账簿类型ID和会计准则ID查询账簿分类
     * @Author liuxiang
     * @Date:15:02 2019/7/2
     * @Param [accountBookCategoryVo]
     * @return java.lang.String
     **/
    @PostMapping("findAccBoListByAccTypeAndStand")
    public Result<List<AccountBookCategoryVo>> findAccBoListByAccTypeAndStand(@RequestBody AccountBookCategoryDto accountBookCategoryDto) {
        return ok(accountBookCategoryService.findAccBoListByAccTypeAndStand(accountBookCategoryDto));
    }

    /**
     * @Description 根据账簿类型ID和租户ID查询账簿分类
     * @Author lj
     * @Date:9:05 2019/7/12
     * @Param [accountBookCategoryDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccBookListByTypeAndEntId")
    public Result<List<AccountBookCatVo>> findAccBookListByTypeAndEntId(@RequestBody AccountBookCategoryDto accountBookCategoryDto) {
        return ok(accountBookCategoryService.findAccBookListByTypeAndEntId(accountBookCategoryDto));
    }

    /**
     * 刘遵通
     * 增加核算账簿分类
     * @param accountBookCategoryDto
     * @return
     */
    @RequestMapping("addAccountBookCategory")
    public Result<Long> addAccountBookCategory(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        FastUtils.checkParams(accountBookCategoryDto.getCode(),accountBookCategoryDto.getAccStandardId(),
                accountBookCategoryDto.getSubjectId(),accountBookCategoryDto.getAccCalendarId(),accountBookCategoryDto.getTypeCode());
        Long id = accountBookCategoryService.addAccountBookCategory(accountBookCategoryDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountBookCategory,
                LogConstant.operation.add, LogConstant.operation.add_type, null));
        return ok(id);
    }
    /** 刘遵通
     * 删除核算账簿分类
     * @param accountBookCategoryDto
     * @return
     */
    @RequestMapping("deleteAccountBookCategory")
    public Result<BatchResult> deleteAccountBookCategory(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        //获取参数集合
        List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
         for(AccountBookCategoryDto accountBookCategory : editList){
             FastUtils.checkParams(accountBookCategory.getId(),accountBookCategory.getVersion());
         }
        BatchResult batchResult = accountBookCategoryService.deleteAccountBookCategory(accountBookCategoryDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountBookCategory,
                LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, null));
         return ok(batchResult);
    }

    /**
     * 刘遵通
     * 审核
     * @param accountBookCategoryDto
     * @return
     */
    @RequestMapping("checkApprove")
    public Result<BatchResult> checkApprove(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        //获取参数集合
        List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(AccountBookCategoryDto accountBookCategory : editList){
            FastUtils.checkParams(accountBookCategory.getId(),accountBookCategory.getVersion());
        }
        BatchResult batchResult = accountBookCategoryService.checkApprove(accountBookCategoryDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountBookCategory,
                LogConstant.operation.approve, LogConstant.operation.approve_type, null));
        return ok(batchResult);
    }
    /**
     * 刘遵通
     * 反审核
     * @param accountBookCategoryDto
     * @return
     */
    @RequestMapping("reversalApprove")
    public Result<BatchResult> reversalApprove(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        //获取参数集合
        List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(AccountBookCategoryDto accountBookCategory : editList){
            FastUtils.checkParams(accountBookCategory.getId(),accountBookCategory.getVersion());
        }
        BatchResult batchResult = accountBookCategoryService.reversalApprove(accountBookCategoryDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountBookCategory,
                LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, null));
        return ok(batchResult);
    }
    /** 刘遵通
     * 查询页面 （分页）
     * @param accountBookCategoryDto
     * @return
     */
    @RequestMapping("findPage")
    public Result<Page<AccountBookCategoryVo>> findPage(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        Page<AccountBookCategoryVo> accBoList = accountBookCategoryService.findPage(accountBookCategoryDto);
        return ok(accBoList);
    }

    /**
     * 导出
     * @param accountBookCategoryDto
     * @param response
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody AccountBookCategoryDto accountBookCategoryDto, HttpServletResponse response){
        accountBookCategoryService.exportExcel(accountBookCategoryDto,response);
    }

    /**
     * 编辑中的查看
     * @param accountBookCategoryDto
     * @return
     */
    @RequestMapping("selectById")
    public Result<AccountBookCategory> selectById(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        AccountBookCategory accountBookCategory = accountBookCategoryService.selectById(accountBookCategoryDto);
        return  ok(accountBookCategory);
    }

    /**
     * 修改
     * @return
     */
    @RequestMapping("updateById")
    public Result<Long> updateById(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        FastUtils.checkParams(accountBookCategoryDto.getCode(),accountBookCategoryDto.getAccStandardId(),
                accountBookCategoryDto.getSubjectId(),accountBookCategoryDto.getAccCalendarId(),
                accountBookCategoryDto.getTypeCode(),accountBookCategoryDto.getId());
        Long id = accountBookCategoryService.updateById(accountBookCategoryDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountBookCategory,
                LogConstant.operation.update, LogConstant.operation.update_type, null));
        return ok(id);
    }

    /**
     * @description: 获取账簿分类，初始化数据
     * @param: [accountBookCategoryDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.AccountBookCategoryVo>> 
     * @author: xdy        
     * @create: 2019-11-15 09:41 
     */
    @RequestMapping("findAccountBookCategoryList")
    public Result<List<AccountBookCategoryVo>> findAccountBookCategoryList(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        return ok(accountBookCategoryService.findAccountBookCategoryList(accountBookCategoryDto));
    }

    /**
     * 刘遵通
     * 发布
     * @param accountBookCategoryDto
     * @return
     */
    @RequestMapping("release")
    public Result<BatchResult> release(@RequestBody AccountBookCategoryDto accountBookCategoryDto){
        //获取参数集合
        List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(AccountBookCategoryDto accountBookCategory : editList){
            FastUtils.checkParams(accountBookCategory.getId(),accountBookCategory.getVersion());
        }
        BatchResult batchResult = accountBookCategoryService.release(accountBookCategoryDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountBookCategory,
                LogConstant.operation.release, LogConstant.operation.release_type, null));
        return ok(batchResult);
    }

}
