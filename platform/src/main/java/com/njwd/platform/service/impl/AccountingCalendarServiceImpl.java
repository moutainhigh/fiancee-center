package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;

import com.njwd.entity.platform.AccountingCalendar;
import com.njwd.entity.platform.AccountingPeriod;
import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.AccountingCalendarMapper;
import com.njwd.platform.mapper.AccountingPeriodMapper;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.service.AccountingCalendarService;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.DateUtils;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author lj
 * @Description 会计日历服务
 * @Date:14:33 2019/6/26
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountingCalendarServiceImpl implements AccountingCalendarService{

    @Autowired
    private AccountingCalendarMapper accountingCalendarMapper;
    @Resource
    private SequenceService sequenceService;
    @Autowired
    private AccountingPeriodMapper accountingPeriodMapper;
    @Resource
    private FileService fileService;
    @Resource
    private BasePlatformMapper basePlatformMapper;
    @Resource
    private ReferenceRelationService referenceRelationService;
    @Resource
    private MessageService messageService;

    /**
     * @Description 根据会计准则id和账簿类型id查询会计日历列表
     * @Author lj
     * @Date:14:30 2019/6/26
     * @Param [accountingCalendarDto]
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingCalendarVo>
     **/
    @Override
    @Cacheable(value = "accCaListByAccTypeAndStand", key = "#accountingCalendarDto.accStandardId+'-'+#accountingCalendarDto.accountTypeId")
    public List<AccountingCalendarVo> findAccCaListByAccTypeAndStand(AccountingCalendarDto accountingCalendarDto) {
        return accountingCalendarMapper.findAccCaListByAccTypeAndStand(accountingCalendarDto);
    }
    /**
     * 刘遵通
     * 增加会计日历
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public Long addAccountingCalendar(AccountingCalendarDto accountingCalendarDto) {
        //先根据名称查数据,有数据 就代表名称存在
        Integer count = selectByName(accountingCalendarDto);
        if(count > 0){
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
        SysUserVo userVo = UserUtil.getUserVo();
        accountingCalendarDto.setCreatorId(userVo.getUserId());
        accountingCalendarDto.setCreatorName(userVo.getName());
        accountingCalendarDto.setCode(sequenceService.getCode(PlatformConstant.PlatformCodeRule.ACCOUNTING_CALENDAR,2));
        AccountingCalendar accountingCalendar = new AccountingCalendar();
        FastUtils.copyProperties(accountingCalendarDto,accountingCalendar);
        //插入会计日历表
        accountingCalendarMapper.insert(accountingCalendar);
        List<AccountingPeriod> accountingPeriodList = accountingCalendarDto.getAccountingPeriodList();
        if(null != accountingPeriodList && accountingPeriodList.size() > 0){
            for(AccountingPeriod accountingPeriod : accountingPeriodList){
                accountingPeriod.setAccCalendarId(accountingCalendar.getId());
                accountingPeriod.setCreatorId(userVo.getUserId());
                accountingPeriod.setCreatorName(userVo.getName());
                //插入会计期间表
                accountingPeriodMapper.insert(accountingPeriod);
            }
        }
        return accountingCalendar.getId();
    }
    /** 刘遵通
     * 查询页面 （分页）
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public Page<AccountingCalendarVo> findPage(AccountingCalendarDto accountingCalendarDto) {
        return accountingCalendarMapper.findPage(accountingCalendarDto.getPage(),accountingCalendarDto);
    }

    /**
     * 刘遵通
     * 生成会计期间
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public List<AccountingPeriod> generateAccountingPeriod(AccountingCalendarDto accountingCalendarDto){
        //会计年度
        String startYear = accountingCalendarDto.getStartYear();
        Integer year = Integer.valueOf(startYear);
        //初始化会计期间的年度数据
        List<AccountingPeriod>  accountingPeriodList = new ArrayList<>();
        //初始化会计期间的期间数据
        List<AccountingPeriod>  accList = new ArrayList<>();
        AccountingPeriod accountingPeriod = null;
        Integer periodYear = 0 ;
        int j = 0;
        //用于追加 功能
        Integer appendYear = accountingCalendarDto.getAppendYear();
        if(appendYear!=null && appendYear != 0){
            for(int i = 0 ; i < appendYear ; i++){
                accountingPeriod = new AccountingPeriod();
                if( j == 0 ){
                    periodYear = year + 1;
                }else {
                    periodYear = periodYear + 1;
                }
                j++;
                accountingPeriod.setPeriodYear(periodYear);
                accountingPeriodList.add(accountingPeriod);
            }
        }else {
            //年数据
            for(int i = 0 ; i < 10 ; i++){
                accountingPeriod = new AccountingPeriod();
                if( j == 0 ){
                    periodYear = year;
                }else {
                    periodYear = periodYear + 1;
                }
                j++;
                accountingPeriod.setPeriodYear(periodYear);
                accountingPeriodList.add(accountingPeriod);
            }
        }

        if(null != accountingPeriodList && accountingPeriodList.size() > 0){
            //调整期个数
            Integer adjustNum = accountingCalendarDto.getAdjustNum();
            //期间数据
            for(AccountingPeriod tempaccountingPeriod : accountingPeriodList){
                  AccountingPeriod acc = null;
                  if(null != adjustNum){
                      //含有调整期
                      int a = adjustNum + 12;
                      for (int i = 1; i <= a ; i++){
                          acc = new AccountingPeriod();
                          acc.setPeriodYear(tempaccountingPeriod.getPeriodYear());
                          acc.setPeriodNum((byte)(i));
                          if( i > 12){
                              Byte adjustment = 1;
                              acc.setIsAdjustment(adjustment);
                          }
                          accList.add(acc);
                      }
                  }else {
                      //不包含调整期
                         for (int i = 1; i < 13 ; i++){
                          acc = new AccountingPeriod();
                          acc.setPeriodYear(tempaccountingPeriod.getPeriodYear());
                          acc.setPeriodNum((byte)(i));
                          accList.add(acc);
                      }
                  }

              }
        }
        if(accList != null && accList.size() >0){
            AccountingPeriod accountingPeriod1 = null;
            Date monthLastDay = null;
            Date endDate = null;
            //遍历期间数据
           for(int i = 0; i < accList.size() ; i++){
               accountingPeriod1 = accList.get(i);
               //	第一个会计年度的第一个期间号的开始日期等于会计日历的开始日期
               if(i == 0){
                   String startDate = accountingCalendarDto.getStartDate();
                   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                   Date parse = null;
                   try {
                       parse = sdf.parse(startDate);
                   } catch (ParseException e) {
                       e.printStackTrace();
                   }
                   //设置开始日期
                   accountingPeriod1.setStartDate(parse);
                   //获取当前月的最后一天 2019-11-30 23:59:59  插入数据库中会变成2019-12-01 这样是有问题的
                   //想要2019-11-30
                   monthLastDay = DateUtils.endOfMonth(parse);
                   //把2019-11-30 23:59:59  变成把2019-11-30 00:00:00 插入数据库中就是2019-11-30
                   monthLastDay = DateUtils.getStartOfDay(monthLastDay);
                   //设置结束日期
                   accountingPeriod1.setEndDate(monthLastDay);
               }else{
                   //期间号 小于12期的 都不是调整期  都为自然月
                   if(accountingPeriod1.getPeriodNum() <= 12){
                       //自然月的最后一天+1 就是下月的1号
                       Date date = DateUtils.addDays(monthLastDay, 1);
                       accountingPeriod1.setStartDate(date);
                       //获取当前月的最后一天 2019-11-30 23:59:59  插入数据库中会变成2019-12-01 这样是有问题的
                       //想要2019-11-30
                       monthLastDay = DateUtils.endOfMonth(date);
                       //把2019-11-30 23:59:59  变成把2019-11-30 00:00:00 插入数据库中就是2019-11-30
                       monthLastDay = DateUtils.getStartOfDay(monthLastDay);
                       //设置结束日期
                       accountingPeriod1.setEndDate(monthLastDay);
                       endDate = accountingPeriod1.getEndDate();
                   }else {
                       //期间号 大于12期的  开始时间和结束时间  都为 非调整期的最后一期的结束时间
                       accountingPeriod1.setStartDate(endDate);
                       accountingPeriod1.setEndDate(endDate);
                   }
               }
           }
        }
        return accList;
    }
    /** 刘遵通
     * 删除会计日历
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public BatchResult deleteAccountingCalendar(AccountingCalendarDto accountingCalendarDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountingCalendarVo> accountingCalendarVos = new ArrayList<AccountingCalendarVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountingCalendarVo> accountingCalendarList = accountingCalendarMapper.findAccountingCalendarByIds(accountingCalendarDto);
        for(AccountingCalendarVo accountingCalendar : accountingCalendarList){
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
            Map<Long, AccountingCalendarDto> map = editList.stream().collect(Collectors.toMap(AccountingCalendarDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountingCalendar.getId()).getVersion();
            if(!accountingCalendar.getVersion().equals(version)){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            else if(accountingCalendar.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("删除失败,无法删除已审核的数据!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountingCalendarVos.add(accountingCalendar);
            rd.setBusinessId(accountingCalendar.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_ACCOUNTING_CALENDAR, result.getSuccessList());
        result.getFailList().addAll(referenceContext.getReferences());
        result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DELETE, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNTING_CALENDAR);
        return result;
    }
    /** 刘遵通
     * 审核
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public BatchResult checkApprove(AccountingCalendarDto accountingCalendarDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountingCalendarVo> accountingCalendarVos = new ArrayList<AccountingCalendarVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountingCalendarVo> accountingCalendarList = accountingCalendarMapper.findAccountingCalendarByIds(accountingCalendarDto);
        for(AccountingCalendarVo accountingCalendar : accountingCalendarList){
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
            Map<Long, AccountingCalendarDto> map = editList.stream().collect(Collectors.toMap(AccountingCalendarDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountingCalendar.getId()).getVersion();
            if(!accountingCalendar.getVersion().equals(version)){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            else if(accountingCalendar.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("审核失败！数据已审核，无需重复审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountingCalendarVos.add(accountingCalendar);
            rd.setBusinessId(accountingCalendar.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.APPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNTING_CALENDAR);
        return result;
    }
    /**刘遵通
     * 反审核
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public BatchResult reversalApprove(AccountingCalendarDto accountingCalendarDto) {
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountingCalendarVo> accountingCalendarVos = new ArrayList<AccountingCalendarVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountingCalendarVo> accountingCalendarList = accountingCalendarMapper.findAccountingCalendarByIds(accountingCalendarDto);
        for(AccountingCalendarVo accountingCalendar : accountingCalendarList){
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
            Map<Long, AccountingCalendarDto> map = editList.stream().collect(Collectors.toMap(AccountingCalendarDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountingCalendar.getId()).getVersion();
            if(!accountingCalendar.getVersion().equals(version)){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            //1.检查当前已选数据的发布状态：0未发布 ，1已发布
            else if(accountingCalendar.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("反审核失败!数据已发布,无法反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //2.检查当前已选数据的审核状态：0未审核，1已审核
            else if(accountingCalendar.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("反审核失败!数据未审核,无需反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountingCalendarVos.add(accountingCalendar);
            rd.setBusinessId(accountingCalendar.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_ACCOUNTING_CALENDAR, result.getSuccessList());
        result.getFailList().addAll(referenceContext.getReferences());
        result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DISAPPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNTING_CALENDAR);
        return result;
    }
    /**刘遵通
     * 发布
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public BatchResult release(AccountingCalendarDto accountingCalendarDto) {
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        List<AccountingCalendarVo> accountingCalendarVos = new ArrayList<AccountingCalendarVo>();
        //根据id查询出所有的数据 进行校验
        List<AccountingCalendarVo> accountingCalendarList = accountingCalendarMapper.findAccountingCalendarByIds(accountingCalendarDto);
        for(AccountingCalendarVo accountingCalendar : accountingCalendarList) {
            ReferenceDescription rd = new ReferenceDescription();
            //把list对象转换成map
            List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
            Map<Long, AccountingCalendarDto> map = editList.stream().collect(Collectors.toMap(AccountingCalendarDto::getId, a -> a, (k1, k2) -> k1));
            //获取页面上传过来的版本号
            Integer version = map.get(accountingCalendar.getId()).getVersion();
            if(!accountingCalendar.getVersion().equals(version)){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("其他用户正在操作,请稍候再使用!");
                result.getFailList().add(rd);
                continue;
            }
            //1.检查当前已选数据的审核状态：0未审核，1已审核
            else if(accountingCalendar.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("发布失败！只能发布已审核的数据!");
                result.getFailList().add(rd);
                continue;
            }
            //2.检查当前已选数据的发布状态：0未发布 ，1已发布
            else if(accountingCalendar.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(accountingCalendar.getId());
                rd.setReferenceDescription("发布失败！数据已发布,无需重复发布!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            accountingCalendarVos.add(accountingCalendar);
            rd.setBusinessId(accountingCalendar.getId());
            result.getSuccessList().add(rd.getBusinessId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE,accountingCalendarDto.getMessageDto());
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), PlatformConstant.TableName.ACCOUNTING_CALENDAR);
        return result;
    }
    /**
     * 编辑中的查看
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public AccountingCalendarVo selectById(AccountingCalendarDto accountingCalendarDto) {
        AccountingCalendar accountingCalendar = accountingCalendarMapper.selectById(accountingCalendarDto.getId());
        List<AccountingPeriod> accountingPeriodList = accountingPeriodMapper.selectList(new LambdaQueryWrapper<AccountingPeriod>()
                .eq(AccountingPeriod::getAccCalendarId, accountingCalendar.getId()));
        AccountingCalendarVo accountingCalendarVo = new AccountingCalendarVo();
        FastUtils.copyProperties(accountingCalendar,accountingCalendarVo);
        accountingCalendarVo.setAccountingPeriodList(accountingPeriodList);
        return accountingCalendarVo;
    }
    /**
     * 根据名称查出数量
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public Integer selectByName(AccountingCalendarDto accountingCalendarDto) {
        LambdaQueryWrapper<AccountingCalendar> queryWrapper = Wrappers.<AccountingCalendar>lambdaQuery().eq(AccountingCalendar::getName,accountingCalendarDto.getName());
        if(accountingCalendarDto.getId()!=null)
            queryWrapper.ne(AccountingCalendar::getId,accountingCalendarDto.getId());
        Integer count = accountingCalendarMapper.selectCount(queryWrapper);
        return count;
    }

    /**
     * 修改
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public Long updateById(AccountingCalendarDto accountingCalendarDto) {
        Integer count = selectByName(accountingCalendarDto);
        if(count > 0){
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
        SysUserVo operator = UserUtil.getUserVo();
        accountingCalendarDto.setUpdatorId(operator.getUserId());
        accountingCalendarDto.setUpdatorName(operator.getName());
        AccountingCalendar accountingCalendar = new AccountingCalendar();
        FastUtils.copyProperties(accountingCalendarDto,accountingCalendar);
        //修改会计日历表
        accountingCalendarMapper.updateById(accountingCalendar);
        //先把原来的会计期间表数据查出来
        AccountingCalendar tempAccountingCalendar = accountingCalendarMapper.selectById(accountingCalendarDto.getId());
        List<AccountingPeriod> tempAccountingPeriodList = accountingPeriodMapper.selectList(new LambdaQueryWrapper<AccountingPeriod>()
                .eq(AccountingPeriod::getAccCalendarId, tempAccountingCalendar.getId()).orderByAsc(AccountingPeriod::getPeriodYear));
        //获取页面上的起始年度
        String startYear = accountingCalendarDto.getStartYear();
        AccountingPeriod acc = null;
        for(int i = 0; i < tempAccountingPeriodList.size(); i++){
            acc = tempAccountingPeriodList.get(i);
            break;
        }
        //获取会计期间表的起始年度
        Integer year = acc.getPeriodYear();
        String tempYear = String.valueOf(year);
        //获取页面上的会计期间表数据
        List<AccountingPeriod> accountingPeriodList = accountingCalendarDto.getAccountingPeriodList();
        //页面上的数量
       Integer num =  accountingPeriodList.size();
       //数据库中的数量
       Integer tempNum =  tempAccountingPeriodList.size();
       //如果数量相等 起始年度相等， 就代表会计期间表数据没有变化, 无需改动
        if(!num.equals(tempNum) || !startYear.equals(tempYear)){
            // 进行删除
            List<Long> ids = new ArrayList<>();
            for(AccountingPeriod accountingPeriod : tempAccountingPeriodList){
                ids.add(accountingPeriod.getId());
            }
            accountingPeriodMapper.deleteBatchIds(ids);
            //再把页面上传过来的会计期间表数据进行插入
            if(null != accountingPeriodList && accountingPeriodList.size() > 0){
                for(AccountingPeriod accountingPeriod : accountingPeriodList){
                    accountingPeriod.setAccCalendarId(accountingCalendar.getId());
                    accountingPeriod.setCreatorId(operator.getUserId());
                    accountingPeriod.setCreatorName(operator.getName());
                    accountingPeriod.setUpdatorId(operator.getUserId());
                    accountingPeriod.setUpdatorName(operator.getName());
                    //插入会计期间表
                    accountingPeriodMapper.insert(accountingPeriod);
                }
            }
        }
        return accountingCalendar.getId();
    }

    /**
     * 追加
     * @param accountingCalendarDto
     * @return
     */
    @Override
    public List<AccountingPeriod> appendById(AccountingCalendarDto accountingCalendarDto) {
        SysUserVo userVo = UserUtil.getUserVo();
        //获取页面的追加年数
        Integer appendYear = accountingCalendarDto.getAppendYear();
        //获取当前日历下所有的会计年度
        List<AccountingPeriod> accountingPeriodList = accountingPeriodMapper.selectList(new LambdaQueryWrapper<AccountingPeriod>()
                .eq(AccountingPeriod::getAccCalendarId, accountingCalendarDto.getId()));
        Integer periodYear = 0;
        Integer tempPeriodYear = 0;
        Integer tempPeriodYear2 = 0;
        //遍历出当前日历中最大的会计年度
        for(int i = 0; i < accountingPeriodList.size(); i++){
            AccountingPeriod accountingPeriod = accountingPeriodList.get(i);
            periodYear = accountingPeriod.getPeriodYear();
            if(i == 0){
                tempPeriodYear  = periodYear;
            }else {
                if(periodYear > tempPeriodYear){
                    tempPeriodYear2 = periodYear;
                }
            }
        }
        List<AccountingPeriod> accountingPeriods = null;
       if(tempPeriodYear2 != null){
           accountingCalendarDto.setStartYear(String.valueOf(tempPeriodYear2));
           //生成会计期间
           accountingPeriods = generateAccountingPeriod(accountingCalendarDto);
       }
        if(null != accountingPeriods && accountingPeriods.size() > 0){
            for(AccountingPeriod accountingPeriod : accountingPeriods){
                accountingPeriod.setAccCalendarId(accountingCalendarDto.getId());
                accountingPeriod.setCreatorId(userVo.getUserId());
                accountingPeriod.setCreatorName(userVo.getName());
                //插入会计期间表
                accountingPeriodMapper.insert(accountingPeriod);
            }
        }
        return accountingPeriods;
    }

    /**
     * 导出
     * @param accountingCalendarDto
     * @param response
     */
    @Override
    public void exportExcel(AccountingCalendarDto accountingCalendarDto, HttpServletResponse response) {
        Page<AccountingCalendarVo> page = accountingCalendarDto.getPage();
        fileService.resetPage(page);
        Page<AccountingCalendarVo> AccountingCalendarList = accountingCalendarMapper.findPage(page, accountingCalendarDto);
        fileService.exportExcel(response,AccountingCalendarList.getRecords(),
                new ExcelColumn("code","编码"),
                new ExcelColumn("name","名称"),
                new ExcelColumn("periodTypeName","期间类型"),
                new ExcelColumn("startDate","开始日期"),
                new ExcelColumn("startYear","起始会计年度"),
                new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED),
                new ExcelColumn("isReleased","发布状态",ExcelDataConstant.SYSTEM_DATA_IS_RELEASED)
        );
    }
}
