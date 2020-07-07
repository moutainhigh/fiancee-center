package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.VoucherEntryInterior;
import com.njwd.entity.ledger.dto.VoucherEntryDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/8/27
 */
public interface VoucherEntryInteriorMapper extends BaseMapper<VoucherEntryInterior> {

    /**
     * 批量插入协同分录关联信息
     *
     * @param interiorGenerateEntryList interiorGenerateEntryList
     * @param voucherId                 voucherId
     * @param interiorVoucherId         interiorVoucherId
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/16 10:31
     **/
    int insertInteriorRelation(@Param("interiorGenerateEntryList") List<VoucherEntryDto> interiorGenerateEntryList, @Param("voucherId") Long voucherId, @Param("interiorVoucherId") Long interiorVoucherId);
}