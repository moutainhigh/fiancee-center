package com.njwd.fileexcel.convert;

/**
 * @description:
 * @author: xdy
 * @create: 2019/5/23 10:51
 */
public interface UnsafeDataConvert extends DataConvert{
    
      ConvertResult unsafeDataConvert(Object source);
}
