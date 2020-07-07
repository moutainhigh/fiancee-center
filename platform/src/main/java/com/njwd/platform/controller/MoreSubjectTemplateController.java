package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.FindAccountSubjectListVo;
import com.njwd.platform.service.AccountSubjectService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 更多科目模板
 * @Date:14:27 2019/6/17
 **/

@RestController
@RequestMapping("moreSubjectTemplate")
public class MoreSubjectTemplateController extends BaseController {

    @Autowired
    private AccountSubjectService accountSubjectService;

    /**
     * @Description 更多科目模板分页查询
     * @Author liuxiang
     * @Date:15:15 2019/7/2
     * @Param [accountSubjectDto]
     * @return java.lang.String
     **/
    @PostMapping("findMoreSubjectTemplatePage")
    public Result<Page<FindAccountSubjectListVo>> findMoreSubjectTemplatePage(@RequestBody AccountSubjectDto accountSubjectDto){
        return ok(accountSubjectService.findAccountSubjectPage(accountSubjectDto));
    }


    /**
     * @Description 更多科目模板列表查询
     * @Author liuxiang
     * @Date:15:15 2019/7/2
     * @Param [accountSubjectDto]
     * @return java.lang.String
     **/
    @PostMapping("findMoreSubjectTemplateList")
    public Result<List<FindAccountSubjectListVo>> findMoreSubjectTemplateList(@RequestBody AccountSubjectDto accountSubjectDto){
        return ok(accountSubjectService.findAccountSubjectList(accountSubjectDto));
    }
}
