package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountingStandard;
import com.njwd.entity.platform.dto.AccountingStandardDto;
import com.njwd.entity.platform.vo.AccountingStandardVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author lj
 * @Description 会计准则
 * @Date:10:51 2019/6/13
 **/
public interface AccountingStandardMapper extends BaseMapper<AccountingStandard> {

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingStandardVo>
     * @Description 查询会计准则列表信息
     * @Author liuxiang
     * @Date:15:25 2019/7/2
     * @Param []
     **/
    List<AccountingStandardVo> findAccountingList();

    /**
     * @return com.njwd.platform.entity.vo.AccountingStandardVo
     * @Description 查询单个会计准则信息
     * @Author liuxiang
     * @Date:15:25 2019/7/2
     * @Param [accountingStandardDto]
     **/
    AccountingStandardVo findAccountingById(AccountingStandardDto accountingStandardDto);

    /**
     * 分页
     *
     * @param accountingStandardDto accountingStandardDto
     * @param page                  page
     * @return java.util.List<com.njwd.entity.platform.vo.AccountingStandardVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/12 13:47
     **/
    List<AccountingStandardVo> findPage(@Param("accountingStandardDto") AccountingStandardDto accountingStandardDto, @Param("page") Page<AccountingStandardVo> page);

    /**
     * 查详情
     *
     * @param id id
     * @return com.njwd.entity.platform.vo.AccountingStandardVo
     * @author xyyxhcj@qq.com
     * @date 2019/11/19 14:11
     **/
    AccountingStandardVo findDetail(@Param("id") Long id);
}
