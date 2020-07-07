package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SubjectCategory;
import com.njwd.entity.platform.dto.SubjectCategoryDto;
import com.njwd.entity.platform.vo.SubjectCategoryVo;

import java.util.List;

/**
 * @Author lj
 * @Description 科目类别
 * @Date:11:42 2019/8/22
 **/
public interface SubjectCategoryService {

    /**
     * 根据ElemId查找科目类别列表
     * @Author lj
     * @Date:11:48 2019/8/22
     * @param subjectCategoryDto
     * @return java.util.List<com.njwd.entity.platform.vo.SubjectCategoryVo>
     **/
    List<SubjectCategoryVo> findListByElemId(SubjectCategoryDto subjectCategoryDto);
    /** 刘遵通
     * 查询页面 （分页）
     * @param subjectCategoryDto
     * @return
     */
    Page<SubjectCategoryVo> findPage(SubjectCategoryDto subjectCategoryDto);
    /**
     * 查详情
     * @param subjectCategoryDto
     * @return
     */
    SubjectCategory selectById(SubjectCategoryDto subjectCategoryDto);
}
