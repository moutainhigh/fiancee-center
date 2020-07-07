package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.exception.FeignClientErrorMsg;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.mapper.AccountBookPeriodMapper;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.support.Result;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 账簿期间表
 *
 * @author zhuzs
 * @date 2019-07-02 19:17
 */
@Service
public class AccountBookPeriodServiceImpl extends ServiceImpl<AccountBookPeriodMapper, AccountBookPeriod> implements AccountBookPeriodService {
    @Resource
    private AccountBookPeriodMapper accountBookPeriodMapper;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    /**
     * 新增账簿期间
     *
     * @param: [accountBookPeriod]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-10-16
     */
    @Override
    public Integer addAccountBookPeriod(AccountBookPeriod accountBookPeriod) {
        return accountBookPeriodMapper.insert(accountBookPeriod);
    }

    /**
     * 账簿ID和子系统标识 删除账簿期间数据
     *
     * @param: [accountBookPeriod]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-10-21
     */
    @Override
    public Integer deleteByAccountBookIdAndSystenSign(AccountBookPeriod accountBookPeriod) {
        return accountBookPeriodMapper.delete(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, accountBookPeriod.getAccountBookId())
                .eq(AccountBookPeriod::getSystemSign, accountBookPeriod.getSystemSign()));
    }

    /**
     * 根据 ID 、账簿ID、子系统标识、期间年、期间号、制单日期、是否查询最小期间、是否查询最大期间 查询账簿期间
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo
     * @author: zhuzs
     * @date: 2019-10-16
     */
    @Override
    public AccountBookPeriodVo findPeriodByAccBookIdAndSystemSign(AccountBookPeriodDto accountBookPeriodDto) {
        return accountBookPeriodMapper.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);
    }

    /**
     * 根据 账簿DI/账簿IDs、子系统标识 获取账簿期间列表
     *
     * @param: [dto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: zhuzs
     * @date: 2019-10-16
     */
    @Override
    public List<AccountBookPeriodVo> findPeriodRangeByAccBookIdsAndSystemSign(AccountBookPeriodDto accountBookPeriodDto) {
        return accountBookPeriodMapper.findPeriodRangeByAccBookIdsAndSystemSign(accountBookPeriodDto);
    }

    /**
     * 根据账簿id和年度查询期间范围
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo
     * @author: 周鹏
     * @date: 2019-10-16
     */
    @Override
    public List<AccountBookPeriodVo> findPeriodAreaByYear(List<BalanceSubjectVo> balanceSubjectList) {
        return accountBookPeriodMapper.findPeriodAreaByYear(balanceSubjectList);
    }

    /**
     * 根据账簿id查询启用期间
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo
     * @author: 周鹏
     * @date: 2019-10-28
     */
    @Override
    public AccountBookPeriodVo findStartPeriodByAccountBook(AccountBookPeriodDto accountBookPeriodDto){
        return accountBookPeriodMapper.findStartPeriodByAccountBook(accountBookPeriodDto);
    }

    /**
     * @param accountBookPeriodDto
     * @return java.lang.Byte
     * @description: 根据年份查询指定年份最大期间数
     * @Param [accountBookPeriodDto]
     * @author LuoY
     * @date 2019/8/16 11:50
     */
    @Override
    public Byte findMaxPeriodNumByYearAndAccountBookId(AccountBookPeriodDto accountBookPeriodDto) {
        return accountBookPeriodMapper.findMaxPeriodNumByYearAndAccountBookId(accountBookPeriodDto);
    }

    /**
     * @description: 获取待整理账簿期间
     * @param: [accountBookPeriodDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: xdy
     * @create: 2019-08-15 09-29
     */
    @Override
    public Page<AccountBookPeriodVo> findToAdjustPage(AccountBookPeriodDto accountBookPeriodDto) {
        Page<AccountBookPeriodVo> page = accountBookPeriodDto.getPage();
        return accountBookPeriodMapper.findToAdjustPage(page, accountBookPeriodDto);
    }

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @Description 根据条件获取账簿列表
     * @Author 朱小明
     * @Date 2019/8/7 16:42
     * @Param [accountBookPeriod]
     **/
    @Override
    public Page<AccountBookPeriodVo> findPageByCondition(AccountBookPeriodDto accountBookPeriodDto) {
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setMenuCode(accountBookPeriodDto.getMenuCode());
        accountBookDto.setIsEnterpriseAdmin(Constant.Is.NO);
        Page<AccountBookVo> page = new Page<>();
        page.setSize(Long.MAX_VALUE);
        page.setCurrent(1);
        accountBookDto.setPage(page);
        Result<Page<AccountBookVo>> accountBookVoPageResult = accountBookFeignClient.findAccountBookPage(accountBookDto);
        if (accountBookVoPageResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_ACCOUNT_PERIOD), ResultCode.FEIGN_CONNECT_ERROR);
        }
        if (accountBookVoPageResult.getData().getRecords().size() == 0) {
            throw new ServiceException(ResultCode.PERMISSION_NOT);
        }
        List<AccountBookVo> voList = accountBookVoPageResult.getData().getRecords();
        voList.stream().forEach(e -> {
            if (accountBookPeriodDto.getCompanyId().equals(e.getCompanyId())) {
                accountBookPeriodDto.setAccountBookId(e.getId());
                return;
            }
        });
        if (accountBookPeriodDto.getAccountBookIds() == null || accountBookPeriodDto.getAccountBookIds().size() == 0) {
            accountBookPeriodDto.setAccountBookIds(accountBookVoPageResult.getData().getRecords().stream().map(e -> e.getId()).collect(Collectors.toList()));
        }
        //根据条件查询
        Page<AccountBookPeriodVo> pageList = accountBookPeriodDto.getPage();
        @NotNull SysUserVo userVo = UserUtils.getUserVo();
        accountBookPeriodDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        //过账时查询使用
        if (accountBookPeriodDto.getIsSettle() == null) {
            pageList = accountBookPeriodMapper.selectRecordsPageForPostPeriod(accountBookPeriodDto.getPage(), accountBookPeriodDto);
        } else {
            //结账时用
            List<AccountBookPeriodVo> accountBookPeriodVos;
            if (Constant.Is.NO.equals(accountBookPeriodDto.getIsSettle())) {
                accountBookPeriodVos = accountBookPeriodMapper.selectSettleNoPageByCondition(accountBookPeriodDto);
            } else {
                accountBookPeriodVos = accountBookPeriodMapper.selectSettleYesPageByCondition(accountBookPeriodDto);
            }
            if (accountBookPeriodVos.size() > 0) {
                accountBookPeriodDto.setAccountBookPeriodVos(accountBookPeriodVos);
                pageList = accountBookPeriodMapper.selectPeriodPage(accountBookPeriodDto.getPage(), accountBookPeriodDto);
            }
        }
        setAccountBootInfo(pageList);
        return pageList;
    }

    /**
     * 查询可 结账/反结账 的账簿列表
     *
     * @param accountBookPeriodDto accountBookPeriodDto
     * @return page
     * @author xyyxhcj@qq.com
     * @date 2019/10/18 15:53
     **/
    @Override
    public Page<AccountBookPeriodVo> findPageForSettle(AccountBookPeriodDto accountBookPeriodDto) {
        // 获取有权的所有账簿
        Result<Page<AccountBookVo>> accountBookVoPageResult = findHasPermAccBooks(accountBookPeriodDto.getMenuCode(), accountBookPeriodDto.getAccountBookIds());
        // 有权的所有账簿id
        List<Long> hasPermAccBookIds = new LinkedList<>();
        // 排序在第一位的账簿ID
        Long firstAccBookId = null;
        // 同时构建账簿名称 编码 类型字典 用于结果集封装,key为账簿ID
        Map<Long, AccountBookVo> accBookDict = new LinkedHashMap<>();
        for (AccountBookVo accountBookVo : accountBookVoPageResult.getData().getRecords()) {
            Long accountBookId = accountBookVo.getId();
            hasPermAccBookIds.add(accountBookId);
            accBookDict.put(accountBookId, accountBookVo);
            if (accountBookPeriodDto.getCompanyId().equals(accountBookVo.getCompanyId())) {
                firstAccBookId = accountBookId;
            }
        }
        if (hasPermAccBookIds.isEmpty()) {
            // 无权限时返回空
            return accountBookPeriodDto.getPage();
        }
        // 在有权的账簿idList中查出符合 '查询期间条件' 的列表
        List<AccountBookPeriodVo> records = accountBookPeriodMapper.findPageForSettle(accountBookPeriodDto.getPage(), accountBookPeriodDto, hasPermAccBookIds, firstAccBookId);
        // 放入账簿名称 编码 类型
        for (AccountBookPeriodVo record : records) {
            AccountBookVo accountBookVo = accBookDict.get(record.getAccountBookId());
            if (accountBookVo == null) {
                continue;
            }
            record.setAccountBookName(accountBookVo.getName());
            record.setAccountBookCode(accountBookVo.getCode());
            record.setAccountBookType(accountBookVo.getAccountBookTypeName());
        }
        return accountBookPeriodDto.getPage().setRecords(records);
    }

    /**
     * 获取有权的所有账簿
     *
     * @param menuCode menuCode
     * @param accountBookIds 账簿id列表
     * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookVo>>
     * @author xyyxhcj@qq.com
     * @date 2019/10/31 9:47
     **/
    @Override
    public Result<Page<AccountBookVo>> findHasPermAccBooks(String menuCode, List<Long> accountBookIds) {
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setPage(new Page<AccountBookVo>().setSize(Long.MAX_VALUE));
        accountBookDto.setMenuCode(menuCode);
        accountBookDto.setIsEnterpriseAdmin(Constant.Is.NO);
        // 同时提供查询条件中的账簿ids
        accountBookDto.setAccountBookIdList(accountBookIds);
        Result<Page<AccountBookVo>> accountBookVoPageResult = accountBookFeignClient.findAccountBookPage(accountBookDto);
        if (accountBookVoPageResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_ACCOUNT_BOOK), ResultCode.FEIGN_CONNECT_ERROR);
        }
        return accountBookVoPageResult;
    }

    /**
     * @return void
     * @Description 根据账簿ID拼接账簿信息
     * @Author 朱小明
     * @Date 2019/8/20 18:25
     * @Param [pageList]
     **/
    private void setAccountBootInfo(Page<AccountBookPeriodVo> pageList) {
        Set<Long> idSet = new HashSet<>();
        if (pageList.getRecords() != null) {
            //取出列表要使用到的id，放入Set中
            pageList.getRecords().forEach(accountBookPeriodVo -> idSet.add(accountBookPeriodVo.getAccountBookId()));
            AccountBookDto accountBookDto = new AccountBookDto();
            accountBookDto.setIdSet(idSet);
            //调用接口根据Set中的id查询出List
            Result<List<AccountBookVo>> result = accountBookFeignClient.findAccountBookListByIdSet(accountBookDto);
            if (result.getData() != null && result.getData().size() > 0) {
                //将获取的账簿名称、账簿编码、账簿类型拼接到列表中
                MergeUtil.merge(pageList.getRecords(), result.getData(),
                        (abpv, abv) -> abpv.getAccountBookId().equals(abv.getId()),
                        (abpv, abv) -> {
                            abpv.setAccountBookName(abv.getName());
                            abpv.setAccountBookCode(abv.getCode());
                            abpv.setAccountBookType(abv.getAccountBookTypeName());
                        }
                );
            }
        }
    }


}

