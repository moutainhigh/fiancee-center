package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.AccountSubjectCurrency;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import org.apache.ibatis.annotations.Param;

/**
 * @Description 会计科目与币种关系mapper
 * @Author 周鹏
 * @Date 2019/6/12
 */
public interface AccountSubjectCurrencyMapper extends BaseMapper<AccountSubjectCurrency> {
    /**
     * 删除会计科目与币种关系信息
     *
     * @param accountSubjectCurrency
     * @return count
     */
    int deleteByParam(@Param("accountSubjectCurrency") AccountSubjectCurrency accountSubjectCurrency);

    /**
     * 删除会计科目与币种关系信息
     *
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/26
     */
    int delete(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目与币种关系信息数量
     *
     * @param accountSubjectCurrency
     * @return count
     */
    int findCountByParam(@Param("accountSubjectCurrency") AccountSubjectCurrency accountSubjectCurrency);
}