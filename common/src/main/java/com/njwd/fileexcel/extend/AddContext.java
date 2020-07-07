package com.njwd.fileexcel.extend;

import net.jodah.typetools.TypeResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/18 17:04
 */
public class AddContext {

    private Map<Integer,AddExtendProxy> addExtendMap = new HashMap<>();
    private Map<Integer,ToEntityHandler> toEntityHandlerMap = new HashMap<>();
    private Map<Integer, List<String>> titleListMap = new HashMap<>();

    public AddExtendProxy getAddExtendProxy(Integer sheetNum){
        return addExtendMap.get(sheetNum);
    }

    public AddContext addAddExtend(Integer sheetNum,AddExtend addExtend){
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(AddExtend.class, addExtend.getClass());
        AddExtendProxy addExtendProxy = new AddExtendProxy(addExtend,typeArgs[0]);
        addExtendProxy.setTitleList(titleListMap.get(sheetNum));
        addExtendMap.put(sheetNum,addExtendProxy);
        if(toEntityHandlerMap.get(sheetNum)!=null){
            addExtendProxy.setToEntityHandler(toEntityHandlerMap.get(sheetNum));
        }
        return this;
    }

    public AddContext addToEntityHandler(Integer sheetNum,ToEntityHandler toEntityHandler){
        toEntityHandlerMap.put(sheetNum,toEntityHandler);
        if(addExtendMap.get(sheetNum)!=null){
            addExtendMap.get(sheetNum).setToEntityHandler(toEntityHandler);
        }
        return this;
    }

    public void setTitleListMap(Map<Integer, List<String>> titleListMap) {
        this.titleListMap = titleListMap;
    }
}
