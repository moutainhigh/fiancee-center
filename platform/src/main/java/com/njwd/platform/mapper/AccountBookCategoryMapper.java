package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountBookCategory;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.vo.AccountBookCategoryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * @Author liuxiang
 * @Description 账簿分类
 * @Date:16:45 2019/6/24
 **/
public interface AccountBookCategoryMapper extends BaseMapper<AccountBookCategory> {

   /**
    * @Description 根据账簿类型ID和会计准则ID查询账簿分类
    * @Author liuxiang
    * @Date:15:23 2019/7/2
    * @Param [accountBookCategoryVo]
    * @return java.util.List<com.njwd.platform.entity.vo.AccountBookCategoryVo>
    **/
   List<AccountBookCategoryVo> findAccBoListByAccTypeAndStand(AccountBookCategoryDto accountBookCategoryDto);

   /**
    * @Description 根据账簿类型ID和租户ID查询账簿分类
    * @Author lj
    * @Date:9:03 2019/7/12
    * @Param [accountBookCategoryDto]
    * @return java.util.List<com.njwd.platform.entity.vo.AccountBookCategoryVo>
    **/
   List<AccountBookCategoryVo> findAccBookListByTypeAndEntId(AccountBookCategoryDto accountBookCategoryDto);
   /** 刘遵通
    * 查询页面 （分页）
    * @param accountBookCategoryDto
    * @return
    */
   Page<AccountBookCategoryVo> findPage(@Param("page") Page<AccountBookCategoryVo> page, @Param("accountBookCategoryDto") AccountBookCategoryDto accountBookCategoryDto);

   /** 刘遵通
    *  根据id集合查询list数据
    * @param accountBookCategoryDto
    * @return
    */
   List<AccountBookCategoryVo> findAccBookListByIds(@Param("accountBookCategoryDto") AccountBookCategoryDto accountBookCategoryDto);

   /**
    * 删除  审核  发布
    * @param accountBookCategoryDto
    * @param AccountBookCategoryList
    * @return
    */
   int delteOrCheckOrRelease(@Param("accountBookCategoryDto") AccountBookCategoryDto accountBookCategoryDto, @Param("accountBookCategoryList") List<AccountBookCategoryVo> AccountBookCategoryList);

    List<AccountBookCategoryVo> findAccountBookCategoryList(AccountBookCategoryDto accountBookCategoryDto);

   AccountBookCategoryVo findAccountBookCategory(@Param("subjectId")Long subjectId);
}