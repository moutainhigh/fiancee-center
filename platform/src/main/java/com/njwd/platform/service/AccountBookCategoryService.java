package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountBookCategory;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.vo.AccountBookCatVo;
import com.njwd.entity.platform.vo.AccountBookCategoryVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
/**
 * @Author liuxiang
 * @Description 账簿分类
 * @Date:16:49 2019/6/24
 **/
public interface AccountBookCategoryService {

    /**
     * @Description 根据账簿类型ID和会计准则ID查询账簿分类
     * @Author lj
     * @Date:10:25
     * @Param [accountBookCategoryDto]
     * @return java.util.List<com.njwd.platform.entity.vo.AccountBookCategoryVo>
     **/
    List<AccountBookCategoryVo> findAccBoListByAccTypeAndStand(AccountBookCategoryDto accountBookCategoryDto);

    /**
     * @Description 根据账簿类型ID和租户ID查询账簿分类
     * @Author lj
     * @Date:9:14 2019/7/12
     * @Param [accountBookCategoryDto]
     * @return java.util.List<com.njwd.platform.entity.vo.AccountBookCatVo>
     **/
    List<AccountBookCatVo> findAccBookListByTypeAndEntId(AccountBookCategoryDto accountBookCategoryDto);

    /** 刘遵通
     * 增加核算账簿分类
     * @param accountBookCategoryDto
     * @return
     */
    Long addAccountBookCategory(AccountBookCategoryDto accountBookCategoryDto);
    /** 刘遵通
     * 删除核算账簿分类
     * @param accountBookCategoryDto
     * @return
     */
    BatchResult deleteAccountBookCategory(AccountBookCategoryDto accountBookCategoryDto);
    /**刘遵通
     * 审核
     * @param accountBookCategoryDto
     * @return
     */
    BatchResult checkApprove(AccountBookCategoryDto accountBookCategoryDto);
    /**刘遵通
     * 反审核
     * @param accountBookCategoryDto
     * @return
     */
    BatchResult reversalApprove(AccountBookCategoryDto accountBookCategoryDto);
    /** 刘遵通
     * 查询页面 （分页）
     * @param accountBookCategoryDto
     * @return
     */
    Page<AccountBookCategoryVo> findPage(AccountBookCategoryDto accountBookCategoryDto);

    /**
     * 导出
     * @param accountBookCategoryDto
     * @param response
     */
    void exportExcel(AccountBookCategoryDto accountBookCategoryDto, HttpServletResponse response);
    /**
     * 编辑中的查看
     * @param accountBookCategoryDto
     * @return
     */
    AccountBookCategory selectById(AccountBookCategoryDto accountBookCategoryDto);
    /**
     * 修改
     * @param accountBookCategoryDto
     * @return
     */
    Long updateById(AccountBookCategoryDto accountBookCategoryDto);

    List<AccountBookCategoryVo> findAccountBookCategoryList(AccountBookCategoryDto accountBookCategoryDto);

    /**
     * 发布
     * @param accountBookCategoryDto
     * @return
     */
    BatchResult release(AccountBookCategoryDto accountBookCategoryDto);
}
