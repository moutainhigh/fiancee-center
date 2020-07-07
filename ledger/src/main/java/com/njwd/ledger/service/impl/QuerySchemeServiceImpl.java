package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.QueryScheme;
import com.njwd.entity.ledger.QuerySchemeDetail;
import com.njwd.entity.ledger.dto.QuerySchemeDetailDto;
import com.njwd.entity.ledger.dto.QuerySchemeDto;
import com.njwd.entity.ledger.vo.QuerySchemeDetailVo;
import com.njwd.entity.ledger.vo.QuerySchemeVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.mapper.QuerySchemeDetailMapper;
import com.njwd.ledger.mapper.QuerySchemeMapper;
import com.njwd.ledger.service.QuerySchemeService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 查询方案实现类
 * @author: xdy
 * @create: 2019-07-30
 */
@Service
public class QuerySchemeServiceImpl implements QuerySchemeService {

    @Resource
    private QuerySchemeMapper querySchemeMapper;
    @Resource
    private QuerySchemeDetailMapper querySchemeDetailMapper;

    
    /**
     * @description: 新增并修改
     * @param: [querySchemeDto]
     * @return: int 
     * @author: xdy        
     * @create: 2019-07-30 17-34 
     */
    @Override
    @Transactional
    public QuerySchemeVo addOrUpdate(QuerySchemeDto querySchemeDto){
        if(querySchemeDto.getId()==null)
            return addQueryScheme(querySchemeDto);
        return updateQueryScheme(querySchemeDto);
    }
    
