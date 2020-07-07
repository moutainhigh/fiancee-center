package com.njwd.ledger.mapper;

import com.njwd.entity.ledger.dto.GeneralLedgerQueryDto;
import com.njwd.entity.ledger.vo.GeneralLedgerVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 总分类账Mapper
 * @Date 2019/7/30 9:14
 * @Author 薛永利
 */
public interface GeneralLedgerMapper {

	/**
	 * @Description 查询总分类账期初数据
	 * @Author 郑勇浩
	 * @Data 2019/9/27 16:52
	 * @Param [generalLedgerQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	List<GeneralLedgerVo> findGeneralLedgerList(@Param("generalLedgerQueryDto") GeneralLedgerQueryDto generalLedgerQueryDto);

	/**
	 * @Description 查询制单日期内符合条件的ID列表
	 * @Author 郑勇浩
	 * @Data 2019/10/9 16:06
	 * @Param [generalLedgerQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	List<GeneralLedgerVo> findDetailInDataIds(@Param("generalLedgerQueryDto") GeneralLedgerQueryDto generalLedgerQueryDto);

	/**
	 * @Description 查询明细分类账具体日数据
	 * @Author 郑勇浩
	 * @Data 2019/9/30 17:43
	 * @Param [generalLedgerQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	List<GeneralLedgerVo> findDetailLedgerList(@Param("generalLedgerQueryDto") GeneralLedgerQueryDto generalLedgerQueryDto);
}
