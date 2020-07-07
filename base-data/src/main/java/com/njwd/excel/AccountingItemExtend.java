package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.mapper.AccountingItemMapper;
import com.njwd.basedata.service.AccountingItemService;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.AccountingItem;
import com.njwd.entity.basedata.dto.AccountingItemDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description 自定义核算项目导入
 * @Date 2019/7/2 17:06
 * @Author 薛永利
 */
@Component
@ExcelExtend(type = "accounting_item")
public class AccountingItemExtend implements AddExtend<AccountingItemDto>, CheckExtend {

    @Resource
    private AccountingItemService accountingItemService;
    @Resource
    private SequenceService sequenceService;
    @Resource
    private AccountingItemMapper accountingItemMapper;
    /**
     * @Description 自定义核算项目批量导入
     * @Author 薛永利
     * @Date 2019/7/3 11:10
     * @Param [datas]
     * @return int
     */
    @Override
    public int addBatch(List<AccountingItemDto> datas) {
        List<AccountingItem> accountingItemList = new ArrayList<>();
        for (AccountingItemDto accountingItemDto : datas) {
            AccountingItem accountingItem = new AccountingItem();
            //设置默认值
            setOperatorInfo(accountingItemDto);
            FastUtils.copyProperties(accountingItemDto, accountingItem);
            //设置完整流水号 Z+两位流水号
            accountingItem.setCode( sequenceService.getCode(
                    Constant.BaseCodeRule.ACC_ITEM,
                    Constant.BaseCodeRule.LENGTH_TWO,
                    accountingItem.getRootEnterpriseId(),
                    Constant.BaseCodeRule.ENTERPRISE
            ));
            //excel导入,codeType默认为0,系统自定义
            accountingItem.setCodeType(Constant.CodeType.SYSTEMCODE);
            accountingItemList.add(accountingItem);
        }
        //批量新增项目
        int result = accountingItemService.addBatchAccountingItem(accountingItemList);
        return result;
    }

    /**
     * @Description 自定义核算项目大区值单行导入
     * @Author 薛永利
     * @Date 2019/7/3 11:10
     * @Param [data]
     * @return int
     */
    @Override
    public int add(AccountingItemDto data) {
        AccountingItem accountingItem = new AccountingItem();
        //设置默认值
        setOperatorInfo(data);
        FastUtils.copyProperties(data, accountingItem);
        //设置完整流水号 Z+两位流水号
        accountingItem.setCode( sequenceService.getCode(
                Constant.BaseCodeRule.ACC_ITEM,
                Constant.BaseCodeRule.LENGTH_TWO,
                accountingItem.getRootEnterpriseId(),
                Constant.BaseCodeRule.ENTERPRISE
        ));
        //excel导入,codeType默认为0,系统自定义
        accountingItem.setCodeType(Constant.CodeType.SYSTEMCODE);
        return accountingItemMapper.insert(accountingItem);
    }

    @Override
    public void check(CheckContext checkContext) {
        checkContext.addSheetHandler("name", getNameCheckHandler());
    }
    /**
     * 校验租户内值名称是否重复
     * @return
     */
    private CheckHandler<AccountingItemDto> getNameCheckHandler() {
        return data -> {
            Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
            data.setRootEnterpriseId(rootEnterpriseId);
            int row = accountingItemService.findAccountingItemByName(data);
            if(row >0) {
                return CheckResult.error(ResultCode.NAME_EXIST.message);
            }
            //校验成功
            return CheckResult.ok();
        };
    }
    private void setOperatorInfo(AccountingItemDto accountingItemDto)
    {
        SysUserVo userVo = UserUtils.getUserVo();
        accountingItemDto.setCreatorId(userVo.getUserId());
        accountingItemDto.setCreatorName(userVo.getName());
        accountingItemDto.setCreateTime(new Date());
        accountingItemDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        accountingItemDto.setDataType((byte)1);
    }
}
