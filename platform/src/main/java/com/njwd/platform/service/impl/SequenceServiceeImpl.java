package com.njwd.platform.service.impl;

import com.njwd.common.Constant;
import com.njwd.platform.mapper.SequenceMapper;
import com.njwd.platform.service.SequenceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class SequenceServiceeImpl implements SequenceService {
    @Resource
    private SequenceMapper sequenceMapper;
    /**
     * 刘遵通
     * @param length 长度
     * @param code 编码
     * @param  preCode 前缀
     * @return
     *前缀 + 取值编码 + 流水号
     */
    @Override
    public String getCode(String preCode,int length, String code) {
        Integer serial = sequenceMapper.findNextValue(preCode, Constant.Number.ZEROL, Constant.Number.ANTI_INITLIZED);
        return preCode + code + StringUtils.leftPad(serial.toString(), length, "0");
    }
    /**
     * 刘遵通
     * @param length 长度
     * @param id   公司id  或者  企业租户id
     * @param type 0公司         1企业
     * @param  preCode 前缀
     * @return
     *前缀 + 流水号
     */
    @Override
    public String getCode(String preCode, int length) {
        Integer serial = sequenceMapper.findNextValue(preCode, Constant.Number.ZEROL, Constant.Number.ANTI_INITLIZED);
        return preCode  + StringUtils.leftPad(serial.toString(), length, "0");
    }
    /**
     * 刘遵通
     * @param length 长度
     * @param id   公司id  或者  企业租户id
     * @param code 编码
     * @param type 0公司         1企业
     * @return
     *取值编码 + 流水号
     */
    @Override
    public String getCode(int length, String code) {
        Integer serial = sequenceMapper.findNextValue("", Constant.Number.ZEROL, Constant.Number.ANTI_INITLIZED);
        return code + StringUtils.leftPad(serial.toString(), length, "0");
    }
}
