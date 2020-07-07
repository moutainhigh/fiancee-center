package com.njwd.platform.controller;

import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.Sequence;
import com.njwd.platform.service.SequenceService;
import com.njwd.support.BaseController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("sequenceCode")
public class SequenceController extends BaseController {

    @Resource
    private SequenceService sequenceService;
    @RequestMapping("getCode")
    public String getCode(@RequestBody Sequence sequence){

        String code = "1";
        String platFromCode = sequenceService.getCode(PlatformConstant.PlatformCodeRule.ACCOUNTING_BOOK_TYPE,3, code);
        return platFromCode;
    }
}
