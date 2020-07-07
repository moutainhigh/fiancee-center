package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.common.LogConstant;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.VoucherVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.CompanyFeignClient;
import com.njwd.ledger.service.VoucherService;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 凭证 前端控制器
 *
 * @author xyyxhcj@qq.com
 * @since 2019/07/25
 */
@RestController
@RequestMapping("voucher")
public class VoucherController extends BaseController {
    @Resource
    private VoucherService voucherService;
    @Resource
    private SenderService senderService;
    @Resource
    private CompanyFeignClient companyFeignClient;

    /**
     * 获取制单日期
     *
     * @return com.njwd.support.Result
     * @author xyyxhcj@qq.com
     * @date 2019/8/19 11:56
     **/
    @PostMapping("generateVoucherDate")
    public Result generateVoucherDate(@RequestBody VoucherDto voucherDto) {
        return ok(voucherService.generateVoucherDate(voucherDto));
    }

    /**
     * 暂存草稿(新增/修改)
     */
    @PostMapping("draft")
    public Result draft(@RequestBody VoucherDto voucherDto) {
        // todo 鉴权
        Long id = checkAndOperate(LogConstant.operation.draft, () -> voucherService.draft(voucherDto), voucherDto);
        // todo 记录日志
        return ok(id);
    }

    /**
     * 保存正式凭证(新增/修改)
     */
    @PostMapping("save")
    public Result save(@RequestBody VoucherDto voucherDto) {
        FastUtils.checkParams(voucherDto.getAccountBookId());
        // 鉴权
        ShiroUtils.checkPerm(Constant.MenuDefine.VOUCHER_EDIT, LedgerUtils.getCompanyId(companyFeignClient, voucherDto.getAccountBookId()));
        Long voucherId = voucherDto.getId();
        if (voucherId == null) {
            // 新增
            voucherId = checkAndOperate(LogConstant.operation.save, () -> voucherService.save(voucherDto), voucherDto);
        } else {
            List<Voucher> interiorVouchers = getInteriorVouchers(Collections.singletonList(voucherDto));
            interiorVouchers.add(voucherDto);
            if (voucherDto.getBeforePeriodYear() != null && voucherDto.getBeforePeriodNum() != null) {
                // 如果变更了期间,需要锁两个期间 前端传参
                Voucher lockPeriod = new Voucher();
                lockPeriod.setAccountBookId(voucherDto.getAccountBookId());
                lockPeriod.setPostingPeriodYear(voucherDto.getBeforePeriodYear());
                lockPeriod.setPostingPeriodNum(voucherDto.getBeforePeriodNum());
                interiorVouchers.add(lockPeriod);
            }
            checkAndOperate(LogConstant.operation.save, () -> voucherService.save(voucherDto), interiorVouchers.toArray(new Voucher[0]));
        }
        // 记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.save, LogConstant.operation.save_type, voucherId.toString()));
        return ok(voucherId);
    }

    /**
     * 查询生成的协同凭证
     **/
    private List<Voucher> getInteriorVouchers(List<VoucherDto> vouchers) {
        List<String> sourceCodes = new LinkedList<>();
        vouchers.forEach(voucher -> sourceCodes.add(voucher.getId().toString()));
        if (sourceCodes.isEmpty()) {
            return new LinkedList<>();
        }
        // 将生成的协同凭证查出 一并上锁
        return voucherService.list(new LambdaQueryWrapper<Voucher>()
                .in(Voucher::getSourceType, LedgerConstant.SourceType.COLLABORATE, LedgerConstant.SourceType.COMPANY_COLL)
                .in(Voucher::getSourceCode, sourceCodes)
                .eq(Voucher::getIsDel, Constant.Is.NO));
    }

    /**
     * 生成冲销凭证 整单冲销
     */
    @PostMapping("generateOffset")
    public Result generateOffset(@RequestBody VoucherDto voucherDto) {
        FastUtils.checkParams(voucherDto.getId(), voucherDto.getAccountBookId());
        // 鉴权
        ShiroUtils.checkPerm(Constant.MenuDefine.VOUCHER_OFFSET, LedgerUtils.getCompanyId(companyFeignClient, voucherDto.getAccountBookId()));
        List<Voucher> interiorVouchers = getInteriorVouchers(Collections.singletonList(voucherDto));
        interiorVouchers.add(voucherDto);
        Long voucherId = checkAndOperate(LogConstant.operation.generateOffset, () -> voucherService.generateOffset(voucherDto), interiorVouchers.toArray(new Voucher[0]));
        // 记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.generateOffset, LogConstant.operation.generateOffset_type, voucherId.toString()));
        return ok(voucherId);
    }

