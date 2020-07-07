package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.ledger.mapper.BaseLedgerMapper;
import com.njwd.ledger.service.BaseLedgerService;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.*;

/**
 *@description:
 *@author: fancl
 *@create: 2019-08-26 
 */
@Service
public class BaseLedgerServiceImpl implements BaseLedgerService {


    @Autowired
    BaseLedgerMapper baseLedgerMapper;


    @Override
    public <T extends BaseModel> Object judgeNull(@NotNull T entity) {
        entity.setTableName(TableInfoHelper.getTableInfo(entity.getClass()).getTableName());
        return baseLedgerMapper.judgeNull(entity);
    }

    @Override
    public <T extends BaseModel> int initJson(@NotNull T entity) {
        entity.setTableName(TableInfoHelper.getTableInfo(entity.getClass()).getTableName());
        return baseLedgerMapper.initJson(entity);
    }

    /**
     * @description manage_info通用Json字段信息修改
     * @author fancl
     * @date 2019/8/26
     * @param
     * @return
     */
    @Override
    public <T extends BaseModel> int updateManageInfo(@NotNull T entity, String type) {
        //FastUtils.checkParams(entity.getBatchIds());
        entity.setTableName(TableInfoHelper.getTableInfo(entity.getClass()).getTableName());
        SysUserVo suv = UserUtils.getUserVo();
        List<Object> fillList = new ArrayList<>();
        switch (type) {
            case LedgerConstant.ManageInfoUpdateType.transferItem:
                fillList.add(LedgerConstant.ManageInfoUpdateType.transferItemUserId);
                fillList.add(suv.getUserId());
                fillList.add(LedgerConstant.ManageInfoUpdateType.transferItemUserName);
                fillList.add(suv.getName());
                fillList.add(LedgerConstant.ManageInfoUpdateType.transferItemTime);
                fillList.add(new Date());
                break;
            default:
                break;
        }
        //entity.setManageInfos(managerInfo);
        //得到填充对象
        baseLedgerMapper.updateManageInfo(entity, fillList);

        return 1;
    }


    /**
     * @description 获取修改Josn对象填充信息
     * @author fancl
     * @date 2019/8/26
     * @param clazz
     * @return
     */
    private List<Object> getFillInfo(Class clazz, LinkedHashMap<String, Object> map) {
        List<Object> list = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
//        List<Object> fillList = Arrays.stream(fields).map(field -> {
//            try {
//                field.setAccessible(true);
//                if (field.get(obj) != null) {
//                    list.add("$." + field.getName());
//                    list.add(field.get(obj));
//                }
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }).collect(Collectors.toList());
        return list;
    }
}
