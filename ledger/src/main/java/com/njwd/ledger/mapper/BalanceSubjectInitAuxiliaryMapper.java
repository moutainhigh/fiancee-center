package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliary;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitAuxiliaryDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceSubjectInitAuxiliaryMapper extends BaseMapper<BalanceSubjectInitAuxiliary> {

    /**
     * 插入辅助核算数据
     * @Author lj
     * @Date:14:14 2019/8/29
     * @param balanceSubjectInitAuxiliary
     * @return void
     **/
    int addSubjectInitAuxiliary(@Param("baSubjectInitAux") BalanceSubjectInitAuxiliaryDto balanceSubjectInitAuxiliary);

    /**
     * 查询辅助核算列表
     * @Author lj
     * @Date:9:31 2019/10/24
     * @param balanceInitDto
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubjectInitAuxiliary>
     **/
    List<BalanceSubjectInitAuxiliary> selectSubjectInitAuxiliaryList(BalanceInitDto balanceInitDto);
}