package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.AccountBookCategory;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.vo.*;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.AccountBookCategoryMapper;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.service.AccountBookCategoryService;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author liuxiang
 * @Description 账簿分类
 * @Date:15:41 2019/7/2
 **/
@Service
public class AccountBookCategoryServiceImpl implements AccountBookCategoryService {

    @Autowired
    private AccountBookCategoryMapper accountBookCategoryMapper;
    @Resource
    private FileService fileService;
    @Resource
    private SequenceService sequenceService;
    @Resource
    private BasePlatformMapper basePlatformMapper;
    @Resource
    private MessageService messageService;

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.AccountBookCategoryVo>
     * @Description 根据账簿类型ID和会计准则ID查询账簿分类
     * @Author liuxiang
     * @Date:15:40 2019/7/2
     * @Param [accountBookCategoryDto]
     **/
    @Override
    @Cacheable(value = "accBoListByAccTypeAndStand", key = "#accountBookCategoryDto.accountTypeId+'-'+#accountBookCategoryDto.accStandardId")
    public List<AccountBookCategoryVo> findAccBoListByAccTypeAndStand(AccountBookCategoryDto accountBookCategoryDto) {
        return accountBookCategoryMapper.findAccBoListByAccTypeAndStand(accountBookCategoryDto);
    }

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.AccountBookCatVo>
     * @Description 根据账簿类型ID和租户ID查询账簿分类
     * @Author lj
     * @Date:9:14 2019/7/12
     * @Param [accountBookCategoryDto]
     **/
    @Override
    @Cacheable(value = "accBookListByTypeAndEntId", key = "#accountBookCategoryDto.accountTypeId+'-'+#accountBookCategoryDto.rootEnterpriseId")
    public List<AccountBookCatVo> findAccBookListByTypeAndEntId(AccountBookCategoryDto accountBookCategoryDto) {
        // 根据账簿类型ID和租户ID查询账簿分类 ,账簿分类List
        List<AccountBookCategoryVo> accountBookCategoryList = accountBookCategoryMapper.findAccBookListByTypeAndEntId(accountBookCategoryDto);
        // 封装接口返回List
        List<AccountBookCatVo> accountBookCatList = new ArrayList<AccountBookCatVo>();
        // 账簿分类List非空情况下
        if (accountBookCategoryList != null && !accountBookCategoryList.isEmpty()) {
            for (AccountBookCategoryVo accountBookCategoryVo : accountBookCategoryList) {
                AccountBookCatVo accountBookCatVo = new AccountBookCatVo();
                accountBookCatVo.setAccStandardId(accountBookCategoryVo.getAccStandardId());
                accountBookCatVo.setAccStandardName(accountBookCategoryVo.getAccStandardName());
                // 封装币种List
                List<TCurrencyVo> currencys = new ArrayList<TCurrencyVo>();
                // 封装税制List
                List<TTaxSystemVo> taxSystems = new ArrayList<TTaxSystemVo>();
                // 将币种ID按逗号分隔，转换为集合
                List<String> currencyIds = Arrays.asList(accountBookCategoryVo.getCurrencyIds().split(","));
                // 将币种名称按逗号分隔，转换为集合
                List<String> currencyNames = Arrays.asList(accountBookCategoryVo.getCurrencyNames().split(","));
                for (int i = 0; i < currencyIds.size(); i++) {
                    TCurrencyVo tCurrencyVo = new TCurrencyVo();
                    tCurrencyVo.setCurrencyId(currencyIds.get(i));
                    tCurrencyVo.setCurrencyName(currencyNames.get(i));
                    // 将币种ID和名称添加到币种List中
                    currencys.add(tCurrencyVo);
                }
                // 将税制ID按逗号分隔，转换为集合
                List<String> taxSystemIds = Arrays.asList(accountBookCategoryVo.getTaxSystemIds().split(","));
                // 将税制名称按逗号分隔，转换为集合
                List<String> taxSystemNames = Arrays.asList(accountBookCategoryVo.getTaxSystemNames().split(","));
                for (int i = 0; i < taxSystemIds.size(); i++) {
                    TTaxSystemVo tTaxSystemVo = new TTaxSystemVo();
                    tTaxSystemVo.setTaxSystemId(taxSystemIds.get(i));
                    tTaxSystemVo.setTaxSystemName(taxSystemNames.get(i));
                    // 将税制ID和名称添加到税制List中
                    taxSystems.add(tTaxSystemVo);
                }
                // 封装币种List到账簿分类中去
                accountBookCatVo.setCurrencys(currencys);
                // 封装税制List到账簿分类中去
                accountBookCatVo.setTaxSystems(taxSystems);
                // 封装账簿分类到账簿集合中去
                accountBookCatList.add(accountBookCatVo);
            }
        }
        return accountBookCatList;
    }

