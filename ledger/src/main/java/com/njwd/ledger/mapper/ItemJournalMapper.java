package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.dto.ItemJournalQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author ZhuHC
 * @Date  2019/8/6 17:35
 * @Param itemJournalQueryDto
 * @return List<GeneralReturnItemJournalVo>
 * @Description 查询 科目日记账
 */
public interface ItemJournalMapper extends BaseMapper {
    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:50
     * @Param itemJournalQueryDto
     * @return List<GeneralReturnItemJournalVo>
     * @Description  期初
     */
    List<GeneralReturnItemJournalVo> getJournalOpening(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);

    /**
     * 刘遵通
     * 获取科目日记账数据
     * @param itemJournalQueryDto
     * @return
     */
    List<GeneralReturnItemJournalVo> getGeneralReturnItemList(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);
    /**
     * 刘遵通
     * 查询制单日期内符合条件的ID列表
     * @param itemJournalQueryDto
     * @return
     */
    List<GeneralReturnItemJournalVo> findDetailInDataIds(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/9/26 9:53
     * @Param
     * @return
     * @Description  账簿期间开始时的期初余额
     */
    List<GeneralReturnItemJournalVo> getStartOpeningInfo(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);
    /**
     * 获取第0期期初余额
     * @param itemJournalQueryDto
     * @return
     */
    List<GeneralReturnItemJournalVo> getZeroOpeningInfo(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);
    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:50
     * @Param itemJournalQueryDto
     * @return List<GeneralReturnItemJournalVo>
     * @Description  明细
     */
    List<GeneralReturnItemJournalVo> getJournalInfo(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:50
     * @Param itemJournalQueryDto
     * @return List<GeneralReturnItemJournalVo>
     * @Description  本期
     */
    List<GeneralReturnItemJournalVo> getJournalPeriodNum(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:50
     * @Param itemJournalQueryDto
     * @return List<GeneralReturnItemJournalVo>
     * @Description  本年
     */
    List<GeneralReturnItemJournalVo> getJournalPeriodYear(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:50
     * @Param itemJournalQueryDto
     * @return GeneralReturnItemJournalVo
     * @Description  期初缺省数据
     */
    GeneralReturnItemJournalVo getOpeningLossInfo(@Param("itemJournalQueryDto") ItemJournalQueryDto itemJournalQueryDto);
}