    /**
     * @description: 新增
     * @param: [querySchemeDto]
     * @return: int 
     * @author: xdy        
     * @create: 2019-07-30 17-35 
     */
    @Transactional
    public QuerySchemeVo addQueryScheme(QuerySchemeDto querySchemeDto) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        //判断方案数量上限
        Integer count = querySchemeMapper.selectCount(Wrappers.<QueryScheme>lambdaQuery().eq(QueryScheme::getRootEnterpriseId,sysUserVo.getRootEnterpriseId()).eq(QueryScheme::getCreatorId,sysUserVo.getUserId())
                .eq(QueryScheme::getMenuCode,querySchemeDto.getMenuCode()));
        if(count!=null&&count>=LedgerConstant.QueryScheme.MAX_RECORD_COUNT){
            throw new ServiceException(ResultCode.QUERY_SCHEME_MAX_COUNT);
        }
        //校验方案是否存在
        checkQueryScheme(querySchemeDto);
        querySchemeDto.setCreatorId(sysUserVo.getUserId());
        querySchemeDto.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
        querySchemeDto.setVersion(null);
        if(Constant.Is.YES.equals(querySchemeDto.getIsDefault())){
            resetDefault(querySchemeDto.getMenuCode());
        }
        querySchemeMapper.insert(querySchemeDto);
        if(querySchemeDto.getDetails()!=null&&!querySchemeDto.getDetails().isEmpty()){
            addDetail(querySchemeDto);
        }
        return findQuerySchemeById(querySchemeDto);
    }
    
    /**
     * @description: 删除
     * @param: [querySchemeDto]
     * @return: int 
     * @author: xdy        
     * @create: 2019-07-30 17-35 
     */
    @Override
    public int delQueryScheme(QuerySchemeDto querySchemeDto) {
        return querySchemeMapper.deleteById(querySchemeDto.getId());
    }

    /**
     * @description: 修改
     * @param: [querySchemeDto]
     * @return: int
     * @author: xdy
     * @create: 2019-07-30 17-35
     */
    @Transactional
    public QuerySchemeVo updateQueryScheme(QuerySchemeDto querySchemeDto){
        //校验方案是否存在
        checkQueryScheme(querySchemeDto);
        SysUserVo sysUserVo = UserUtils.getUserVo();
        querySchemeDto.setUpdatorId(sysUserVo.getUserId());
        if(Constant.Is.YES.equals(querySchemeDto.getIsDefault())){
            QueryScheme queryScheme = querySchemeMapper.selectById(querySchemeDto.getId());
            resetDefault(queryScheme.getMenuCode());
        }
        int res = querySchemeMapper.updateQueryScheme(querySchemeDto);
        if(querySchemeDto.getDetails()!=null&&!querySchemeDto.getDetails().isEmpty()){
            querySchemeDetailMapper.delete(Wrappers.<QuerySchemeDetail>lambdaQuery().eq(QuerySchemeDetail::getSchemeId,querySchemeDto.getId()));
            addDetail(querySchemeDto);
        }
        QuerySchemeVo querySchemeVo = findQuerySchemeById(querySchemeDto);
        if(res==0){
            querySchemeVo.setIsVersionSame(Constant.Is.NO);
        }
        return querySchemeVo;
    }
    
    /**
     * @description: 校验方案是否存在
     * @param: [querySchemeDto]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-06 10:27 
     */
    public void checkQueryScheme(QuerySchemeDto querySchemeDto){
        SysUserVo sysUserVo = UserUtils.getUserVo();
        LambdaQueryWrapper<QueryScheme> queryWrapper=    Wrappers.<QueryScheme>lambdaQuery()
                .eq(QueryScheme::getRootEnterpriseId,sysUserVo.getRootEnterpriseId()).eq(QueryScheme::getCreatorId,sysUserVo.getUserId())
                .eq(QueryScheme::getMenuCode,querySchemeDto.getMenuCode()).eq(QueryScheme::getSchemeName,querySchemeDto.getSchemeName());
        if(querySchemeDto.getId()!=null)
            queryWrapper.ne(QueryScheme::getId,querySchemeDto.getId());
        Integer count = querySchemeMapper.selectCount(queryWrapper);
        if(count!=null&&count>0)
            throw new ServiceException(ResultCode.NAME_EXIST);
    }

    /**
     * @description: 查询方案明细
     * @param: [querySchemeDto]
     * @return: void 
     * @author: xdy        
     * @create: 2019-07-30 15-25 
     */
    @Transactional
    void addDetail(QuerySchemeDto querySchemeDto){
        for(QuerySchemeDetailDto detail:querySchemeDto.getDetails()){
            detail.setSchemeId(querySchemeDto.getId());
            detail.setFirstValue(stringValue(detail.getValue()));
            detail.setFirstName(stringValue(detail.getName()));
            detail.setColType(valueType(detail.getValue()));
        }
        querySchemeDetailMapper.addBatch(querySchemeDto.getDetails());
    }
    
    /**
     * @description: 查询方案
     * @param: [querySchemeDto]
     * @return: com.njwd.ledger.entity.vo.QuerySchemeVo 
     * @author: xdy        
     * @create: 2019-07-30 17-21 
     */
    @Override
    public QuerySchemeVo findQuerySchemeById(QuerySchemeDto querySchemeDto){
        QueryScheme queryScheme = querySchemeMapper.selectById(querySchemeDto.getId());
        QuerySchemeVo querySchemeVo = new QuerySchemeVo();
        FastUtils.copyProperties(queryScheme,querySchemeVo);
        querySchemeVo.setDetails(findDetails(querySchemeDto.getId()));
        return querySchemeVo;
    }
    
    /**
     * @description: 查询方案
     * @param: [querySchemeDto]
     * @return: java.util.List<com.njwd.ledger.entity.vo.QuerySchemeVo> 
     * @author: xdy        
     * @create: 2019-07-30 17-20 
     */
    @Override
    public List<QuerySchemeVo> findQueryScheme(QuerySchemeDto querySchemeDto) {
        SysUserVo userVo = UserUtils.getUserVo();
        querySchemeDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        querySchemeDto.setCreatorId(userVo.getUserId());
        List<QuerySchemeVo> querySchemes = querySchemeMapper.findQueryScheme(querySchemeDto);
        for(QuerySchemeVo queryScheme:querySchemes){
            if(Constant.Is.YES.equals(queryScheme.getIsDefault())){
                queryScheme.setDetails(findDetails(queryScheme.getId()));
            }
        }
        return querySchemes;
    }

    /**
     * @description: 获取明细
     * @param: [schemeId]
     * @return: java.util.List<com.njwd.ledger.entity.vo.QuerySchemeDetailVo> 
     * @author: xdy        
     * @create: 2019-07-30 17-20 
     */
    public List<QuerySchemeDetailVo> findDetails(Long schemeId){
        List<QuerySchemeDetail> details = querySchemeDetailMapper.selectList(Wrappers.<QuerySchemeDetail>lambdaQuery().eq(QuerySchemeDetail::getSchemeId,schemeId));
        List<QuerySchemeDetailVo> detailVos = new ArrayList<>();
        if(details!=null&&!details.isEmpty()){
            for(QuerySchemeDetail detail:details){
                QuerySchemeDetailVo detailVo = new QuerySchemeDetailVo();
                FastUtils.copyProperties(detail,detailVo);
                detailVo.setName(detailVo.getFirstName());
                detailVo.setValue(objectValue(detail.getColType(),detail.getFirstValue()));
                detailVos.add(detailVo);
            }
        }
        return detailVos;
    }


    /**
     * @description: 重置默认值
     * @param: [menuCode]
     * @return: void 
     * @author: xdy        
     * @create: 2019-07-30 15-12 
     */
    private void resetDefault(String menuCode){
        SysUserVo sysUserVo = UserUtils.getUserVo();
        querySchemeMapper.update(null,Wrappers.<QueryScheme>lambdaUpdate()
                .eq(QueryScheme::getMenuCode,menuCode)
                .eq(QueryScheme::getCreatorId,sysUserVo.getUserId())
                .set(QueryScheme::getIsDefault,Constant.Is.NO));
    }
    
    /**
     * @description: 值类型
     * @param: [value]
     * @return: java.lang.Byte 
     * @author: xdy        
     * @create: 2019-07-30 15-12 
     */
    private Byte valueType(Object value){
        if(value==null)
            return null;
        if(value instanceof Integer)
            return LedgerConstant.QueryScheme.TYPE_INTEGER;
        else if(value instanceof Double)
            return LedgerConstant.QueryScheme.TYPE_DECIMAL;
        else if(value instanceof List){
            return LedgerConstant.QueryScheme.TYPE_ARRAY;
        }else if(value instanceof LinkedHashMap){
            return LedgerConstant.QueryScheme.TYPE_JSON;
        }else
            return LedgerConstant.QueryScheme.TYPE_STRING;
    }

    /**
     * @description: 前端数据转为string
     * @param: [value]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-23 17-16 
     */
    private String stringValue(Object value){
        if(value==null)
            return null;
        if(value instanceof List||value instanceof LinkedHashMap){
            return JsonUtils.object2Json(value);
        }
        return value.toString();
    }

    /**
     * @description: 返回原前端数据
     * @param: [valueType, value]
     * @return: java.lang.Object 
     * @author: xdy        
     * @create: 2019-08-23 17-17 
     */
    private Object objectValue(Byte valueType,String value){
        if(value==null)
            return null;
        if(LedgerConstant.QueryScheme.TYPE_INTEGER==valueType)
            return Integer.valueOf(value);
        else if(LedgerConstant.QueryScheme.TYPE_DECIMAL==valueType)
            return Double.valueOf(value);
        else if(LedgerConstant.QueryScheme.TYPE_ARRAY==valueType)
            return JsonUtils.json2Pojo(value,List.class);
        else if(LedgerConstant.QueryScheme.TYPE_JSON==valueType){
            return JsonUtils.json2Pojo(value, Map.class);
        }
        return value;
    }


}
