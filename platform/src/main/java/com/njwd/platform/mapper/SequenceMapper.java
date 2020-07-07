package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.Sequence;
import org.apache.ibatis.annotations.Param;

public interface SequenceMapper extends BaseMapper<Sequence> {
    /**
     * 获取下一个sequence值
     * @param name seq名称
     * @param companyId 公司ID
     * @return
     */
    Integer findNextValue(@Param("name") String name, @Param("companyId") Long companyId, @Param("type") Byte type);

}
