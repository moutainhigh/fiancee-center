package com.njwd.basedata.service;

import com.njwd.entity.basedata.Sequence;

public interface SequenceService {
    /**
     * 根据序列名查询序列
     * @author luoy
     * @param seqName 序列名
     * @param companyId 公司id
     * @return
     */
    Sequence findSequence(String seqName, Long companyId);

    /**
     * 更改序列
     * @author luoy
     * @param sequence
     * @return
     */
    int updateSequence(Sequence sequence);

    /**
     * @Description 添加序列
     * @Author LuoY
     * @Date 2019/6/24 16:02
     * @Param []
     * @return int
     */
    int addSequence(Sequence sequence);

    /** @Description 获取自动编码:不带核算主体
     * @Author 朱小明
     * @Date 2019/6/24 19:33
     * @Param [preCode, length, isAdd, id]
     * @return java.lang.String
     **/
    String getCode(String preCode, int length, Long id, Byte type);

    /**
     * @Description 获取自动编码:带核算主体
     * @Author 朱小明
     * @Date 2019/6/24 19:33
     * @Param [preCode, length, isAdd, id]
     * @return java.lang.String
     **/
    String getCode(String preCode, int length, Long id, String code, Byte type);

    /**
     * @Description 获取自动编码:业务单元
     * @Author 朱小明
     * @Date 2019/6/26 17:32
     * @Param [length, id, code, type]
     * @return java.lang.String
     **/
    String getCode(int length, Long id, String code, Byte type);

    /**
     * 获取流水号
     * @param seqName
     * @param companyId
     * @param type
     * @return Integer
     */
    Integer findNextValue(String seqName, Long companyId, byte type);

    /**@Description 自动获取编码：凭证主号
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [credWord：凭证字, year：年度, periodNo:期间号, accountId:账簿ID，entityId：核算主体 ]
     * @return java.lang.Long
     **/
    Integer getCode(Byte credWord, Integer year, Byte periodNo, Long accountId, Long entityId);

    /**@Description 自动获取编码：凭证主号
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [credWord：凭证字, year：年度, periodNo:期间号, accountId:账簿ID，entityId：核算主体, mainCode:主号， childCode:子号 ]
     * @return java.lang.Long
     **/
    int resetVoucherNo(Byte credWord, Integer year, Byte periodNo, Long accountId, Long entityId, Integer code);
}
