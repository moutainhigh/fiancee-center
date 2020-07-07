package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountingItemValue;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description AccountingItemMapper
 * @Author 薛永利
 * @Date 2019/6/21 9:22
 */
public interface AccountingItemValueMapper extends BaseMapper<AccountingItemValue> {

    /**
     * @Description 查询自定义核算项目大区值列表 分页
     * @Author 薛永利
     * @Date 2019/7/4 9:53
     * @Param [accountingItemValueDto, page]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountingItemValueVo>
     */
    Page<AccountingItemValueVo> findPage(@Param("page") Page<AccountingItemValueVo> page, @Param("accountingItemValueDto") AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 根据ID查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:29
     * @Param [id]
     * @return com.njwd.entity.basedata.vo.AccountingItemValueVo
     */
    AccountingItemValueVo findById(@Param("id") Long id);

    /**
     * @Description 根据 编码名称 查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:29
     * @Param [accountingItemValueDto]
     * @return int
     */
    int findAccountingItemValueByName(AccountingItemValueDto accountingItemValueDto);
    /**
     * @Description 校验自定义核算项目版本号是否一致
     * @Author 薛永利
     * @Date 2019/6/21 9:14
     * @Param [accountingItemDto]
     * @return int
     */
    int findAccountingItemValueByVersion(AccountingItemValueDto accountingItemValueDto);
    /**
     * @Description 根据 编码 查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:28
     * @Param [accountingItemValueDto]
     * @return int
     */
    int findAccountingItemValueByCode(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 批量插入自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/3 15:09
     * @Param [accountingItemValueList]
     * @return int
     */
    int addBatchAccountingItemValue(List<AccountingItemValue> accountingItemValueList);

    /**
     * @Description 查询自定义核算项目大区值是否被删除, 被禁用明细
     * @Author 薛永利
     * @Date 2019/7/9 10:28
     * @Param [accountingItemValueDto]
     * @return java.util.List<com.njwd.entity.basedata.vo.AccountingItemValueVo>
     */
    List<AccountingItemValueVo> findIsDel(AccountingItemValueDto accountingItemValueDto);
    /**
     * @Description 根据ids查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:28
     * @Param [accountingItemValueDto]
     * @return java.util.List<com.njwd.entity.basedata.vo.AccountingItemValueVo>
     */
    List<AccountingItemValueVo> findItemValueListById(@Param("accountingItemValueDto") AccountingItemValueDto accountingItemValueDto);
    /**
     * @Description 查询所有未删除的自定义核算
     * @Author wuweiming
     * @Param [AccountingItemValueDto]
     * @return Result<List<AccountingItemValueVo>>
     */
    List<AccountingItemValueVo> findAllAccountItemValueByItemId(@Param("accountingItemValueDto") AccountingItemValueDto dto);

    /**
     * @Description 查询所有未删除的辅助核算
     * @Author wuweiming
     * @Param [AuxiliaryItemDto]
     * @return List<AuxiliaryItemVo>
     **/
    List<AccountingItemValueVo> findAllAuxiliaryItemValue(@Param("accountingItemValueDto") AccountingItemValueDto dto);
}