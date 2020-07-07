package com.njwd.fileexcel.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/10 17:10
 */
public class DefaultData extends AbstractMappingData{

    DefaultData() {
        super();
    }

    @Override
    public Map<Object, Object> findData() {
        return new HashMap<>();
    }
}
