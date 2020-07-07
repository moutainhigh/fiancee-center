package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.njwd.basedata.mapper.DeptUseCompanyMapper;
import com.njwd.basedata.service.DeptUseCompanyService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.DeptUseCompany;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jds
 * @Description 使用公司
 * @create 2019/7/7 15:40
 */
@Service
public class DeptUseCompanyImpl implements DeptUseCompanyService {

    @Resource
    private DeptUseCompanyMapper deptUseCompanyMapper;

    /**
     * @Description  新增历史记录
     * @Author jds
     * @Date 2019/6/28 16:25
     * @Param [deptDto, operator]
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addUseCompany(DeptUseCompany deptUseCompany){
        Integer result=deptUseCompanyMapper.insert(deptUseCompany);
        return result;
    }

    /**
     * @Description  批量新增历史记录
     * @Author jds
     * @Date 2019/6/28 16:25
     * @Param [deptDto, operator]
     * @return void
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addUseCompanyBatch(List<DeptUseCompany> deptUseCompany){
        Integer result=deptUseCompanyMapper.addUseCompanyBatch(deptUseCompany);
        return result;
    }


    /**
     * @Description  删除未启用的历史记录
     * @Author jds
     * @Date 2019/6/28 16:25
     * @Param [deptDto, operator]
     * @return void
     **/
    @Override
    public Integer delete(List<Long> list){
        DeptUseCompany deptUseCompany=new DeptUseCompany();
        SysUserVo operator = UserUtils.getUserVo();
        deptUseCompany.setIsDel(Constant.Is.YES);
        deptUseCompany.setUpdatorId(operator.getUserId());
        deptUseCompany.setUpdatorName(operator.getName());
        return deptUseCompanyMapper.update(deptUseCompany,new QueryWrapper<DeptUseCompany>().in(Constant.ColumnName.ID,list)) ;
    }


    /**
     * @Description //c重定义记录
     * @Author jds
     * @Date 2019/8/29 9:10
     * @Param [deptUseCompany]
     * @return java.lang.Integer
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DeptUseCompany deptUseCompany){
        return deptUseCompanyMapper.update(deptUseCompany, new QueryWrapper<DeptUseCompany>().eq(Constant.ColumnName.DEPT_ID,deptUseCompany.getDeptId()));
    }


    /**
     * @Description  关联删除历史记录  物理删除
     * @Author jds
     * @Date 2019/6/28 16:25
     * @Param [deptDto, operator]
     * @return void
     **/
    @Override
    public Integer deleteDept(List<Long> list){
        DeptUseCompany deptUseCompany=new DeptUseCompany();
        deptUseCompany.setIsDel(Constant.Is.YES);
        return deptUseCompanyMapper.update(deptUseCompany,  new QueryWrapper<DeptUseCompany>().in(Constant.CompanyAndBusinessUnit.DEPT_ID,list)) ;
    }



    /**
     * @Description 查询历史记录
     * @Author jds
     * @Date 2019/7/7 15:59
     * @Param [deptDto]
     * @return com.njwd.entity.basedata.DeptUseCompany
     **/
    @Override
    public List<DeptUseCompany>  findUseCompanyList(DeptUseCompany useCompany){
        List<DeptUseCompany> list = deptUseCompanyMapper.findUseCompanyList(useCompany);
        return list;
    }






}
