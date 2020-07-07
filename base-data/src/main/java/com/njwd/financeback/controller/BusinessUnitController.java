package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.vo.BusinessUnitVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.financeback.service.BusinessUnitService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 业务单元 controller
 * @Author: Zhuzs
 * @Date: 2019-05-15 11:07
 */
@RestController
@RequestMapping("businessUnit")
public class BusinessUnitController extends BaseController {

    @Autowired
    private BusinessUnitService businessUnitService;
    @Autowired
    private SenderService senderService;

    /**
     * 新增业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.BusinessUnit>
     * @author: zhuzs
     * @date: 2019-09-16 17:09
     */
    @RequestMapping("addBusinessUnit")
    public Result<BusinessUnit> addBusinessUnit(@RequestBody BusinessUnitDto businessUnitDto){
        SysUserVo operator = UserUtils.getUserVo();
        businessUnitDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        if (Constant.Is.NO.equals(businessUnitDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.BUSINESS_UNIT_UPDATE, businessUnitDto.getCompanyId());
        } else {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        BusinessUnit result = businessUnitService.addBusinessUnit(businessUnitDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.busiUnit,
                LogConstant.operation.add,
                LogConstant.operation.add_type,
                String.valueOf(businessUnitDto.getId())));
        return ok(result);
    }

    /**
     * 批量删除
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: zhuzs
     * @date: 2019-09-16 17:10
     */
    @RequestMapping("deleteBatch")
    public Result<BatchResult> deleteBatch(@RequestBody BusinessUnitDto businessUnitDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        BatchResult result ;
        if(Constant.Number.ONE.equals(businessUnitDto.getIdList().size())){
            result = businessUnitService.delete(businessUnitDto);
        }else{
            result = businessUnitService.deleteBatch(businessUnitDto);
            // 清除缓存
            RedisUtils.removeBatch(Constant.RedisCache.BUSINESS_UNIT, result.getSuccessList());
        }

        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.busiUnit,
                LogConstant.operation.deleteBatch,
                LogConstant.operation.deleteBatch_type,
                String.valueOf(result.getSuccessList())));
        return ok(result);
    }

    /**
     * 修改业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.BusinessUnit>
     * @author: zhuzs
     * @date: 2019-09-16 17:10
     */
    @RequestMapping("updateBusinessUnit")
    public Result<BusinessUnit> updateBusinessUnit(@RequestBody BusinessUnitDto businessUnitDto){
        SysUserVo operator = UserUtils.getUserVo();
        if (Constant.Is.NO.equals(businessUnitDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.BUSINESS_UNIT_UPDATE, businessUnitDto.getCompanyId());
        } else {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        businessUnitDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        BusinessUnit result = businessUnitService.updateBusinessUnit(businessUnitDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.busiUnit,
                LogConstant.operation.update,
                LogConstant.operation.update_type,
                String.valueOf(businessUnitDto.getId())));
        return ok(result);
    }

    /**
     * 根据ID查找
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.BusinessUnit>
     * @author: zhuzs
     * @date: 2019-09-16 17:10
     */
    @RequestMapping("findBusinessUnitById")
    public Result<BusinessUnit> findBusinessUnitById(@RequestBody BusinessUnitDto businessUnitDto){
        SysUserVo operator = UserUtils.getUserVo();
        if (Constant.Is.YES.equals(businessUnitDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        return ok(businessUnitService.findBusinessUnitById(businessUnitDto));
    }

    /**
     * 根据名称查找
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.BusinessUnit>
     * @author: zhuzs
     * @date: 2019-09-16 17:10
     */
    @RequestMapping("findBusinessUnitByName")
    public Result<BusinessUnit> findBusinessUnitByName(@RequestBody BusinessUnitDto businessUnitDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        return ok(businessUnitService.findBusinessUnitByName(businessUnitDto));
    }

    /**
     * 查询业务单元列表(含公司ID、公司名称）
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.BusinessUnitVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:10
     */
    @RequestMapping("findBusinessUnitPage")
    public Result<Page<BusinessUnitVo>> findBusinessUnitPage(@RequestBody BusinessUnitDto businessUnitDto){
        SysUserVo operator = UserUtils.getUserVo();
        if (Constant.Is.YES.equals(businessUnitDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        return ok(businessUnitService.findBusinessUnitPage(businessUnitDto));
    }

    /**
     * 查询业务单元列表(含公司ID、公司名称）
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.BusinessUnitVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:10
     */
    @RequestMapping("findBusinessUnitList")
    public Result<List<BusinessUnitVo>> findBusinessUnitList(@RequestBody BusinessUnitDto businessUnitDto){
//        SysUserVo operator = UserUtils.getUserVo();
//        if (Constant.Is.NO.equals(businessUnitDto.getIsEnterpriseAdmin())) {
////            ShiroUtils.checkPerm(Constant.MenuDefine.BUSINESS_UNIT_FIND, businessUnitDto.getCompanyIdList().get(0));
//        } else {
//            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
//        }
        return ok(businessUnitService.findBusinessUnitList(businessUnitDto));
    }

    /**
     * 获取 业务单元形态 列表
     *
     * @param: []
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.SysAuxDataVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:12
     */
    @RequestMapping("findFormList")
    public Result<List<SysAuxDataVo>> findFormList(){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        return ok(businessUnitService.findFormList());
    }

    /**
     * 导出
     *
     * @param: [businessUnitDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:12
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody BusinessUnitDto businessUnitDto, HttpServletResponse response){
        SysUserVo operator = UserUtils.getUserVo();
        if (Constant.Is.NO.equals(businessUnitDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.BUSINESS_UNIT_FIND, businessUnitDto.getCompanyIdList().get(0));
        } else {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        businessUnitService.exportExcel(businessUnitDto,response);
    }

}
