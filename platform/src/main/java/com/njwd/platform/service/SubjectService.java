package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.SubjectVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 科目
 * @Date:14:17 2019/6/13
 **/
public interface SubjectService extends IService<Subject> {

    /**
     * @Description 查询科目列表
     * @Author liuxiang
     * @Date:9:49 2019/7/4
     * @Param [subjectDto]
     * @return java.util.List<com.njwd.platform.entity.vo.SubjectVo>
     **/
    List<SubjectVo> findSubjectList(SubjectDto subjectDto);

    /**
     * 查询科目列表分页
     * @Author lj
     * @Date:14:18 2019/10/29
     * @param subjectDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.SubjectVo>
     **/
    Page<SubjectVo> findSubjectListPage(SubjectDto subjectDto);

	/**
	 * 分页
	 *
	 * @param subjectDto subjectDto
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.SubjectVo>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/12 15:45
	 **/
	Page<SubjectVo> findPage(SubjectDto subjectDto);

	/**
	 * add
	 *
	 * @param subjectDto subjectDto
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/14 11:04
	 **/
	Long add(SubjectDto subjectDto);

	/**
	 * update
	 *
	 * @param subjectDto subjectDto
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/14 16:19
	 **/
	Long update(SubjectDto subjectDto);

	/**
	 * batch delete
	 *
	 * @param subjectDto subjectDto
	 * @return com.njwd.support.BatchResult
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/14 17:32
	 **/
	BatchResult delete(SubjectDto subjectDto);

	/**
	 * 批量审核
	 *
	 * @param subjectDto subjectDto
	 * @return com.njwd.support.BatchResult
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/18 10:58
	 **/
	BatchResult approve(SubjectDto subjectDto);

	/**
	 * 批量反审
	 *
	 * @param subjectDto subjectDto
	 * @return com.njwd.support.BatchResult
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/18 11:21
	 **/
	BatchResult reversalApprove(SubjectDto subjectDto);

	/**
	 * 批量发布
	 *
	 * @param subjectDto subjectDto
	 * @return com.njwd.support.BatchResult
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/18 11:32
	 **/
	BatchResult release(SubjectDto subjectDto);

	/**
	 * 查详情
	 *
	 * @param subjectDto subjectDto
	 * @return com.njwd.entity.platform.vo.SubjectVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/18 17:48
	 **/
	SubjectVo findDetail(SubjectDto subjectDto);

	/**
	 * 导出
	 *
	 * @param subjectDto subjectDto
	 * @param response   response
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/24 10:02
	 **/
	void exportExcel(SubjectDto subjectDto, HttpServletResponse response);
}
