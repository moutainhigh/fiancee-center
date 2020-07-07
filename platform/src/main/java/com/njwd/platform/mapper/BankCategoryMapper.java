package com.njwd.platform.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.BankCategory;
import com.njwd.entity.platform.dto.BankCategoryDto;
import com.njwd.entity.platform.vo.BankCategoryVo;
import org.apache.ibatis.annotations.Param;

/**
 * 银行账号mapper
 *
 * @author 周鹏
 * @date 2019/11/18
 */
public interface BankCategoryMapper extends BaseMapper<BankCategory> {
    /**
     * 查询分页列表
     *
     * @param bankCategoryDto
     * @return
     */
    Page<BankCategoryVo> findBankCategoryPage(Page<BankCategoryVo> page, @Param("bankCategoryDto") BankCategoryDto bankCategoryDto);

    /**
     * 根据id查询银行信息
     *
     * @param bankCategoryDto
     * @return
     */
    BankCategoryVo findBankCategoryById(@Param("bankCategoryDto") BankCategoryDto bankCategoryDto);
}