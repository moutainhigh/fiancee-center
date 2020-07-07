package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.VoucherVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    /**
     * 批量插入
     *
     * @param voucherList voucherList
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/15 17:41
     **/
    int insertGenerateBatch(@Param("voucherList") Collection<VoucherDto> voucherList);
    /**
     * 批量删除
     *
     * @param removeVouchers removeVouchers
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 10:34
     **/
    int deleteBatch(@Param("removeVouchers") List<VoucherDto> removeVouchers);
    /**
     * 刘遵通
     * 根据条件修改当前凭证
     *
     * @param voucherDto
     * @param list
     * @return
     */
    int updateVoucher(@Param("voucherDto") VoucherDto voucherDto, @Param("vouchers") List<VoucherVo> list);
    /**
     * @param
     * @return
     * @description 过账时根据条件修改凭证状态及过账人信息
     * @author fancl
     * @date 2019/8/19
     */
    int updateVoucherStatusForPeriod(@Param("accountBookPeriod") AccountBookPeriod accountBookPeriod, @Param("voucher") Voucher voucher);
    /**
     * @description: 批量凭证号整理
     * @param: [vouchers]
     * @return: int
     * @author: xdy
     * @create: 2019-08-12 10-12
     */
    int updateVoucherCode(List<Voucher> vouchers);
    /**
     * @return com.njwd.ledger.entity.vo.VoucherVo
     * @Description 根据账簿ID和期间年查询凭证
     * @Author liuxiang
     * @Date:19:22 2019/7/26
     * @Param [voucherDto]
     **/
    List<VoucherVo> findVoucherByAccBookIdAndYear(VoucherDto voucherDto);
    /**
     * 刘遵通
     * 根据条件查询凭证列表
     *
     * @param voucherDto
     * @return
     */
    List<VoucherVo> findVoucherList(@Param("voucherDto") VoucherDto voucherDto);
    /**
     * 刘遵通
     * 根据条件查询凭证列表
     *
     * @param sourceCodeList
     * @return
     */
    List<VoucherVo> findVoucherListBySourceCode(@Param("sourceCodeList") List<String> sourceCodeList);
    /**
     * @description: 期间和状态查询凭证列表
     * @param: [accountBookPeriod, voucherStatus]
     * @return: java.util.List<com.njwd.entity.ledger.Voucher>
     * @author: xdy
     * @create: 2019-08-12 10-11
     */
    List<Voucher> findVouchersByPeriod(@Param("accountBookPeriod") AccountBookPeriodVo accountBookPeriod, @Param("voucherStatus") List<Byte> voucherStatus);
    /**
     * @return Page<VoucherVo>
     * @Author ZhuHC
     * @Date 2019/8/14 13:42
     * @Param [page, voucherDto]
     * @Description 凭证列表
     */
    Page<VoucherVo> findPage(@Param("page") Page<VoucherDto> page, @Param("voucherDto") VoucherDto voucherDto);
    /**
     * @Author ZhuHC
     * @Date  2019/9/5 11:20
     * @Param
     * @return
     * @Description 凭证列表 打印查询
     */
    List<VoucherVo> findPage(@Param("voucherDto") VoucherDto voucherDto);
    /**
     * @Author ZhuHC
     * @Date  2019/9/23 11:02
     * @Param
     * @return
     * @Description 根据ID查询凭证信息
     */
    VoucherVo findVoucherById(@Param("id") Long id);
    /**
     * @return List<VoucherVo>
     * @Author ZhuHC
     * @Date 2019/8/19 17:17
     * @Param voucherDto
     * @Description 凭证打印查询
     */
    List<VoucherVo> findByEntriesAndPeriod(@Param("voucherDto") VoucherDto voucherDto);
    /**
     * @return java.util.List<java.lang.String>
     * @Description 查询未过账的凭证号
     * @Author 朱小明
     * @Date 2019/8/13 20:54
     * @Param [accountBookPeriod]
     * checkCashType:0未过账  1:已过账
     **/
    List<Voucher> selectPostingVoucher(
            @Param("abp") AccountBookPeriod accountBookPeriod, @Param("checkCashType") Byte checkCashType);
    /**
     * 查存在的凭证列表
     *
     * @param voucherIds voucherIds
     * @return java.util.List<com.njwd.entity.ledger.dto.VoucherDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/27 9:51
     **/
    List<VoucherDto> findExistListByIds(@Param("voucherIds") Collection<Long> voucherIds);
    /**
     * @param
     * @return
     * @description 根据条件查询凭证
     * @author fancl
     * @date 2019/8/15
     */
    List<VoucherVo> findByCondition(@Param("voucher") Voucher voucher, @Param("voucherStatus") List<Byte> voucherStatus);

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherVo>
     * @description: 根据核算账簿查询凭证列表
     * @Param [voucherDto]
     * @author LuoY
     * @date 2019/8/26 11:52
     */
    Integer findVoucherByAccountBookId(VoucherDto voucherDto);

    /**
     * @Description 查询存在凭证的账簿id1
     * @Author 郑勇浩
     * @Data 2019/11/1 14:01
     * @Param [voucherDto]
     * @return java.util.List<java.lang.Long>
     */
    List<Long> findHasVoucherByAccountBookId(VoucherDto voucherDto);

    /**
    * @Description 查询整个账簿期间的损益凭证
    * @Author 朱小明
    * @Date 2019/8/30
    * @param accountBookPeriod
    * @return java.util.List<java.lang.Long>
    **/
    List<VoucherDto> findLossProfitIdsByAccountBookPeriod(AccountBookPeriod accountBookPeriod);
    /**
     * 根据核算主体查询核算主体名称
     * @param accountBookPeriod
     * @return
     */
    List<Voucher> selectListByAbEntity(AccountBookPeriodDto accountBookPeriod);

    /**
    * @Description 根据期间查询本期的损益凭证
    * @Author 朱小明
    * @Date 2019/9/24
    * @param accountBookPeriod
    * @return java.util.List<com.njwd.entity.ledger.Voucher>
    **/

    List<Voucher> selectLossProfitList(AccountBookPeriodDto accountBookPeriod);
    /**
     *根据租户id和科目id去查询凭证  用于公司间协同 租户端 启用功能
     * 刘遵通
     * @param voucherDto
     * @return
     */
    List<VoucherVo> findVoucherByRootIdAndSubjectid(@Param("voucherDto") VoucherDto voucherDto);
    /**
     * 刷新生成的原协同凭证
     *
     * @param voucherDto        voucherDto
     * @param operator          operator
     * @param unchangedEntryIds unchangedEntryIds
     * @param now               now
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/28 13:54
     **/
    int refreshInteriorVouchers(@Param("voucherDto") VoucherDto voucherDto, @Param("operator") SysUserVo operator, @Param("unchangedEntryIds") List<Long> unchangedEntryIds, @Param("now") Date now);

}
