package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.support.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * @Description 现金流量项目Controller类
 * @Author 朱小明
 * @Date 2019/6/11 17:38
 */
@RequestMapping("financeback/cashFlowItem")
public interface CashFlowItemApi {

    /**
     * @Author 朱小明
     * @Description 现金流量项目新增下级
     * @Date  2019/6/12 17:46
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("addCashFlowItem")
    Result addCashFlowItem(CashFlowItemDto cashFlowItemDto);


    /**
     * @Author 朱小明
     * @Description 导入平台获取现金流量数据
     * @Date  2019/6/19 10:38
     * @Param cashFlowItemDto
     * @return
     */
    @RequestMapping("addCashFlowItemTempleteData")
    Result addCashFlowItemTempleteData(CashFlowItemDto cashFlowItemDto);

    @RequestMapping("addInitData")
    Result addInitData();

    /**
     * @Author 朱小明
     * @Description 根据Id 删除现金流量项目（逻辑删除）
     * @Date  2019/6/12 17:53
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("delCashFlowItemById")
    Result delCashFlowItemById(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author 朱小明
     * @Description 批量删除（逻辑删除）
     * @Date  2019/6/14 17:26
     */
    @RequestMapping("delBatch")
     Result delBatch(List<CashFlowItemDto> cashFlowItemDtos);

   /**
    * @Author 朱小明
    * @Description 根据Id修改现金流量项目数据状态，禁用 (批量禁用)、已失效
    * @Date  2019/6/13 10:50
    * @Param [cashFlowItemDto]
    * @return java.lang.String
    */
    @RequestMapping("updateOrBatchDisable")
    Result updateOrBatchDisable(List<CashFlowItemDto> cashFlowItemDtos);

    /**
     * @Author 朱小明
     * @Description 根据Id修改现金流量项目
     * @Date  2019/6/26 10:52
     * @Param
     * @return
     */

    @RequestMapping("updateCashFlowItemById")
    Result updateCashFlowItemById(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author 朱小明
     * @Description 根据Id修改现金流量项目数据状态，反禁用 （批量反禁用）、已生效
     * @Date  2019/6/13 10:50
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("updateOrBatchEnable")
    Result updateOrBatchEnable(List<CashFlowItemDto> cashFlowItemDtos);

    /**
     * @Author 朱小明
     * @Description 查询分组
     * @Date  2019/6/18 13:53
     * @return
     */
    @RequestMapping("findGroup")
    Result findGroup(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author 朱小明
     * @Description 根据Id查询现金流量项目
     * @Date  2019/6/12 17:50
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("findCashFlowItemById")
    Result findCashFlowItemById(CashFlowItemDto cashFlowItemDto);


    /**
     * @Author 朱小明
     * @Description 从平台获取现金流量模板集合
     * @Date  2019/6/19 10:38
     * @Param
     * @return
     */
    @RequestMapping("findCashFlowItemSelection")
    Result findCashFlowItemSelect(CashFlowDto cashFlowDto);


    /**
     * 账簿类型下拉框.
     *
     * @param
     * @return java.lang.String
     * @author 李宝
     * @date 2019/6/25
     */
    @PostMapping("findAccountBookTypeSelection")
    Result findAccountBookTypeSelection();

    /**
     * 会计准则下拉框.
     *
     * @param
     * @return java.lang.String
     * @author 李宝
     * @date 2019/6/25
     */
    @PostMapping("findAccountingStandardSelection")
    Result findAccountingStandardSelection();

    /**
     * @Author 朱小明
     * @Description 分页展示平台获取现金流量数据
     * @Date  2019/6/19 10:38
     * @Param platformCashFlowItemDto
     * @return jsonstr
     */
    @RequestMapping("findCashFlowItemTempleteByPage")
    Result findCashFlowItemTempleteByPage(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author 朱小明
     * @Description 获取现金流量项目列表，分页
     * @Date  2019/6/11 17:57
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     **/
    @RequestMapping("findCashFlowItemByPage")
    Result<Page<CashFlowItemVo>> findCashFlowItemByPage(@RequestBody CashFlowItemDto cashFlowItemDto);

    /**
     * @Author 朱小明
     * @Description 查询现金流量项目数据（提供外部）
     * @Date 2019/6/11 17:57
     * @param cashFlowItemDto
     * @return java.lang.String
     */
    @RequestMapping("findCashFlowItemListByTemplateCashFlowId")
    Result findCashFlowItemList(CashFlowItemDto cashFlowItemDto);

    /**
     * @param cashFlowItemDto
     * @return java.lang.String
     * @Author 朱小明
	 * @Description 查询现金流量表Id
     * @Date 2019/6/11 17:57
     */
    @RequestMapping("findCashFlowItemTemplateId")
    Result findCashFlowItemTemplateId(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author Libao
     * @Description 查询现金流量项目信息，用于总账报表拼接
     * @Date  2019/8/6 16:50
     * @Param []
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>>
     */
    @RequestMapping("findCashFlowItemForReport")
    Result<List<CashFlowItemVo>> findCashFlowItemForReport(CashFlowItemDto cashFlowItemDto);


/*    *//**
     * @Author 朱小明
     * @Description导出现金流量项目数据
     * @Date 2019/6/11 17:57
     * @param cashFlowItemDto
     * @param response
     *//*
    @RequestMapping("exportCashFlowItemExcel")
    void exportCashFlowItemExcel(CashFlowItemDto cashFlowItemDto, HttpServletResponse response);*/

    /**
     * @Author 朱小明
     * @Description 判断数据是否被引用
     * @Date  2019/7/9 11:51
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     */
    @RequestMapping("checkIsUsed")
    Result checkIsUsed(CashFlowItemDto cashFlowItemDto);

    @RequestMapping("checkIsUsedSingle")
    Result checkIsUsedSingle(CashFlowItemDto cashFlowItemDto);


    /**
     *@Description 下载基础资料模板
     *@Author 朱小明
     *@Date 2019/6/17
     *@Param []
     *@return ResponseEntity
     */
    @RequestMapping("downloadTemplate")
    ResponseEntity downloadTemplate()throws Exception;
    /**
     *@Author 朱小明
     *@Description 上传并校验excel
     *@Param [MultipartFile]
     *@Date 2019/6/17
     *@return String
     */
    @RequestMapping("uploadAndCheckExcelData")
    Result uploadAndCheckExcelData(@RequestParam(value = "file") MultipartFile file, String cashFlowId);

    /**
     *@Description 导入数据
     *@Author 朱小明
     *@Date 2019/6/17
     *@Param [Map]
     *@return String
     */
    @RequestMapping("importCashFlowItemData")
    Result importCashFlowItemData(Map paramMap);

}