    /**
     * 单独保存现金流量
     */
    @PostMapping("saveCashFlow")
    public Result saveCashFlow(@RequestBody VoucherDto voucherDto) {
        Long voucherId = voucherDto.getId();
        FastUtils.checkParams(voucherId, voucherDto.getAccountBookId());
        // 鉴权
        ShiroUtils.checkPerm(Constant.MenuDefine.VOUCHER_EDIT, LedgerUtils.getCompanyId(companyFeignClient, voucherDto.getAccountBookId()));
        checkAndOperate(LogConstant.operation.saveCashFlow, () -> voucherService.saveCashFlow(voucherDto), voucherDto);
        // 记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.saveCashFlow, LogConstant.operation.saveCashFlow_type, voucherId.toString()));
        return ok(voucherId);
    }

    /**
     * 删除批量/逐张
     **/
    @PostMapping("deleteBatch")
    public Result deleteBatch(@RequestBody VoucherDto voucherDto) {
        // 鉴权
        List<VoucherDto> editVoucherList = voucherDto.getEditVoucherList();
        FastUtils.checkParams(editVoucherList);
        final BatchResult batchResult = filterNotPermData(editVoucherList, Constant.MenuDefine.VOUCHER_DELETE);
        if (editVoucherList.isEmpty()) {
            return ok(batchResult);
        }
        List<Voucher> interiorVouchers = getInteriorVouchers(editVoucherList);
        interiorVouchers.addAll(editVoucherList);
        LedgerUtils.lockVoucher(LogConstant.operation.delete, () -> voucherService.deleteBatch(voucherDto, batchResult), interiorVouchers.toArray(new Voucher[0]));
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * 过滤掉无权限的数据
     *
     * @param editVoucherList editVoucherList
     * @param menuDefine 权限定义
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/10 15:43
     **/
    private BatchResult filterNotPermData(List<VoucherDto> editVoucherList, String menuDefine) {
        Set<Long> accountBookIds = new HashSet<>(editVoucherList.size());
        for (VoucherDto dto : editVoucherList) {
            FastUtils.checkParams(dto.getAccountBookId());
            accountBookIds.add(dto.getAccountBookId());
        }
        Map<Long, Long> accountBookCompanyDict = LedgerUtils.getAccountBookCompanyDict(companyFeignClient, accountBookIds);
        return ShiroUtils.filterNotPermData(editVoucherList, menuDefine, new ShiroUtils.CheckPermSupport<VoucherDto>() {
            @Override
            public Long getBusinessId(VoucherDto voucherDto) {
                return voucherDto.getId();
            }

            @Override
            public Long getCompanyId(VoucherDto voucherDto) {
                return accountBookCompanyDict.get(voucherDto.getAccountBookId());
            }
        });
    }

    /**
     * @return com.njwd.support.Result
     * @Author ZhuHC
     * @Date 2019/8/15 15:32
     * @Param [voucherDto]
     * @Description 凭证明细
     */
    @PostMapping("findDetail")
    public Result findDetail(@RequestBody VoucherDto voucherDto) {
        FastUtils.checkParams(voucherDto.getId());
        VoucherVo vo = new VoucherVo();
        vo.setId(voucherDto.getId());
        return ok(voucherService.findDetail(vo));
    }

    /**
     * @return com.njwd.support.Result
     * @Author ZhuHC
     * @Date 2019/8/15 15:32
     * @Param [voucherDto]
     * @Description 凭证列表
     */
    @PostMapping("findPage")
    public Result findPage(@RequestBody VoucherDto voucherDto) {
        FastUtils.checkListNullOrEmpty(voucherDto.getVoucherDates(),voucherDto.getPeriodYearNumList());
        FastUtils.checkParams(voucherDto.getAccountBookIds(),voucherDto.getAccountBookEntityIds(),voucherDto.getIsDetailShow());
        voucherDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Result result;
        //是否展示明细 1：展示 返回分录数据，0：不展示 直接返回凭证数据
        if(Constant.Is.NO .equals(voucherDto.getIsDetailShow())){
            result = ok(voucherService.findPage(voucherDto));
        }else {
            result = ok(voucherService.findVoucherEntries(voucherDto));
        }
        return result;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/23 14:02
     * @Param [voucherDto, response]
     * @return void
     * @Description 凭证列表导出
     */
    @PostMapping("exportVoucherListExcel")
    public void exportVoucherListExcel(@RequestBody VoucherDto voucherDto,HttpServletResponse response) {
        FastUtils.checkListNullOrEmpty(voucherDto.getVoucherDates(),voucherDto.getPeriodYearNumList());
        FastUtils.checkParams(voucherDto.getAccountBookIds(),voucherDto.getAccountBookEntityIds(),voucherDto.getIsDetailShow());
        voucherService.exportVoucherListExcel(voucherDto,response);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 14:21
     * @Param [voucherDto]
     * @return com.njwd.support.Result
     * @Description  凭证列表 根据凭证ID查询
     */
    @PostMapping("findVouchersByIds")
    public Result findVouchersByIds(@RequestBody VoucherDto voucherDto) {
        FastUtils.checkParams(voucherDto.getVoucherIds());
        return ok(voucherService.findVouchersByIds(voucherDto));
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/5 11:12
     * @Param [voucherDto]
     * @return com.njwd.support.Result
     * @Description 凭证列表 打印查询
     */
    @PostMapping("findVoucherForPrint")
    public Result findVoucherForPrint(@RequestBody VoucherDto voucherDto) {
        FastUtils.checkListNullOrEmpty(voucherDto.getVoucherDates(),voucherDto.getPeriodYearNumList());
        FastUtils.checkParams(voucherDto.getAccountBookIds(),voucherDto.getAccountBookEntityIds());
        return ok(voucherService.findVoucherForPrint(voucherDto));
    }

    /**
     * @return com.njwd.support.Result
     * @Author ZhuHC
     * @Date 2019/8/19 17:15
     * @Param [voucherDto]
     * @Description 凭证 打印 查询
     */
    @PostMapping("findVoucherInfoAndDetail")
    public Result findVoucherInfoAndDetail(@RequestBody VoucherDto voucherDto) {
        FastUtils.checkParams(voucherDto.getAccountBookIdLists(),voucherDto.getAccountBookEntityIdLists(),voucherDto.getPeriodYearNumLists());
        return ok(voucherService.findVoucherInfoAndDetail(voucherDto));
    }

    /**
     * 校验是否可操作,如果一致且有凭证ID则上同步锁，不可操作时抛出对应异常
     */
    private <T> T checkAndOperate(String operType, RedisUtils.LockProcess<T> lockProcess, Voucher... vouchers) {
        return LedgerUtils.lockVoucher(operType, lockProcess, vouchers);
    }

    /**
     * @param voucherDto
     * @return
     * @Author 刘遵通
     * 检查审核
     */
    @PostMapping("checkApprove")
    public Result<BatchResult> checkApprove(@RequestBody VoucherDto voucherDto) {
        //获取参数集合
        List<VoucherDto> editVoucherList =  voucherDto.getEditVoucherList();
       // BatchResult batchResults = new BatchResult();
        //editVoucherList 为空直接返回
        if (CollectionUtils.isEmpty(editVoucherList)) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for (VoucherDto v : editVoucherList){
            FastUtils.checkParams(v.getId(),v.getAccountBookId(),v.getPostingPeriodYear(),v.getPostingPeriodNum(),v.getVersion());
        }
        //过滤掉无权限的数据
        final BatchResult batchResult = filterNotPermData(editVoucherList, Constant.MenuDefine.VOUCHER_CHECK);
        if(!editVoucherList.isEmpty()){
            //检查账簿期间是否锁定
            Voucher[] vouchers = voucherDto.getEditVoucherList().toArray(new Voucher[0]);
            LedgerUtils.lockVoucher(LogConstant.operation.approve, () -> voucherService.checkApprove(voucherDto,batchResult), vouchers);
        }
        //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.approve, LogConstant.operation.approve_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * @param voucherDto
     * @return
     * @Author 刘遵通
     * 反审核
     */
    @PostMapping("reversalApprove")
    public Result<BatchResult> reversalApprove(@RequestBody VoucherDto voucherDto) {
        //获取参数集合
        List<VoucherDto> editVoucherList =  voucherDto.getEditVoucherList();
        //editVoucherList 为空直接返回
        if (CollectionUtils.isEmpty(editVoucherList)) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for (VoucherDto v : editVoucherList){
            FastUtils.checkParams(v.getId(),v.getAccountBookId(),v.getPostingPeriodYear(),v.getPostingPeriodNum(),v.getVersion());
        }
        //过滤掉无权限的数据
        final BatchResult batchResult = filterNotPermData(editVoucherList, Constant.MenuDefine.VOUCHER_UNCHECK);
        if(!editVoucherList.isEmpty()){
            //检查账簿期间是否锁定
            Voucher[] vouchers = voucherDto.getEditVoucherList().toArray(new Voucher[0]);
            LedgerUtils.lockVoucher(LogConstant.operation.reversalApprove, () -> voucherService.reversalApprove(voucherDto,batchResult), vouchers);
        }
     //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * @param voucherDto
     * @return
     * @Author 刘遵通
     * 复核
     */
    @PostMapping("checkReview")
    public Result<BatchResult> checkReview(@RequestBody VoucherDto voucherDto) {
        //获取参数集合
        List<VoucherDto> editVoucherList =  voucherDto.getEditVoucherList();
        //editVoucherList 为空直接返回
        if (CollectionUtils.isEmpty(editVoucherList)) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for (VoucherDto v : editVoucherList){
            FastUtils.checkParams(v.getId(),v.getAccountBookId(),v.getPostingPeriodYear(),v.getPostingPeriodNum(),v.getVersion());
        }
        //过滤掉无权限的数据
        final BatchResult batchResult = filterNotPermData(editVoucherList, Constant.MenuDefine.VOUCHER_REVIEW);
        if(!editVoucherList.isEmpty()){
            //检查账簿期间是否锁定
            Voucher[] vouchers = voucherDto.getEditVoucherList().toArray(new Voucher[0]);
            LedgerUtils.lockVoucher(LogConstant.operation.review, () -> voucherService.checkReview(voucherDto,batchResult), vouchers);
        }
        //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.review, LogConstant.operation.review_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * @param voucherDto
     * @return
     * @Author 刘遵通
     * 反复核
     */
    @PostMapping("reversalReview")
    public Result<BatchResult> reversalReview(@RequestBody VoucherDto voucherDto) {
        //获取参数集合
        List<VoucherDto> editVoucherList =  voucherDto.getEditVoucherList();
        //editVoucherList 为空直接返回
        if (CollectionUtils.isEmpty(editVoucherList)) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for (VoucherDto v : editVoucherList){
            FastUtils.checkParams(v.getId(),v.getAccountBookId(),v.getPostingPeriodYear(),v.getPostingPeriodNum(),v.getVersion());
        }
        //过滤掉无权限的数据
        final BatchResult batchResult = filterNotPermData(editVoucherList, Constant.MenuDefine.VOUCHER_REVIEW);
        if(!editVoucherList.isEmpty()){
            //检查账簿期间是否锁定
            Voucher[] vouchers = voucherDto.getEditVoucherList().toArray(new Voucher[0]);
            LedgerUtils.lockVoucher(LogConstant.operation.reversalReview, () -> voucherService.reversalReview(voucherDto,batchResult), vouchers);
        }
       //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.voucher, LogConstant.operation.reversalReview, LogConstant.operation.reversalReview_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * @return com.njwd.support.Result<com.njwd.entity.ledger.VoucherEntryAuxiliary>
     * @description: 根据凭证id+数据来源表+核算项目值id查询凭证分录是否存在
     * @Param [voucherEntryAuxiliaryDto]
     * @author LuoY
     * @date 2019/8/26 11:15
     */
    @PostMapping("findVoucherEntryAuxiliaryByItemValueId")
    public Result<Integer> findVoucherEntryAuxiliaryByItemValueId(@RequestBody VoucherDto voucherDto) {
        Integer voucherEntryAuxiliary = voucherService.findVoucherEntryAuxiliary(voucherDto);
        return ok(voucherEntryAuxiliary);
    }

    /**
     *根据租户id和科目id去查询凭证  用于公司间协同 租户端 启用功能
     * 刘遵通
     * @param voucherDto
     * @return
     */
    @PostMapping("findVoucherByRootIdAndSubjectId")
    public List<VoucherVo> findVoucherByRootIdAndSubjectId(@RequestBody VoucherDto voucherDto) {
       return voucherService.findVoucherByRootIdAndSubjectid(voucherDto);
    }

}
