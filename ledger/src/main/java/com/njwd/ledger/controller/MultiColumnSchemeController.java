package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.MultiColumnScheme;
import com.njwd.entity.ledger.dto.MultiColumnReportDto;
import com.njwd.entity.ledger.dto.MultiColumnSchemeDto;
import com.njwd.entity.ledger.vo.MultiColumnReportVo;
import com.njwd.entity.ledger.vo.MultiColumnSchemeVo;
import com.njwd.ledger.service.MultiColumnSchemeService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 多栏账方案
 * @Date:10:04 2019/7/25
 **/
@RestController
@RequestMapping("multiColumnScheme")
public class MultiColumnSchemeController extends BaseController {

    @Autowired
    private MultiColumnSchemeService multiColumnSchemeService;

    @Autowired
    private SenderService senderService;

    /**
     * @Description 新增多栏账方案
     * @Author liuxiang
     * @Date:19:17 2019/7/25
     * @Param [parameterSetDto]
     * @return java.lang.String
     **/
    @PostMapping("addMultiColumnScheme")
    public Result<MultiColumnSchemeVo> addMultiColumnScheme(@RequestBody MultiColumnSchemeDto multiColumnSchemeDto){
        //校验入参
        FastUtils.checkParams(multiColumnSchemeDto.getSchemeType());
        if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(multiColumnSchemeDto.getSchemeType())){
            FastUtils.checkParams(multiColumnSchemeDto.getAuxiliaryItemId());
        }else{
            multiColumnSchemeDto.setAuxiliaryItemId(-1L);
            multiColumnSchemeDto.setAuxiliaryItemName("");
        }
        SysUserVo operator = UserUtils.getUserVo();
        multiColumnSchemeDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        multiColumnSchemeDto.setCreatorId(operator.getUserId());
        multiColumnSchemeDto.setCreatorName(operator.getName());
        return ok(multiColumnSchemeService.addMultiColumnScheme(multiColumnSchemeDto));
    }


    /**
     * @Description 批量删除多栏账方案
     * @Author liuxiang
     * @Date:19:17 2019/7/25
     * @Param [parameterSetDto]
     * @return java.lang.String
     **/
    @PostMapping("batchDeleteMultiColumnScheme")
    public Result<BatchResult> batchDeleteMultiColumnScheme(@RequestBody MultiColumnSchemeDto multiColumnSchemeDto){
        SysUserVo operator = UserUtils.getUserVo();
        multiColumnSchemeDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        MultiColumnScheme multiColumnScheme=new MultiColumnScheme();
        FastUtils.copyProperties(multiColumnSchemeDto,multiColumnScheme);
        //UPDATE 批量删除
        BatchResult result = multiColumnSchemeService.deleteMultiColumnSchemeBatch(multiColumnSchemeDto);
        return ok(result);
    }

    /**
     * @Description 修改多栏账方案设置
     * @Author liuxiang
     * @Date:13:56 2019/7/31
     * @Param [multiColumnSchemeDto]
     * @return java.lang.String
     **/
    @PostMapping("updateMultiColumnScheme")
    public Result<Integer> updateMultiColumnScheme(@RequestBody MultiColumnSchemeDto multiColumnSchemeDto){
        int result;
        FastUtils.checkParams(multiColumnSchemeDto.getSchemeType());
        if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(multiColumnSchemeDto.getSchemeType())){
            FastUtils.checkParams(multiColumnSchemeDto.getAuxiliaryItemId());
        }else{
            multiColumnSchemeDto.setAuxiliaryItemId(-1L);
            multiColumnSchemeDto.setAuxiliaryItemName("");
        }
        SysUserVo operator = UserUtils.getUserVo();
        multiColumnSchemeDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        multiColumnSchemeDto.setUpdatorId(operator.getUserId());
        multiColumnSchemeDto.setUpdatorName(operator.getName());
        result=multiColumnSchemeService.updateMultiColumnScheme(multiColumnSchemeDto);
        return ok(result);
    }

    /**
     * @Description 查询多栏账方案分页
     * @Author liuxiang
     * @Date:19:17 2019/7/25
     * @Param [parameterSetDto]
     * @return java.lang.String
     **/
    @PostMapping("findMultiColumnSchemePage")
    public Result<Page<MultiColumnSchemeVo>> findMultiColumnSchemePage(@RequestBody MultiColumnSchemeDto multiColumnSchemeDto){
        Page<MultiColumnSchemeVo> multiColumnSchemeVoList=multiColumnSchemeService.findMultiColumnSchemePageByCodeOrName(multiColumnSchemeDto);
        return ok(multiColumnSchemeVoList);
    }

    /**
     * @Description 根据ID查询多栏账方案
     * @Author liuxiang
     * @Date:15:11 2019/8/19
     * @Param [multiColumnSchemeDto]
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.MultiColumnSchemeVo>>
     **/
    @PostMapping("findMultiColumnSchemeById")
    public Result<MultiColumnSchemeVo> findMultiColumnSchemeById(@RequestBody MultiColumnSchemeDto multiColumnSchemeDto){
        FastUtils.checkParams(multiColumnSchemeDto.getId());
        MultiColumnSchemeVo multiColumnSchemeVo=multiColumnSchemeService.findMultiColumnSchemeById(multiColumnSchemeDto);
        return ok(multiColumnSchemeVo);
    }

    /**
     * @description: 获取多栏明细账
     * @param: []
     * @return: com.njwd.support.Result<com.njwd.entity.ledger.vo.MultiColumnReportVo> 
     * @author: xdy        
     * @create: 2019-08-31 11-47 
     */
    @PostMapping("findMultiColumnReport")
    public Result<MultiColumnReportVo> findMultiColumnReport(@RequestBody MultiColumnReportDto multiColumnReportDto){
        FastUtils.checkParams(multiColumnReportDto.getAccountBookId(),multiColumnReportDto.getAccountBookEntityIds()
            ,multiColumnReportDto.getPeriodNumbers(),multiColumnReportDto.getPeriodYears(),multiColumnReportDto.getSchemeId());
        return ok(multiColumnSchemeService.findMultiColumnReport(multiColumnReportDto));
    }


    /**
     * @description: 导出多栏明细账
     * @param: [multiColumnReportDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-09 10:29 
     */
    @PostMapping("exportMultiColumnReport")
    public void exportMultiColumnReport(@RequestBody MultiColumnReportDto multiColumnReportDto, HttpServletResponse response){
        multiColumnSchemeService.exportMultiColumnReport(multiColumnReportDto,response);
    }


    /**
     * @Description 查询未删除状态的多栏账方案
     * @Author liuxiang
     * @Date:19:17 2019/7/25
     * @Param [parameterSetDto]
     * @return java.lang.String
     **/
    @PostMapping("findMultiColumnSchemeList")
    public Result<List<MultiColumnSchemeVo>> findMultiColumnSchemeList(){
        List<MultiColumnSchemeVo> multiColumnSchemeVoList=multiColumnSchemeService.findMultiColumnSchemeList();
        return ok(multiColumnSchemeVoList);
    }

}
