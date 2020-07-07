package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.api.PublicDataApi;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.SequenceDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.AccountBookPeriodReport;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.ledger.VoucherAdjust;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.AccountBookPeriodReportDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodReportVo;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.mapper.AccountBookPeriodMapper;
import com.njwd.ledger.mapper.AccountBookPeriodReportMapper;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.VoucherAdjustService;
import com.njwd.ledger.service.VoucherService;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/7 11:33
 */
@Service
public class VoucherAdjustServiceImpl implements VoucherAdjustService {

    @Resource
    private AccountBookPeriodService accountBookPeriodService;
    @Resource
    private AccountBookPeriodMapper accountBookPeriodMapper;
    @Resource
    private VoucherService voucherService;
    @Resource
    private PublicDataApi publicDataApi;

    //1：记 、2：收、3：付、4：转
    private String[] wordNameArr = {"","记","收","付","转"};
    @Resource
    private AccountBookPeriodReportMapper reportMapper;
    @Resource
    private AccountBookFeignClient accountBookFeignClient;


    /**
     * @description: 整理断号
     * @param: [ids] 账簿期间ID集合
     * @return: java.util.List<com.njwd.entity.ledger.VoucherAdjust> 
     * @author: xdy        
     * @create: 2019-08-08 14-06 
     */
    @Override
    @Transactional
    public List<VoucherAdjust> adjust(List<Long> ids) {
        if(ids!=null&&!ids.isEmpty()){
            AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
            accountBookPeriodDto.setIdList(ids);
            List<AccountBookPeriodVo> accountBookPeriods = accountBookPeriodService.findPeriodRangeByAccBookIdsAndSystemSign(accountBookPeriodDto);
            return LedgerUtils.lockAccountBook(()->
                adjustExcute(accountBookPeriods)
            ,accountBookPeriods.toArray(new AccountBookPeriod[0]));
        }
        return new ArrayList<>();
    }

    /**
    * @Description
    * @Author 朱小明
    * @Date 2019/9/23
    * @param accountBookPeriods
    * @return java.lang.Object
    **/
    @Override
    @Transactional
    public List<VoucherAdjust> adjustExcute(List<AccountBookPeriodVo> accountBookPeriods) {
        List<VoucherAdjust> list = new ArrayList<>();
        for(AccountBookPeriodVo accountBookPeriod:accountBookPeriods){
            List<Voucher> vouchers = voucherService.findVouchersByPeriod(accountBookPeriod, Arrays.asList(LedgerConstant.VoucherStatus.PENDING, LedgerConstant.VoucherStatus.POSTING,LedgerConstant.VoucherStatus.POST));
            Map<String,List<Voucher>> voucherMap = new HashMap<>();
            for(Voucher voucher:vouchers){
                if(Constant.Is.NO.equals(voucher.getIsDel())){
                    //按凭证字 列表
                    putData(voucherMap,generateMainKey(voucher.getCredentialWord()),voucher);
                    //按主体-凭证字 列表
                    putData(voucherMap,generateChildKey(voucher.getAccountBookEntityId(),voucher.getCredentialWord()),voucher);
                }
            }
            //查询所有断号索引
            Map<String,List<Integer>> indexMap = findBrokenIndex(voucherMap,LedgerConstant.VoucherAdjust.BROKEN_GROUP_ONE);

            VoucherAdjust voucherAdjust = new VoucherAdjust();
            if(!indexMap.isEmpty()){
                adjust(voucherMap,indexMap,accountBookPeriod);
                FastUtils.copyProperties(accountBookPeriod,voucherAdjust);
                voucherAdjust.setAdjustStatus(LedgerConstant.VoucherAdjust.ADJUST_STATUS_OK);
            }else {
                FastUtils.copyProperties(accountBookPeriod,voucherAdjust);
                voucherAdjust.setAdjustStatus(LedgerConstant.VoucherAdjust.ADJUST_STATUS_NORMAL);
            }
            //重置序列号
            resetCode(voucherMap);
            list.add(voucherAdjust);
        }
        return list;
    }

