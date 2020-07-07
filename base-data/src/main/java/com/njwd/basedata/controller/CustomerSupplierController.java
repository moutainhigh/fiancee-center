package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.CustomerSupplierService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.CustomerSupplierVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 基础资料的客户和供应商控制层.
 * @author 朱小明
 * @since 2019/6/11
 */
@RestController
@RequestMapping("customerSupplier")
public class CustomerSupplierController extends BaseController {

    @Resource
    private CustomerSupplierService customerSupplierService;
    @Resource
    private SenderService senderService;

    /**
     * @Description 新增客户供应商.
     * @Author 朱小明
     * @Date 2019/6/12 18:01
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("addCustomerSupplier")
    public Result<Long> addCustomerSupplier(@RequestBody CustomerSupplierDto customerSupplierDto) {
        //鉴权
        checkPerm(customerSupplierDto);
        //非空校验
        FastUtils.checkParams(customerSupplierDto.getName(),customerSupplierDto.getIsEnterpriseAdmin(),customerSupplierDto.getDataType());
        //两者  必传一个
        FastUtils.checkParamsForOr(customerSupplierDto.getCustomerType(),customerSupplierDto.getSupplierType());
        Long id = customerSupplierService.addCustomerSupplier(customerSupplierDto);
        return ok(id);
    }

    /**
     * @Description 批量新增客户供应商.
     * @Author wuweiming
     * @Date 2019/8/29
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("batchAddCustomerSupplier")
    public Result<List<Long>> batchAddCustomerSupplier(@RequestBody CustomerSupplierDto customerSupplierDto) {

        //非空校验
        FastUtils.checkParams(customerSupplierDto.getIsEnterpriseAdmin(),customerSupplierDto.getDataType(),
                customerSupplierDto.getNames(),customerSupplierDto.getCodes(),customerSupplierDto.getDataTypeList());

        List<Long> ids = new ArrayList<>();
        CustomerSupplierDto dto = null;
        //遍历信息  新增
        for (int i=0;i<customerSupplierDto.getNames().size();i++){
            dto = customerSupplierDto;
            dto.setInnerCompanyId(customerSupplierDto.getInnerCompanyIds().get(i));
            dto.setCode(customerSupplierDto.getCodes().get(i));
            dto.setName(customerSupplierDto.getNames().get(i));
            dto.setDataTypes(customerSupplierDto.getDataTypeList().get(i));
            Long id = customerSupplierService.batchAddCustomerSupplier(dto);
            ids.add(id);
        }
        return ok(ids);
    }

    /**
     * @Description 批量删除
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("deleteCustomerSupplierByIds")
    public Result<BatchResult> deleteCustomerSupplierByIds(@RequestBody CustomerSupplierDto customerSupplierDto) {
        //非空校验
        FastUtils.checkParams(customerSupplierDto.getIdS(),customerSupplierDto.getIsEnterpriseAdmin(),customerSupplierDto.getVersions());

        //补参处理：用户信息
        customerSupplierDto.setUpdatorName(UserUtils.getUserVo().getName());
        customerSupplierDto.setUpdatorId(UserUtils.getUserVo().getUserId());
        customerSupplierDto.setDeleteName(UserUtils.getUserVo().getName());
        customerSupplierDto.setDeleteId(UserUtils.getUserVo().getUserId());
        customerSupplierDto.setIsDel(Constant.Is.YES);
        //判断是否为业务管理员
        BatchResult batchResult = customerSupplierService.updateBatchDelete(customerSupplierDto);
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())){
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.customer,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, customerSupplierDto.getIdS().toString()));
        } else {
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.supplier,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, customerSupplierDto.getIdS().toString()));
        }

        return ok(batchResult);
    }

    /**
     * @Description 根据ID更新客户供应商
     * @Author 朱小明
     * @Date 2019/6/19 15:54
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("updateCustomerSupplierById")
    public Result<Integer> updateCustomerSupplierById(@RequestBody CustomerSupplierDto customerSupplierDto) {
        //鉴权
        //checkPerm(customerSupplierDto);

        //非空校验
        FastUtils.checkParams(customerSupplierDto.getId(),customerSupplierDto.getIsEnterpriseAdmin(),customerSupplierDto.getVersion());

        //补参处理：用户信息
        customerSupplierDto.setUpdatorName(UserUtils.getUserVo().getName());
        customerSupplierDto.setUpdatorId(UserUtils.getUserVo().getUserId());
        customerSupplierDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        //判断是否为业务管理员
        if (Constant.Is.YES.equals(customerSupplierDto.getIsEnterpriseAdmin())){
            customerSupplierService.updateCustomerSupplierById(customerSupplierDto);
        } else {
            customerSupplierService.updateCustomerSupplierCoById(customerSupplierDto);
        }

        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())){
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.customer,
                    LogConstant.operation.update, LogConstant.operation.update_type, customerSupplierDto.getId().toString()));
        } else {
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.supplier,
                    LogConstant.operation.update, LogConstant.operation.update_type, customerSupplierDto.getId().toString()));
        }

        return ok();
    }

    /**
     * 客户 or 供应商 升级操作
     * wuweiming
     * @param customerSupplierDto
     * @return
     */
    @PostMapping("upGradeById")
    public Result<Integer> upGradeById(@RequestBody CustomerSupplierDto customerSupplierDto){
        //非空校验
        FastUtils.checkParams(customerSupplierDto.getId(),customerSupplierDto.getVersion());

        Integer version = customerSupplierService.upGradeById(customerSupplierDto);

        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())){
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.customer,
                    LogConstant.operation.update, LogConstant.operation.update_type, customerSupplierDto.getId().toString()));
        } else {
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.supplier,
                    LogConstant.operation.update, LogConstant.operation.update_type, customerSupplierDto.getId().toString()));
        }
        return ok(version);
    }

    /**
     * @Description 批量启用
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("enableCustomerSupplierByIds")
    public Result<BatchResult> enableCustomerSupplierByIds(@RequestBody CustomerSupplierDto customerSupplierDto) {
        //非空校验
        FastUtils.checkParams(customerSupplierDto.getIdS(),customerSupplierDto.getIsEnterpriseAdmin(),customerSupplierDto.getVersions());

        //补参处理：用户信息
        customerSupplierDto.setUpdatorName(UserUtils.getUserVo().getName());
        customerSupplierDto.setUpdatorId(UserUtils.getUserVo().getUserId());
        customerSupplierDto.setIsEnable(Constant.Is.YES);
        //判断是否为业务管理员
        BatchResult batchResult = customerSupplierService.updateBatchEnable(customerSupplierDto);
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())){
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.customer,
                    LogConstant.operation.forbiddenBatch, LogConstant.operation.forbiddenBatch_type, batchResult.getSuccessList().toString()));
        } else {
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.supplier,
                    LogConstant.operation.forbiddenBatch, LogConstant.operation.forbiddenBatch_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * @Description 批量禁用
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("disableCustomerSupplierByIds")
    public Result<BatchResult> disableCustomerSupplierByIds(@RequestBody CustomerSupplierDto customerSupplierDto) {
        //非空校验
        FastUtils.checkParams(customerSupplierDto.getIdS(),customerSupplierDto.getIsEnterpriseAdmin(),customerSupplierDto.getVersions());

        //补参处理：用户信息
        customerSupplierDto.setUpdatorName(UserUtils.getUserVo().getName());
        customerSupplierDto.setUpdatorId(UserUtils.getUserVo().getUserId());
        customerSupplierDto.setIsEnable(Constant.Is.NO);
        //判断是否为业务管理员
        BatchResult batchResult = customerSupplierService.updateBatchEnable(customerSupplierDto);
        if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())){
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.customer,
                    LogConstant.operation.antiForbiddenBatch, LogConstant.operation.antiForbiddenBatch_type, batchResult.getSuccessList().toString()));
        } else {
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.supplier,
                    LogConstant.operation.antiForbiddenBatch, LogConstant.operation.antiForbiddenBatch_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * @description: 批量分配客户供应商使用公司
     * @Param [CustomerSupplierDto]
     * @return com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author wuweiming
     * @date 2019/8/24 16:02
     */
    @PostMapping("updateBatchCustomerSupplierUseCompany")
    public Result<BatchResult> updateBatchCustomerSupplierUseCompany(@RequestBody CustomerSupplierDto dto){
        //非空校验
        FastUtils.checkParams(dto.getIdS(),dto.getCompanyList(),dto.getDataType());

        BatchResult batchResult = new BatchResult();

        if (dto.getDataType().equals(Constant.CustomerSupplier.CUSTOMER)){
            //批量分配客户
            batchResult = customerSupplierService.updateBatchCustomerUseCompany(dto);

            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.customer,
                    LogConstant.operation.update, LogConstant.operation.update_type,batchResult.getSuccessList().toString()));
        }else if (dto.getDataType().equals(Constant.CustomerSupplier.SUPPLIER)){
            //批量分配供应商
            batchResult = customerSupplierService.updateBatchSupplierUseCompany(dto);

            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.supplier,
                    LogConstant.operation.update, LogConstant.operation.update_type,batchResult.getSuccessList().toString()));
        }

        return ok(batchResult);
    }

    /**
     * @description: 取消分配客户供应商使用公司
     * @Param [CustomerSupplierDto]
     * @return com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author wuweiming
     * @date 2019/8/24 16:02
     */
    @PostMapping("cancelBatchCustomerSupplierUseCompany")
    public Result<BatchResult> cancelBatchCustomerSupplierUseCompany(@RequestBody CustomerSupplierDto dto){
        //非空校验
        FastUtils.checkParams(dto.getIdS(),dto.getCompanyList(),dto.getDataType(),dto.getVersions());

        BatchResult batchResult = new BatchResult();

        if (dto.getDataType().equals(Constant.CustomerSupplier.CUSTOMER)){
            //批量取消分配客户
            batchResult = customerSupplierService.cancelBatchCustomerUseCompany(dto);

            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.customer,
                    LogConstant.operation.update, LogConstant.operation.update_type,batchResult.getSuccessList().toString()));
        }else if (dto.getDataType().equals(Constant.CustomerSupplier.SUPPLIER)){
            //批量取消分配供应商
            batchResult = customerSupplierService.cancelBatchSupplierUseCompany(dto);

            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.supplier,
                    LogConstant.operation.update, LogConstant.operation.update_type,batchResult.getSuccessList().toString()));
        }

        return ok(batchResult);
    }

    /**
     * @Description 根据id查询客户供应商信息
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("findCustomerSupplierById")
    public Result<CustomerSupplierVo> findCustomerSupplierById(@RequestBody CustomerSupplierDto customerSupplierDto) {
        FastUtils.checkParams(customerSupplierDto.getId(),customerSupplierDto.getIsEnterpriseAdmin(),customerSupplierDto.getDataType());
        UserUtils.getUserVo().getRootEnterpriseId();
        //判断是否为业务管理员
        if (Constant.Is.YES.equals(customerSupplierDto.getIsEnterpriseAdmin())){
            if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
                return ok(customerSupplierService.findCustomerById(customerSupplierDto.getId()));
            } else {
                return ok(customerSupplierService.findSupplierById(customerSupplierDto.getId()));
            }
        } else {
            if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())) {
                return ok(customerSupplierService.findCustomerCoById(customerSupplierDto.getId()));
            } else {
                return ok(customerSupplierService.findSupplierCoById(customerSupplierDto.getId()));
            }

        }

    }

    /**
     * @Description 查询列表（分页）
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("findCustomerSupplierPage")
    public Result<Page<CustomerSupplierVo>> findCustomerSupplierPage(@RequestBody CustomerSupplierDto customerSupplierDto) {
        FastUtils.checkParams(customerSupplierDto.getIsEnterpriseAdmin());
        customerSupplierDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Page<CustomerSupplierVo> list;
        //判断是否为租户管理员(ADMIN画面)
        if (Constant.Is.YES.equals(customerSupplierDto.getIsEnterpriseAdmin())){
            list = customerSupplierService.findCustomerSupplierPage(customerSupplierDto);
        } else {
            list = customerSupplierService.findCustomerSupplierCoPage(customerSupplierDto);
        }
        return ok(list);
    }

    /**
     * @Description 校验字段重复
     * @Author 朱小明
     * @Date 2019/6/12 18:01
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("checkColumn")
    public Result<Set<String>> checkColumn(@RequestBody CustomerSupplierDto customerSupplierDto) {
        FastUtils.checkParamsForOr(customerSupplierDto.getName(),customerSupplierDto.getUnifiedSocialCreditCode()
                ,customerSupplierDto.getIdCardNum());
        FastUtils.checkParams(customerSupplierDto.getIsEnterpriseAdmin());
        // 补参：企业ID
        customerSupplierDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        //duplicateFlag 重复标识 true:重复  false:不重复
        Boolean duplicateFlag = customerSupplierService.checkDuplicate(customerSupplierDto);
        if (duplicateFlag) {
            return ok();
        }
        return ok(ResultCode.COLUMN_EXIST, customerSupplierDto.getRcSet());
    }

    /**
     * @Description excel导出
     * @Author 朱小明
     * @Date 2019/6/28 10:18
     * @Param [accountSubjectDto, response]
     * @return void
     **/
    @PostMapping("exportExcel")
    public void exportExcel(@RequestBody CustomerSupplierDto customerSupplierDto, HttpServletResponse response) {
        //非空校验
        FastUtils.checkParams(customerSupplierDto.getIsEnterpriseAdmin());

        customerSupplierDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        //判断是否为业务管理员
        if (Constant.Is.YES.equals(customerSupplierDto.getIsEnterpriseAdmin())){
            customerSupplierService.exportExcelForAdmin(customerSupplierDto, response);
        } else {
            customerSupplierService.exportExcelForUser(customerSupplierDto, response);
        }
    }

    /**
     * 鉴权
     * @param customerSupplierDto
     */
    public void checkPerm(CustomerSupplierDto customerSupplierDto){
        // 鉴权
        if (Constant.Is.NO.equals(customerSupplierDto.getIsEnterpriseAdmin())){
            if (Constant.BlocInfo.BLOCID.equals(customerSupplierDto.getCompanyId())){
                //集团数据，USER无权操作
                throw new ServiceException(ResultCode.GROUP_DATA_RULE);
            }
            Boolean hasPerm = false;
            if (Constant.CustomerSupplier.CUSTOMER.equals(customerSupplierDto.getDataType())){
                //客户
                hasPerm = ShiroUtils.hasPerm(Constant.MenuDefine.CUSTOMER_ITEM_EDIT,customerSupplierDto.getCompanyId());
            }else {
                //供应商
                hasPerm = ShiroUtils.hasPerm(Constant.MenuDefine.SUPPLIER_ITEM_EDIT,customerSupplierDto.getCompanyId());
            }
            //无权限
            if (!hasPerm){
                //无访问权限
                throw new ServiceException(ResultCode.USER_NOT_EXIST);
            }
        }
    }

}
