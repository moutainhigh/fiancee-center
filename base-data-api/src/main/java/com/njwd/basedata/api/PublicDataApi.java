package com.njwd.basedata.api;

import com.njwd.entity.basedata.dto.SequenceDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("financeback/publicData")
public interface PublicDataApi {

    /**
     * @Description 获取流凭证流水号
     * @Author 朱小明
     * @Date 2019/8/6 9:15
     * @Param [sequenceDto]
     * @return com.njwd.support.Result<java.lang.Integer>
     **/
    @PostMapping("getCredWord")
    Result<Integer> getCredWord(SequenceDto sequenceDto);

    /** @Description 获取自动编码:不带核算主体
     * @Author 朱小明
     * @Date 2019/6/24 19:33
     * @Param [preCode, length, isAdd, id]
     * @return java.lang.String
     **/
    @PostMapping("getCode")
    Result<String> getCode(SequenceDto sequenceDto);

    /**@Description  整理凭证号流水
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [credWord：凭证字, year：年度, periodNo:期间号, accountId:账簿ID，entityId：核算主体 ]
     * @return java.lang.Long
     **/
    @PostMapping("resetVoucherNo")
    Result<Integer> resetVoucherNo(@RequestBody SequenceDto sequenceDto);

}
