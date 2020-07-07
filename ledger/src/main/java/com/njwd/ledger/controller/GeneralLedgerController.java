package com.njwd.ledger.controller;

import com.njwd.entity.ledger.dto.GeneralLedgerQueryDto;
import com.njwd.entity.ledger.vo.GeneralLedgerVo;
import com.njwd.ledger.service.GeneralLedgerService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 总分类账Controller
 * @Date 2019/7/30 9:14
 * @Author 薛永利
 */
@RestController
@RequestMapping("generalLedger")
public class GeneralLedgerController extends BaseController {
	@Resource
	private GeneralLedgerService generalLedgerService;

	/**
	 * @Description 查询总分类账
	 * @Author 薛永利
	 * @Date 2019/7/30 9:31
	 * @Param [generalLedgerDto]
	 * @return java.lang.String
	 */
	@PostMapping("findGeneralLedgerList")
	public Result<List<GeneralLedgerVo>> findGeneralLedgerList(@RequestBody GeneralLedgerQueryDto generalLedgerQueryDto) {
		//必填校验
		FastUtils.checkParams(generalLedgerQueryDto.getAccountBookEntityList(), generalLedgerQueryDto.getPeriodYearNum1(), generalLedgerQueryDto.getPeriodYearNum2(), generalLedgerQueryDto.getIsFinal(),
				generalLedgerQueryDto.getIsIncludeUnbooked(), generalLedgerQueryDto.getIsIncludeProfitAndLoss(), generalLedgerQueryDto.getShowCondition(), generalLedgerQueryDto.getFullNameFlag()
		);

		UserUtils.getUserVo();
		return ok(generalLedgerService.findGeneralLedgerList(generalLedgerQueryDto));
	}

	/**
	 * @return java.lang.String
	 * @Description 查询明细分类账
	 * @Author 薛永利
	 * @Date 2019/7/30 9:31
	 * @Param [generalLedgerDto]
	 */
	@PostMapping("findDetaillLedgerList")
	public Result<List<GeneralLedgerVo>> findDetaillLedgerList(@RequestBody GeneralLedgerQueryDto generalLedgerQueryDto) {
		//必填校验
		FastUtils.checkParams(generalLedgerQueryDto.getAccountBookEntityList(), generalLedgerQueryDto.getIsFinal(), generalLedgerQueryDto.getIsIncludeUnbooked(),
				generalLedgerQueryDto.getShowCondition(), generalLedgerQueryDto.getFullNameFlag(), generalLedgerQueryDto.getOppositeSubjectFlag(), generalLedgerQueryDto.getIsIncludeProfitAndLoss()
		);

		UserUtils.getUserVo();
		return ok(generalLedgerService.findDetailLedgerList(generalLedgerQueryDto));
	}

	/**
	 * @Description @Description导出总分类账excel
	 * @Author 薛永利
	 * @Date 2019/9/2 14:01
	 * @Param [generalLedgerQueryDto, response]
	 * @return void
	 */
	@RequestMapping("exportGeneralLedgerExcel")
	public void exportGeneralLedgerExcel(@RequestBody GeneralLedgerQueryDto generalLedgerQueryDto, HttpServletResponse response) {
		generalLedgerService.exportGeneralLedgerExcel(generalLedgerQueryDto, response);
	}

	/**
	 * @Description @Description 导出明细分类账excel
	 * @Author 薛永利
	 * @Date 2019/9/2 14:01
	 * @Param [generalLedgerQueryDto, response]
	 * @return void
	 */
	@RequestMapping("exportDetailLedgerExcel")
	public void exportDetailLedgerExcel(@RequestBody GeneralLedgerQueryDto generalLedgerQueryDto, HttpServletResponse response) {
		generalLedgerService.exportDetailLedgerExcel(generalLedgerQueryDto, response);
	}
}
