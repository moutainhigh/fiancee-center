package com.njwd.platform.api;

import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.vo.AccountBookCatVo;
import com.njwd.entity.platform.vo.AccountBookCategoryVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description
 * @Date:16:54 2019/6/24
 **/
@RequestMapping("platform/accountbookcategory")
public interface AccountBookCategoryApi {
    
    /**
     * @Description 根据账簿类型ID和会计准则ID查询账簿分类
     * @Param [platformAccountBookCategoryDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccBoListByAccTypeAndStand")
    Result<List<AccountBookCategoryVo>> findAccBoListByAccTypeAndStand(AccountBookCategoryDto accountBookCategoryDto);

    /**
     * @Description 根据账簿类型ID和租户ID查询账簿分类
     * @Author lj
     * @Date:9:06 2019/7/12
     * @Param [platformAccountBookCategoryDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccBookListByTypeAndEntId")
    Result<List<AccountBookCatVo>> findAccBookListByTypeAndEntId(AccountBookCategoryDto accountBookCategoryDto);


    /**
     * @description: 查询账簿分类
     * @param: [accountBookCategoryDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.AccountBookCategoryVo>> 
     * @author: xdy        
     * @create: 2019-11-18 09:30 
     */
    @RequestMapping("findAccountBookCategoryList")
    Result<List<AccountBookCategoryVo>> findAccountBookCategoryList(@RequestBody AccountBookCategoryDto accountBookCategoryDto);
    
}
