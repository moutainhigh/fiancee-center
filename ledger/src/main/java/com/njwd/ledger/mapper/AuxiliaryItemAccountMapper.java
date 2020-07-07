package com.njwd.ledger.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.dto.AuxiliaryAccountingQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhc
 */
public interface AuxiliaryItemAccountMapper extends BaseMapper{

    /**
     * @Author ZhuHC
     * @Date  2019/9/26 9:53
     * @Param
     * @return
     * @Description  账簿期间开始时的期初余额
     */
    List<GeneralReturnAuxiliaryVo> getStartOpeningInfo(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * 获取第0期期初余额
     * @param auxiliaryAccountingQueryDto
     * @return
     */
    List<GeneralReturnAuxiliaryVo> getZeroOpeningInfo(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * 获取辅助核算明细账数据
     * @param auxiliaryAccountingQueryDto
     * @return
     */
    List<GeneralReturnAuxiliaryVo> getGeneralReturnAuxiliaryList(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto,@Param("tableName") String tableName);
    /**
     * 刘遵通
     * 查询制单日期内符合条件的ID列表
     * @param auxiliaryAccountingQueryDto
     * @return
     */
    List<GeneralReturnAuxiliaryVo> findDetailInDataIds(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/1 16:23
     * @Param auxiliaryAccountingQueryDto
     * @return List<GeneralReturnAuxiliaryVo>
     * @Description 辅助核算  期初
     */
    List<GeneralReturnAuxiliaryVo> getOpeningAccount(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:34
     * @Param auxiliaryAccountingQueryDto
     * @return List<GeneralReturnAuxiliaryVo>
     * @Description  明细
     */
    List<GeneralReturnAuxiliaryVo> getDetailAccount(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:34
     * @Param auxiliaryAccountingQueryDto
     * @return List<GeneralReturnAuxiliaryVo>
     * @Description  本期
     */
    List<GeneralReturnAuxiliaryVo> getPeriodNumAccount(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:34
     * @Param auxiliaryAccountingQueryDto
     * @return List<GeneralReturnAuxiliaryVo>
     * @Description  本年
     */
    List<GeneralReturnAuxiliaryVo> getPeriodYearAccount(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:34
     * @Param auxiliaryAccountingQueryDto
     * @return GeneralReturnAuxiliaryVo
     * @Description  期初时的本期缺省
     */
    List<GeneralReturnAuxiliaryVo> getOpeningLossInfo(@Param("auxiliaryAccountingQueryDto") AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

}

