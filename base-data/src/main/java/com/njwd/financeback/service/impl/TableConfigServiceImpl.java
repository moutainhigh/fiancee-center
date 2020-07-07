package com.njwd.financeback.service.impl;

import com.njwd.basedata.cloudclient.SysTabColumnFeignClient;
import com.njwd.entity.basedata.SysMenuTabColumn;
import com.njwd.entity.basedata.dto.SysMenuTabColumnDto;
import com.njwd.entity.basedata.dto.query.TableConfigQueryDto;
import com.njwd.entity.basedata.vo.SysMenuTabColumnVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.SysTabColumnDto;
import com.njwd.entity.platform.vo.SysTabColumnVo;
import com.njwd.financeback.mapper.SysMenuTabColumnMapper;
import com.njwd.financeback.service.TableConfigService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @description:
 * @author: fancl
 * @create: 2019-06-19
 */
@Service
public class TableConfigServiceImpl implements TableConfigService {

    @Resource
    SysMenuTabColumnMapper tabColumnMapper;

    @Autowired
    SysTabColumnFeignClient sysTabColumnFeignClient;

    @Override
    public List<SysMenuTabColumnVo> findList(TableConfigQueryDto queryDto) {
        return tabColumnMapper.findList(queryDto);
    }

    @Override
    public List<SysMenuTabColumn> findUserList(TableConfigQueryDto queryDto) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        queryDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        queryDto.setUserId(sysUserVo.getUserId());
        List<SysMenuTabColumn> sysMenuTabColumns = tabColumnMapper.findUserList(queryDto);
        //获取平台表格设置
        if(sysMenuTabColumns ==null||sysMenuTabColumns.isEmpty()){
            //组装参数
            SysTabColumnDto platformSysTabColumnDto = new SysTabColumnDto();
            platformSysTabColumnDto.setMenuCode(queryDto.getMenuCode());
            platformSysTabColumnDto.setIsEnterpriseAdmin(queryDto.getIsEnterpriseAdmin());
            //调运营平台接口
            Result<List<SysTabColumnVo>> recJsonStr = sysTabColumnFeignClient.findSysTabColumnList(platformSysTabColumnDto);
            List<SysTabColumnVo> columnVos = recJsonStr.getData();
            List<SysMenuTabColumn> globalConfigList = new ArrayList<>();
            for(SysTabColumnVo columnVo:columnVos){
                SysMenuTabColumn sysMenuTabColumn = new SysMenuTabColumn();
                FastUtils.copyProperties(columnVo,sysMenuTabColumn);
                globalConfigList.add(sysMenuTabColumn);
            }
            //处理成本地list
            //List<SysMenuTabColumn> globalConfigList = recJsonStr.getData();
            if(globalConfigList!=null){
                List<SysMenuTabColumn> temp = new ArrayList<>();
                for(SysMenuTabColumn sysMenuTabColumn:globalConfigList){
                    if(new Byte((byte)1).equals(sysMenuTabColumn.getIsShow())){
                        temp.add(sysMenuTabColumn);
                    }
                }
                if(!temp.isEmpty()){
                    temp.sort(Comparator.comparing(SysMenuTabColumn::getSortNum));
                    sysMenuTabColumns = temp;
                }
            }
        }
        return sysMenuTabColumns;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(TableConfigQueryDto queryDto, List<SysMenuTabColumnDto> tabColumnList)  {
        //获取用户
        @NotNull SysUserVo userVo = UserUtils.getUserVo();
        queryDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        queryDto.setUserId(userVo.getUserId());
        //先删除数据
        tabColumnMapper.batchDelete(queryDto);
        //再插入列表数据
        for(SysMenuTabColumnDto tab:tabColumnList){
            tab.setRootEnterpriseId(queryDto.getRootEnterpriseId());
            tab.setCreatorId(userVo.getUserId());
            if(queryDto.getSchemeId()==null){
                tab.setSchemeId(-1L);
            }else{
                tab.setSchemeId(queryDto.getSchemeId());
            }
        }
        tabColumnMapper.batchUpdate(tabColumnList, queryDto.getIsEnterpriseAdmin());

    }
}
