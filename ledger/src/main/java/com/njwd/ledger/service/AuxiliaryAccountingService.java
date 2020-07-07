package com.njwd.ledger.service;


import com.njwd.entity.basedata.dto.AccountingItemValueDto;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import com.njwd.entity.ledger.dto.AuxiliaryAccountingQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface AuxiliaryAccountingService {

    /**
     * @Author ZhuHC
     * @Date  2019/7/30 11:24
     * @Param auxiliaryAccountingQueryDto
     * @return List<GeneralReturnAuxiliaryVo>
     * @Description 根据辅助核算项目  查询明细
     */
    List<GeneralReturnAuxiliaryVo> findAuxiliaryDetails(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * @Author wuweiming
     * @Date  2019/08/07 09:59
     * @Param []
     * @return List<AuxiliaryItemVo>
     * @Description 查询所有辅助核算（包含自定义）类型
     */
    List<AuxiliaryItemVo> findAuxiliaryItemList();

    /**
     * @Author wuweiming
     * @Date  2019/08/07 14:19
     * @Param []
     * @return List<AuxiliaryItemVo>
     * @Description 查询所有辅助核算（包含自定义）
     */
    List<AccountingItemValueVo> findAuxiliaryItemValues(AccountingItemValueDto dto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 10:16
     * @Param
     * @return
     * @Description 辅助核算明细账 数据导出
     */
    void exportExcel(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto, HttpServletResponse response);

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 17:00
     * @Param [credentialWord]
     * @return java.lang.String
     * @Description 凭证字数据转换
     */
     String convertData(Byte credentialWord);
}
