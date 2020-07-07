package com.njwd.platform.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.FindAccountSubjectListVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 更多科目模板
 * @Date:10:23 2019/6/19
 **/
@RequestMapping("platform/moreSubjectTemplate")
public interface MoreSubjectTemplateApi {

    /**
     * @Description 查询菜单选项列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findMoreSubjectTemplatePage")
    Result<Page<FindAccountSubjectListVo>> findMoreSubjectTemplatePage(AccountSubjectDto accountSubject);

    /**
     * @Description 根据ID查询菜单选项
     * @Param [sysMenuOption]
     * @return java.lang.String
     **/
    @PostMapping("findMoreSubjectTemplateList")
    Result<List<FindAccountSubjectListVo>> findMoreSubjectTemplateList(AccountSubjectDto accountSubject);


}
