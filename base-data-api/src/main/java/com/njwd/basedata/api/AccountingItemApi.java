package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountingItem;
import com.njwd.entity.basedata.dto.AccountingItemDto;
import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 自定义核算项目大区值
 * @Author 朱小明
 * @Date 2019/6/21 8:57
 */
@RequestMapping("financeback/accountingItem")
public interface AccountingItemApi {

    /**
     * @Description 新增自定义核算项目
     * @Author 朱小明
     * @Date 2019/7/31 19:27
     * @Param [accountingItemDto]
     * @return java.lang.String
     **/
    @RequestMapping("addAccountingItem")
    Result<Long> addAccountingItem(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据IDS批量删除自定义核算项目
     * @Author 朱小明
     * @Date 2019/7/9 10:29
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @RequestMapping("deleteAccountingItemByIds")
    Result<BatchResult> deleteAccountingItemByIds(AccountingItemDto accountingItemDto);

    /**
     * @Description 修改自定义核算项目信息
     * @Author 朱小明
     * @Date 2019/7/9 10:29
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @RequestMapping("updateAccountingItem")
    Result<Integer> updateAccountingItem(AccountingItemDto accountingItemDto);

    /**
     * @Description 禁用自定义核算项目
     * @Author 朱小明
     * @Date 2019/6/21 8:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @PostMapping("disableAccountingItemBatch")
    Result<BatchResult> disableBatch(AccountingItemDto accountingItemDto);

    /**
     * @Description 反禁用自定义核算项目
     * @Author 朱小明
     * @Date 2019/6/21 8:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @PostMapping("enableAccountingItemBatch")
    Result<BatchResult> enableBatch(AccountingItemDto accountingItemDto);

    /**
     * @Description 查询自定义核算项目列表 分页
     * @Author 朱小明
     * @Date 2019/6/21 8:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @PostMapping("findAccountingItemList")
    Result<Page<AccountingItemVo>> findAccountingItemList(AccountingItemDto accountingItemDto);

    /**
     * @Description 根据id查自定义核算项目详情
     * @Author 朱小明
     * @Date 2019/6/20 17:52
     * @Param [accountingItemDto]
     * @return java.lang.String
     */
    @RequestMapping("findAccountingItemById")
    Result<AccountingItemVo> findById(AccountingItemDto accountingItemDto);

    /**
     * @Description 下载自定义核算项目模板
     * @Author 朱小明
     * @Date 2019/6/21 8:54
     * @Param []
     * @return org.springframework.http.ResponseEntity
     */
    @RequestMapping("downloadAccountingItemTemplate")
    ResponseEntity downloadAccountingItemTemplate() throws Exception;

    /**
     * @Description 查询所有未删除的自定义核算类型
     * @Author wuweiming
     * @Date 2019/08/07 09:44
     * @Param []
     * @return Result<AccountingItemVo>
     */
    @RequestMapping("findAllAccountItem")
    Result<List<AccountingItemVo>> findAllAccountItem();
}
