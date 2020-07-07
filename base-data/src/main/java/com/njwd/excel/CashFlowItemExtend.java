package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.mapper.CashFlowItemMapper;
import com.njwd.basedata.service.CashFlowItemService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CashFlowItemExtend
 * @Description 现金流量项目导入校验
 * @Author admin
 * @Date 2019/7/8 13:47
 */
@Component
@ExcelExtend(type = "cash_flow_item")
public class CashFlowItemExtend  implements AddExtend<CashFlowItemDto>, CheckExtend {

    @Resource
    private CashFlowItemService cashFlowItemService;

	@Resource
	private CashFlowItemMapper cashFlowItemMapper;

	/**
     * 批量录入
     * @Author libao
     * @param list
     * @return
     */
    @Override
    public int addBatch(List<CashFlowItemDto> list) {
        for (CashFlowItemDto caFlowItemDto:list) {
             cashFlowItemService.addCashFlowItem(caFlowItemDto);
        }
        return 1;
    }

    /**
     * 逐条录入
     *
     * @param caFlowItemDto
     * @return
     */
    @Override
    public int add(CashFlowItemDto caFlowItemDto) {
        cashFlowItemService.addCashFlowItem(caFlowItemDto);
        return 1;
    }

    /**
     * 扩展列的校验
     *
     * @param checkContext
     */
    @Override
    public void check(CheckContext checkContext) {
		Long cashFlowId = checkContext.getLongValue("cashFlowId");
		checkContext.addSheetHandler("code", getCodeCheckHandler(cashFlowId))
				.addSheetHandler("name", getNameCheckHandler(cashFlowId))
				.addSheetHandler("upCode", getUpCheckHandler(cashFlowId));
    }

    /**
     * @Author Libao
     * @Description 扩展code校验规则
     * @Date  2019/7/8 14:12
     * @Param
     * @return
     */
	private CheckHandler<CashFlowItemDto> getCodeCheckHandler(Long cashFlowId) {
        return data -> {
			data.setCashFlowId(cashFlowId);
			String code = data.getCode();
			String newCode = code.substring(code.length() -2);
			if (Constant.Character.ZERO.equals(newCode)){
				return CheckResult.error(ResultCode.CASH_FLOW_ITEM_CHECK_CODE.message);
			}
           int result = cashFlowItemService.checkCode(data);
           if (result > Constant.Number.ZERO){
              return CheckResult.error(ResultCode.CASH_FLOW_ITEM_CODE_EXIST.message);
           }
            return CheckResult.ok();
        };
    }


    /**
     * @Author Libao
     * @Description 扩展name校验规则
     * @Date  2019/7/8 14:12
     * @Param
     * @return
     */
	private CheckHandler<CashFlowItemDto> getNameCheckHandler(Long cashFlowId) {
        return data -> {
			data.setCashFlowId(cashFlowId);
            int result = cashFlowItemService.checkName(data);
            //判断名称是否重复
            if (result > Constant.Number.ZERO){
                return CheckResult.error(ResultCode.CASH_FLOW_ITEM_NAME_EXIST.message);
            }
            return CheckResult.ok();
        };
    }


    /**
     * @Author Libao
     * @Description 扩展上级编码校验规则
     * @Date  2019/7/8 14:12
     * @Param
     * @return
     */
	private CheckHandler<CashFlowItemDto> getUpCheckHandler(Long cashFlowId) {
        return data -> {
			data.setCashFlowId(cashFlowId);
            //判断上级项目是否存在
            CashFlowItemVo cashFlowItemVo= cashFlowItemService.checkUpCashFlowItem(data);
            if (cashFlowItemVo == null){
				throw new ServiceException(ResultCode.CASH_FLOW_ITEM_UP_NOT_EXIST);
			}
			//系统预置的内部往来项目为三级项目，且是末级项目，不能新增下级
			if(Constant.Is.YES.equals(cashFlowItemVo.getIsInteriorContact()) && Constant.Is.YES.equals(cashFlowItemVo.getIsInit()) && Constant.Is.YES.equals(cashFlowItemVo.getIsFinal())){
				throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_FINAL_INTERIOR_INIT);
			}

			if (Constant.Is.NO.equals(cashFlowItemVo.getIsEnable())) {
				//抛出数据已禁用
				throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_DISABLE);
			}
			//查询预置数据是否存在下级预置数据，存在则不可以新增
			CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
			cashFlowItemDto.setCashFlowId(cashFlowId);
			cashFlowItemDto.setUpCode(data.getUpCode());
			int downCount = cashFlowItemMapper.findCashFlowItemCountByUpCode(cashFlowItemDto);
			if (downCount > 0) {
				throw new ServiceException(ResultCode.CASH_FLOW_ITEM_IS_INIT_AND_NOT_FINAL);
			}

			//父类code
			String upCode = cashFlowItemVo.getCode();
            //code截去后两位
            String code = data.getCode();
            String newCode = code.substring(0,code.length()-2);
            if (!upCode.equals(newCode)){
                return CheckResult.error(ResultCode.CASH_FLOW_ITEM_UP_CODE_NOT_MATCH_DOWN.message);
            }
            //层级校验
            String maxLevel = cashFlowItemVo.getMaxLevel();
            String [] maxLevelArr = maxLevel.split("-");
            if (cashFlowItemVo.getLevel()+1 > maxLevelArr.length){
                return CheckResult.error(ResultCode.CASH_FLOW_ITEM_MAX_LEVEL.message);
            }

            //校验完成，组装数据
            SysUserVo operator = UserUtils.getUserVo();
            data.setRootEnterpriseId(operator.getRootEnterpriseId());
            data.setCreatorId(operator.getCreatorId());
            data.setCreatorName(operator.getCreatorName());
            data.setUpCode(cashFlowItemVo.getCode());
            data.setFullName(cashFlowItemVo.getFullName()+"_"+data.getName());
            data.setCashFlowDirection(cashFlowItemVo.getCashFlowDirection());
            data.setCashFlowId(cashFlowItemVo.getCashFlowId());
            data.setMaxLevel(maxLevel);
            Byte level = (byte)(cashFlowItemVo.getLevel()+1);
            data.setLevel(level);
            data.setCompanyId(cashFlowItemVo.getCompanyId());
            data.setUseCompanyId(cashFlowItemVo.getUseCompanyId());
            return CheckResult.ok();
        };
    }

}