    /** 刘遵通
     * 增加核算账簿分类
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public Long addAccountBookCategory(AccountBookCategoryDto accountBookCategoryDto) {
        //先根据名称查数据,有数据 就代表名称存在
        Integer count = selectByName(accountBookCategoryDto);
        if(count > 0){
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
        SysUserVo userVo = UserUtil.getUserVo();
        accountBookCategoryDto.setCreatorId(userVo.getUserId());
        accountBookCategoryDto.setCreatorName(userVo.getName());
        accountBookCategoryDto.setCode(sequenceService.getCode(PlatformConstant.PlatformCodeRule.ACCOUNTING_BOOK_TYPE,3,accountBookCategoryDto.getCode()));
        AccountBookCategory accountBookCategory = new AccountBookCategory();
        FastUtils.copyProperties(accountBookCategoryDto,accountBookCategory);
         accountBookCategoryMapper.insert(accountBookCategory);
        return accountBookCategory.getId();
    }
    /** 刘遵通
     * 删除核算账簿分类
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public BatchResult deleteAccountBookCategory(AccountBookCategoryDto accountBookCategoryDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountBookCategoryVo> accountBookCategorys = new ArrayList<AccountBookCategoryVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountBookCategoryVo> AccountBookCategoryList = accountBookCategoryMapper.findAccBookListByIds(accountBookCategoryDto);
        for(AccountBookCategoryVo accountBookCategory : AccountBookCategoryList){
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
            Map<Long, AccountBookCategoryDto> map = editList.stream().collect(Collectors.toMap(AccountBookCategoryDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountBookCategory.getId()).getVersion();
            if(!accountBookCategory.getVersion().equals(version)){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            else if(accountBookCategory.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("该数据已审核,无法删除,请先反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountBookCategorys.add(accountBookCategory);
            rd.setBusinessId(accountBookCategory.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DELETE, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNT_BOOK_CATEGORY);
        return result;
    }
    /**刘遵通
     * 审核
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public BatchResult checkApprove(AccountBookCategoryDto accountBookCategoryDto) {
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountBookCategoryVo> accountBookCategorys = new ArrayList<AccountBookCategoryVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountBookCategoryVo> AccountBookCategoryList = accountBookCategoryMapper.findAccBookListByIds(accountBookCategoryDto);
        for(AccountBookCategoryVo accountBookCategory : AccountBookCategoryList){
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
            Map<Long, AccountBookCategoryDto> map = editList.stream().collect(Collectors.toMap(AccountBookCategoryDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountBookCategory.getId()).getVersion();
            if(!accountBookCategory.getVersion().equals(version)){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            else if(accountBookCategory.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("审核失败！数据已审核，无需重复审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountBookCategorys.add(accountBookCategory);
            rd.setBusinessId(accountBookCategory.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.APPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNT_BOOK_CATEGORY);
        return result;
    }
    /**刘遵通
     * 反审核
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public BatchResult reversalApprove(AccountBookCategoryDto accountBookCategoryDto) {
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountBookCategoryVo> accountBookCategorys = new ArrayList<AccountBookCategoryVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountBookCategoryVo> AccountBookCategoryList = accountBookCategoryMapper.findAccBookListByIds(accountBookCategoryDto);
        for(AccountBookCategoryVo accountBookCategory : AccountBookCategoryList){
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
            Map<Long, AccountBookCategoryDto> map = editList.stream().collect(Collectors.toMap(AccountBookCategoryDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountBookCategory.getId()).getVersion();
            if(!accountBookCategory.getVersion().equals(version)){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            //1.检查当前已选数据的发布状态：0未发布 ，1已发布
           else if(accountBookCategory.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("反审核失败!数据已发布,无法反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //2.检查当前已选数据的审核状态：0未审核，1已审核
            else if(accountBookCategory.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("反审核失败!数据未审核,无需反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountBookCategorys.add(accountBookCategory);
            rd.setBusinessId(accountBookCategory.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DISAPPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNT_BOOK_CATEGORY);
        return result;
    }
    /** 刘遵通
     * 查询页面 （分页）
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public Page<AccountBookCategoryVo> findPage(AccountBookCategoryDto accountBookCategoryDto) {
        return accountBookCategoryMapper.findPage(accountBookCategoryDto.getPage(),accountBookCategoryDto);
    }
    /**
     * 编辑中的查看
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public AccountBookCategory selectById(AccountBookCategoryDto accountBookCategoryDto) {
        AccountBookCategory accountBookCategory = accountBookCategoryMapper.selectById(accountBookCategoryDto.getId());
        return accountBookCategory;
    }
    /**
     * 修改
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public Long updateById(AccountBookCategoryDto accountBookCategoryDto) {
        Integer count = selectByName(accountBookCategoryDto);
        if(count > 0){
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
        SysUserVo userVo = UserUtil.getUserVo();
        accountBookCategoryDto.setUpdatorId(userVo.getUserId());
        accountBookCategoryDto.setUpdatorName(userVo.getName());
        AccountBookCategory accountBookCategory = new AccountBookCategory();
        FastUtils.copyProperties(accountBookCategoryDto,accountBookCategory);
        accountBookCategoryMapper.updateById(accountBookCategory);
        return accountBookCategory.getId();
    }


    @Override
    public List<AccountBookCategoryVo> findAccountBookCategoryList(AccountBookCategoryDto accountBookCategoryDto) {
        return accountBookCategoryMapper.findAccountBookCategoryList(accountBookCategoryDto);
    }
    /**
     * 导出
     * @param accountBookCategoryDto
     * @param response
     */
    @Override
    public void exportExcel(AccountBookCategoryDto accountBookCategoryDto, HttpServletResponse response) {
        Page<AccountBookCategoryVo> page = accountBookCategoryDto.getPage();
        fileService.resetPage(page);
        Page<AccountBookCategoryVo> AccountBookCategoryList = accountBookCategoryMapper.findPage(page, accountBookCategoryDto);
        fileService.exportExcel(response,AccountBookCategoryList.getRecords(),
                new ExcelColumn("code","编码"),
                new ExcelColumn("accStandardName","会计准则"),
                new ExcelColumn("typeCode","期间类型"),
                new ExcelColumn("calendarName","会计日历"),
                new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED),
                new ExcelColumn("isReleased","发布状态",ExcelDataConstant.SYSTEM_DATA_IS_RELEASED)
                );
    }
    /**
     * 发布
     * @param accountBookCategoryDto
     * @return
     */
    @Override
    public BatchResult release(AccountBookCategoryDto accountBookCategoryDto) {
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountBookCategoryVo> accountBookCategorys = new ArrayList<AccountBookCategoryVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountBookCategoryVo> AccountBookCategoryList = accountBookCategoryMapper.findAccBookListByIds(accountBookCategoryDto);
        for(AccountBookCategoryVo accountBookCategory : AccountBookCategoryList){
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountBookCategoryDto> editList = accountBookCategoryDto.getEditList();
            Map<Long, AccountBookCategoryDto> map = editList.stream().collect(Collectors.toMap(AccountBookCategoryDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountBookCategory.getId()).getVersion();
            if(!accountBookCategory.getVersion().equals(version)){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            //1.检查当前已选数据的审核状态：0未审核，1已审核
            else if(accountBookCategory.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("发布失败！只能发布已审核的数据!");
                result.getFailList().add(rd);
                continue;
            }
            //2.检查当前已选数据的发布状态：0未发布 ，1已发布
            else if(accountBookCategory.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(accountBookCategory.getId());
                rd.setReferenceDescription("发布失败！数据已发布,无需重复发布!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountBookCategorys.add(accountBookCategory);
            rd.setBusinessId(accountBookCategory.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        //发送消息
        messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE,accountBookCategoryDto.getMessageDto());
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNT_BOOK_CATEGORY);
        return result;
    }
    public Integer selectByName(AccountBookCategoryDto accountBookCategoryDto) {
        LambdaQueryWrapper<AccountBookCategory> queryWrapper = Wrappers.<AccountBookCategory>lambdaQuery().eq(AccountBookCategory::getAccStandardId,accountBookCategoryDto.getAccStandardId());
        if(accountBookCategoryDto.getId()!=null)
            queryWrapper.ne(AccountBookCategory::getId,accountBookCategoryDto.getId());
        Integer count = accountBookCategoryMapper.selectCount(queryWrapper);
        return count;
    }
}
