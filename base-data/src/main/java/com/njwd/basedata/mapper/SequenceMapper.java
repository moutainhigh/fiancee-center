package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.Sequence;
import org.apache.ibatis.annotations.Param;

public interface SequenceMapper extends BaseMapper<Sequence> {

    /**
     * 获取当前sequence值
     * @param name seq名称
     * @param companyId 公司ID
     * @return
     */
    Integer findCurrentValue(@Param("name") String name, @Param("companyId") Long companyId, @Param("type") Byte type);

    /**
     * 获取下一个sequence值
     * @param name seq名称
     * @param companyId 公司ID
     * @return
     */
    Integer findNextValue(@Param("name") String name, @Param("companyId") Long companyId, @Param("type") Byte type);

    int resetNextValue(@Param("name") String name, @Param("companyId") Long companyId, @Param("type") Byte type, @Param("code") Integer code);
}