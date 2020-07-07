package com.njwd.ledger.service.impl;


import com.njwd.common.Constant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.ledger.mapper.SysInitDataMapper;
import com.njwd.ledger.service.SysInitDataService;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


/**
 * @description:
 * @author: xdy
 * @create: 2019/8/6 14:12
 */
@Service
public class SysInitDataServiceImpl implements SysInitDataService {

    @Resource
    private SysInitDataMapper sysInitDataMapper;

    /**
     * @description: 创建表
     * @param: []
     * @return: boolean 
     * @author: xdy        
     * @create: 2019-08-12 14-36 
     */
    @Override
    @Transactional
    public boolean createTable(){
        SysUserVo userVo = UserUtils.getUserVo();
        String[] tableNames = {Constant.DataInit.VOUCHER,Constant.DataInit.VOUCHER_ENTRY,Constant.DataInit.VOUCHER_ENTRY_AUXILIARY,
                Constant.DataInit.VOUCHER_ENTRY_CASH_FLOW,Constant.DataInit.VOUCHER_ENTRY_INTERIOR};
        for(String tempTableName:tableNames){
            String tableName = String.format(tempTableName,userVo.getRootEnterpriseId());
            Integer count = sysInitDataMapper.existTable(tableName);
            if(count!=null&&count>0){
                continue;
            }
            createTable(tableName,tempTableName);
        }
        return true;
    }
    
    /**
     * @description: 创建表
     * @param: [tableName]
     * @return: int 
     * @author: xdy        
     * @create: 2019-08-12 11-12 
     */
    private int createTable(String tableName,String tempTableName){
        int res=0;
        switch (tempTableName){
            case Constant.DataInit.VOUCHER:
                res = sysInitDataMapper.createVoucher(tableName);
                break;
            case Constant.DataInit.VOUCHER_ENTRY:
                res = sysInitDataMapper.createVoucherEntry(tableName);
                break;
            case Constant.DataInit.VOUCHER_ENTRY_AUXILIARY:
                res = sysInitDataMapper.createVoucherEntryAuxiliary(tableName);
                break;
            case Constant.DataInit.VOUCHER_ENTRY_CASH_FLOW:
                res = sysInitDataMapper.createVoucherEntryCashFlow(tableName);
                break;
            case Constant.DataInit.VOUCHER_ENTRY_INTERIOR:
                res = sysInitDataMapper.createVoucherEntryInterior(tableName);
                break;
        }
        return res;
    }

}
