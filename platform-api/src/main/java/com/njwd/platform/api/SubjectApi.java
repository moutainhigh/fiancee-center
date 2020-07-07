package com.njwd.platform.api;

import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.SubjectVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 科目表
 * @Date:16:38 2019/6/19
 **/
@RequestMapping("platform/subject")
public interface SubjectApi {

    /**
     * @Description 查询科目列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findSubjectList")
    Result<List<SubjectVo>> findSubjectList(SubjectDto subjectDto);

}
