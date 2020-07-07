package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author Libao
 * @Description 现金流量项目Service类
 * @Date  2019/6/11 17:40
 **/
public interface CashFlowItemService {

    /**
     * @Author Libao
     * @Description 导入现金流量模板数据
     * @Date  2019/6/19 11:45
     * @Param temleteList
     * @return  int
     */
    int  addCashFlowItemBatch(List<CashFlowItemDto> temleteList);


    /**
     * @Author Libao
     * @Description 现金流量项目新增下级
     * @Date 2019/6/12 14:10
     * @Param cashFlowItemDto
     * @return Long
     */
    CashFlowItemVo addCashFlowItem(CashFlowItemDto cashFlowItemDto);

    int findCount(Long rootEnterpriseId);

    /**
     * @Author Libao
     * @Description 根据Id逻辑删除现金流量项目
     * @Date 2019/6/12 17:01
     * @Param [cashFlowItemDto]
     * @return int
     */
    int delCashFlowItemById(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 批量删除现金流量项目
     * @Date 2019/6/13 14:28
     * @Param [cashFlowItemDtos]
     * @return java.lang.String
     */
    BatchResult delBatch(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 根据Id修改现流量项目数据状态(禁用、批量禁用、反禁用、批量反禁用)
     * @Date 2019/6/13 10:33
     * @Param [cashFlowItemDto]
     * @return CashFlowItemVo
     */
    BatchResult updateOrBatch(CashFlowItemDto cashFlowItemDto, byte flag);

    /**
     * @Author Libao
     * @Description 根据Id修改现金流量项目
     * @Date  2019/6/26 11:08
     * @Param  cashFlowItemDto
     * @return  int
     */
	Long updateCashFlowItemById(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 分页查询现金流量项目
     * @Date 2019/6/11 17:54
     * @Param [cashFlowItemDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.basedata.entity.CashFlowItemVo>
     */
    Page<CashFlowItemVo> findPage(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 根据Id查询现金流量项目
     * @Date 2019/6/12 15:58
     * @Param [cashFlowItemDto]
     * @return com.njwd.entity.basedata.CashFlowItem
     */
    CashFlowItemVo findCashFlowItemById(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Date 2019/6/12 14:10
     * @Description 根据 编码 查询现金流量项目
     * @param cashFlowItemDto
     * @return Ingeter
     */
    Integer findCashFlowItemByCode(CashFlowItemDto cashFlowItemDto);


    /**
     * @Author Libao
     * @Date 2019/6/12 14:10
     * @Description 根据 名称 查询现金流量项目
     * @param cashFlowItemDto
     * @return Ingeter
     */
    Integer findCashFlowItemByName(CashFlowItemDto cashFlowItemDto);


   /**
    * @Author Libao
    * @Description 查询分组和方向
    * @Date  2019/6/18 13:57
    * @Param []
    * @return com.njwd.entity.basedata.vo.CashFlowItemVo
    */
   List<CashFlowItemVo> findGroup(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 根据租户Id 和 现金流量项目表Id查询现金流量项目
     * @Date  2019/7/2 10:32
     * @ParamcashFlowItemDto
     * @return list
     */
    List<CashFlowItemVo> findCashFlowItemList(CashFlowItemDto cashFlowItemDto);


    /**
     * @Author Libao
     * @Description 导入参数code校验
     * @Date  2019/7/5 9:07
     * @Param [cashFlowItemDto]
     * @return int
     */
    int checkCode(CashFlowItemDto cashFlowItemDto);
    /**
     * @Author Libao
     * @Description 导入参数name校验
     * @Date  2019/7/5 9:07
     * @Param [cashFlowItemDto]
     * @return int
     */
    int checkName(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 查询上级项目（用于校验）
     * @Date  2019/7/5 9:07
     * @Param [cashFlowItemDto]
     * @return CashFlowItemVo
     */
    CashFlowItemVo checkUpCashFlowItem(CashFlowItemDto cashFlowItemDto);
    /**
     * @Author Libao
     * @Description 查询现金流量项目信息，用于总账报表拼接
     * @Date  2019/8/6 16:27
     * @Param []
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     */
    List<CashFlowItemVo> findCashFlowItemForReport(CashFlowItemDto cashFlowItemDto);


    /**
     * @Author Libao
     * @Description 查询现金流量项目信息，校验是否存在禁用上级
     * @Date  2019/8/6 16:27
     * @Param [cashFlowItemDtos]
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     */
    List<CashFlowItemVo> findCashFlowItemForEnable(List<CashFlowItemDto> cashFlowItemDtos);



    /**
     * @Author Libao
     * @Description 查询上级id（用于报表）
     * @Date  2019/7/5 9:07
     * @Param [cashFlowItemDto]
     * @return CashFlowItemVo
     */
    List<CashFlowItemVo>  findCashFlowItemIdForReport(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 根据Id查询未使用下级
     * @Date  2019/9/4 11:27
     * @Param [cashFlowItemDto]
     * @return com.njwd.entity.platform.vo.CashFlowItemVo
     */
    CashFlowItemVo findCashFlowItemCodeForAdd(CashFlowItemDto cashFlowItemDto);


    /**
     * @Author Libao
     * @Description 根据上级code查询上级Id
     * @Date  2019/9/6 9:37
     * @Param [cashFlowItemDto]
     * @return com.njwd.entity.platform.vo.CashFlowItemVo
     */
    CashFlowItemVo findUpCashFlowItemByCode(CashFlowItemDto cashFlowItemDto);

    /**
     * 查询现金流量项目列表-平台增量数据
     * @Author lj
     * @Date:16:56 2019/12/2
     * @param cashFlowItemDto
     * @return com.njwd.support.Result
     **/
    List<CashFlowItemVo> findPlatformCashFlowItemList(CashFlowItemDto cashFlowItemDto);


    /**
     * @Author Libao
     * @Description 查询是否被引用
     * @Date  2019/7/9 11:43
     * @Param [cashFlowItemDto]
     * @return boolean
     */
    Boolean checkIsUsed(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 判断是否被引用
     * @Date  2019/9/2 15:07
     * @Param [cashFlowItemDto]
     * @return ReferenceResult
     */
    ReferenceResult checkIsUsedSingle(CashFlowItemDto cashFlowItemDto);


    /**
     * 导出excel
     * @param cashFlowItemDto
     * @param response
     */
    void exportExcel(CashFlowItemDto cashFlowItemDto, HttpServletResponse response);

    /**
     * @Author Libao
     * @Description 版本号校验
     * @Date  2019/9/10 17:30
     * @Param [cashFlowItemDto]
     * @return void
     */
    void checkVersion(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author 周鹏
     * @Description 查询现金流量项目信息，用于会计科目初始化和切换模板时将平台的现金流量信息更新成基础资料的信息
     * @Date  2019/9/20 16:27
     * @Param [cashFlowItemDto]
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     */
    List<CashFlowItemVo> findCashFlowItemInfoList(CashFlowItemDto cashFlowItemDto);
}

