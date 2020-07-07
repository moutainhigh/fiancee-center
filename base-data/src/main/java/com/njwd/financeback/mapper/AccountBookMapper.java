package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBook;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 *
 * @Author: Zhuzs
 * @Date: 2019-05-21 15:22
 */
public interface AccountBookMapper extends BaseMapper<AccountBook> {

    /**
     * 根据ID查询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:28
     */
    AccountBookVo selectEntityById(AccountBookDto accountBookDto);

    /**
     * 根据公司ID查询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:28
     */
    AccountBookVo selectByCompanyId(AccountBookDto accountBookDto);

    /**
     * 根据 公司ID list/账簿ID list 查询核算账簿（默认核算主体，会计准则，科目表）
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:29
     */
    AccountBookVo findAccBookDetailInfoByCompanyIdOrAccBookId(AccountBookDto accountBookDto);

    /**
     * 查询核算账簿列表 分页
     *
     * @param: [accountBookDto, page]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:29
     */
    List<AccountBookVo> findPage(@Param("accountBookDto") AccountBookDto accountBookDto, @Param("page") Page<AccountBookVo> page);

    /**
     * @param idSet
     * @return java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @Description 根据idSet查询数据
     * @Author 朱小明
     * @Date 2019/8/7 11:53
     * @Param [idSet]
     */
    List<AccountBookVo> selectListByIdSet(Set<Long> idSet);

    /**
     * @description: 获取权限内所有账簿
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo> 
     * @author: xdy        
     * @create: 2019-08-22 18-52 
     */
    List<AccountBookVo> findAuthAll(AccountBookDto accountBookDto);

}