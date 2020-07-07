package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.AccountBookType;
import com.njwd.entity.platform.vo.AccountBookTypeVo;

import java.util.List;

public interface AccountBookTypeMapper extends BaseMapper<AccountBookType> {

    /**
     * @Description 查询账簿类型列表
     * @Author lj
     * @Date:17:03 2019/6/25
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.AccountBookTypeVo>
     **/
    List<AccountBookTypeVo> findAccountBookTypeList();
}