package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.VoucherEntryAuxiliary;
import com.njwd.entity.ledger.dto.VoucherEntryAuxiliaryDto;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import com.njwd.entity.ledger.vo.VoucherEntryAuxiliaryVo;
import com.njwd.entity.ledger.vo.VoucherVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface VoucherEntryAuxiliaryMapper extends BaseMapper<VoucherEntryAuxiliary> {
    /**
     * 批量插入
     *
     * @param editAuxiliaryList editAuxiliaryList
     * @param voucherId         voucherId
     * @author xyyxhcj@qq.com
     * @date 2019/8/5 15:59
     **/
    void insertBatch(@Param("editAuxiliaryList") List<VoucherEntryAuxiliaryDto> editAuxiliaryList, @Param("voucherId") Long voucherId);



    /**
     * 查询核算明细
     *
     * @param voucherIds voucherIds
     * @return java.util.LinkedList<com.njwd.entity.ledger.dto.VoucherEntryAuxiliaryDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 9:47
     **/
    LinkedList<VoucherEntryAuxiliaryDto> findList(@Param("voucherIds") Collection<Long> voucherIds);

    
    /**
     * @description: 根据分录主键获取凭证辅助项
     * @param: [entryIds]
     * @return: java.util.List<com.njwd.entity.ledger.VoucherEntryAuxiliary> 
     * @author: xdy        
     * @create: 2019-09-03 17:13 
     */
    List<VoucherEntryAuxiliary> findListByEntryId(@Param("entryIds") List<Long> entryIds);

    /**
     * @Author ZhuHC
     * @Date  2019/9/27 15:48
     * @Param
     * @return
     * @Description 根据条件查找辅助核算分录
     */
    List<VoucherEntryAuxiliaryVo> findListByAuxiliary(@Param("voucherEntryAuxiliaryDto") VoucherEntryAuxiliaryDto voucherEntryAuxiliaryDto);
    
    
    /**
     * @description 根据凭证列表查询辅助核算明细
     * @author fancl
     * @date 2019/9/30
     * @param 
     * @return 
     */
    List<PostPeriodBalanceVo> findAuxForPostPeriod(@Param("voucherList") List<VoucherVo> voucherList);

    /**
     * @description 根据凭证列表查询分录
     * @author fancl
     * @date 2019/9/30
     * @param
     * @return
     */
    List<PostPeriodBalanceVo> findEntryForPostPeriod(@Param("voucherList") List<VoucherVo> voucherList);
}