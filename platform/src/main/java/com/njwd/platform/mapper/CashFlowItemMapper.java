package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.CashFlowItem;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CashFlowItemMapper extends BaseMapper<CashFlowItem> {

    /**
     * 批量更新
     * @Author lj
     * @Date:11:06 2019/11/13
     * @param ids
     * @param type
     * @return int
     **/
    int updateCashFlowItemBatch(@Param("ids") List<Long> ids,@Param("type") int type);

    /**
     * 根据ID查询现金流量项目
     * @Author lj
     * @Date:16:55 2019/11/13
     * @param cashFlowItemDto
     * @return com.njwd.entity.platform.vo.CashFlowVo
     **/
    CashFlowItemVo findCashFlowItemById(CashFlowItemDto cashFlowItemDto);

    /**
     * @Description 查询现金流量项目列表
     * @Author liuxiang
     * @Date:15:30 2019/7/2
     * @Param [cashFlowItemDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowItemVo>
     **/
    List<CashFlowItemVo> findCashFlowItemList(CashFlowItemDto cashFlowItemDto);

    /**
     * 查询是否存在下级数据
     * @Author lj
     * @Date:16:04 2019/11/14
     * @param cashFlowItemDto
     * @return java.lang.Integer
     **/
    Integer findIsExistNextCashFlowItem(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * 查询现金流量项目状态列表
     * @Author lj
     * @Date:17:03 2019/11/14
     * @param cashFlowItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     **/
    List<CashFlowItemVo> findCashFlowItemListStatus(CashFlowItemDto cashFlowItemDto);

    /**
     * 根据code和cashFlowId查询所有下级数据
     * @Author lj
     * @Date:17:42 2019/11/14
     * @param cashFlowItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     **/
    List<CashFlowItemVo> findAllCashFlowItemByCode(CashFlowItemDto cashFlowItemDto);


    /**
     * 版本校验
     * @Author lj
     * @Date:16:31 2019/11/14
     * @param cashFlowItemDto
     * @return java.lang.Long
     **/
    Long checkVersion(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);


    /**
     * @Description 查询现金流量项目分页
     * @Author liuxiang
     * @Date:15:30 2019/7/2
     * @Param [cashFlowItemDto, page]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowItemVo>
     **/
    Page<CashFlowItemVo> findCashFlowItemPage(Page<CashFlowItemVo> page,@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * 查询现金流量项目分页
     * @Author lj
     * @Date:17:19 2019/11/13
     * @param page
     * @param cashFlowItemDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowItemVo>
     **/
    Page<CashFlowItemVo> findCashFlowItemPageNew(Page<CashFlowItemVo> page,@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);
}