    /**
     * @description: 凭证整理
     * @param: [voucherMap(凭证map), indexMap(断号索引map)]
     * @return: boolean
     * @author: xdy
     * @create: 2019-08-08 14-28
     */
    public boolean adjust(Map<String,List<Voucher>> voucherMap,Map<String,List<Integer>> indexMap,AccountBookPeriod accountBookPeriod){
        Set<Voucher> toUpdate = new HashSet<>();
        Map<String, AccountBookPeriodReport> reportMap = new HashMap<>();
        for(String key:indexMap.keySet()){
            List<Integer> indexs = indexMap.get(key);
            if(indexs!=null&&!indexs.isEmpty()){
                Integer index = indexs.get(0);
                List<Voucher> vouchers = voucherMap.get(key);
                if(vouchers!=null){
                    for(int i=index;i<vouchers.size();i++){
                        Voucher voucher = vouchers.get(i);
                        //修改前
                        putReport(reportMap,accountBookPeriod,voucher);
                        if(i==0){
                            setCode(voucher,key,LedgerConstant.VoucherAdjust.FIRST_CODE);
                        }else{
                            Integer code = getCode(vouchers.get(i-1),key)+1;
                            setCode(voucher,key,code);
                        }
                        //
                        toUpdate.add(voucher);
                        //修改后
                        putReport(reportMap,accountBookPeriod,voucher);

                    }
                }
            }
        }
        //保存报告数据
        List<AccountBookPeriodReport> reportList = new ArrayList<>(reportMap.values());
        if(reportList!=null&&!reportList.isEmpty()){
            reportMapper.delete(Wrappers.<AccountBookPeriodReport>lambdaQuery().eq(AccountBookPeriodReport::getPeriodId,accountBookPeriod.getId()));
            reportMapper.addBatch(reportList);
        }
        //修改凭证号
        if(toUpdate!=null&&!toUpdate.isEmpty()){
            voucherService.updateVoucherCode(new ArrayList<>(toUpdate));
        }
        //凭证期间状态
        SysUserVo userVo = UserUtils.getUserVo();
        AccountBookPeriod periodUpdate = new AccountBookPeriod();
        periodUpdate.setId(accountBookPeriod.getId());
        periodUpdate.setAdjustTime(new Date());
        periodUpdate.setIsMakeReport(Constant.Is.YES);
        periodUpdate.setAdjustUserId(userVo.getUserId());
        periodUpdate.setAdjustUserName(userVo.getName());
        accountBookPeriodMapper.updateById(periodUpdate);

        accountBookPeriod.setIsMakeReport(periodUpdate.getIsMakeReport());
        accountBookPeriod.setAdjustTime(periodUpdate.getAdjustTime());
        accountBookPeriod.setAdjustUserId(periodUpdate.getAdjustUserId());
        accountBookPeriod.setAdjustUserName(periodUpdate.getAdjustUserName());
        return true;
    }

    /**
     * @description: 获取待整理列表
     * @param: [accountBookPeriodDto] 账簿期间   账簿ID集合
     * @return: java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: xdy
     * @create: 2019-08-07 14-21
     */
    @Override
    public Page<AccountBookPeriodVo> findToAdjustList(AccountBookPeriodDto accountBookPeriodDto){
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setMenuCode(accountBookPeriodDto.getMenuCode());
        Result<List<AccountBookVo>> result = accountBookFeignClient.findAuthAll(accountBookDto);
        List<AccountBookVo> accountBookVoList = result.getData();
        if(accountBookVoList==null||accountBookVoList.isEmpty()){
            return null;
        }
        List<Long> accountBookIdList = accountBookVoList.stream().map(i->i.getId()).collect(Collectors.toList());
        if(accountBookPeriodDto.getAccountBookIds()==null||accountBookPeriodDto.getAccountBookIds().isEmpty()){
            accountBookPeriodDto.setAccountBookIds(accountBookIdList);
        }else{
            List<Long> ids = accountBookPeriodDto.getAccountBookIds().stream().filter(i->accountBookIdList.contains(i)).collect(Collectors.toList());
            if(ids==null||ids.isEmpty()) {
                return null;
            }
            accountBookPeriodDto.setAccountBookIds(ids);
        }
        SysUserVo userVo = UserUtils.getUserVo();
        accountBookDto = new AccountBookDto();
        accountBookDto.setCompanyId(accountBookPeriodDto.getCompanyId());
        AccountBookVo accountBookVo =  accountBookFeignClient.findAccountBookByCompanyId(accountBookDto).getData();
        accountBookPeriodDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        if(accountBookVo!=null) {
            accountBookPeriodDto.setAccountBookId(accountBookVo.getId());
        }
        return accountBookPeriodService.findToAdjustPage(accountBookPeriodDto);
    }

