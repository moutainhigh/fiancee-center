package com.njwd.financeback.service.impl;

import com.njwd.basedata.cloudclient.AccountStandardFeignClient;
import com.njwd.entity.basedata.AccountStandard;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.vo.AccountBookCatVo;
import com.njwd.financeback.service.AccountStandardService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * 根据 账簿类型ID 和 租户ID 获取会计准则（含税制、记账本位币信息）
 *
 * @author zhuzs
 * @date 2019-07-08 10:31
 */
@Service
public class AccountStandardServiceImpl implements AccountStandardService {
    @Autowired
    private AccountStandardFeignClient accountStandardFeignClient;

    /**
     * 获取会计准则
     */
    @Override
    public List<AccountStandard> accountStandardList(AccountBookCategoryDto platformAccountBookCategoryDto){
        Result<List<AccountBookCatVo>> result = accountStandardFeignClient.findAccBookListByTypeAndEntId(platformAccountBookCategoryDto);
        List<AccountStandard> list = new ArrayList<>();
        if(result.getData() != null && result.getData().size() != 0){
            for(AccountBookCatVo accountBookCatVo:result.getData()){
                AccountStandard accountStandard = new AccountStandard();
                FastUtils.copyProperties(accountBookCatVo,accountStandard);
                list.add(accountStandard);
            }
        }
        return list;
    }

}

