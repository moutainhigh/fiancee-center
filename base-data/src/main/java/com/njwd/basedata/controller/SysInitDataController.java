package com.njwd.basedata.controller;

import com.njwd.basedata.service.SysInitDataService;
import com.njwd.entity.basedata.vo.SysInitDataVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/12 11:47
 */
@RestController
@RequestMapping("sysInitData")
public class SysInitDataController extends BaseController {

    @Resource
    private SysInitDataService sysInitDataService;

    private Logger logger = LoggerFactory.getLogger(SysInitDataController.class);
    
    /**
     * @description: 获取初始化状态
     * @param: []
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.SysInitDataVo> 
     * @author: xdy        
     * @create: 2019-08-30 15-50 
     */
    @RequestMapping("findSysInitData")
    public Result<SysInitDataVo> findSysInitData(){
        SysInitDataVo sysInitDataVo = sysInitDataService.findSysInitData();
        SysUserVo userVo = UserUtils.getUserVo();
        logger.debug("findSysInitData,rootEnterpriseId:{},SysInitDataVo:{}",userVo.getRootEnterpriseId(),sysInitDataVo);
        return ok(sysInitDataVo);
    }
    
    /**
     * @description: 初始化数据
     * @param: []
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.SysInitDataVo> 
     * @author: xdy        
     * @create: 2019-08-30 15-51 
     */
    @RequestMapping("init")
    public Result<SysInitDataVo> initData(){
        SysInitDataVo sysInitDataVo = sysInitDataService.initData();
        SysUserVo userVo = UserUtils.getUserVo();
        logger.debug("initData,rootEnterpriseId:{},SysInitDataVo:{}",userVo.getRootEnterpriseId(),sysInitDataVo);
        return ok(sysInitDataVo);
    }


}