    /**
     * @description: 检测断号
     * @param: [ids] 账簿期间ID集合
     * @return: java.util.List<com.njwd.entity.ledger.VoucherAdjust>
     * @author: xdy
     * @create: 2019-08-08 11-41
     */
    @Override
    public List<VoucherAdjust> checkBroken(List<Long> ids){
        return checkBroken(ids,Constant.Is.NO);
    }

    /**
     * @description: 检测断号
     * @param: [ids, isLock]
     * @return: java.util.List<com.njwd.entity.ledger.VoucherAdjust> 
     * @author: xdy        
     * @create: 2019-09-17 10:24 
     */
    @Override
    public List<VoucherAdjust> checkBroken(List<Long> ids, Byte isLock){
        List<VoucherAdjust> list = new ArrayList<>();
        if(ids!=null&&!ids.isEmpty()){
            AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
            accountBookPeriodDto.setIdList(ids);
            List<AccountBookPeriodVo> accountBookPeriods = accountBookPeriodService.findPeriodRangeByAccBookIdsAndSystemSign(accountBookPeriodDto);
            if(Constant.Is.YES.equals(isLock)){
                LedgerUtils.lockAccountBook(()->{
                    checkBroken(accountBookPeriods,list);
                    return null;
                },accountBookPeriods.toArray(new AccountBookPeriod[0]));
            }else{
                checkBroken(accountBookPeriods,list);
            }
        }
        return list;
    }

    /**
     * @description:
     * @param: [accountBookPeriods, list]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-17 10:23 
     */
    private void checkBroken(List<AccountBookPeriodVo> accountBookPeriods,List<VoucherAdjust> list){
        for(AccountBookPeriodVo accountBookPeriod:accountBookPeriods){
            List<Voucher> vouchers = voucherService.findVouchersByPeriod(accountBookPeriod,Arrays.asList(LedgerConstant.VoucherStatus.PENDING,LedgerConstant.VoucherStatus.POSTING,LedgerConstant.VoucherStatus.POST));
            VoucherAdjust voucherAdjust = new VoucherAdjust();
            FastUtils.copyProperties(accountBookPeriod,voucherAdjust);
            voucherAdjust.setAdjustStatus(LedgerConstant.VoucherAdjust.ADJUST_STATUS_OK);
            if(vouchers!=null&&!vouchers.isEmpty()){
                Map<String,List<Voucher>> voucherMap = new HashMap<>();
                Map<String,List<Voucher>> delMap = new HashMap<>();
                for(Voucher voucher:vouchers){
                    if(Constant.Is.NO.equals(voucher.getIsDel())){
                        //按凭证字 列表
                        putData(voucherMap,generateMainKey(voucher.getCredentialWord()),voucher);
                        //按主体-凭证字 列表
                        putData(voucherMap,generateChildKey(voucher.getAccountBookEntityId(),voucher.getCredentialWord()),voucher);
                    }else{//已删除列表
                        //按凭证字 列表
                        putData(delMap,generateMainKey(voucher.getCredentialWord(),voucher.getMainCode()),voucher);
                        //按主体-凭证字 列表
                        putData(delMap,generateChildKey(voucher.getAccountBookEntityId(),voucher.getCredentialWord(),voucher.getChildCode()),voucher);
                    }
                }
                //查询所有断号索引
                Map<String,List<Integer>> indexMap = findBrokenIndex(voucherMap,LedgerConstant.VoucherAdjust.BROKEN_ALL);
                //存在断号
                if(!indexMap.isEmpty()){
                    voucherAdjust.setAdjustStatus(LedgerConstant.VoucherAdjust.ADJUST_STATUS_FAIL);
                    voucherAdjust.setBrokenVoucher(new ArrayList(findDelCredential(voucherMap,delMap,indexMap)));
                }
            }
            list.add(voucherAdjust);
        }
    }

