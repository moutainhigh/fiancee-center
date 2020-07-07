package com.njwd.basedata.controller;

import com.njwd.basedata.service.SequenceService;
import com.njwd.entity.basedata.dto.SequenceDto;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * 基础资料的客户和供应商控制层.
 * @author 朱小明
 * @since 2019/6/11
 */
@RestController
@RequestMapping("publicData")
public class PublicDataController extends BaseController {

    @Resource
    private SequenceService sequenceService;

    /**@Description 自动获取编码：凭证主号
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [credWord：凭证字, year：年度, periodNo:期间号, accountId:账簿ID，entityId：核算主体 ]
     * @return java.lang.Long
     **/
    @PostMapping("getCredWord")
    public Result<Integer> getCredWord(@RequestBody SequenceDto sequenceDto) {
        FastUtils.checkParams(sequenceDto.getCredWord(),sequenceDto.getAccountId(),
                    sequenceDto.getYear(), sequenceDto.getPeriodNo() );
        return ok(sequenceService.getCode(sequenceDto.getCredWord(),
                sequenceDto.getYear(), sequenceDto.getPeriodNo(),
                sequenceDto.getAccountId(),sequenceDto.getEntityId()));
    }

    /**@Description  整理凭证号流水
     * @Author 朱小明
     * @Date 2019/6/24 18:54
     * @Param [credWord：凭证字, year：年度, periodNo:期间号, accountId:账簿ID，entityId：核算主体 ]
     * @return java.lang.Long
     **/
    @PostMapping("resetVoucherNo")
    public Result<Integer> resetVoucherNo(@RequestBody SequenceDto sequenceDto) {
        FastUtils.checkParams(sequenceDto.getCredWord(),sequenceDto.getAccountId(),
                sequenceDto.getYear(), sequenceDto.getPeriodNo(),sequenceDto.getCode());
        return ok(sequenceService.resetVoucherNo(sequenceDto.getCredWord(),
                sequenceDto.getYear(), sequenceDto.getPeriodNo(),
                sequenceDto.getAccountId(),sequenceDto.getEntityId(),sequenceDto.getCode()));
    }

    /**
     * @Description 获取自动编码:业务单元
     * @Author 朱小明
     * @Date 2019/6/26 17:32
     * @Param [length, id, code, type]
     * @return java.lang.String
     **/
    @PostMapping("getCode")
    public Result<String> getCode(@RequestBody SequenceDto sequenceDto){
        return ok(sequenceService.getCode(sequenceDto.getPreCode(),sequenceDto.getLength(),sequenceDto.getRootEnterPriseId(),sequenceDto.getType()));
    }
}
