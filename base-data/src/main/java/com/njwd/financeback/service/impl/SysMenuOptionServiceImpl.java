package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.basedata.cloudclient.SysMenuOptionFeignClient;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.SysMenuOptionTable;
import com.njwd.entity.basedata.vo.SysMenuOptionComplexVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.SysMenuOption;
import com.njwd.entity.platform.dto.SysMenuOptionDto;
import com.njwd.entity.platform.vo.SysMenuOptionVo;
import com.njwd.financeback.mapper.SysMenuOptionMapper;
import com.njwd.financeback.service.SysMenuOptionService;
import com.njwd.support.Result;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/19 9:28
 */
@Service
public class SysMenuOptionServiceImpl implements SysMenuOptionService {

    @Resource
    private SysMenuOptionMapper sysMenuOptionMapper;
    @Resource
    private SysMenuOptionFeignClient sysMenuOptionFeignClient;

    /**
     * 获取选项信息
     * @param sysMenuOption
     * @return
     */
    @Override
    public SysMenuOptionComplexVo findMenuOption(SysMenuOption sysMenuOption) {

        SysMenuOptionDto platformSysMenuOptionDto = new SysMenuOptionDto();
        platformSysMenuOptionDto.setMenuCode(sysMenuOption.getMenuCode());
        Result<List<SysMenuOptionVo>> ss = sysMenuOptionFeignClient.findSysMenuOptionList(platformSysMenuOptionDto);

        List<SysMenuOptionVo> menuOptionVoList= ss.getData();
        //获取租户选中的选项
        SysUserVo sysUserVo = UserUtils.getUserVo();
        Long rootEnterpriseId =sysUserVo.getRootEnterpriseId();
        sysMenuOption.setRootEnterpriseId(rootEnterpriseId);
        List<SysMenuOptionVo> selectedList = sysMenuOptionMapper.findList(sysMenuOption);
        //标识选中项
        List<SysMenuOptionVo>  localList = new ArrayList<>();
        if(selectedList==null||selectedList.isEmpty()){

            for(SysMenuOptionVo sysMenuOptionVo:menuOptionVoList) {
                if(Constant.Is.YES.equals(sysMenuOptionVo.getIsDefault())){
                    sysMenuOptionVo.setIsSelected(Constant.Is.YES);
                    localList.add(sysMenuOptionVo);
                }
            }
        }else {
            for(SysMenuOptionVo sysMenuOptionVo:menuOptionVoList) {
                for(SysMenuOptionVo s:selectedList){
                    if(sysMenuOptionVo.getDataType().equals(s.getDataType())
                            &&sysMenuOptionVo.getOptionValue().equals(s.getOptionValue())){
                        sysMenuOptionVo.setIsSelected(Constant.Is.YES);
                        localList.add(sysMenuOptionVo);
                        break;
                    }
                }
            }
        }

        List<SysMenuOptionVo> codeOption = new ArrayList<>();
        for(SysMenuOptionVo sysMenuOptionVo:menuOptionVoList){
            if(Constant.SysMenuOption.DATA_TYPE_CODE.equals(sysMenuOptionVo.getDataType())){
                codeOption.add(sysMenuOptionVo);
            }
        }
        if(!codeOption.isEmpty()){
            boolean isCanUpdate = isCanUpdate(platformSysMenuOptionDto);
            if(!isCanUpdate){
                for(SysMenuOptionVo sysMenuOptionVo:codeOption){
                    sysMenuOptionVo.setIsCanUpdate(Constant.Is.NO);
                }
            }
        }
        SysMenuOptionComplexVo sysMenuOptionComplexVo = new SysMenuOptionComplexVo();
        sysMenuOptionComplexVo.setGlobalMenuOptionList(menuOptionVoList);
        sysMenuOptionComplexVo.setLocalMenuOptionList(localList);
        return sysMenuOptionComplexVo;
    }

    private boolean isCanUpdate(SysMenuOptionDto platformSysMenuOptionDto){
         SysMenuOptionTable sysMenuOptionTable = sysMenuOptionFeignClient.findOptionTable(platformSysMenuOptionDto).getData();
        if(sysMenuOptionTable!=null){
            if(Constant.Is.YES.equals(sysMenuOptionTable.getIsFilterRootEnterprise())){
                SysUserVo userVo = UserUtils.getUserVo();
                sysMenuOptionTable.setRootEnterpriseId(userVo.getRootEnterpriseId());
            }
            Integer count = sysMenuOptionMapper.findTableDataCount(sysMenuOptionTable);
            if(count!=null&&count>0){
                return false;
            }
        }
        return true;
    }

    /**
     * 修改选项
     * @param sysMenuOptionList
     * @return
     */
    @Override
    @Transactional
    public int updateMenuOption(List<SysMenuOption> sysMenuOptionList) {
        if(sysMenuOptionList!=null&&!sysMenuOptionList.isEmpty()){
            String menuCode = sysMenuOptionList.get(0).getMenuCode();
            SysUserVo sysUserVo = UserUtils.getUserVo();
            //删除旧数据
            sysMenuOptionMapper.delete(Wrappers.<SysMenuOption>lambdaQuery().eq(SysMenuOption::getMenuCode,menuCode).eq(SysMenuOption::getRootEnterpriseId,sysUserVo.getRootEnterpriseId()));
            //录入选中数据
            Date currentDate = new Date();
            for (SysMenuOption sysMenuOption:sysMenuOptionList){
                sysMenuOption.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
                sysMenuOption.setCreatorId(sysUserVo.getUserId());
                sysMenuOption.setCreatorName(sysUserVo.getName());
                sysMenuOption.setCreateTime(currentDate);
                sysMenuOptionMapper.insert(sysMenuOption);
            }
        }
        return 1;
    }
}
