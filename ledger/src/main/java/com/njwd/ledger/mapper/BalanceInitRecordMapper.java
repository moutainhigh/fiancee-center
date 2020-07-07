package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.BalanceInitRecord;
import com.njwd.entity.ledger.dto.BalanceInitRecordDto;
import com.njwd.entity.ledger.vo.BalanceInitRecordVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author lj
 * @Description 期初录入记录mapper
 * @Date:16:51 2019/10/16
 **/
public interface BalanceInitRecordMapper extends BaseMapper<BalanceInitRecord> {

    /**
     * 添加核算主体
     * @Author lj
     * @Date:10:10 2019/8/2
     * @param balanceInitRecordDtos
     * @return int
     **/
    int addBalanceInitRecordBatch(@Param("balanceInitRecordDtos") List<BalanceInitRecordDto> balanceInitRecordDtos);

    /**
     * 查询期初录入记录列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    List<BalanceInitRecordVo> findListByParam(@Param("balanceInitRecordDto") BalanceInitRecordDto balanceInitRecordDto);

    /**
     * 查询期初录入表账簿id列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    List<Long> findAccoutBookIdListByParam(@Param("balanceInitRecordDto") BalanceInitRecordDto balanceInitRecordDto);

    /**
     * 查询期初录入表核算主体id列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    List<Long> findEntityIdListByParam(@Param("balanceInitRecordDto") BalanceInitRecordDto balanceInitRecordDto);
}