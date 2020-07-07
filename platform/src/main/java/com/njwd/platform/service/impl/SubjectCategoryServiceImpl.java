package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SubjectCategory;
import com.njwd.entity.platform.dto.SubjectCategoryDto;
import com.njwd.entity.platform.vo.SubjectCategoryVo;
import com.njwd.platform.mapper.SubjectCategoryMapper;
import com.njwd.platform.service.SubjectCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author lj
 * @Description 科目类别
 * @Date:11:42 2019/8/22
 **/
@Service
public class SubjectCategoryServiceImpl implements SubjectCategoryService {
    @Autowired
    private SubjectCategoryMapper subjectCategoryMapper;

    /**
     * 根据ElemId查找科目类别列表
     * @Author lj
     * @Date:11:48 2019/8/22
     * @param subjectCategoryDto
     * @return java.util.List<com.njwd.entity.platform.vo.SubjectCategoryVo>
     **/
    @Override
  /*  @Cacheable(value = "findListByElemId", key = "#subjectCategoryDto.elementId+''")*/
    public List<SubjectCategoryVo> findListByElemId(SubjectCategoryDto subjectCategoryDto) {
        return subjectCategoryMapper.findListByElemId(subjectCategoryDto);
    }
    /** 刘遵通
     * 查询页面 （分页）
     * @param subjectCategoryDto
     * @return
     */
    @Override
    public Page<SubjectCategoryVo> findPage(SubjectCategoryDto subjectCategoryDto) {
        return subjectCategoryMapper.findPage(subjectCategoryDto.getPage(), subjectCategoryDto);
    }

    /**
     * 查详情
     * @param subjectCategoryDto
     * @return
     */
    @Override
    public SubjectCategory selectById(SubjectCategoryDto subjectCategoryDto) {
        SubjectCategory subjectCategory = subjectCategoryMapper.selectById(subjectCategoryDto);
        return subjectCategory;
    }
}
