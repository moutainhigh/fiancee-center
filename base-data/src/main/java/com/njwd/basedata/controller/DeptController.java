package com.njwd.basedata.controller;


import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.DeptService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.excel.ExcelRequest;
import com.njwd.entity.basedata.excel.ExcelResult;
import com.njwd.entity.basedata.vo.DeptVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 部门
 *
 * @author jds
 * @Date 2019/6/20 18:00
 */
@RestController
@RequestMapping("dept")
public class DeptController extends BaseController {
    @Resource
    private DeptService deptService;

    @Resource
    private SenderService senderService;

    @Resource
    private FileService fileService;



    /**
     * @Description 新增部门/下级部门
     * @Author jds
     * @Date 2019/6/20 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("addDept")
    public Result<Long> addDept(@RequestBody  DeptDto deptDto){
        FastUtils.checkParams(deptDto.getCodeType(),deptDto.getName(),deptDto.getAttrBusinessUnitId(),deptDto.getUseCompanyId(),deptDto.getCompanyId(),deptDto.getBusinessUnitId(),deptDto.getDeptType(),deptDto.getDeptTypeName(),deptDto.getIsEnterpriseAdmin());
        if (Constant.Is.NO.equals(deptDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.DEPT_EDIT, deptDto.getCompanyId());
        }
        deptService.add(deptDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.department,
                LogConstant. operation.add,LogConstant. operation.add_type,deptDto.getId().toString()));
        return ok(deptDto.getId());
    }


    /**
     * @Description 删除
     * @Author jds
     * @Date 2019/6/20 18:02
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @PostMapping("deleteDeptBatch")
    public Result<BatchResult> deleteDeptBatch(@RequestBody DeptDto deptDto) {
        FastUtils.checkParams(deptDto.getIdList(),deptDto.getVersionList(),deptDto.getIsEnterpriseAdmin());
        deptDto.setMenuDefine(Constant.MenuDefine.DEPT_DELETE);
        deptDto.setIsDel(Constant.Is.YES);
        BatchResult result=deptService.deleteDeptBatch(deptDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.department,
                LogConstant. operation.delete,LogConstant. operation.delete_type,deptDto.getIdList().toString()));
        return ok(result);
    }


    /**
     * @Description 变更部门的使用公司
     * @Author jds
     * @Date 2019/6/20 18:01
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("updateDept")
    public Result<Integer> updateDept(@RequestBody DeptDto deptDto){
        FastUtils.checkParams(deptDto.getId(),deptDto.getUsedId(),deptDto.getUseCompanyId(),deptDto.getBusinessUnitId(),deptDto.getIsEnterpriseAdmin());
        int result=deptService.update(deptDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.department,
                LogConstant. operation.update,LogConstant. operation.update_type,deptDto.getId().toString()));
        return  ok(result);
    }

    /**
     * @Description 保存
     * @Author jds
     * @Date 2019/7/1 9:03
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("saveDeptChange")
    public Result<Integer> saveDeptChange(@RequestBody DeptDto deptDto){
        FastUtils.checkParams(deptDto.getId(),deptDto.getCodeType(),deptDto.getVersion(),deptDto.getName(), deptDto.getCompanyId(),deptDto.getAttrBusinessUnitId(),deptDto.getDeptType(),deptDto.getDeptTypeName(),deptDto.getIsEnterpriseAdmin());
        Integer version=deptService.saveDeptChange(deptDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.department,
                LogConstant. operation.update,LogConstant. operation.update_type,deptDto.getId().toString()));
        return ok(version);
    }

    /**
     * @Description  启用/(反禁用)
     * @Author jds
     * @Date 2019/6/20 18:01
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @PostMapping("enableBatch")
    public Result<BatchResult> enableBatch(@RequestBody DeptDto deptDto) {
        FastUtils.checkParams(deptDto.getIdList(),deptDto.getVersionList(),deptDto.getIsEnterpriseAdmin());
        deptDto.setIsEnable(Constant.Is.YES);
        deptDto.setMenuDefine(Constant.MenuDefine.DEPT_ENABLE);
        BatchResult batchResult=deptService.updateDeptStatusBatch(deptDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.department,
                LogConstant. operation.antiForbidden,LogConstant. operation.antiForbidden_type,deptDto.getIdList().toString()));
        return ok(batchResult);
    }


    /**
     * @Description 禁用
     * @Author jds
     * @Date 2019/6/20 18:02
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @PostMapping("disableBatch")
    public Result<BatchResult> disableBatch(@RequestBody DeptDto deptDto) {
        FastUtils.checkParams(deptDto.getIdList(),deptDto.getVersionList(),deptDto.getIsEnterpriseAdmin());
        deptDto.setMenuDefine(Constant.MenuDefine.DEPT_DISABLE);
        deptDto.setIsEnable(Constant.Is.NO);
        BatchResult batchResult=deptService.updateDeptStatusBatch(deptDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.department,
                LogConstant. operation.forbidden,LogConstant. operation.forbidden_type,deptDto.getIdList().toString()));
        return ok(batchResult);
    }

    /**
     * @Description 分页查询部门
     * @Author jds
     * @Date 2019/6/20 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptPage")
    public Result<Page<DeptVo>> findDeptPage(@RequestBody DeptDto deptDto) {
        if(deptDto.getQueryConditions()==null){
            FastUtils.checkParams(deptDto.getIsEnterpriseAdmin());
            if((Constant.Is.NO).equals(deptDto.getIsEnterpriseAdmin())){
                if(!CollectionUtils.isEmpty(deptDto.getCompanyIdList())){
                    List<Long> idList = ShiroUtils.filterPerm(Constant.MenuDefine.DEPT_FIND, deptDto.getCompanyIdList());
                    if(CollectionUtils.isEmpty(idList)){
                        return ok(deptDto.getPage());
                    }
                    deptDto.setCompanyIdList(idList);
                }
            }
        }else {
            deptDto.setIsEnterpriseAdmin(null);
        }
        SysUserVo operator = UserUtils.getUserVo();
        deptDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        deptDto.setUserId(operator.getUserId());
        Page<DeptVo> list=deptService.findDeptPage(deptDto);
        return ok(list);
    }

    /**
     * @Description   获取部门下拉列表（不分页）
     * @Author jds
     * @Date 2019/6/28 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptList")
    public Result<List<DeptVo>> findDeptList(@RequestBody DeptDto deptDto) {
        SysUserVo operator = UserUtils.getUserVo();
        deptDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        List<DeptVo> list=deptService.findDeptList(deptDto);
        return ok(list);
    }

    /**
     * @Description   获取上级部门列表
     * @Author jds
     * @Date 2019/6/28 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findParentDeptList")
    public Result<Page<DeptVo>> findParentDeptList(@RequestBody DeptDto deptDto) {
        // 根据创建公司业务单元和部门属性查询对应的上级部门  ,deptDto.getAttrBusinessUnitId(),deptDto.getDeptType()
        FastUtils.checkParams(deptDto.getCompanyId());
        Page<DeptVo> list=deptService.findParentDeptList(deptDto);
        return ok(list);
    }



    /**
     * @Description 获取部门属性列表(  分页      从辅助资料表获取)
     * @Author jds
     * @Date 2019/6/27 9:29
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptTypePage")
    public Result findDeptTypePage(@RequestBody SysAuxDataDto platformSysAuxDataDto) {
        return deptService.findDpetTypeList(platformSysAuxDataDto);
    }


    /**
     * @Description 获取部门属性列表(  不分页      从辅助资料表获取)
     * @Author jds
     * @Date 2019/6/28 11:12
     * @Param [platformSysAuxDataDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptTypeList")
    public Result findDeptTypeList(@RequestBody SysAuxDataDto platformSysAuxDataDto) {
        List<SysAuxDataVo> value =deptService.findTypeList(platformSysAuxDataDto);
        return ok(value);
    }


    /**
     * @Description 查询使用公司变更历史记录
     * @Author jds
     * @Date 2019/6/23 21:52
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findCompanyList")
    public Result<DeptVo> findCompanyList(@RequestBody DeptDto deptDto) {
        FastUtils.checkParams(deptDto.getId());
        DeptVo deptVo=deptService.findCompanyList(deptDto);
        return ok(deptVo);
    }


    /**
     * @Description 根据 ID 查找部门
     * @Author jds
     * @Date 2019/6/20 18:01
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptById")
    public Result<DeptVo> findDeptById(@RequestBody DeptDto deptDto){
        FastUtils.checkParams(deptDto.getId(),deptDto.getIsEnterpriseAdmin());
        DeptVo deptVo=deptService.findDeptById(deptDto);
        return ok(deptVo);
    }


    /**
     * @Description  验证当前数据
     * @Author jds
     * @Date 2019/6/20 18:01
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("checkIsUsed")
    public Result<Integer> checkIsUsed(@RequestBody DeptDto deptDto){
        FastUtils.checkParams(deptDto.getId());
        deptDto.setPrarentId(deptDto.getId());
        int result=deptService.findPre(deptDto);
        return ok(result);
    }

    /**
     * @Description 导出
     * @Author jds
     * @Date 2019/6/20 18:02
     * @Param [deptDto, response]
     **/
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody DeptDto deptDto, HttpServletResponse response) {
        FastUtils.checkParams(deptDto.getIsEnterpriseAdmin());
        if((Constant.Is.NO).equals(deptDto.getIsEnterpriseAdmin())){
            FastUtils.checkParams(deptDto.getCompanyIdList());
            deptDto.setCompanyIdList(ShiroUtils.filterPerm(Constant.MenuDefine.DEPT_EXPORT,deptDto.getCompanyIdList()));
        }
        deptDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        deptService.exportExcel(deptDto, response);

    }


    /**
     * @Description 下载模板
     * @Author jds
     * @Date 2019/6/20 18:03
     * @Param []
     * @return org.springframework.http.ResponseEntity
     **/

    @RequestMapping("downloadDeptTemplate")
    public ResponseEntity downloadDeptTemplate()throws Exception{
        return fileService.downloadExcelTemplate(Constant.Reference.DEPT);
    }

    /**
     * @Description 验证导入数据
     * @Author jds
     * @Date 2019/7/8 16:19
     * @Param [file]
     * @return java.lang.String
     **/
    @RequestMapping("uploadAndCheckExcel")
    public Result<ExcelResult> uploadAndCheckExcel(@RequestParam(value = "file") MultipartFile file) {
        return ok(fileService.uploadAndCheckExcel(file, Constant.Reference.DEPT));
    }

    /**
     * @Description 导入数据
     * @Author jds
     * @Date 2019/7/8 16:20
     * @Param [request]
     * @return java.lang.String
     **/
    @RequestMapping("importExcel")
    public Result<ExcelResult> importExcel(@RequestBody ExcelRequest request) {
        FastUtils.checkParams(request.getUuid());
        return ok(fileService.importExcel(request.getUuid()));
    }

}
