package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBookEntity;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 核算主体
 *
 * @Author: Zhuzs
 * @Date: 2019-05-21 15:19
 */
public interface AccountBookEntityMapper extends BaseMapper<AccountBookEntity> {

    /**
     * 根据 账簿ID/公司ID/公司编码或名称 查询核算主体列表 不分页
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:42
     */
    List<AccountBookEntityVo> findList(AccountBookDto accountBookDto);

	/**
	 * @Description 查询核算主体列表 分页
	 * @Author 郑勇浩
	 * @Data 2019/7/31 16:00
	 * @Param [page, accountBookDto] 账簿ID/公司ID/公司编码或名称
	 * @return java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 */
	Page<AccountBookEntityVo> findPage(Page<AccountBookVo> page, @Param("AccountBookDto") AccountBookDto accountBookDto);

	/**
	 * 查询 核算主体列表 分页
	 *
	 * @param: [page, accountBookEntityDto]
	 * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 * @author: zhuzs
	 * @date: 2019-09-16 17:42
	 */
    Page<AccountBookEntityVo> findAccountBookEntityPageByAccBookIdList(@Param("page")Page<AccountBookEntityVo> page,@Param("accountBookEntityDto") AccountBookEntityDto accountBookEntityDto);

	/**
	 * 根据 账簿ID List 查询 用户有操作权限的核算主体列表 分页
	 *
	 * @param: [page, accountBookEntityDto]
	 * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 * @author: zhuzs
	 * @date: 2019-09-16 17:43
	 */
	Page<AccountBookEntityVo> findAuthOperationalEntityPage(@Param("page")Page<AccountBookEntityVo> page,@Param("accountBookEntityDto") AccountBookEntityDto accountBookEntityDto);

	/**
	 * 根据 账簿ID 查询 用户有操作权限的核算主体
	 *
	 * @param: [accountBookEntityDto]
	 * @return: com.njwd.entity.basedata.vo.AccountBookEntityVo
	 * @author: zhuzs
	 * @date: 2019-09-16 17:43
	 */
	AccountBookEntityVo findAuthOperationalEntity(@Param("accountBookEntityDto") AccountBookEntityDto accountBookEntityDto);
	
	/**
	 * @description: 
	 * @param: [accountBookEntityDto]
	 * @return: com.njwd.entity.basedata.vo.AccountBookEntityVo 
	 * @author: xdy        
	 * @create: 2019-10-17 09:08 
	 */
	AccountBookEntityVo findAccountBookEntityById(AccountBookEntityDto accountBookEntityDto);
	
	/**
	 * 根据业务单元idList 获取核算主体列表
	 * 
	 * @param: [accountBookEntityDto]
	 * @return: java.util.List<java.lang.Long>
	 * @author: zhuzs
	 * @date: 2019-12-05 
	 */
	List<AccountBookEntityVo> findIdsByBusinessIdList(@Param("accountBookEntityDto") AccountBookEntityDto accountBookEntityDto);

}
