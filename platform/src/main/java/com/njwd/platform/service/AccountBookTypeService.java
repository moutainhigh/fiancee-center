package com.njwd.platform.service;


import com.njwd.entity.platform.vo.AccountBookTypeVo;

import java.util.List;

/**
 * @Author lj
 * @Description 账簿类型
 * @Date:17:08 2019/6/25
 **/
public interface AccountBookTypeService {

    /**
     * @Description 查询账簿类型列表
     * @Author lj
     * @Date:17:03 2019/6/25
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.AccountBookTypeVo>
     **/
    List<AccountBookTypeVo> findAccountBookTypeList();
}
