package com.njwd.fileexcel.convert;

/**
 * @description: 默认值转换
 * @author: xdy
 * @create: 2019/5/23 10:32
 */
public class DefaultDataConvert extends AbstractDataConvert{

    @Override
    public ConvertResult unsafeDataConvert(Object source) {
        ConvertResult convertResult = new ConvertResult();
        convertResult.setOk(false);
        convertResult.setSource(source);
        convertResult.setErrorMsg("不存在该转换类型");
        return  convertResult;
    }

}
