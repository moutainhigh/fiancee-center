package com.njwd.fileexcel.convert;

/**
 * @description: 处理转换异常
 * @author: xdy
 * @create: 2019/5/23 10:52
 */
public abstract class AbstractDataConvert implements UnsafeDataConvert{

    @Override
    public ConvertResult convert(Object source) {
        ConvertResult convertResult;
        try {
            convertResult = unsafeDataConvert(source);
        }catch (Exception e){
            convertResult = new ConvertResult();
            convertResult.setOk(false);
            convertResult.setSource(source);
            convertResult.setErrorMsg(e.getMessage());
        }
        if(convertResult==null){
            convertResult = new ConvertResult();
            convertResult.setOk(false);
            convertResult.setSource(source);
            convertResult.setErrorMsg("值转换失败");
        }
        return convertResult;
    }

}
