package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountingItem;
import com.njwd.entity.basedata.dto.AccountingItemDto;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.entity.platform.Subject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description AccountingItemMapper
 * @Author 薛永利
 * @Date 2019/6/21 9:22
 */
public interface AccountingItemMapper extends BaseMapper<AccountingItem> {

    /**
     * @Description 查询自定义核算项目列表分页
     * @Author 薛永利
     * @Date 2019/7/4 9:52
     * @Param [page, accountingItemDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountingItemVo>
     */
    Page<AccountingItemVo> findPage(@Param("page") Page<AccountingItemVo> page, @Param("accountingItemDto") AccountingItemDto accountingItemDto);

    /**
     * @Description 根据ID查询自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:27
     * @Param [id]
     * @return com.njwd.entity.basedata.vo.AccountingItemVo
     */
    AccountingItemVo findById(@Param("id") Long id);

    /**
     * @Description 根据item_id删除/禁用/反禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/6/21 9:24
     * @Param [accountingItemDto]
     * @return int
     */
    int updateAccountingItemValue(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据 编码名称 查询自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:27
     * @Param [accountingItemDto]
     * @return int
     */
    int findAccountingItemByName(AccountingItemDto accountingItemDto);
    /**
     * @Description 校验自定义核算项目版本号是否一致
     * @Author 薛永利
     * @Date 2019/6/21 9:14
     * @Param [accountingItemDto]
     * @return int
     */
    int findAccountingItemByVersion(AccountingItemDto accountingItemDto);
    /**
     * @Description 根据 编码 查询自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:27
     * @Param [accountingItemDto]
     * @return int
     */
    int findAccountingItemByCode(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据 编码名称 查询自定义核算项目数量
     * @Author 薛永利
     * @Date 2019/8/23 9:27
     * @Param [accountingItemDto]
     * @return com.njwd.entity.basedata.vo.AccountingItemVo
     */
    AccountingItemVo findAccountingItemByItemName(AccountingItemDto accountingItemDto);

    /**
     * @Description 批量插入自定义核算项目
     * @Author 薛永利
     * @Date 2019/7/9 10:27
     * @Param [accountingItemList]
     * @return int
     */
    int addBatchAccountingItem(List<AccountingItem> accountingItemList);

    /**
     * @Description 查询自定义核算项目是否被删除, 被禁用明细
     * @Author 薛永利
     * @Date 2019/7/9 10:27
     * @Param [accountingItemDto]
     * @return java.util.List<com.njwd.entity.basedata.vo.AccountingItemVo>
     */
    List<AccountingItemVo> findIsDel(AccountingItemDto accountingItemDto);

    /**
     * @Description 查询所有未删除的自定义核算类型
     * @Author 周鹏
     * @Date 2019/8/23 17:12
     * @Param subjectInfo
     * @return List<AccountingItemVo>
     */
    List<AccountingItemVo> findAllAccountItemInfo(Subject subjectInfo);
    /**
     * @Description 根据itemId查询大区值id
     * @Author 薛永利
     * @Date 2019/8/16 15:44
     * @Param [accountingItemDto]
     * @return java.util.List<java.lang.Long>
     */
    List<Long> findAccountingItemValueByItemId(AccountingItemDto accountingItemDto);
    /**
     * @Description 查询项目是否有区值
     * @Author 薛永利
     * @Date 2019/8/21 9:26 
     * @Param [accountingItemDto]
     * @return java.util.List<java.lang.Long>
     */
    List<Long> findItemRelaValueById(AccountingItemDto accountingItemDto);
}