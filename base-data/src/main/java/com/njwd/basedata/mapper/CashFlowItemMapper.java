package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.CashFlowItem;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.entity.platform.vo.CashFlowVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author Libao
 * @Description CashFlowItemMapper
 * @Date 2019/6/12 9:14
 */
public interface CashFlowItemMapper extends BaseMapper<CashFlowItem> {


    int findCount(@Param("rootEnterpriseId") Long rootEnterpriseId);

    /**
     * @return int
     * @Author Libao
     * @Description 根据Id删除现金流量项目
     * @Date 2019/7/4 9:51
     * @Param [cashFlowItem]
     */
    int delCashFlowItemById(@Param("cashFlowItem") CashFlowItem cashFlowItem);

    /**
     * @return int
     * @Author Libao
     * @Description 根据CashFlowId删除现金流量项目
     * @Date 2019/7/4 9:51
     * @Param [cashFlowItem]
     */
    int delCashFlowByCashFlowId(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    int updateDisableOrEnable(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);


    /**
     * @return CashFlowItemVo
     * @Author Libao
     * @Description 分页查询现金流量项目列表
     * @Date 2019/6/12 9:15
     * @Param CashFlowItemDto, page
     */
    Page<CashFlowItemVo> findPage(@Param("page") Page<CashFlowItemVo> page, @Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return CashFlowItemVo
     * @Author Libao
     * @Description 查询现金流量项目
     * @Date 2019/6/12 9:15
     * @Param CashFlowItemDto, page
     */
    CashFlowItemVo findCashFlowItemById(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);


    /**
     * @return CashFlowItemVo
     * @Author Libao
     * @Description 查询现金流量项目
     * @Date 2019/6/12 9:15
     * @Param CashFlowItemDto, page
     */
    List<CashFlowItemVo> findCashFlowItemByUpCode(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return List<CashFlowItem>
     * @Author Libao
     * @Description 查询分组
     * @Date 2019/7/4 9:51
     * @Param [cashFlowItem]
     */
    List<CashFlowItemVo> findGroup(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @param cashFlowItemDto
     * @return CashFlowItemVo
     * @author 周鹏
     * @description 根据参数查询现金流量项目信息
     * @date 2019/6/24 16:15
     */
    List<CashFlowItemVo> findListByParam(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return CashFlowItemVo
     * @Author Libao
     * @Description 查询现金流量集合
     * @Date 2019/6/28 11:19
     * @Param [cashFlowItemDto]
     */
    List<CashFlowItemVo> findCashFlowItemList(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return List<Long>
     * @Author Libao
     * @Description 根据CashFlowId查询现金流量项目Id
     * @Date 2019/6/28 11:19
     * @Param [cashFlowItemDto]
     */
    List<Long> findIdsByCashFlowId(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return com.njwd.entity.basedata.vo.CashFlowItemVo
     * @Author Libao
     * @Description 根据上级编码查询数据
     * @Date 2019/7/10 11:01
     * @Param [cashFlowItemDto]
     */
    CashFlowItemVo findUpCashFlowItemByCode(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return Integer
     * @Author Libao
     * @Description 查询是否存在下级预置数据
     * @Date 2019/7/10 11:01
     * @Param [cashFlowItemDto]
     */
    Integer findCashFlowItemCountByUpCode(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return Integer
     * @Author Libao
     * @Description 查询是否存在下级数据
     * @Date 2019/7/10 11:01
     * @Param [cashFlowItemDto]
     */
    Integer findIsExistNextCashFlowItem(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     * @Author Libao
     * @Description 提供总账的查询接口
     * @Date 2019/9/2 15:09
     * @Param [cashFlowItemDto]
     */
    List<CashFlowItemVo> findCashFlowItemForReport(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     * @Author Libao
     * @Description 根据code查询现金流量项目
     * @Date 2019/9/2 15:09
     * @Param [cashFlowItemDto]
     */
    List<CashFlowItemVo> findCashFlowItemByCode(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return List<CashFlowItemVo>
     * @Author Libao
     * @Description 根据code查询现金流量Id，用于总账报表
     * @Date 2019/8/26 15:02
     * @Param [cashFlowItemDto]
     */
    List<CashFlowItemVo> findCashFlowItemIdsByCode(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     * @Author Libao
     * @Description 根据Id查询现金流量集合
     * @Date 2019/9/9 15:18
     * @Param [cashFlowItemDto]
     */
    List<CashFlowItemVo> findCashFlowItemCodeForAdd(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<java.lang.Long>
     * @Author Libao
     * @Description 根据编码查询Id集合
     * @Date 2019/9/9 15:18
     * @Param [cashFlowItemDto]
     */
    List<Long> findOperateIdsByParam(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<java.lang.String>
     * @Author Libao
     * @Description 查询内部往来数据
     * @Date 2019/9/9 16:17
     * @Param [cashFlowItemDto]
     */
    List<String> findInteriorContactCode(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<java.lang.Long>
     * @Author Libao
     * @Description 查询内部往来数据及其上级Id
     * @Date 2019/9/9 16:17
     * @Param [cashFlowItemDto]
     */
    List<String> findInteriorContactAndUp(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<java.lang.String>
     * @Author Libao
     * @Description 根据ids查询codes
     * @Date 2019/9/10 15:58
     * @Param [cashFlowItemDto]
     */
    List<String> findCodesByIds(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.lang.Long
     * @Author Libao
     * @Description 校验版本号
     * @Date 2019/9/10 17:04
     * @Param [cashFlowItemDto]
     */
    Long checkVersion(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     * @Author 周鹏
     * @Description 查询现金流量项目信息，用于会计科目初始化和切换模板时将平台的现金流量信息更新成基础资料的信息
     * @Date 2019/9/20 16:27
     * @Param [cashFlowItemDto]
     */
    List<CashFlowItemVo> findCashFlowItemInfoList(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);

    /**
     * 查询现金流量表信息
     *
     * @param cashFlowDto
     * @return
     */
    CashFlowVo findCashFlowInfo(@Param("cashFlowDto") CashFlowDto cashFlowDto);

    /**
     * 查询存在的平台现金流量项目
     * @Author lj
     * @Date:17:06 2019/12/2
     * @param cashFlowItemDto
     * @return java.util.List<java.lang.Long>
     **/
    List<Long> findCashFlowItemPlatformIds(@Param("cashFlowItemDto") CashFlowItemDto cashFlowItemDto);
}