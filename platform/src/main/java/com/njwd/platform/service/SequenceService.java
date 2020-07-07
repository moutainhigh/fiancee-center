package com.njwd.platform.service;

public interface SequenceService {
    //前缀 + 取值编码 + 流水号
    String getCode(String preCode,int length, String code);

    //前缀  + 流水号
    String getCode(String preCode,int length);

    //取值编码 + 流水号
    String getCode(int length,String code);
}
