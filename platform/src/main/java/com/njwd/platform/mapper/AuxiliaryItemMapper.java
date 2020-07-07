package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AuxiliaryItem;
import com.njwd.entity.platform.SourceOfValue;
import com.njwd.entity.platform.dto.AuxiliaryItemDto;
import com.njwd.entity.platform.dto.SourceOfValueDto;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuxiliaryItemMapper extends BaseMapper<AuxiliaryItem> {

    /**
     * @Description 根据ID查询辅助核算
     * @Author liuxiang
     * @Date:15:26 2019/7/2
     * @Param [auxiliaryItemVo]
     * @return com.njwd.platform.entity.vo.AuxiliaryItemVo
     **/
    AuxiliaryItemVo findAuxiliaryById(AuxiliaryItemDto auxiliaryItemDto);

    /**
     * @Description 根据codeOrName查询辅助核算分页
     * @Author liuxiang
     * @Date:15:27 2019/7/2
     * @Param [auxiliaryItemDto, page]
     * @return java.util.List<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     **/
    Page<AuxiliaryItemVo> findAuxiliaryItemPage(Page<AuxiliaryItemVo> page,@Param("auxiliaryDto") AuxiliaryItemDto auxiliaryItemDto);

    /**
     * @Description 根据辅助核算名称集合查询辅助核算列表
     * @Author liuxiang
     * @Date:15:28 2019/7/2
     * @Param [auxiliaryItemVo]
     * @return java.util.List<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     **/
    List<AuxiliaryItemVo> findAuxiliaryListByNames(AuxiliaryItemDto auxiliaryItemDto);

    /**
     * @Description 查询所有未删除的辅助核算类型
     * @Author wuweiming
     * @Param []
     * @return List<AuxiliaryItemVo>
     **/
    List<AuxiliaryItemVo> findAllAuxiliaryItem();

    /**
     * @description: 值来源分页
     * @param: [page, sourceOfValueDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.SourceOfValue>
     * @author: xdy
     * @create: 2019-11-14 14:28
     */
    Page<SourceOfValue> findSourceOfValuePage(Page<SourceOfValue> page,@Param("sourceOfValueDto") SourceOfValueDto sourceOfValueDto);

    int updateBatch(AuxiliaryItemDto auxiliaryItemDto);

    SourceOfValue findSourceOfValuePageById(@Param("id") Long id);

    /**
     * 查询科目表配置的辅助核算
     *
     * @param subjectId subjectId
     * @return java.util.List<com.njwd.entity.platform.vo.AuxiliaryItemVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/22 15:14
     **/
    List<AuxiliaryItemVo> findBySubjectId(@Param("subjectId") Long subjectId);
}
