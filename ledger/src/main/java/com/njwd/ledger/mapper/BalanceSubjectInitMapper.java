package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.BalanceSubjectInit;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceSubjectInitMapper extends BaseMapper<BalanceSubjectInit> {

    /**
     * 科目期初录入
     *
     * @param balanceSubjectInitList
     * @return int
     * @Author lj
     * @Date:10:20 2019/8/2
     **/
    int addSubjectInitBatch(@Param("balanceSubjectInitList") List<BalanceSubjectInitDto> balanceSubjectInitList);

    /**
     * 根据条件批量删除
     * @Author lj
     * @Date:16:47 2019/10/25
     * @param balanceSubjectInitList
     * @return int
     **/
    int deleteSubjectInitBatch(@Param("balanceSubjectInitList") List<BalanceSubjectInitDto> balanceSubjectInitList);

    /**
     * 查询科目期初
     * @Author lj
     * @Date:9:18 2019/10/24
     * @param balanceInitDto
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubjectInit>
     **/
    List<BalanceSubjectInit> selectSubjectInitList(BalanceInitDto balanceInitDto);
}