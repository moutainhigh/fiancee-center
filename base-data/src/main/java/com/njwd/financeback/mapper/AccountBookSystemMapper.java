package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-21 11:34
 */
public interface AccountBookSystemMapper extends BaseMapper<AccountBookSystem> {
    /**
     * 根据子系统标识 查询当前企业已启用子系统列表
     *
     * @param: [page, accountBookSystemDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:48
     */
    Page<AccountBookSystemVo> findLedgerList(@Param("page")Page<AccountBookSystemVo> page, @Param("accountBookSystemDto") AccountBookSystemDto accountBookSystemDto);
     //根据子系统标识 查询当前企业用户已启用子系统列表
    Page<AccountBookSystemVo> findLedgerListByUserId(@Param("page")Page<AccountBookSystemVo> page, @Param("accountBookSystemDto") AccountBookSystemDto accountBookSystemDto);

    /**
     * 根据 账簿ID/子系统标识 查询已启用子系统
     *
     * @param: [accountBookSystemDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:48
     */
    List<AccountBookSystemVo> findListByAccBookId(AccountBookSystemDto accountBookSystemDto);

    /**
     * @Description 根据系统标识+账簿ID+会计年度+会计期间查询是否已初始化
     * @Author 朱小明
     * @Date 2019/8/14 16:56
     * @Param [accountBookSystemDto]
     * @return java.lang.Byte
     **/
    AccountBookSystem selectInitStatusByCondition(AccountBookSystemDto accountBookSystemDto);
}
