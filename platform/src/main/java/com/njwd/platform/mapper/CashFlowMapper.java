package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.vo.CashFlowVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author lj
 * @Description 现金流量项目表
 * @Date:16:00 2019/6/13
 **/
public interface CashFlowMapper extends BaseMapper<CashFlow> {

    /**
     * 批量更新
     * @Author lj
     * @Date:11:06 2019/11/13
     * @param ids
     * @param type
     * @return int
     **/
    int updateCashFlowBatch(@Param("ids") List<Long> ids,@Param("type") int type);

    /**
     * @Description 查询现金流量项目表状态列表
     * @Author liuxiang
     * @Date:15:30 2019/7/2
     * @Param [cashFlowDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowVo>
     **/
    List<CashFlowVo> findCashFlowListStatus(CashFlowDto cashFlowDto);

    /**
     * @Description 查询现金流量项目表列表
     * @Author liuxiang
     * @Date:15:30 2019/7/2
     * @Param [cashFlowDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowVo>
     **/
    List<CashFlowVo> findCashFlowList(CashFlowDto cashFlowDto);

    /**
     * 根据ID查询现金流量项目表
     * @Author lj
     * @Date:16:55 2019/11/13
     * @param cashFlowDto
     * @return com.njwd.entity.platform.vo.CashFlowVo
     **/
    CashFlowVo findCashFlowById(CashFlowDto cashFlowDto);

    /**
     * 查询现金流量项目表列表分页
     * @Author lj
     * @Date:10:55 2019/11/12
     * @param page
     * @param cashFlowDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowVo>
     **/
    Page<CashFlowVo> findCashFlowListPage(Page<CashFlowVo> page, @Param("cashFlowDto") CashFlowDto cashFlowDto);


    /**
     * @Description 根据会计准则id、账簿类型id查询现金流量项目表列表
     * @Author liuxiang
     * @Date:15:31 2019/7/2
     * @Param [cashFlowDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowVo>
     **/
    List<CashFlowVo> findCashListByStandIdAndTypeId(CashFlowDto cashFlowDto);
}