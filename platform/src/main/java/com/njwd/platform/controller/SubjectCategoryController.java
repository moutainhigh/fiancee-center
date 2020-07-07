package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountElementItem;
import com.njwd.entity.platform.SubjectCategory;
import com.njwd.entity.platform.dto.AccountElementItemDto;
import com.njwd.entity.platform.dto.SubjectCategoryDto;
import com.njwd.entity.platform.vo.AccountElementItemVo;
import com.njwd.entity.platform.vo.SubjectCategoryVo;
import com.njwd.platform.service.AccountElementItemService;
import com.njwd.platform.service.SubjectCategoryService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author lj
 * @Description 科目类别
 * @Date:17:15 2019/6/25
 **/
@RestController
@RequestMapping("subjectCategory")
public class SubjectCategoryController extends BaseController {
    @Resource
    private SubjectCategoryService subjectCategoryService;

    /**
     * 根据ElemId查找科目类别列表
     * @Author lj
     * @Date:11:48 2019/8/22
     * @param subjectCategoryDto
     * @return Result<List<SubjectCategoryVo>>
     **/
    @PostMapping("findListByElemId")
    public Result<List<SubjectCategoryVo>> findListByElemId(@RequestBody SubjectCategoryDto subjectCategoryDto){
        return ok(subjectCategoryService.findListByElemId(subjectCategoryDto));
    }
    /** 刘遵通
     * 查询页面 （分页）
     * @param subjectCategoryDto
     * @return
     */
    @RequestMapping("findPage")
    public Result<Page<SubjectCategoryVo>> findPage(@RequestBody SubjectCategoryDto subjectCategoryDto){
        Page<SubjectCategoryVo> subjectCategoryList = subjectCategoryService.findPage(subjectCategoryDto);
        return ok(subjectCategoryList);
    }
    /**
     * 查详情
     * @param subjectCategoryDto
     * @return
     */
    @RequestMapping("selectById")
    public Result<SubjectCategory> selectById(@RequestBody SubjectCategoryDto subjectCategoryDto){
        SubjectCategory subjectCategory = subjectCategoryService.selectById(subjectCategoryDto);
        return  ok(subjectCategory);
    }
}