    /**
     * @description: 获取报告数据
     * @param: [accountBookPeriodReportDto] 断号报告
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodReportVo>
     * @author: xdy
     * @create: 2019-08-09 15-50
     */
    @Override
    public Page<AccountBookPeriodReportVo> findReportPage(AccountBookPeriodReportDto accountBookPeriodReportDto) {
        Page<AccountBookPeriodReportVo> page = accountBookPeriodReportDto.getPage();
        Page<AccountBookPeriodReportVo> businessUnitVoList = reportMapper.findPage(page,accountBookPeriodReportDto);
        return  businessUnitVoList;
    }


    /**
     * @description: 查询断号索引 0全局一个，1分组各一个，2全部
     * @param: [voucherMap(凭证map), brokenDepth(查找断号深度)]
     * @return: java.util.Map<java.lang.String,java.util.List<java.lang.Integer>> 
     * @author: xdy        
     * @create: 2019-08-08 09-33 
     */
    private Map<String,List<Integer>> findBrokenIndex(Map<String,List<Voucher>> voucherMap,Byte brokenDepth){
        Map<String,List<Integer>> indexMap = new HashMap<>();
        for(String key:voucherMap.keySet()){
            List<Voucher> vouchers = voucherMap.get(key);
            if(vouchers!=null){
                //查询断号位置
                Integer prevCode = null;
                for(int i=0;i<vouchers.size();i++){
                    Integer currentCode = getCode(vouchers.get(i),key);
                    boolean isBroken = false;
                    if(currentCode==null){
                        isBroken = true;
                    }else if(i==0){
                        if(!currentCode.equals(LedgerConstant.VoucherAdjust.FIRST_CODE)) {
                            isBroken = true;
                        }
                    }else{
                        if(prevCode==null){
                            if(!currentCode.equals(LedgerConstant.VoucherAdjust.FIRST_CODE)) {
                                isBroken = true;
                            }
                        }else if(!currentCode.equals(prevCode+1)){
                            isBroken = true;
                        }
                    }
                    if(currentCode!=null) {
                        prevCode = currentCode;
                    }
                    if(isBroken){
                        putData(indexMap,key,i);
                        if(LedgerConstant.VoucherAdjust.BROKEN_GLOBAL_ONE==brokenDepth){
                            return indexMap;
                        }
                        else if(LedgerConstant.VoucherAdjust.BROKEN_GROUP_ONE==brokenDepth){
                            break;
                        }
                    }
                }
            }
        }
        return indexMap;
    }

