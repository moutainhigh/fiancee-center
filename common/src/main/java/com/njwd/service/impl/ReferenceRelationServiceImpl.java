package com.njwd.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.mapper.ReferenceRelationMapper;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.Result;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @description: 是否被引用
 * @author: xdy
 * @create: 2019/6/27 17:10
 */
@Service
public class ReferenceRelationServiceImpl implements ReferenceRelationService {

    @Resource
    private ReferenceRelationMapper referenceRelationMapper;
    @Resource(name="restTemplate0")
    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(ReferenceRelationServiceImpl.class);


    /**
     * @description: 是否被引用
     * @param: [businessModule, id]
     * @return: com.njwd.entity.basedata.ReferenceResult
     * @author: xdy
     * @create: 2019-06-28 10-34
     */
    @Override
    public ReferenceResult isReference(@NotNull String businessModule,@NotNull Long id) {
        List<ReferenceRelation> relations = findReferenceRelation0(businessModule);
        Map<String,Map<String,Object>> businessMap = findBusinessData(Arrays.asList(id),relations);
        ReferenceResult referenceResult = isReference(id,relations,businessMap.get(String.valueOf(id)));
        if(referenceResult.isReference()){
            logger.debug("businessModule:{},businessId:{},referenceDescription:{}",businessModule,id,referenceResult.getReferenceDescription());
        }
        return referenceResult;
    }


