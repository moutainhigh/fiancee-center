package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.basedata.mapper.SequenceMapper;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.Sequence;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Description 流水号处理
 * @Author 朱小明
 * @Date 2019/6/24 19:28
 **/
@Service
public class SequenceServiceeImpl implements SequenceService {
    @Resource
    private SequenceMapper sequenceMapper;

    /**
     * 查询序列额
     * @author luoy
     * @param seqName
     * @return
     */
    @Override
    public Sequence findSequence(String seqName,Long companyId) {
        return sequenceMapper.selectOne(new LambdaQueryWrapper<Sequence>().eq(Sequence::getSeqName,seqName).eq(Sequence::getValId,companyId));
    }

    /**
     * 更改序列信息
     * @author luoy
     * @param sequence
     * @return
     */
    @Override
    public int updateSequence(Sequence sequence) {
        return sequenceMapper.updateById(sequence);
    }

    /**
     * @Description 添加序列号
     * @Author LuoY
     * @Date 2019/6/24 16:03
     * @Param []
     * @return int
     */
    @Override
    public int addSequence(Sequence sequence) {
        return sequenceMapper.insert(sequence);
    }
     /**@Description 自动获取编码：不带核算主体
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [[preCode：前缀、length：流水号位数, id：公司或企业ID, code:公司编码, type：主体类型：0、公司1、企业]
     * @return java.lang.String
     **/
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String getCode(String preCode, int length, Long id, Byte type) {
        Integer serial = sequenceMapper.findNextValue(preCode, id, type);
        return preCode + StringUtils.leftPad(serial.toString(), length, "0");
    }

    /**
     * @Description 自动获取编码：带核算主体
     * @Author 朱小明
     * @Date 2019/6/24 20:42
     * @Param [preCode：前缀、length：流水号位数, id：公司或企业ID, code:公司编码, type：主体类型：0、公司1、企业]
     * @return java.lang.String
     **/
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String getCode(String preCode, int length,Long id, String code, Byte type) {
        Integer serial = sequenceMapper.findNextValue(preCode, id, type);
        return preCode + code + StringUtils.leftPad(serial.toString(), length, "0");
    }

    /**
     * @Description 自动获取编码：业务单元
     * @Author 朱小明
     * @Date 2019/6/24 20:42
     * @Param [length：流水号位数, id：公司或企业ID, code:公司编码, type：主体类型：0、公司1、企业]
     * @return java.lang.String
     **/
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String getCode(int length,Long id, String code, Byte type) {
        Integer serial = sequenceMapper.findNextValue(Constant.BaseCodeRule.BUSINESS_UNIT, id, type);
        return code + StringUtils.leftPad(serial.toString(), length, "0");
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/2 14:06
     * @Param [seqName, companyId, type, companyCode]
     * @return java.lang.Integer
     * @Description 获取流水号
     */
    @Override
    public Integer findNextValue(String seqName,Long companyId,byte type){
        return sequenceMapper.findNextValue(seqName,companyId,type);
    }

    /**@Description 自动获取编码：凭证主号
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [credWord：凭证字, year：年度, periodNo:期间号, accountId:账簿ID，entityId：核算主体 ]
     * @return java.lang.Long
     **/
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Integer getCode(Byte credWord, Integer year, Byte periodNo, Long accountId, Long entityId) {
        String proCode;
        if (entityId != null) {
            proCode = Constant.BaseCodeRule.VOUCHER_SUN_NO+year+periodNo+credWord+accountId+"-"+entityId;
        } else {
            proCode = Constant.BaseCodeRule.VOUCHER_NO+year+periodNo+credWord+accountId;
        }
        Integer serial = sequenceMapper.findNextValue(proCode, Long.valueOf(0), Byte.valueOf("2"));
        return serial;
    }



    /**@Description 自动获取编码：凭证主号或子号
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [credWord：凭证字, year：年度, periodNo:期间号, accountId:账簿ID，entityId：核算主体,code:凭证字号或主号]
     * @return java.lang.Long
     **/
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int resetVoucherNo(Byte credWord, Integer year, Byte periodNo, Long accountId, Long entityId, Integer code) {
        String proCode;
        if (entityId != null) {
            proCode = Constant.BaseCodeRule.VOUCHER_SUN_NO+year+periodNo+credWord+accountId+"-"+entityId;
        } else {
            proCode = Constant.BaseCodeRule.VOUCHER_NO+year+periodNo+credWord+accountId;
        }
        return sequenceMapper.resetNextValue(proCode, Long.valueOf(0), Byte.valueOf("2"), code);
    }
}