    /**
     * @description: 获取删除的凭证
     * @param: [voucherMap(凭证map), delMap(删除凭证map), indexMap(断号索引凭证)]
     * @return: java.util.Set<java.lang.String> 
     * @author: xdy        
     * @create: 2019-08-08 09-48 
     */
    private Set<Voucher> findDelCredential(Map<String,List<Voucher>> voucherMap,Map<String,List<Voucher>> delMap,Map<String,List<Integer>> indexMap){
        //Set<String> credentialArr = new HashSet<>();
        Set<Voucher> brokenVoucher = new HashSet<>();
        for(String key:indexMap.keySet()){
            List<Integer> indexs = indexMap.get(key);
            if(indexs!=null){
                List<Voucher> vouchers = voucherMap.get(key);
                Integer prevCode = null;
                for(Integer index:indexs){
                    Integer currentCode = getCode(vouchers.get(index),key);
                    if(currentCode==null) {
                        continue;
                    }
                    Integer beginIndex;
                    if(index==0){
                        beginIndex = LedgerConstant.VoucherAdjust.FIRST_CODE;
                    }else{
                        if(prevCode==null) {
                            beginIndex = LedgerConstant.VoucherAdjust.FIRST_CODE;
                        } else {
                            beginIndex = prevCode+1;
                        }
                    }
                    for(int i=beginIndex;i<currentCode;i++){
                        List<Voucher> dels = delMap.get(generateKey(key,i));
                        if(dels!=null){
                            brokenVoucher.addAll(dels);
                            /*for(Voucher del:dels){
                                credentialArr.add(String.format(Constant.VoucherAdjust.CREDENTIAL,convertToName(del.getCredentialWord()),del.getMainCode(),del.getChildCode()));
                            }*/
                        }
                    }
                    prevCode = currentCode;
                }
            }
        }
        /*StringBuffer result = new StringBuffer();
        if(!credentialArr.isEmpty()){
            for(String credential:credentialArr){
                result.append(credential).append(";");
            }
            result.delete(result.length()-1,result.length());
            result.append("已被删除");
        }*/
        return brokenVoucher;
    }
    
    /**
     * @description: 转换成名称
     * @param: [credentialWord] 凭证字
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-08 10-52 
     */
    private String convertToName(Byte credentialWord){
        if(credentialWord==null) {
            return null;
        }
        if(credentialWord>=wordNameArr.length||credentialWord<0) {
            return String.valueOf(credentialWord);
        }
        return wordNameArr[credentialWord];
    }
    
    /**
     * @description: 获取code
     * @param: [voucher(凭证), key(缓存key)]
     * @return: java.lang.Integer 
     * @author: xdy        
     * @create: 2019-08-08 10-22 
     */
    private Integer getCode(Voucher voucher,String key){
        Integer code;
        if(key.startsWith(LedgerConstant.VoucherAdjust.CHILD_KEY_PRE)){
            code = voucher.getChildCode();
        }else{
            code = voucher.getMainCode();
        }
        return code;
    }

    /**
     * @description: 设置code
     * @param: [voucher(凭证), key(缓存key), code(凭证编码)]
     * @return: void 
     * @author: xdy        
     * @create: 2019-08-08 14-24 
     */
    private void setCode(Voucher voucher,String key,Integer code){
        if(key.startsWith(LedgerConstant.VoucherAdjust.CHILD_KEY_PRE)){
            voucher.setChildCode(code);
        }else {
            voucher.setMainCode(code);
        }
    }
    
    /**
     * @description: 重置code
     * @param: [voucher, key]
     * @return: void 
     * @author: xdy        
     * @create: 2019-08-28 17-05 
     */
    private void resetCode(Voucher voucher,String key){
        SequenceDto sequenceDto = new SequenceDto();
        sequenceDto.setCredWord(voucher.getCredentialWord());
        sequenceDto.setYear(voucher.getPostingPeriodYear());
        sequenceDto.setPeriodNo(voucher.getPostingPeriodNum());
        sequenceDto.setAccountId(voucher.getAccountBookId());
        //子号凭证
        if(key.startsWith(LedgerConstant.VoucherAdjust.CHILD_KEY_PRE)){
            sequenceDto.setEntityId(voucher.getAccountBookEntityId());
            sequenceDto.setCode(voucher.getChildCode());
        }else {
            sequenceDto.setCode(voucher.getMainCode());
        }
        publicDataApi.resetVoucherNo(sequenceDto);
    }

    /**
     * @description: 重置code
     * @param: [voucherMap]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-27 11:08 
     */
    private void resetCode(Map<String,List<Voucher>> voucherMap){
        for(String key:voucherMap.keySet()) {
            List<Voucher> vouchers = voucherMap.get(key);
            if (vouchers != null&&!vouchers.isEmpty()) {
                resetCode(vouchers.get(vouchers.size()-1),key);
            }
        }
    }

