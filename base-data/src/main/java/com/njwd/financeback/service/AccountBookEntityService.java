package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBookEntity;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;

import java.util.List;

/**
 * 核算主体
 *
 * @Author: Zhuzs
 * @Date: 2019-05-17 13:43
 */

public interface AccountBookEntityService {

    /**
     * 新增
     *
     * @param: [accountBookEntity]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:31
     */
    int insert(AccountBookEntity accountBookEntity);

    /**
     * 查询核算主体列表
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:31
     */
    List<AccountBookEntityVo> findAccountBookEntityList(AccountBookDto accountBookDto);

	/**
	 * @Description 查询核算主体分页
	 * @Author 郑勇浩
	 * @Data 2019/7/31 16:05
	 * @Param [accountBookDto] 账簿ID/公司ID/公司编码或名称
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 */
	Page<AccountBookEntityVo> findAccountBookEntityPage(AccountBookDto accountBookDto);

	/**
	 * 账簿ID list 查询核算主体列表 分页
	 *
	 * @param: [accountBookEntityDto]
	 * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 * @author: zhuzs
	 * @date: 2019-09-16 17:31
	 */
	Page<AccountBookEntityVo> findAccountBookEntityPageByAccBookIdList(AccountBookEntityDto accountBookEntityDto);

	/**
	 * 根据 账簿ID list 查询核算主体列表
	 *
	 * @param: [accountBookEntityDto]
	 * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 * @author: zhuzs
	 * @date: 2019-09-16 17:32
	 */
	List<AccountBookEntityVo> findAccountBookEntityListByAccBookIdList(AccountBookEntityDto accountBookEntityDto);

	/**
	 * 查询 用户有操作权限的核算主体列表 分页(不传参数返回空)
	 *
	 * @param: [accountBookEntityDto]
	 * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 * @author: zhuzs
	 * @date: 2019-09-16 17:40
	 */
	Page<AccountBookEntityVo> findAuthOperationalEntityPage(AccountBookEntityDto accountBookEntityDto);

	/**
	 * 查询 用户有操作权限的核算主体列表 (不传参数返回空)
	 *
	 * @param: [accountBookEntityDto]
	 * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 * @author: zhuzs
	 * @date: 2019-09-16 17:40
	 */
	Page<AccountBookEntityVo> findAuthOperationalEntityList(AccountBookEntityDto accountBookEntityDto);

	/**
	 * 查询 用户有操作权限的核算主体
	 *
	 * @param: [accountBookEntityDto]
	 * @return: com.njwd.entity.basedata.vo.AccountBookEntityVo
	 * @author: zhuzs
	 * @date: 2019-09-16 17:40
	 */
	AccountBookEntityVo findAuthOperationalEntity(AccountBookEntityDto accountBookEntityDto);

	/**
	 * @description: 
	 * @param: [accountBookEntityDto]
	 * @return: com.njwd.entity.basedata.vo.AccountBookEntityVo 
	 * @author: xdy        
	 * @create: 2019-10-17 09:07
	 */
	AccountBookEntityVo findAccountBookEntityById(AccountBookEntityDto accountBookEntityDto);

}