    /**
     * @description: 是否被引用
     * @param: [id, relations]
     * @return: com.njwd.entity.basedata.ReferenceResult
     * @author: xdy        
     * @create: 2019-06-28 10-36 
     */
    private ReferenceResult isReference(@NotNull Object key,List<ReferenceRelation> relations,Map<String,Object> businessData) {
        ReferenceResult referenceResult = new ReferenceResult();
        referenceResult.setReference(false);
        if(relations!=null){
            for(ReferenceRelation referenceRelation:relations){
                referenceRelation.setBusinessKey(key);
                if(StringUtils.isNoneBlank(referenceRelation.getBusinessColumn())&&businessData!=null)
                    referenceRelation.setBusinessKey(businessData.get(lineToHump(referenceRelation.getBusinessColumn())));
                referenceRelation.setReferenceColumnArr(referenceRelation.getReferenceColumn().split(","));
                if(Constant.Is.YES.equals(referenceRelation.getIsFilterRootEnterprise())){
                    SysUserVo sysUserVo = UserUtils.getUserVo();
                    referenceRelation.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
                }
                String serviceName = referenceRelation.getServiceName();
                if(StringUtils.isBlank(serviceName)){
                    referenceResult.setReference(true);
                    referenceResult.setReferenceDescription("联系管理员配置引用服务名称");
                    break;
                }
                convert(referenceRelation);
                try{
                    String serviceUrl = findServiceUrl(serviceName)+"/findReferenceCount";
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<String> request = new HttpEntity(JsonUtils.object2Json(referenceRelation),headers);
                    ParameterizedTypeReference<Result<Integer>> responseBodyType = new ParameterizedTypeReference<Result<Integer>>(){};
                    String businessModule = referenceRelation.getBusinessModule();
                    Integer count;
                    if(businessModule.contains("plat")){
                        count = findReferenceCount(referenceRelation);
                    }else {
                        ResponseEntity<Result<Integer>> resp = restTemplate.exchange(serviceUrl, HttpMethod.POST,request,responseBodyType);
                        Result<Integer> result = resp.getBody();
                         count = result.getData();
                    }
                    if(count!=null&&count>0){
                        referenceResult.setReference(true);
                        if(StringUtils.isNoneBlank(referenceRelation.getReferenceDescription())){
                            referenceResult.setReferenceDescription(referenceRelation.getReferenceDescription());
                        }else {
                            referenceResult.setReferenceDescription(String.format("已被%s模块引用",referenceRelation.getReferenceModule()));
                        }
                        break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    referenceResult.setReference(true);
                    referenceResult.setReferenceDescription(String.format("%s服务引用调用异常",serviceName));
                    break;
                }
            }
        }
        return referenceResult;
    }

    /**
     * @description: 下划线转驼峰
     * @param: [str]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-09-16 14:27 
     */
    private String lineToHump(String str) {
        Pattern linePattern = Pattern.compile("_(\\w)");
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * @description: 下划线转驼峰
     * @param: [str, map]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-09-16 14:28 
     */
    private String lineToHump(String str,Map<String,String> map){
        if(map==null)
            return lineToHump(str);
        String res = map.get(str);
        if(res==null){
            res = lineToHump(str);
            map.put(str,res);
        }
        return res;
    }

    /**
     * @description: 服务地址
     * @param: [serviceName]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-14 16-24 
     */
    private String findServiceUrl(String serviceName){
        StringBuffer sb = new StringBuffer();
        sb.append("http://").append(serviceName).append("/");
        switch (serviceName){
            case Constant.Context.BASE_DATA_FEIGN:
                sb.append(Constant.Context.BASE_DATA);
                break;
            case Constant.Context.LEDGER_FEIGN:
                sb.append(Constant.Context.LEDGER);
                break;
            case Constant.Context.PLATFORM_FEIGN:
                sb.append(Constant.Context.PLATFORM);
                break;
        }
        sb.append("referenceRelation");
        return sb.toString();
    }

    
    /**
     * @description: 是否被引用 暂时废弃
     * @param: [businessModule, ids]
     * @return: com.njwd.entity.basedata.ReferenceContext
     * @author: xdy
     * @create: 2019-06-28 10-34
     */
    @Override
    public ReferenceContext isReference0(@NotNull String businessModule, @NotNull List<Long> ids){
        List<ReferenceRelation> relations = findReferenceRelation0(businessModule);
        ReferenceContext referenceContext = new ReferenceContext();
        Map<String,Map<String,Object>> businessMap = findBusinessData(ids,relations);
        for(Long id:ids){
            ReferenceResult referenceResult = isReference(id,relations,businessMap.get(String.valueOf(id)));
            if(referenceResult.isReference()){
                ReferenceDescription description = new ReferenceDescription();
                description.setBusinessId(id);
                description.setReferenceDescription(referenceResult.getReferenceDescription());
                referenceContext.getReferences().add(description);
            }else {
                referenceContext.getNotReferences().add(id);
            }
        }
        if(!referenceContext.getReferences().isEmpty()){
            logger.debug("businessModule:{},referenceData:{}",businessModule,referenceContext.getReferences());
        }
        return referenceContext;
    }

    /**
     * @description: 
     * @param: [ids, relations]
     * @return: java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Object>> 
     * @author: xdy        
     * @create: 2019-09-16 14:14 
     */
    private Map<String, Map<String,Object>> findBusinessData(List<Long> ids,List<ReferenceRelation> relations){
        Map<String, Map<String,Object>> res = new HashMap<>();
        if(ids==null||ids.isEmpty()||relations==null||relations.isEmpty()){
            return res;
        }
        ReferenceRelation queryRelation=null;
        for(ReferenceRelation relation:relations){
            if(StringUtils.isNoneBlank(relation.getBusinessColumn())){
                queryRelation = relation;
                break;
            }
        }
        if(queryRelation!=null){
            queryRelation.setBusinessIds(ids);
            String serviceUrl = findServiceUrl(queryRelation.getBusinessServiceName())+"/findBusinessData";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity(JsonUtils.object2Json(queryRelation),headers);
            ParameterizedTypeReference<Result<List<Map<String,Object>>>> responseBodyType = new ParameterizedTypeReference<Result<List<Map<String,Object>>>>(){};
            String businessModule = queryRelation.getBusinessModule();
            List<Map<String, Object>> list = null;
            if(businessModule.contains("plat")){
                list = findBusinessData(queryRelation);
            }else {
                ResponseEntity<Result<List<Map<String,Object>>>> resp = restTemplate.exchange(serviceUrl, HttpMethod.POST,request,responseBodyType);
                list = resp.getBody().getData();
            }
            if(list!=null){
                res = list.stream().collect(Collectors.toMap(i->String.valueOf(i.get("id")),i->i));
            }
        }
        return res;
    }
    
    /**
     * @description: 获取业务数据
     * @param: [referenceRelation]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @author: xdy        
     * @create: 2019-09-16 14:18 
     */
    @Override
    public List<Map<String,Object>> findBusinessData(ReferenceRelation referenceRelation){
        return referenceRelationMapper.findBusinessData(referenceRelation);
    }
    
    /**
     * @description: 是否引用
     * @param: [businessModule, code]
     * @return: com.njwd.entity.basedata.ReferenceResult 
     * @author: xdy        
     * @create: 2019-09-16 14:28 
     */
    @Override
    public ReferenceResult isReferenceByCode(@NotNull String businessModule, @NotNull String code) {
        List<ReferenceRelation> relations = findReferenceRelation0(businessModule);
        return isReference(code,relations,null);
    }
    
    /**
     * @description: 是否引用
     * @param: [businessModule, codes]
     * @return: com.njwd.entity.basedata.ReferenceContext 
     * @author: xdy        
     * @create: 2019-09-16 14:29 
     */
    @Override
    public ReferenceContext isReferenceByCode(@NotNull String businessModule, @NotNull List<String> codes){
        List<ReferenceRelation> relations = findReferenceRelation0(businessModule);
        ReferenceContext referenceContext = new ReferenceContext();
        for(String code:codes){
            ReferenceResult referenceResult = isReference(code,relations,null);
            if(referenceResult.isReference()){
                ReferenceDescription description = new ReferenceDescription();
                description.setBusinessCode(code);
                description.setReferenceDescription(referenceResult.getReferenceDescription());
                referenceContext.getReferences().add(description);
            }else {
                referenceContext.getNotReferenceCodes().add(code);
            }
        }
        return referenceContext;
    }

    /**
     * @description: 获取引用关系
     * @param: [businessModule]
     * @return: java.util.List<com.njwd.entity.basedata.ReferenceRelation> 
     * @author: xdy        
     * @create: 2019-09-16 14:30 
     */
    @Override
    public List<ReferenceRelation> findReferenceRelation(String businessModule){
        List<ReferenceRelation> relations = referenceRelationMapper.selectList(Wrappers.<ReferenceRelation>lambdaQuery()
                .eq(ReferenceRelation::getBusinessModule,businessModule).orderByAsc(ReferenceRelation::getSort));
        return relations;
    }
    
    /**
     * @description: 获取引用关系
     * @param: [businessModule]
     * @return: java.util.List<com.njwd.entity.basedata.ReferenceRelation> 
     * @author: xdy        
     * @create: 2019-09-16 14:30 
     */
    public List<ReferenceRelation> findReferenceRelation0(String businessModule){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap();
        map.add("businessModule", businessModule);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(map,headers);
        ParameterizedTypeReference<Result<List<ReferenceRelation>>> responseBodyType = new ParameterizedTypeReference<Result<List<ReferenceRelation>>>(){};
        ResponseEntity<Result<List<ReferenceRelation>>> resp = null;
        List<ReferenceRelation> referenceRelations = null;
        if(businessModule.contains("plat")){
            referenceRelations = findReferenceRelation(businessModule);
        }else {
            resp = restTemplate.exchange("http://"+Constant.Context.PLATFORM_FEIGN+"/platform/referenceRelation/findReferenceRelation",HttpMethod.POST,request,responseBodyType);
            Result<List<ReferenceRelation>> result = resp.getBody();
            referenceRelations = result.getData();
        }
        return referenceRelations;
    }
    
    /**
     * @description: 分表转换
     * @param: [referenceRelation]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-16 14:31 
     */
    private void convertTable(ReferenceRelation referenceRelation){
        Pattern p = Pattern.compile("\\b(wd_voucher|wd_voucher_entry|wd_voucher_entry_auxiliary|wd_voucher_entry_cash_flow)\\b");
        Matcher m = p.matcher(referenceRelation.getReferenceTable());
        StringBuffer sb = new StringBuffer();
        if(m.find()){
            SysUserVo userVo = UserUtils.getUserVo();
            while (m.find()){
                m.appendReplacement(sb,m.group()+"_"+userVo.getRootEnterpriseId());
            }
        }
        m.appendTail(sb);
        referenceRelation.setReferenceTable(sb.toString());
    }

    /**
     * @description: 数据转换
     * @param: [referenceRelation]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-16 14:31 
     */
    private void convert(ReferenceRelation referenceRelation){
        convertTable(referenceRelation);
    }
    
    /**
     * @description: 获取被引用次数
     * @param: [referenceRelation]
     * @return: java.lang.Integer 
     * @author: xdy        
     * @create: 2019-09-16 14:31 
     */
    @Override
    public Integer findReferenceCount(ReferenceRelation referenceRelation){
        Integer count = referenceRelationMapper.findReferenceCount(referenceRelation);
        return count;
    }

    /**
     * @description: 批量获取被引用次数
     * @param: [referenceRelation]
     * @return: java.util.List<com.njwd.entity.basedata.ReferenceRelation> 
     * @author: xdy        
     * @create: 2019-09-16 14:32 
     */
    @Override
    public List<ReferenceRelation> findReferenceCountList(ReferenceRelation referenceRelation){
        List<ReferenceRelation> referenceRelations = new ArrayList<>();
        for(String referenceColumn:referenceRelation.getReferenceColumnArr()){
            referenceRelation.setReferenceColumn(referenceColumn);
            List<ReferenceRelation> list = referenceRelationMapper.findReferenceCountList(referenceRelation);
            if(list!=null)
                referenceRelations.addAll(list);
        }
        return referenceRelations;
    }
    
    /**
     * @description: 是否被引用
     * @param: [referenceRelation, businessMap]
     * @return: com.njwd.entity.basedata.ReferenceContext 
     * @author: xdy        
     * @create: 2019-09-16 14:34 
     */
    public ReferenceContext isReference(ReferenceRelation referenceRelation,Map<String,Map<String,Object>> businessMap){
        ReferenceContext referenceContext = new ReferenceContext();
        String serviceName = referenceRelation.getServiceName();
        if(StringUtils.isBlank(serviceName)){
            for(Long id:referenceRelation.getBusinessIds()){
                ReferenceDescription referenceDescription = new ReferenceDescription();
                referenceDescription.setBusinessId(id);
                referenceDescription.setReferenceDescription("联系管理员配置引用服务名称");
                referenceContext.getReferences().add(referenceDescription);
            }
            return referenceContext;
        }
        List<Object> businessKeys = new ArrayList<>();
        Map<String,String> columnMap = new HashMap<>();
        Map<String,Long> keyToId = new HashMap<>();
        if(StringUtils.isNoneBlank(referenceRelation.getBusinessColumn())&&businessMap!=null){
            for(Long id:referenceRelation.getBusinessIds()){
                Map<String,Object> businessData = businessMap.get(String.valueOf(id));
                if(businessData!=null){
                    Object key =businessData.get(lineToHump(referenceRelation.getBusinessColumn(),columnMap));
                    businessKeys.add(key);
                    keyToId.put(String.valueOf(key),id);
                }
            }
        }else{
            for(Long id:referenceRelation.getBusinessIds()){
                businessKeys.add(id);
                keyToId.put(String.valueOf(id),id);
            }
        }
        referenceRelation.setBusinessKeys(businessKeys);
        try {
            String serviceUrl = findServiceUrl(referenceRelation.getServiceName())+"/findReferenceCountList";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity(JsonUtils.object2Json(referenceRelation),headers);
            ParameterizedTypeReference<Result<List<ReferenceRelation>>> responseBodyType = new ParameterizedTypeReference<Result<List<ReferenceRelation>>>(){};
            String businessModule = referenceRelation.getBusinessModule();
            List<ReferenceRelation> referenceRelations = null;
            Result<List<ReferenceRelation>> result = null;
            if(businessModule.contains("plat")){
                referenceRelations = findReferenceCountList(referenceRelation);
            }else {
                ResponseEntity<Result<List<ReferenceRelation>>> resp = restTemplate.exchange(serviceUrl, HttpMethod.POST,request,responseBodyType);
                result = resp.getBody();
                referenceRelations = result.getData();
            }
            if(referenceRelations!=null){
                Map<Long,List<ReferenceRelation>> map = referenceRelations.stream().map(i->{
                    i.setBusinessId(keyToId.get(String.valueOf(i.getBusinessKey())));
                    return i;
                }).collect(Collectors.groupingBy(i->i.getBusinessId()));
                Map<Long,Integer> countMap = new HashMap<>();
                for(Map.Entry<Long,List<ReferenceRelation>> entry:map.entrySet()){
                    List<ReferenceRelation> list = entry.getValue();
                    Integer referenceCount = list.stream().max(Comparator.comparing(ReferenceRelation::getReferenceCount)).get().getReferenceCount();
                    countMap.put(entry.getKey(),referenceCount);
                }
                for(Long id:referenceRelation.getBusinessIds()){
                    Integer count = countMap.get(id);
                    if(count!=null&&count>0){
                        ReferenceDescription description = new ReferenceDescription();
                        description.setBusinessId(id);
                        if(StringUtils.isNoneBlank(referenceRelation.getReferenceDescription())){
                            description.setReferenceDescription(referenceRelation.getReferenceDescription());
                        }else {
                            description.setReferenceDescription(String.format("已被%s模块引用",referenceRelation.getReferenceModule()));
                        }
                        referenceContext.getReferences().add(description);
                    }else {
                        referenceContext.getNotReferences().add(id);
                    }
                }
            }else{
                for(Long id:referenceRelation.getBusinessIds()){
                    ReferenceDescription referenceDescription = new ReferenceDescription();
                    referenceDescription.setBusinessId(id);
                    referenceDescription.setReferenceDescription(String.format("%s服务引用检测异常",serviceName));
                    referenceContext.getReferences().add(referenceDescription);
                }
            }
        }catch (Exception e){
            for(Long id:referenceRelation.getBusinessIds()){
                ReferenceDescription referenceDescription = new ReferenceDescription();
                referenceDescription.setBusinessId(id);
                referenceDescription.setReferenceDescription(String.format("%s服务引用调用异常",serviceName));
                referenceContext.getReferences().add(referenceDescription);
            }
        }
        return referenceContext;
    }

    /**
     * @description: 是否被引用
     * @param: [businessModule, ids]
     * @return: com.njwd.entity.basedata.ReferenceContext 
     * @author: xdy        
     * @create: 2019-09-16 14:34 
     */
    @Override
    public ReferenceContext isReference(@NotNull String businessModule, @NotNull List<Long> ids){
        return isReference(businessModule,ids,null);
    }
    
    /**
     * @description: 是否被引用
     * @param: [businessModule, ids, ignoreTables]
     * @return: com.njwd.entity.basedata.ReferenceContext 
     * @author: xdy        
     * @create: 2019-09-16 14:34 
     */
    @Override
    public ReferenceContext isReference(@NotNull String businessModule, @NotNull List<Long> ids,List<String> ignoreTables){
        List<ReferenceRelation> relations = findReferenceRelation0(businessModule);
        ReferenceContext referenceContext = new ReferenceContext();
        Map<String,Map<String,Object>> businessMap = findBusinessData(ids,relations);
        for(ReferenceRelation referenceRelation:relations){
            if(ids.isEmpty())
                break;
            if(ignoreTables!=null&&ignoreTables.contains(referenceRelation.getReferenceTable())){
                continue;
            }
            convert(referenceRelation);
            referenceRelation.setReferenceColumnArr(referenceRelation.getReferenceColumn().split(","));
            if(Constant.Is.YES.equals(referenceRelation.getIsFilterRootEnterprise())){
                SysUserVo sysUserVo = UserUtils.getUserVo();
                referenceRelation.setRootEnterpriseId(sysUserVo.getRootEnterpriseId());
            }
            referenceRelation.setBusinessIds(ids);
            ReferenceContext context = isReference(referenceRelation,businessMap);
            referenceContext.getReferences().addAll(context.getReferences());
            referenceContext.setNotReferences(context.getNotReferences());
            ids = context.getNotReferences();
        }
        if(!referenceContext.getReferences().isEmpty()) {
            logger.debug("businessModule:{},referenceData:{}",businessModule,referenceContext.getReferences());
        }
        return referenceContext;
    }

}
