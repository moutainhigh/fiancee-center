package com.njwd.ledger.api;

import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.VoucherVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 凭证
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-05
 */
@RequestMapping("ledger/voucher")
public interface VoucherApi {
    /**
    * @description: 根据凭证id+数据来源表+核算项目值id查询凭证分录是否存在
    * @Param [voucherDto, voucherEntryAuxiliaryDto]
    * @return com.njwd.entity.ledger.VoucherEntryAuxiliary
    * @author LuoY
    * @date 2019/8/26 13:40
    */
    @PostMapping("findVoucherEntryAuxiliaryByItemValueId")
    Result<Integer> findVoucherEntryAuxiliaryByItemValueId(@RequestBody VoucherDto voucherDto);
    /**
     *根据租户id和科目id去查询凭证  用于公司间协同 租户端 启用功能
     * 刘遵通
     * @param voucherDto
     * @return
     */
    @PostMapping("findVoucherByRootIdAndSubjectId")
    List<VoucherVo> findVoucherByRootIdAndSubjectId(@RequestBody VoucherDto voucherDto);
}
