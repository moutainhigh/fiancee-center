package com.njwd.platform.api;

import com.njwd.entity.platform.dto.SubjectCategoryDto;
import com.njwd.entity.platform.vo.SubjectCategoryVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author lj
 * @Description 科目类别
 * @Date:17:15 2019/6/25
 **/
@RequestMapping("platform/subjectCategory")
public interface SubjectCategoryApi {

    /**
     * 根据ElemId查找科目类别列表
     * @Author lj
     * @Date:11:48 2019/8/22
     * @param subjectCategoryDto
     * @return Result<List<SubjectCategoryVo>>
     **/
    @PostMapping("findListByElemId")
    Result<List<SubjectCategoryVo>> findListByElemId(SubjectCategoryDto subjectCategoryDto);
}
