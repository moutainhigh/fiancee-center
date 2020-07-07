package com.njwd.financeback.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.AccountBookEntity;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.fileexcel.export.DataGet;
import com.njwd.financeback.mapper.AccountBookEntityMapper;
import com.njwd.financeback.service.AccountBookEntityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

/**
 * 核算主体
 *
 * @Author: Zhuzs
 * @Date: 2019-06-05 13:52
 */
@Service
public class AccountBookEntityServiceImpl implements AccountBookEntityService {

    @Resource
    private AccountBookEntityMapper accountBookEntityMapper;

    /**
     * 新增 核算主体
     *
     * @param: [accountEntity]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:41
     */
    @Override
    public int insert(AccountBookEntity accountEntity) {
        return accountBookEntityMapper.insert(accountEntity);
    }

    /**
     * 查询 核算主体列表 不分页
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:41
     */
    @Override
    public List<AccountBookEntityVo> findAccountBookEntityList(AccountBookDto accountBookDto) {
        return accountBookEntityMapper.findList(accountBookDto);
    }

	/**
	 * @return java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
	 * @Description 查询 核算主体列表 分页
	 * @Author 郑勇浩
	 * @Data 2019/7/31 16:01
	 * @Param [accountBookDto] 账簿ID/公司ID/公司编码或名称
	 */
	@Override
	public Page<AccountBookEntityVo> findAccountBookEntityPage(AccountBookDto accountBookDto) {
		Page<AccountBookEntityVo> result = accountBookEntityMapper.findPage(accountBookDto.getPage(), accountBookDto);
		if (result == null || CollectionUtils.isEmpty(result.getRecords())) {
			return result;
		}
		for (AccountBookEntityVo accountBookEntityVo : result.getRecords()) {
			accountBookEntityVo.setName(accountBookEntityVo.getEntityName());
		}
		return result;
	}

    /**
     * 查询核算主体列表 分页 admin端
     *
     * @param: [accountBookEntityDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:41
     */
    @Override
    public Page<AccountBookEntityVo> findAccountBookEntityPageByAccBookIdList(AccountBookEntityDto accountBookEntityDto) {
        Page<AccountBookEntityVo> page = accountBookEntityDto.getPage();
        page = accountBookEntityMapper.findAccountBookEntityPageByAccBookIdList(page, accountBookEntityDto);
        List<AccountBookEntityVo> accountBookEntityVos = page.getRecords();
        filterEmpty(accountBookEntityVos);
        return page;
    }

    /**
     * 查询核算主体列表 不分页 admin端
     *
     * @param: [accountBookEntityDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:41
     */
    @Override
    public List<AccountBookEntityVo> findAccountBookEntityListByAccBookIdList(AccountBookEntityDto accountBookEntityDto) {
        Page<AccountBookEntityVo> page = accountBookEntityDto.getPage();
        page.setSize(DataGet.MAX_PAGE_SIZE);
        page = findAccountBookEntityPageByAccBookIdList(accountBookEntityDto);
        return page.getRecords();
    }

    /**
     * 查询 用户有操作权限的核算主体列表 分页(不传参数返回空)
     *
     * @param: [accountBookEntityDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:41
     */
    @Override
    public Page<AccountBookEntityVo> findAuthOperationalEntityPage(AccountBookEntityDto accountBookEntityDto) {
        Page<AccountBookEntityVo> page = accountBookEntityDto.getPage();

        if (accountBookEntityDto.getAccountBookIdList() != null && accountBookEntityDto.getAccountBookIdList().size() != 0) {
            List<AccountBookEntityVo> accountBookEntityVos = accountBookEntityMapper.findAuthOperationalEntityPage(page, accountBookEntityDto).getRecords();
            filterEmpty(accountBookEntityVos);
            return page.setRecords(accountBookEntityVos);
        }
        return page;
    }

    /**
     * 查询 用户有操作权限的核算主体列表 (不传参数返回空)
     *
     * @param: [accountBookEntityDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:41
     */
    @Override
    public Page<AccountBookEntityVo> findAuthOperationalEntityList(AccountBookEntityDto accountBookEntityDto) {
        Page<AccountBookEntityVo> page = accountBookEntityDto.getPage();
        page.setSize(DataGet.MAX_PAGE_SIZE);

        if (accountBookEntityDto.getAccountBookIdList() != null && accountBookEntityDto.getAccountBookIdList().size() != 0) {
            List<AccountBookEntityVo> accountBookEntityVos = accountBookEntityMapper.findAuthOperationalEntityPage(page, accountBookEntityDto).getRecords();
            filterEmpty(accountBookEntityVos);
            return page.setRecords(accountBookEntityVos);
        }
        return page;
    }

    /**
     * 查询 用户有操作权限的核算主体
     *
     * @param: [accountBookEntityDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookEntityVo
     * @author: zhuzs
     * @date: 2019-09-16 17:41
     */
    @Override
    public AccountBookEntityVo findAuthOperationalEntity(AccountBookEntityDto accountBookEntityDto) {
        accountBookEntityDto.setIsDefault(Constant.Is.YES);
        AccountBookEntityVo accountBookEntityVo = accountBookEntityMapper.findAuthOperationalEntity(accountBookEntityDto);
        if(accountBookEntityVo == null){
            accountBookEntityDto.setIsDefault(null);
            accountBookEntityVo = accountBookEntityMapper.findAuthOperationalEntity(accountBookEntityDto);
        }
        return accountBookEntityVo;
    }

    /**
     * @description:
     * @param: [accountBookEntityDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookEntityVo
     * @author: xdy
     * @create: 2019-10-17 09:25
     */
    @Override
    public AccountBookEntityVo findAccountBookEntityById(AccountBookEntityDto accountBookEntityDto) {
        return accountBookEntityMapper.findAccountBookEntityById(accountBookEntityDto);
    }

    /**
     * 过滤空值
     *
     * @param: [accountBookEntityVos]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:40
     */
    private void filterEmpty(List<AccountBookEntityVo> accountBookEntityVos){
        for (Iterator<AccountBookEntityVo> entityIterator = accountBookEntityVos.iterator(); entityIterator.hasNext(); ) {
            AccountBookEntityVo entity = entityIterator.next();
            if(null == entity.getEntityName() || null == entity.getCode()){
                entityIterator.remove();
            }
        }
    }
}
