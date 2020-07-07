package com.njwd.ledger.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.ledger.VoucherEntryAuxiliary;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.VoucherEntryVo;
import com.njwd.entity.ledger.vo.VoucherVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-07-25
 */
public interface VoucherService extends IService<Voucher> {

    /**
     * 保存草稿
     *
     * @param voucherDto voucherDto
     * @return java.lang.Long 主键
     * @author xyyxhcj@qq.com
     * @date 2019/7/26 11:24
     **/
    Long draft(VoucherDto voucherDto);
	/**
	 * 保存
	 *
	 * @param voucherDto voucherDto
	 * @return java.lang.Long 主键
	 * @author xyyxhcj@qq.com
	 * @date 2019/7/26 11:24
	 **/
	Long save(VoucherDto voucherDto);
	/**
	 * 保存现金流量
	 *
	 * @param voucherDto voucherDto
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/8/19 9:00
	 **/
	Long saveCashFlow(VoucherDto voucherDto);
	/**
	 * 批量删除
	 *
	 * @param voucherDto voucherDto
	 * @param batchResult batchResult
	 * @return com.njwd.support.BatchResult
	 * @author xyyxhcj@qq.com
	 * @date 2019/8/19 9:30
	 **/
	BatchResult deleteBatch(VoucherDto voucherDto, BatchResult batchResult);

	/**
	 * @description: 批量更新凭证号
	 * @param: [vouchers]
	 * @return: int
	 * @author: xdy
	 * @create: 2019-08-12 10-19
	 */
	int updateVoucherCode(List<Voucher> vouchers);

	/**
	 * @description 修改凭证状态 及过账信息
	 * @author fancl
	 * @date 2019/8/19
	 * @param
	 * @return
	 */
	int updateVoucherStatusForPeriod(AccountBookPeriod accountBookPeriod,Voucher voucher);

	/**
	 * @Description 删除凭证前.更新余额表
	 * @Author 朱小明
	 * @Date 2019/8/30
	 * @param voucherIds, removeVouchers
	 * @return void
	 **/
	void updateBalanceForRemoveVouchers(Collection<Long> voucherIds, List<VoucherDto> removeVouchers);
	/**
	 * 批量更新凭证号
	 *
	 * @param updateVouchers      updateVouchers
	 * @param isEmpty             isEmpty 集合是否为空
	 * @param generateVoucherList generateVoucherList
	 * @author xyyxhcj@qq.com
	 * @date 2019/8/22 16:16
	 **/
	void updateVoucherCodeBatch(List<Voucher> updateVouchers, boolean isEmpty, Collection<VoucherDto> generateVoucherList);

	/**
     * @Description 根据账簿ID和期间年查询凭证
     * @Author liuxiang
     * @Date:19:21 2019/7/26
     * @Param [voucherDto]
     * @return com.njwd.ledger.entity.vo.VoucherVo
     **/
	List<VoucherVo> findVoucherByAccBookIdAndYear(VoucherDto voucherDto);

	/**
	 * 查详情
	 *
	 * @param  vo
	 * @return com.njwd.ledger.entity.vo.VoucherVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/7/30 9:52
	 **/
	VoucherVo findDetail(VoucherVo vo);

	/**
	 * @Author ZhuHC
	 * @Date  2019/8/14 9:15
	 * @Param voucherDto
	 * @return Page<VoucherVo>
	 * @Description 凭证列表
	 */
	Page<VoucherVo> findPage(VoucherDto voucherDto);

	/**
	 * @Author ZhuHC
	 * @Date  2019/9/23 10:53
	 * @Param [voucherDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.VoucherEntryVo>
	 * @Description 凭证列表 带明细
	 */
	Page<VoucherEntryVo> findVoucherEntries(VoucherDto voucherDto);



	/**
	 * @Author ZhuHC
	 * @Date  2019/9/17 14:21
	 * @Param [voucherDto]
	 * @return com.njwd.support.Result
	 * @Description  凭证列表 根据凭证ID查询
	 */
	List<VoucherVo> findVouchersByIds(VoucherDto voucherDto);

	/**
	 * @Author ZhuHC
	 * @Date  2019/9/5 11:12
	 * @Param
	 * @return
	 * @Description 凭证列表 打印查询
	 */
	VoucherVo findVoucherForPrint(VoucherDto voucherDto);

