package com.njwd.controller;

import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceRelation;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/2 13:36
 */
@RestController
@RequestMapping("referenceRelation")
public class ReferenceRelationController extends BaseController {

    @Resource
    private ReferenceRelationService referenceRelationService;

    @RequestMapping("findReferenceRelation")
    public Result<List<ReferenceRelation>> findReferenceRelation(String businessModule){
        return ok(referenceRelationService.findReferenceRelation(businessModule));
    }

    @RequestMapping("findReferenceCount")
    public Result<Integer> findReferenceCount(@RequestBody ReferenceRelation referenceRelation){
        return ok(referenceRelationService.findReferenceCount(referenceRelation));
    }

    @RequestMapping("findBusinessData")
    public Result<List<Map<String,Object>>> findBusinessData(@RequestBody ReferenceRelation referenceRelation){
        return ok(referenceRelationService.findBusinessData(referenceRelation));
    }

    @RequestMapping("findReferenceCountList")
    public Result<List<ReferenceRelation>> findReferenceCountList(@RequestBody ReferenceRelation referenceRelation){
        return ok(referenceRelationService.findReferenceCountList(referenceRelation));
    }

    @RequestMapping("testReference")
    public Result<ReferenceContext> testReference(@RequestBody ReferenceRelation referenceRelation){
        return ok(referenceRelationService.isReference(referenceRelation.getBusinessModule(),referenceRelation.getBusinessIds()));
    }

}
