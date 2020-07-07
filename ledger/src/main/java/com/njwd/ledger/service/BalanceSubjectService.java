package com.njwd.ledger.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.BalanceSubject;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectDto;
import com.njwd.entity.ledger.dto.BalanceSubjectQueryDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryItemVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description 科目余额表接口
 * @Author 周鹏
 * @Date 2019/8/5
 */
public interface BalanceSubjectService extends IService<BalanceSubject> {
    /**
     * 更新发生额
     *
     * @param balanceSubjects balanceSubjects
     * @param voucherDto      voucherDto
     * @param updateType
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/13 20:28
     **/
    int updateBatch(Collection<BalanceSubjectDto> balanceSubjects, VoucherDto voucherDto, byte updateType);

    /**
     * 根据条件统计科目余额表
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/5
     */
    List<BalanceSubjectVo> findListByParam(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 根据条件查询凭证号
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/17
     */
    String findVoucherNumberByParam(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 根据条件统计科目汇总表
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/16
     */
    List<BalanceSubjectVo> findCollectListByParam(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 根据账簿id/核算主体id 查询科目信息
     *
     * @param balanceSubjectQueryDto
     * @return
     */
    List<BalanceSubjectVo> findListByAccountBookIdAndEntityId(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 查询所有辅助核算项信息
     *
     * @param auxiliaryItemVoList 辅助核算值来源表及id信息
     * @return
     */
    List<List<Map<String, Object>>> findAllSourceTableInfo(List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList);

    /**
     * 根据账簿id/核算主体id/期间 查询科目发生额累计
     *
     * @param balanceSubjectQueryDto
     * @return
     */
    List<BalanceSubjectVo> getAccumulateBalanceByPeriodNum(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 组装辅助核算编码和名称
     *
     * @param auxiliaryList 辅助核算信息
     * @param sourceTable   辅助核算表名
     * @param itemValueId   辅助核算id
     * @param auxiliaryCode 辅助核算编码
     * @param auxiliaryName 辅助核算名称
     */
    void initAuxiliaryNameAndCode(List<Map<String, Object>> auxiliaryList, StringBuilder sourceTable, StringBuilder itemValueId, StringBuilder auxiliaryCode, StringBuilder auxiliaryName);

    /**
     * Excel 导出科目余额表
     *
     * @param balanceSubjectQueryDto
     * @param response
     * @author: 周鹏
     * @create: 2019/8/29
     */
    void exportListExcel(BalanceSubjectQueryDto balanceSubjectQueryDto, HttpServletResponse response);

    /**
     * Excel 导出科目汇总表
     *
     * @param balanceSubjectQueryDto
     * @param response
     * @author: 周鹏
     * @create: 2019/8/29
     */
    void exportCollectListExcel(BalanceSubjectQueryDto balanceSubjectQueryDto, HttpServletResponse response);

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectVo>
     * @description: 根据科目id查询指定科目余额信息
     * @Param [balanceSubjectQueryDto]
     * @author LuoY
     * @date 2019/9/18 9:55
     */
    List<BalanceSubject> findBalanceSubjectInfoBySubjectId(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubject>
     * @description: 根据核算账簿, 主体, 期间查询利润表数据
     * @Param [balanceSubjectQueryDto]
     * @author LuoY
     * @date 2019/9/27 16:33
     */
    List<BalanceSubjectVo> findBalanceSubjectInfoByAccountInfo(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 组装查询辅助核算余额信息参数
     *
     * @param auxiliaryItemQueryDto
     * @param auxiliaryItemVoList
     * @return
     */
    BalanceSubjectAuxiliaryItemQueryDto initAuxiliaryItemQuery(BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto,
                                                               List<BalanceSubjectAuxiliaryItemVo> auxiliaryItemVoList);
}
