package com.njwd.basedata.api;


import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.excel.ExcelRequest;
import com.njwd.support.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;


/**
 * 部门
 *
 * @author 朱小明
 * @create 2019-06-12 11:18
 */
@RequestMapping("dept")
public interface DeptApi {

    /**
     * @Description 新增部门/下级部门
     * @Author 朱小明
     * @Date 2019/6/20 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("addDept")
    Result addDept(DeptDto deptDto);


    /**
     * @Description 删除
     * @Author 朱小明
     * @Date 2019/6/20 18:02
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @PostMapping("deleteDeptBatch")
    Result deleteDeptBatch(DeptDto deptDto);


    /**
     * @Description 变更部门的使用公司
     * @Author 朱小明
     * @Date 2019/6/20 18:01
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("updateDept")
    Result updateDept(DeptDto deptDto);

    /**
     * @Description 保存
     * @Author 朱小明
     * @Date 2019/7/1 9:03
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("saveDeptChange")
    Result saveDeptChange(DeptDto deptDto);

    /**
     * @Description  启用/(反禁用)
     * @Author 朱小明
     * @Date 2019/6/20 18:01
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @PostMapping("enableBatch")
    Result enableBatch(DeptDto deptDto);


    /**
     * @Description 禁用
     * @Author 朱小明
     * @Date 2019/6/20 18:02
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @PostMapping("disableBatch")
    Result disableBatch(DeptDto deptDto);

    /**
     * @Description 分页查询部门
     * @Author 朱小明
     * @Date 2019/6/20 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptPage")
    Result findDeptPage(DeptDto deptDto);

    /**
     * @Description   获取部门下拉列表（不分页）
     * @Author 朱小明
     * @Date 2019/6/28 18:00
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptList")
    Result findDeptList(DeptDto deptDto);


    /**
     * @Description 获取部门属性列表(  分页      从辅助资料表获取)
     * @Author 朱小明
     * @Date 2019/6/27 9:29
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptTypePage")
    Result findDeptTypePage(com.njwd.entity.platform.dto.SysAuxDataDto platformSysAuxDataDto);

    /**
     * @Description 获取部门属性列表(  不分页      从辅助资料表获取)
     * @Author 朱小明
     * @Date 2019/6/28 11:12
     * @Param [platformSysAuxDataDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptTypeList")
    Result findDeptTypeList(com.njwd.entity.platform.dto.SysAuxDataDto platformSysAuxDataDto);


    /**
     * @Description 查询使用公司变更历史记录
     * @Author 朱小明
     * @Date 2019/6/23 21:52
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findCompanyList")
    Result findCompanyList(DeptDto deptDto);


    /**
     * @Description 根据 ID 查找部门
     * @Author 朱小明
     * @Date 2019/6/20 18:01
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findDeptById")
    Result findDeptById(DeptDto deptDto);


    /**
     * @Description  根据 ID 查找使用公司
     * @Author 朱小明
     * @Date 2019/6/23 21:49
     * @Param [deptDto]
     * @return java.lang.String
     **/
    @RequestMapping("findUseCompanyById")
    Result findUseCompanyById(DeptDto deptDto);


    /**
     * @Description 导出
     * @Author 朱小明
     * @Date 2019/6/20 18:02
     * @Param [deptDto, response]
     * @return void
     **/
    @RequestMapping("exportExcel")
    void exportExcel(DeptDto deptDto, HttpServletResponse response);


    /**
     * @Description 下载模板
     * @Author 朱小明
     * @Date 2019/6/20 18:03
     * @Param []
     * @return org.springframework.http.ResponseEntity
     **/

    @RequestMapping("downloadDeptTemplate")
    ResponseEntity downloadDeptTemplate()throws Exception;

    /**
     * @Description 验证导入数据
     * @Author 朱小明
     * @Date 2019/7/8 16:19
     * @Param [file]
     * @return java.lang.String
     **/
    @RequestMapping("uploadAndCheckExcel")
    Result uploadAndCheckExcel(MultipartFile file);

    /**
     * @Description 导入数据
     * @Author 朱小明
     * @Date 2019/7/8 16:20
     * @Param [request]
     * @return java.lang.String
     **/
    @RequestMapping("importExcel")
    Result importExcel(ExcelRequest request);

}
