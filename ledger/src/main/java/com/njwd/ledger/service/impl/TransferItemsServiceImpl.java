package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.*;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.PostPeriodDto;
import com.njwd.entity.ledger.vo.*;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryItemMapper;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryMapper;
import com.njwd.ledger.mapper.VoucherEntryAuxiliaryMapper;
import com.njwd.ledger.service.*;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 过账实现类
 * fancl
 * 2019-08-13
 */
@Service
public class TransferItemsServiceImpl implements TransferItemsService {

    @Autowired
    AccountBookPeriodService accountBookPeriodService;

    @Autowired
    VoucherService voucherService;

    @Autowired
    CommonService commonService;

    @Autowired
    VoucherAdjustService voucherAdjustService;

    @Autowired
    VoucherEntryService voucherEntryService;

    @Autowired
    VoucherEntryAuxiliaryService voucherEntryAuxiliaryService;

    @Autowired
    VoucherEntryCashFlowService voucherEntryCashFlowService;

    @Autowired
    private BaseLedgerService baseLedgerService;

    @Autowired
    private VoucherEntryAuxiliaryMapper voucherEntryAuxiliaryMapper;

    @Autowired
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;

    @Autowired
    private BalanceSubjectAuxiliaryMapper balanceSubjectAuxiliaryMapper;


    //记录日志
    Logger logger = LoggerFactory.getLogger(TransferItemsServiceImpl.class);