	/**
	 * @Author ZhuHC
	 * @Date  2019/8/19 17:15
	 * @Param [voucherDto]
	 * @return com.njwd.support.Result
	 * @Description 凭证 打印 查询
	 */
	VoucherVo findVoucherInfoAndDetail(VoucherDto voucherDto);
	/**
	 * @description: 期间和状态查询凭证列表
	 * @param: [accountBookPeriod, voucherStatus]
	 * @return: java.util.List<com.njwd.entity.ledger.Voucher>
	 * @author: xdy
	 * @create: 2019-08-12 10-18
	 */
	List<Voucher> findVouchersByPeriod(AccountBookPeriodVo accountBookPeriod, List<Byte> voucherStatus);
    /**
     * @description 根据条件查凭证
     * @author fancl
     * @date 2019/8/15
     * @param
     * @return
     */
    List<VoucherVo> findByCondition(Voucher voucher, List<Byte> voucherStatus);
	/**
	 * 生成冲销凭证
	 *
	 * @param voucherDto voucherDto
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/8/19 9:04
	 **/
	Long generateOffset(VoucherDto voucherDto);
	/**
	 * 获取制单日期
	 *
	 * @param voucherDto voucherDto
	 * @return java.util.Date
	 * @author xyyxhcj@qq.com
	 * @date 2019/8/19 11:59
	 **/
    VoucherVo generateVoucherDate(VoucherDto voucherDto);
	/**
	 * @param voucherDto
	 * @return void
	 * @Description 获取下个流水号
	 * @Author 朱小明
	 * @Date 2019/8/22
	 **/
	Voucher generateCode(Voucher voucherDto);
	/**
	* @description: 根据核算账簿id查询凭证分录表是否包含指定凭证分录
	* @Param [voucher, voucherEntryAuxiliaryDto]
	* @return com.njwd.entity.ledger.VoucherEntryAuxiliary
	* @author LuoY
	* @date 2019/8/26 11:41
	*/
	Integer findVoucherEntryAuxiliary(VoucherDto voucherDto);

	/**
	 * @Description 查询范围内的账簿id存在凭证的账簿id
	 * @Author 郑勇浩
	 * @Data 2019/11/1 14:02
	 * @Param [voucherDto]
	 * @return java.util.List<java.lang.Long>
	 */
	List<Long> findHasVoucherByAccountBookId(VoucherDto voucherDto);

	/**
	* @Description 获取过滤凭证
	* @Author 朱小明
	* @Date 2019/8/30
	* @param voucherIds
	* @return java.util.List<com.njwd.entity.ledger.dto.VoucherDto>
	**/
	List<VoucherDto> getExistVouchersByIds(Collection<Long> voucherIds);

	/**
	 * 构造待存储核算明细
	 *
	 * @param voucherDto        voucherDto
	 * @param voucherId         voucherId
	 * @param unchangedEntryIds 未变更的分录ID
	 * @return java.util.List<com.njwd.entity.ledger.VoucherEntryAuxiliary> 待存储核算明细数据
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/25 14:42
	 **/
	@NotNull List<VoucherEntryAuxiliary> getVoucherEntryAuxiliaries(VoucherDto voucherDto, Long voucherId, List<Long> unchangedEntryIds);
    /**
	 *根据租户id和科目id去查询凭证  用于公司间协同 租户端 启用功能
	 * 刘遵通
	 * @param voucherDto
	 * @return
	 */
	List<VoucherVo> findVoucherByRootIdAndSubjectid(VoucherDto voucherDto);

	/**
	 * @Author ZhuHC
	 * @Date  2019/9/23 14:02
	 * @Param [voucherDto, response]
	 * @return void
	 * @Description 凭证列表导出
	 */
	void exportVoucherListExcel(VoucherDto voucherDto, HttpServletResponse response);
	/** 刘遵通
	 * 检查审核
	 * @param voucherDto
	 * @return
	 */
	BatchResult checkApprove(VoucherDto voucherDto,BatchResult batchResult);
	/** 刘遵通
	 * 反审核
	 * @param voucherDto
	 * @return
	 */
	BatchResult reversalApprove(VoucherDto voucherDto,BatchResult batchResult);

	/** 刘遵通
	 *  复核
	 * @param voucherDto
	 * @return
	 */
	BatchResult checkReview(VoucherDto voucherDto,BatchResult batchResult);

	/** 刘遵通
	 *  反复核
	 * @param voucherDto
	 * @return
	 */
	BatchResult reversalReview(VoucherDto voucherDto,BatchResult batchResult);
}
