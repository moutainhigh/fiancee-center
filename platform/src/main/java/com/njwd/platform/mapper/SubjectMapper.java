package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.SubjectVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author lj
 * @Description 科目
 * @Date:14:16 2019/6/13
 **/
public interface SubjectMapper extends BaseMapper<Subject> {

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.SubjectVo>
     * @Description 查询科目列表
     * @Author liuxiang
     * @Date:15:32 2019/7/2
     * @Param [subjectDto]
     **/
    List<SubjectVo> findSubjectList(SubjectDto subjectDto);

    /**
     * 查询科目列表分页
     *
     * @param page
     * @param subjectDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.SubjectVo>
     * @Author lj
     * @Date:14:18 2019/10/29
     **/
    Page<SubjectVo> findSubjectListPage(Page<SubjectVo> page, @Param("subjectDto") SubjectDto subjectDto);

    /**
     * 分页
     *
     * @param subjectDto subjectDto
     * @param page       page
     * @return java.util.List<com.njwd.entity.platform.vo.SubjectVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/12 15:58
     **/
    List<SubjectVo> findPage(@Param("subjectDto") SubjectDto subjectDto, @Param("page") Page<SubjectVo> page);

    /**
     * 查详情
     *
     * @param id id
     * @return com.njwd.entity.platform.vo.SubjectVo
     * @author xyyxhcj@qq.com
     * @date 2019/11/18 17:50
     **/
	SubjectVo findDetail(@Param("id") Long id);
}