    /**
     * @param postPeriodDto 过账Dto对象
     * @return 各账簿期间检查情况的list
     * @description 过账方法
     * @author fancl
     * @date 2019/8/15
     */
    public Page<AccountBookPeriodVo> doTransferItems(PostPeriodDto postPeriodDto) {
        //取得参数信息
        ParameterSetVo parameterSet = commonService.getParameterSet(null);
        //先定义List 再转换成Page对象
        Page<AccountBookPeriodVo> transferItemPage = postPeriodDto.getPage();
        List<AccountBookPeriodVo> transferItemList = new ArrayList<>();
        //定义账簿检查返回对象
        AccountBookPeriodVo transferItemVo = null;
        //循环每个账簿期间
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        for (AccountBookPeriod accountBookPeriod : postPeriodDto.getPeriodList()) {
            //总账参数设置,如果账簿Id只有为0的,则为平台级参数;如果有账簿私有的,则使用私有的
            ParameterSetSub parameterSetSub = FastUtils.getParamSetSub(parameterSet ,accountBookPeriod.getAccountBookId(),Constant.ParameterSetKey.IS_CASHIER_REVIEW);

            //是否要复核
            boolean isReview = false;
            if (Constant.Is.YES.equals(parameterSetSub.getValue().byteValue())) {
                isReview = true;
            }
            accountBookPeriodDto.setId(accountBookPeriod.getId());
            AccountBookPeriodVo accountBookPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);
            //账簿期间未找到
            if (accountBookPeriodVo == null) {
                accountBookPeriodVo = new AccountBookPeriodVo();
                accountBookPeriodVo.setId(accountBookPeriod.getId());
                transferItemList = stepVoucherStatus(transferItemList, accountBookPeriodVo, accountBookPeriod.getId(), LedgerConstant.PostPeriod.isNull, LedgerConstant.PostPeriod.isNull_message);
                //执行下一个账簿期间
                continue;
            }
            //如果未开启，生成错误提示
            if (Constant.Is.NO.equals(accountBookPeriodVo.getStatus())) {
                transferItemList = stepVoucherStatus(transferItemList, accountBookPeriodVo, accountBookPeriod.getId(), LedgerConstant.PostPeriod.unOpen, LedgerConstant.PostPeriod.unOpen_message);
                //执行下一个账簿期间
                continue;
            }
            //如果已结账,生成错误信息
            if (Constant.Is.YES.equals(accountBookPeriodVo.getIsSettle())) {
                transferItemList = stepVoucherStatus(transferItemList, accountBookPeriodVo, accountBookPeriod.getId(), LedgerConstant.PostPeriod.settled, LedgerConstant.PostPeriod.settled_message);
                //执行下一个账簿期间
                continue;
            }

            /**
             * 凭证未审核、未复核、断号检查
             */
            transferItemVo = stepCheck(accountBookPeriod.getId(), accountBookPeriodVo, isReview);
            transferItemVo.setId(accountBookPeriodVo.getId());
            //如果有新检查项出现
            if (transferItemVo.getMessageList().size() > 0) {
                //有错误信息,返回false
                transferItemVo.setTransferFlag(false);
                transferItemList.add(transferItemVo);
            } else {
                //没有错误信息,继续看过账执行结果
                //调用过账方法
                try {
                    AccountBookPeriod bookPeriod = new AccountBookPeriod();
                    FastUtils.copyProperties(accountBookPeriodVo, bookPeriod);
                    transferItemVo = calcBalanceForTransfer(bookPeriod);
                    transferItemVo.setTransferFlag(true);
                } catch (ServiceException e) {
                    e.getMessage();
                    transferItemVo.setTransferFlag(false);
                    transferItemVo = this.packingItem(transferItemVo, accountBookPeriod.getId(),
                            LedgerConstant.PostPeriod.transferSQLFail, LedgerConstant.PostPeriod.transferSQLFail_message);
                }
                transferItemList.add(transferItemVo);
            }

        }
        transferItemPage.setRecords(transferItemList);
        transferItemPage.setTotal(postPeriodDto.getPeriodList().size());
        return transferItemPage;
    }


    /**
     * @description 检查凭证状态
     * @author fancl
     * @date 2019/8/19
     * @param
     * @return
     */
    private List<AccountBookPeriodVo> stepVoucherStatus(List<AccountBookPeriodVo> transferItemList,
                                                        AccountBookPeriodVo transferItemVo,
                                                        Long periodId, String checkType, String messageDesc) {

        transferItemVo.setTransferFlag(false);
        transferItemVo = this.packingItem(transferItemVo, periodId,
                checkType, messageDesc);
        transferItemList.add(transferItemVo);
        return transferItemList;
    }

    /**
     * @description 检查审核、未审核、凭证断号
     * @author fancl
     * @date 2019/8/23
     * @param
     * @return
     */
    private AccountBookPeriodVo stepCheck(Long periodId,
                                          AccountBookPeriodVo transferItemVo,
                                          boolean isReview) {
        //新建一个账簿期间返回实体
        Voucher voucher = new Voucher();
        voucher.setAccountBookId(transferItemVo.getAccountBookId());
        voucher.setPeriodYearNum(transferItemVo.getPeriodYearNum());
        //未审核的
        voucher.setApproveStatus(Constant.Number.ANTI_INITLIZED);
        voucher.setIsDel(Constant.Number.ANTI_INITLIZED);
        //查询待审核凭证
        List<Byte> staList = new ArrayList<>();
        List<VoucherVo> vouchersNotAuditList = voucherService.findByCondition(voucher, staList);
        //如果有未审核的
        if (!vouchersNotAuditList.isEmpty()) {
            transferItemVo = this.packingItem(transferItemVo, periodId,
                    LedgerConstant.PostPeriod.unAudit, String.format(LedgerConstant.PostPeriod.unAudit_message, vouchersNotAuditList.size()));
        }

        //如果需要复核
        if (isReview) {
            //已审核未复核记录数
            //复核在审核之前
            voucher.setApproveStatus(null);
            voucher.setReviewStatus(Constant.Number.ANTI_INITLIZED);
            List<VoucherVo> vouchersNotReviewList = voucherService.findByCondition(voucher, staList);
            if (!vouchersNotReviewList.isEmpty()) {
                //现金类需要复核的
                int iCount = 0;
                //检查是否存在现金类的  NEEDLESS 非现金
                for (VoucherVo v : vouchersNotReviewList) {
                    if (v.getCashCheckType() != Constant.CashFlowCheckType.NEEDLESS) {
                        ++iCount;
                    }
                }
                if (iCount > 0) {
                    transferItemVo = this.packingItem(transferItemVo, periodId,
                            LedgerConstant.PostPeriod.unReview, String.format(LedgerConstant.PostPeriod.unReview_message, iCount));
                }
            }
        }

        //断号检查,每次只检查一个账簿期间
        List<Long> checkIdList = new ArrayList<>();
        checkIdList.add(periodId);

        List<VoucherAdjust> voucherAdjusts = voucherAdjustService.checkBroken(checkIdList);
        if (!voucherAdjusts.isEmpty()) {
            //如果需要整理(存在断号)

            if (Constant.Number.ANTI_INITLIZED.equals(voucherAdjusts.get(0).getAdjustStatus())) {
                transferItemVo = this.packingItem(transferItemVo, periodId,
                        LedgerConstant.PostPeriod.broken, LedgerConstant.PostPeriod.broken_message);
            }

        }
        return transferItemVo;
    }


    /**
     * @description 处理包装的detail返回信息
     * @author fancl
     * @date 2019/8/23
     * @param
     * @return
     */
    private AccountBookPeriodVo packingItem(AccountBookPeriodVo transferItemVo,
                                            Long periodId, String checkType,
                                            String messageDesc) {
        //构造返回对象 TransferDetail
        TransferDetail detail = new TransferDetail();
        detail.setPeriodId(periodId);
        detail.setCheckType(checkType);
        detail.setMessageDesc(messageDesc);
        //将错误描述放到List中
        transferItemVo.getMessageList().add(detail);
        return transferItemVo;
    }

    /**
     * @description 过账相关计算和update
     * @author fancl
     * @date 2019/8/16
     * @param accountBookPeriod 账簿期间对象
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountBookPeriodVo calcBalanceForTransfer(AccountBookPeriod accountBookPeriod) {
        AccountBookPeriodVo accountVo = null;
        try {
            //修改科目余额
            //先查询出计算后的科目汇总金额
            List<PostPeriodBalanceVo> balanceBeforeUpdateForPostPeriodList = voucherEntryService.findBalanceBeforeUpdateForPostPeriod(accountBookPeriod);
            //再循环修改科目金额
            if (!balanceBeforeUpdateForPostPeriodList.isEmpty() && balanceBeforeUpdateForPostPeriodList.get(0) != null) {
                voucherEntryService.updateVoucherBalanceForPostPeriod(balanceBeforeUpdateForPostPeriodList);
            }
            //辅助核算
            dealAux(accountBookPeriod);

            //修改现金流量余额
            //先查询出现金流量金额
            List<PostPeriodBalanceVo> cashFlowBalanceBeforeUpdateForPostPeriodList = voucherEntryCashFlowService.findCashFlowBalanceBeforeUpdateForPostPeriod(accountBookPeriod);
            if (!cashFlowBalanceBeforeUpdateForPostPeriodList.isEmpty() && cashFlowBalanceBeforeUpdateForPostPeriodList.get(0) != null) {
                voucherEntryCashFlowService.updateCashFlowBalanceForPostPeriod(cashFlowBalanceBeforeUpdateForPostPeriodList);
            }

            //修改金额后才修改过账状态
            //修改凭证状态为过账
            SysUserVo operator = UserUtils.getUserVo();
            Voucher voucher = new Voucher();
            voucher.setStatus(LedgerConstant.VoucherStatus.POST);
            voucher.setPostingUserId(operator.getUserId());
            voucher.setPostingUserName(operator.getName());
            voucher.setPostingTime(new Date());
            voucherService.updateVoucherStatusForPeriod(accountBookPeriod, voucher);
            //修改过账人信息
            Object o = baseLedgerService.judgeNull(accountBookPeriod);
            if (o == null) {
                baseLedgerService.initJson(accountBookPeriod);
            }
            baseLedgerService.updateManageInfo(accountBookPeriod, LedgerConstant.ManageInfoUpdateType.transferItem);

            //重新查询数据
            AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
            accountBookPeriodDto.setId(accountBookPeriod.getId());
            accountVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            e.printStackTrace();
            throw new ServiceException(ResultCode.TRANSFER_CALC_BALANCE_ERROR);
        }
        return accountVo;
    }

    //查询并处理辅助核算数据
    private void dealAux(AccountBookPeriod accountBookPeriod) {
        //查询凭证数据
        Voucher voucher = new Voucher();
        voucher.setAccountBookId(accountBookPeriod.getAccountBookId());
        voucher.setPeriodYearNum(accountBookPeriod.getPeriodYearNum());
        voucher.setIsDel(Constant.Is.NO);
        List<Byte> staList = new ArrayList<>();
        staList.add(LedgerConstant.VoucherStatus.POSTING);
        List<VoucherVo> voucherList = voucherService.findByCondition(voucher, staList);

        //按核算主体分组的凭证
        //凭证要根据核算主体分组,这样才能唯一匹配到辅助核算余额明细中唯一的记录
        Map<Long, List<VoucherVo>> entityGroupMap = new LinkedHashMap<>();
        if (!voucherList.isEmpty()) {
            entityGroupMap = voucherList.stream().collect(Collectors.groupingBy(VoucherVo::getAccountBookEntityId));
        }

        //遍历 核算主体为Key, List<Long>类型的凭证id
        for (Map.Entry<Long, List<VoucherVo>> entityMap : entityGroupMap.entrySet()) {
            //核算主体下面的凭证ids
            List<VoucherVo> voucherIds = entityMap.getValue();
            if (voucherIds.isEmpty()) {
                continue;
            }
            //定义以 subject_soucetableItemId_为Key,以List<PostPeriodBalanceVo> 为value的map集合
            Map<String, PostPeriodBalanceVo> subMap = new HashMap<>();
            //分录
            List<PostPeriodBalanceVo> entryList = voucherEntryAuxiliaryMapper.findEntryForPostPeriod(voucherIds);
            //辅助核算
            List<PostPeriodBalanceVo> auxList = voucherEntryAuxiliaryMapper.findAuxForPostPeriod(voucherIds);
            StringBuffer sb = null;
            //遍历分录和辅助核算
            for (PostPeriodBalanceVo entry : entryList) {
                sb = new StringBuffer();
                boolean hasAux = false; //是否有辅助核算标识
                sb.append(entry.getAccountSubjectId()).append("_");
                for (PostPeriodBalanceVo aux : auxList) {
                    //当辅助核算entryid与外层分录entryid相等,表明是该分录的辅助核算,等找到下一个不相等的entryid时,外层进行下一次循环
                    if (entry.getEntryId().equals(aux.getEntryId())) {
                        sb.append(aux.getSourceTable()).append("_").append(aux.getItemValueId()).append("_");
                        hasAux = true;
                    }
                }
                //内层循环结束判断分录是否有辅助核算,并合并map中的金额
                if (hasAux) {
                    String key = sb.toString();
                    logger.info("Key:" + key);
                    //不包含则创建,包含则累加
                    if (!subMap.containsKey(key)) {
                        subMap.put(key, entry);
                    } else {
                        //累加借贷金额
                        PostPeriodBalanceVo original = subMap.get(key);
                        BigDecimal debit = original.getDebitAmount().add(entry.getDebitAmount());
                        BigDecimal credit = original.getCreditAmount().add(entry.getCreditAmount());
                        original.setDebitAmount(debit);
                        original.setCreditAmount(credit);
                        subMap.put(key, original);
                    }
                }
            }


            //构造一个辅助核算明细项 查询实体
            Balance balanceQueryDto = new Balance();
            balanceQueryDto.setAccountBookId(accountBookPeriod.getAccountBookId());
            balanceQueryDto.setAccountBookEntityId(entityMap.getKey());
            balanceQueryDto.setPeriodYearNum(accountBookPeriod.getPeriodYearNum());
            //生成key为balance_auxiliary_id，value为 subject_source_item_的Map
            Map<Long, String> itemMap = addItemMap(balanceQueryDto);
            //此时已生成balanceAuxiliaryId为key , subject_source_item_为value的map
            //对比两个map,将subMap中的每个值取出来,并设置其balanceAuxiliaryId,然后放到List中
            List<PostPeriodBalanceVo> balanceList = generateUpdateList(itemMap, subMap);
            //更新辅助核算余额
            if (!balanceList.isEmpty()) {
                balanceSubjectAuxiliaryMapper.updateAuxBalanceForPostPeriod(balanceList);
            }
        }

    }


    //生成itemMap
    private Map<Long, String> addItemMap(Balance balanceQueryDto) {

        //从辅助核算明细表中查询记录
        List<PostPeriodBalanceVo> auxItemForPostPeriodList = balanceSubjectAuxiliaryItemMapper.findAuxItemForPostPeriod(balanceQueryDto);
        //以subject_source_item为 key
        Map<Long, String> itemMap = new HashMap<>();
        String keyStr = null;
        StringBuffer sb = null;
        for (PostPeriodBalanceVo item : auxItemForPostPeriodList) {
            //以balanceAuxiliaryId 作为key , subject_source_item_为value的
            //不包含则创建,包含则拼接
            sb = new StringBuffer();
            if (!itemMap.containsKey(item.getBalanceAuxiliaryId())) {
                keyStr = sb.append(item.getAccountSubjectId()).append("_").append(item.getSourceTable()).append("_").append(item.getItemValueId()).append("_").toString();
            } else {
                keyStr = itemMap.get(item.getBalanceAuxiliaryId()) + sb.append(item.getSourceTable()).append("_").append(item.getItemValueId()).append("_");
            }
            itemMap.put(item.getBalanceAuxiliaryId(), keyStr);
        }
        //打印测试
        logger.info("itemMap:" + itemMap);
        return itemMap;
    }

    //生成更新list
    private List<PostPeriodBalanceVo> generateUpdateList(Map<Long, String> itemMap, Map<String, PostPeriodBalanceVo> subMap) {
        List<PostPeriodBalanceVo> balanceAuxList = new ArrayList<>();
        String vItem = null;    //外层辅助核算中的每个subject_source_item

        for (Map.Entry<Long, String> entry : itemMap.entrySet()) {
            vItem = entry.getValue();
            if (subMap.containsKey(vItem)) {
                //将外层balanceAuxId取出来设置到对象中
                subMap.get(vItem).setBalanceAuxiliaryId(entry.getKey());
                balanceAuxList.add(subMap.get(vItem));
            }
        }
        //测试 打印集合中每个元素
        logger.info("balanceAuxList:");
        for (PostPeriodBalanceVo p : balanceAuxList) {
            logger.info(p.toString());
        }
        return balanceAuxList;
    }

}
