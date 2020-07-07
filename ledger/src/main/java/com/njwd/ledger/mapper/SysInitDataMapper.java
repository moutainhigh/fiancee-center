package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.SysInitData;
import org.apache.ibatis.annotations.Param;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/6 14:25
 */
public interface SysInitDataMapper extends BaseMapper<SysInitData> {

    Integer existTable(String tableName);

    int createVoucher(@Param("tableName") String tableName);

    int createVoucherEntry(@Param("tableName") String tableName);

    int createVoucherEntryAuxiliary(@Param("tableName") String tableName);

    int createVoucherEntryCashFlow(@Param("tableName") String tableName);

    int createVoucherEntryInterior(@Param("tableName") String tableName);

}