    /**
     * @description: 缓存数据
     * @param: [cacheMap(缓存map), key(缓存key), data(缓存数据)]
     * @return: void 
     * @author: xdy        
     * @create: 2019-08-08 09-23 
     */
    private <T> void putData(Map<String,List<T>> cacheMap,String key,T data){
        List<T> datas = cacheMap.get(key);
        if(datas==null){
            datas = new ArrayList<>();
            cacheMap.put(key,datas);
        }
        datas.add(data);
    }

    /**
     * @description: 缓存整理报告
     * @param: [reportMap(报告map), accountBookPeriod(账簿期间), voucher(凭证)]
     * @return: void 
     * @author: xdy        
     * @create: 2019-08-09 17-45 
     */
    private void putReport(Map<String, AccountBookPeriodReport> reportMap,AccountBookPeriod accountBookPeriod,Voucher voucher){
        AccountBookPeriodReport report = reportMap.get(generateReportKey(voucher.getId()));
        if(report==null){
            SysUserVo userVo = UserUtils.getUserVo();
            report = new AccountBookPeriodReport();
            report.setRootEnterpriseId(userVo.getRootEnterpriseId());
            report.setPeriodId(accountBookPeriod.getId());
            report.setAccountBookId(accountBookPeriod.getAccountBookId());
            report.setAccountBookName(accountBookPeriod.getAccountBookName());
            report.setAccountBookEntityId(voucher.getAccountBookEntityId());
            report.setAccountBookEntityName(voucher.getAccountBookEntityName());
            report.setCredentialWordName(convertToName(voucher.getCredentialWord()));
            report.setOldVoucherCode(String.format(LedgerConstant.VoucherAdjust.CREDENTIAL_CODE,voucher.getMainCode(),voucher.getChildCode()));
            reportMap.put(generateReportKey(voucher.getId()),report);
        }else {
            report.setNewVoucherCode(String.format(LedgerConstant.VoucherAdjust.CREDENTIAL_CODE,voucher.getMainCode(),voucher.getChildCode()));
        }
    }

    /**
     * @description: 生成主号key
     * @param: [credentialWord] 凭证字
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-07 16-28 
     */
    private String generateMainKey(Byte credentialWord){
        return String.format(LedgerConstant.VoucherAdjust.MAIN_KEY,credentialWord);
    }
    
    /**
     * @description: 生成主号key
     * @param: [credentialWord(凭证字), mainCode(主编码)]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-08 09-13 
     */
    private String generateMainKey(Byte credentialWord,Integer mainCode){
        return generateKey(generateMainKey(credentialWord),mainCode);
    }

    /**
     * @description: 生成子号key
     * @param: [accountBookEntityId(账簿主体ID), credentialWord(凭证字)]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-08-07 16-29
     */
    private String generateChildKey(Long accountBookEntityId,Byte credentialWord){
        return String.format(LedgerConstant.VoucherAdjust.CHILD_KEY,accountBookEntityId,credentialWord);
    }

    /**
     * @description: 生成子号key
     * @param: [accountBookEntityId(账簿主体ID), credentialWord(凭证字), childCode(子编码)]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-08 09-22 
     */
    private String generateChildKey(Long accountBookEntityId,Byte credentialWord,Integer childCode){
        return generateKey(generateChildKey(accountBookEntityId,credentialWord),childCode);
    }

    /**
     * @description: 生成key
     * @param: [key(凭证key), code(凭证编码)]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-09 17-48 
     */
    private String generateKey(String key,Integer code){
        return String.format(LedgerConstant.VoucherAdjust.KEY,key,code);
    }
    
    /**
     * @description: 生成报告key
     * @param: [voucherId] 凭证ID
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-08 14-55 
     */
    private String generateReportKey(Long voucherId){
        return String.format(LedgerConstant.VoucherAdjust.REPORT_KEY,voucherId);
    }



}
