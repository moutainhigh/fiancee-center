package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.SysSystem;
import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.platform.mapper.SysSystemMapper;
import com.njwd.platform.service.SysSystemService;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @Author liuxiang
 * @Description 子系统说明
 * @Date:17:01 2019/7/2
 **/
@Service
public class SysSystemServiceImpl implements SysSystemService {

    @Autowired
    private SysSystemMapper sysSystemMapper;

    /**
     * @Description 查询子系统说明列表
     * @Author liuxiang
     * @Date:16:59 2019/7/2
     * @Param [sysSystemDto]
     * @return java.util.List<com.njwd.platform.entity.vo.SysSystemVo>
     **/
    @Override
    @Cacheable(value = "sysSystemList", key = "#sysSystemDto.rootEnterpriseId+''")
    public List<SysSystemVo> findSysSystemList(SysSystemDto sysSystemDto) {
        return sysSystemMapper.findSysSystemList(sysSystemDto);
    }

    /**
     * @Description 根据ID查询子系统说明
     * @Author liuxiang
     * @Date:17:00 2019/7/2
     * @Param [sysSystemDto]
     * @return com.njwd.platform.entity.vo.SysSystemVo
     **/
    @Override
    @Cacheable(value = "sysSystemById", key = "#sysSystemDto.id+''",unless="#result == null")
    public SysSystemVo findSysSystemById(SysSystemDto sysSystemDto) {
        return sysSystemMapper.findSysSystemById(sysSystemDto);
    }
    
    /**
     * @description: 初始化数据
     * @param: []
     * @return: int 
     * @author: xdy        
     * @create: 2019-09-05 15:51 
     */
    @Override
    public int initData(SysSystemDto sysSystemDto) {
        Integer count = sysSystemMapper.selectCount(Wrappers.<SysSystem>lambdaQuery().eq(SysSystem::getRootEnterpriseId,sysSystemDto.getRootEnterpriseId()));
        if(count!=null&&count>0)
            return Constant.Is.YES;
        SysSystem[] sysSystems = new SysSystem[]{
                new SysSystem("总账","ledger")
                //new SysSystem("资产","asset"),
                //new SysSystem("应收","receive")
        };
        for(SysSystem sysSystem:sysSystems){
            sysSystem.setRootEnterpriseId(sysSystemDto.getRootEnterpriseId());
            sysSystem.setCreatorId(sysSystemDto.getCreatorId());
            sysSystem.setCreatorName(sysSystemDto.getCreatorName());
            sysSystemMapper.insert(sysSystem);
        }
        return Constant.Is.YES;
    }
}
