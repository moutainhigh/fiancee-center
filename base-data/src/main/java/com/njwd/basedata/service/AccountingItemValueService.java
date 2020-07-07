package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountingItemValue;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 自定义核算项目大区值 service
 * @Author 薛永利
 * @Date 2019/6/21 8:57
 */
public interface AccountingItemValueService {

    /**
     * @Description 新增自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 14:52
     * @Param [accountingItemValueDto]
     * @return java.lang.Long
     */
    Long addAccountingItemValue(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 根据 编码 查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/6/21 8:59
     * @Param [accountingItemValueDto]
     * @return int
     */
    int findAccountingItemValueByCode(AccountingItemValueDto accountingItemValueDto);
    /**
     * @Description 查看项目值时 校验项目值是否被引用
     * @Author 薛永利
     * @Date 2019/8/29 16:25
     * @Param [accountingItemValueDto]
     * @return int
     */
    int findItemValueIsReference(AccountingItemValueDto accountingItemValueDto);
    /**
     * @Description 根据 编码名称 查询自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/6/21 9:00
     * @Param [accountingItemValueDto]
     * @return int
     */
    int findAccountingItemValueByName(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 修改自定义核算项目大区值信息
     * @Author 薛永利
     * @Date 2019/6/21 9:01
     * @Param [accountingItemValueDto]
     * @return int
     */
    int updateAccountingItemValue(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 根据IDS批量删除自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 13:35
     * @Param [accountingItemValueDto]
     * @return com.njwd.support.BatchResult
     */
    BatchResult deleteAccountingItemValueByIds(AccountingItemValueDto accountingItemValueDto);

    /**
     * @return com.njwd.support.BatchResult
     * @Description 根据ID删除自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 13:35
     * @Param [accountingItemValueDto]
     */
    BatchResult deleteAccountingItemValueById(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 批量禁用/反禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 13:40
     * @Param [accountingItemValueDto]
     * @return com.njwd.support.BatchResult
     */
    BatchResult updateBatch(AccountingItemValueDto accountingItemValueDto);

    /**
     * @return com.njwd.support.BatchResult
     * @Description 单个禁用/反禁用自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/5 13:40
     * @Param [accountingItemValueDto]
     */
    BatchResult updateById(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 查询自定义核算项目大区值列表 分页
     * @Author 薛永利
     * @Date 2019/6/21 9:02
     * @Param [accountingItemValueDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountingItemVo>
     */
    Page<AccountingItemValueVo> findAccountingItemValueList(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 根据id查自定义核算项目大区值详情
     * @Author 薛永利
     * @Date 2019/6/21 9:02
     * @Param [accountingItemValueDto]
     * @return com.njwd.entity.basedata.vo.AccountingItemVo
     */
    AccountingItemValueVo findById(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 导出自定义核算项目大区值excel
     * @Author 薛永利
     * @Date 2019/6/21 9:02
     * @Param [accountingItemValueDto, response]
     * @return void
     */
    void exportExcel(AccountingItemValueDto accountingItemValueDto, HttpServletResponse response);

    /**
     * @Description 批量插入自定义核算项目大区值
     * @Author 薛永利
     * @Date 2019/7/9 10:26
     * @Param [accountingItemValueList]
     * @return int
     */
    int addBatchAccountingItemValue(List<AccountingItemValue> accountingItemValueList);

    /**
     * @Description 查询所有未删除的自定义核算
     * @Author wuweiming
     * @Param [AccountingItemValueDto]
     * @return Result<List<AccountingItemValueVo>>
     */
    List<AccountingItemValueVo> findAllAccountItemValueByItemId(AccountingItemValueDto dto);

    /**
     * @Description 查询所有未删除的辅助核算
     * @Author wuweiming
     * @Param [AuxiliaryItemDto]
     * @return List<AccountingItemValueVo>
     **/
    List<AccountingItemValueVo> findAllAuxiliaryItemValue(AccountingItemValueDto dto);
}
