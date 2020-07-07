package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountingItem;
import com.njwd.entity.basedata.dto.AccountingItemDto;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.entity.platform.Subject;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 自定义核算项目 service
 * @Author 薛永利
 * @Date 2019/6/21 8:57
 */
public interface AccountingItemService {

    /**
     * @Description 新增自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 14:55
     * @Param [accountingItemDto]
     * @return java.lang.Long
     */
    Long addAccountingItem(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据 编码 查询自定义核算项目数量
     * @Author 薛永利
     * @Date 2019/6/21 9:03
     * @Param [accountingItemDto]
     * @return int
     */
    int findAccountingItemByCode(AccountingItemDto accountingItemDto);
    /**
     * @Description 查看项目时 校验项目是否被引用
     * @Author 薛永利
     * @Date 2019/8/29 16:25
     * @Param [accountingItemDto]
     * @return int
     */
    int findItemIsReference(AccountingItemDto accountingItemDto);
    /**
     * @Description 根据 名称 查询自定义核算项目数量
     * @Author 薛永利
     * @Date 2019/6/21 9:04
     * @Param [accountingItemDto]
     * @return int
     */
    int findAccountingItemByName(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据 编码名称 查询自定义核算项目数量
     * @Author 薛永利
     * @Date 2019/8/23 9:27
     * @Param [accountingItemDto]
     * @return com.njwd.entity.basedata.vo.AccountingItemVo
     */
    AccountingItemVo findAccountingItemByItemName(AccountingItemDto accountingItemDto);

    /**
     * @Description 修改自定义核算项目信息
     * @Author 薛永利
     * @Date 2019/6/21 9:04
     * @Param [accountingItemDto]
     * @return int
     */
    int updateAccountingItem(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据IDS批量删除自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:36
     * @Param [accountingItemDto]
     * @return com.njwd.support.BatchResult
     */
    BatchResult deleteAccountingItemByIds(AccountingItemDto accountingItemDto);

    /**
     * @return com.njwd.support.BatchResult
     * @Description 根据ID删除自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:36
     * @Param [accountingItemDto]
     */
    BatchResult deleteAccountingItemById(AccountingItemDto accountingItemDto);

    /**
     * @Description 批量禁用/反禁用自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:49
     * @Param [accountingItemDto]
     * @return com.njwd.support.BatchResult
     */
    BatchResult updateBatch(AccountingItemDto accountingItemDto);

    /**
     * @return com.njwd.support.BatchResult
     * @Description 单个禁用/反禁用自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/5 11:49
     * @Param [accountingItemDto]
     */
    BatchResult updateById(AccountingItemDto accountingItemDto);

    /**
     * @Description 查询自定义核算项目列表 分页
     * @Author 薛永利
     * @Date 2019/6/21 9:05
     * @Param [accountingItemDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountingItemVo>
     */
    Page<AccountingItemVo> findAccountingItemList(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据id查自定义核算项目详情
     * @Author 薛永利
     * @Date 2019/6/21 9:05
     * @Param [accountingItemDto]
     * @return com.njwd.entity.basedata.vo.AccountingItemVo
     */
    AccountingItemVo findById(AccountingItemDto accountingItemDto);

    /**
     * @Description 导出自定义核算项目excel
     * @Author 薛永利
     * @Date 2019/6/21 9:06
     * @Param [accountingItemDto, response]
     * @return void
     */
    void exportExcel(AccountingItemDto accountingItemDto, HttpServletResponse response);

    /**
     * @Description 批量插入自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:24
     * @Param [accountingItemList]
     * @return int
     */
    int addBatchAccountingItem(List<AccountingItem> accountingItemList);

    /**
     * @Description 查询所有未删除的自定义核算类型
     * @Author 周鹏
     * @Date 2019/8/23 17:12
     * @Param subjectInfo
     * @return List<AccountingItemVo>
     */
    List<AccountingItemVo> findAllAccountItem(Subject subjectInfo);

    /**
     * @Description 查询项目是否有区值
     * @Author 薛永利
     * @Date 2019/8/21 9:26
     * @Param [accountingItemDto]
     * @return java.util.List<java.lang.Long>
     */
    List<Long> findItemRelaValueById(AccountingItemDto accountingItemDto);
}
