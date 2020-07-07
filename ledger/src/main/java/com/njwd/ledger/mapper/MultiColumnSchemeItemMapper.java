package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.MultiColumnSchemeItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 多栏账方案明细
 * @Date:14:51 2019/7/31
 **/
public interface MultiColumnSchemeItemMapper extends BaseMapper<MultiColumnSchemeItem> {
    /**
     * @return int
     * @Description 批量新增多栏账方案明细
     * @Author liuxiang
     * @Date:14:51 2019/7/31
     * @Param [multiColumnSchemeItemDto]
     **/
    int insertBatch(@Param("schemeId") Long schemeId, @Param("schemeItems") List<MultiColumnSchemeItem> schemeItems);


}