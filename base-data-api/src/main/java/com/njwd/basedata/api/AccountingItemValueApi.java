package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Description 自定义核算项目大区值
 * @Author 朱小明
 * @Date 2019/6/21 8:57
 */
@RequestMapping("financeback/accountingItemValue")
public interface AccountingItemValueApi {

    /**
     * @Description 新增自定义核算项目大区值
     * @Author 朱小明
     * @Date 2019/6/20 18:11
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @RequestMapping("addAccountingItemValue")
    Result<Long> addAccountingItemValue(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 根据IDS批量删除自定义核算项目大区值
     * @Author 朱小明
     * @Date 2019/7/9 10:30
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @RequestMapping("deleteAccountingItemValueByIds")
    public Result<BatchResult> deleteAccountingItemValueByIds(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 修改自定义核算项目大区值信息
     * @Author 朱小明
     * @Date 2019/6/20 18:11
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @RequestMapping("updateAccountingItemValue")
    Result<Integer> updateAccountingItemValue(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 禁用自定义核算项目大区值
     * @Author 朱小明
     * @Date 2019/6/20 18:12
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("disableAccountingItemValueBatch")
    Result<BatchResult> disableBatch(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 反禁用自定义核算项目大区值
     * @Author 朱小明
     * @Date 2019/6/20 18:12
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("enableAccountingItemValueBatch")
    Result<BatchResult> enableBatch(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 查询自定义核算项目大区值列表 分页
     * @Author 朱小明
     * @Date 2019/6/20 18:13
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("findAccountingItemValueList")
    Result<Page<AccountingItemValueVo>> findAccountingItemValueList(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 根据id查自定义核算项目大区值详情
     * @Author 朱小明
     * @Date 2019/6/20 18:13
     * @Param [accountingItemValueDto]
     * @return java.lang.String
     */
    @PostMapping("findAccountingItemValueById")
    Result<AccountingItemValueVo> findById(AccountingItemValueDto accountingItemValueDto);

    /**
     * @Description 下载自定义核算项目大区值模板
     * @Author 朱小明
     * @Date 2019/6/20 18:14
     * @Param []
     * @return org.springframework.http.ResponseEntity
     */
    @RequestMapping("downloadAccountingItemValueTemplate")
    ResponseEntity downloadAccountingItemValueTemplate() throws Exception;

    /**
     * @Description 查询所有未删除的自定义核算项目
     * @Author wuweiming
     * @Date 2019/08/07 14:36
     * @Param []
     * @return Result<AccountingItemValueVo>
     */
    @RequestMapping("findAllAccountItemValueByItemId")
    Result<List<AccountingItemValueVo>> findAllAccountItemValueByItemId(AccountingItemValueDto dto);

    /**
     * @Description 查询所有未删除的辅助核算
     * @Author wuweiming
     * @Param [AccountingItemValueDto]
     * @return Result<List<AccountingItemValueVo>>
     **/
    @PostMapping("findAllAuxiliaryItemValue")
    Result<List<AccountingItemValueVo>> findAllAuxiliaryItemValue(AccountingItemValueDto dto);
}
