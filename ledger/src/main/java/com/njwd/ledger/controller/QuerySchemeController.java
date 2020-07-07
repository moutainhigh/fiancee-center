package com.njwd.ledger.controller;


import com.njwd.entity.ledger.dto.QuerySchemeDto;
import com.njwd.entity.ledger.vo.QuerySchemeVo;
import com.njwd.ledger.service.QuerySchemeService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 查询方案Controller
 * @author: xdy
 * @create: 2019-07-30
 */
@RestController
@RequestMapping("queryScheme")
public class QuerySchemeController extends BaseController {

    @Resource
    QuerySchemeService schemeService;
    
    /**
     * @description: 新增或修改
     * @param: [querySchemeDto]
     * @return: com.njwd.support.Result<java.lang.Integer> 
     * @author: xdy        
     * @create: 2019-08-30 15-42 
     */
    @RequestMapping("addOrUpdate")
    public Result<QuerySchemeVo> addQueryScheme(@RequestBody QuerySchemeDto querySchemeDto){
        return ok(schemeService.addOrUpdate(querySchemeDto));
    }
    
    /**
     * @description: 删除
     * @param: [querySchemeDto]
     * @return: com.njwd.support.Result<java.lang.Integer> 
     * @author: xdy        
     * @create: 2019-08-30 15-42 
     */
    @RequestMapping("delQueryScheme")
    public Result<Integer> delQueryScheme(@RequestBody QuerySchemeDto querySchemeDto){
        FastUtils.checkNull(querySchemeDto.getId());
        return ok(schemeService.delQueryScheme(querySchemeDto));
    }
    
    /**
     * @description: 查询详情
     * @param: [querySchemeDto]
     * @return: com.njwd.support.Result<com.njwd.entity.ledger.vo.QuerySchemeVo> 
     * @author: xdy        
     * @create: 2019-08-30 15-43 
     */
    @RequestMapping("findQuerySchemeById")
    public Result<QuerySchemeVo> findQuerySchemeById(@RequestBody QuerySchemeDto querySchemeDto){
        FastUtils.checkNull(querySchemeDto.getId());
        return ok(schemeService.findQuerySchemeById(querySchemeDto));
    }
    
    /**
     * @description: 分页
     * @param: [querySchemeDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.QuerySchemeVo>> 
     * @author: xdy        
     * @create: 2019-08-30 15-43 
     */
    @RequestMapping("findQueryScheme")
    public Result<List<QuerySchemeVo>> findQueryScheme(@RequestBody QuerySchemeDto querySchemeDto){
        FastUtils.checkNull(querySchemeDto.getMenuCode());
        return ok(schemeService.findQueryScheme(querySchemeDto));
    }




}
