package com.njwd.fileexcel.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.njwd.common.ExcelDataConstant.*;


/**
 * @description: 系统数据
 * @author: xdy
 * @create: 2019/6/10 16:54
 */
public class SystemData extends AbstractMappingData{

    private static Map<String,Map> cacheDataMap = new ConcurrentHashMap<>();

    SystemData(String dataType) {
        this(dataType,false);
    }

    SystemData(String dataType,boolean reverse) {
        super(reverse);
        this.dataType = dataType;
    }

    /**
     * @description: 获取系统数据
     * @param: []
     * @return: java.util.Map<java.lang.Object,java.lang.Object>
     * @author: xdy
     * @create: 2019-06-10 17-21
     */
    @Override
    public Map<Object, Object> findData() {
        Map<Object,Object> map = cacheDataMap.get(dataType);
        if(map!=null) {
            return map;
        }
        map = new HashMap<>();
        switch (dataType){
            case SYSTEM_DATA_BALANCE_DIRECTION://余额方向
                map.put("借",0);
                map.put("贷",1);
                map.put("平",2);
                break;
            case SYSTEM_DATA_OCCURRENCE_DIRECTION://发生额方向
                map.put("不控制",0);
                map.put("借方",1);
                map.put("贷方",2);
                break;
            case  SYSTEM_DATA_IS_DEL://是否删除
                map.put("已删除",1);
                map.put("未删除",0);
                break;
            case SYSTEM_DATA_IS_ENABLE://是否启动
                map.put("已生效",1);
                map.put("未生效",0);
                break;
            case SYSTEM_DATA_IS_ACCOUNTING://是否建账
                map.put("已建账",1);
                map.put("未建账",0);
                break;
            case SYSTEM_DATA_CODE_TYPE://编码类型
                map.put("系统",0);
                map.put("自定义",1);
                break;
            case SYSTEM_DATA_IS_SELECTED://是否选中
                map.put("未选",0);
                map.put("已选",1);
                break;
            case SYSTEM_DATA_IS_INTERIOR://是否内部往来
                map.put("非内部往来",0);
                map.put("内部往来",1);
                break;
            case SYSTEM_DATA_IS_INIT://是否预置
                map.put("非预置",0);
                map.put("预置",1);
                break;
            case SYSTEM_DATA_IS_FINAL://是否末级
                map.put("否",0);
                map.put("是",1);
                break;
            case SYSTEM_DATA_CASH_FLOW_DIRECTION://现金流向
                map.put("现金流出",0);
                map.put("现金流入",1);
                break;
            case SYSTEM_DATA_CUSTOMER_TYPE://客户类型
                map.put("企业","0");
                map.put("个人","1");
                break;
            case SYSTEM_DATA_SUPPLIER_TYPE://供应商类型
                map.put("企业","0");
                map.put("个人","1");
                break;
            case SYSTEM_DATA_IS_INTERNAL_CUSTOMER://是否内部客户
                map.put("否","0");
                map.put("是","1");
                break;
            case SYSTEM_DATA_ACCOUNT_CATEGORY:
                map.put("现金科目","A01");
                map.put("银行科目","A02");
                map.put("现金等价物","A03");
                map.put("一般科目","B01");
                break;
            case SYSTEM_DATA_DATA_TYPE:
                map.put("客户",0);
                map.put("供应商",1);
                break;
            case SYSTEM_DATA_IS_ACCOUNT_ENTITY:
                map.put("否",0);
                map.put("是",1);
                break;
            case SYSTEM_DATA_HAS_SUB_ACCOUNT:
                map.put("否",0);
                map.put("是",1);
                break;
            case SYSTEM_DATA_BASE_DATA_TYPE://资料类型
                map.put("共享型",1);
                map.put("分配型",2);
                map.put("私有型",3);
                break;
            case SYSTEM_DATA_SOURCE_TYPE:
                map.put("手工",0);
                map.put("协同",1);
                map.put("损益结转",2);
                map.put("冲销",3);
                map.put("业务系统",4);
                break;
            case SYSTEM_DATA_IS_APPROVED:
                map.put("未审核",0);
                map.put("已审核",1);
                break;
            case SYSTEM_DATA_IS_RELEASED:
                map.put("未发布",0);
                map.put("已发布",1);
                break;
            case SYSTEM_DATA_IS_BASE:
                map.put("否",0);
                map.put("是",1);
                break;
            case SYSTEM_DATA_ROUNDING_TYPE:
                map.put("四舍五入",0);
                map.put("舍去",1);
                map.put("进位",2);
                break;
        }
        cacheDataMap.put(dataType,map);
        return map;
    }
}
