package com.njwd.ledger.excel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.annotation.ExcelExtend;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.ledger.dto.BalanceCashFlowInitDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.export.DataGetGroup;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.fileexcel.extend.DownloadExtend;
import com.njwd.ledger.cloudclient.CashFlowReportClient;
import com.njwd.ledger.service.BalanceCashFlowInitService;
import com.njwd.utils.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/22 9:21
 */
@Component
@ExcelExtend(type = "balance_cash_flow_init")
public class BalanceCashFlowInitExtend extends InitExtend implements DownloadExtend, CheckExtend , AddExtend<BalanceCashFlowInitDto> {

    @Resource
    private CashFlowReportClient cashFlowReportClient;

    @Resource
    private BalanceCashFlowInitService balanceCashFlowInitService;
    
    /**
     * @description: 校验
     * @param: [checkContext]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-22 10:27 
     */
    @Override
    public void check(CheckContext checkContext) {
        Long accountBookEntityId = checkContext.getLongValue("accountBookEntityId");
        Long accountBookId = checkContext.getLongValue("accountBookId");
        Long accountBookSystemId = checkContext.getLongValue("accountBookSystemId");
        Integer periodYear = checkContext.getIntegerValue("periodYear");
        Byte periodNum = checkContext.getByteValue("periodNum");
        //checkFileName(accountBookEntityId,checkContext.getFileName());

        checkContext.addSheetHandler(getBalanceCashFlowInitCheck(accountBookEntityId,accountBookId,accountBookSystemId,periodYear,periodNum));
    }

    /**
     * 现金流量校验
     * @Author lj
     * @Date:14:42 2019/10/23
     * @param
     * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.ledger.dto.BalanceCashFlowInitDto>
     **/
    private CheckHandler<BalanceCashFlowInitDto> getBalanceCashFlowInitCheck(Long accountBookEntityId,
    Long accountBookId, Long accountBookSystemId, Integer periodYear, Byte periodNum){
        List<CashFlowItemVo> cashFlowItemVoList = getCashFlowItemVos();
        Map<String,List<CashFlowItemVo>> cashFlowItemMap = cashFlowItemVoList.stream().collect(Collectors.groupingBy(CashFlowItemVo::getCode));
        //校验现金流量编码是否重复
        List<String> cashFlowItemList = new ArrayList<String>();
        return data -> {
            data.setAccountBookEntityId(accountBookEntityId);
            data.setAccountBookId(accountBookId);
            data.setAccountBookSystemId(accountBookSystemId);
            data.setPeriodYear(periodYear);
            data.setPeriodNum(periodNum);
            data.setIsFinal(Constant.Is.YES);
            String message=checkBalanceCashFlowInit(cashFlowItemList,cashFlowItemMap,data);
            cashFlowItemList.add(data.getCashFlowCode());
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            Long itemId = cashFlowItemMap.get(data.getCashFlowCode()).get(Constant.Number.ZERO).getId();
            data.setItemId(itemId);
            return CheckResult.ok();
        };
    }

    private String checkBalanceCashFlowInit(List<String> cashFlowItemList,Map<String,List<CashFlowItemVo>> cashFlowItemMap,BalanceCashFlowInitDto data) {
        String message="";

        //如果现金流量编码不存在于系统中，记录行号：@行号，记录异常：现金流量编码 @现金流量编码 不存在。
        if(!cashFlowItemMap.containsKey(data.getCashFlowCode())){
            message = "现金流量编码 不存在";
            return message;
        }
        //todo 如果期初余额列非数字格式，记录行号：@行号，记录异常：期初余额数据格式异常。

        //如果存在相同现金流量编码，记录行号：@行号1，@行号2，…(以最小行值填表)，记录异常：存在相同现金流量编码
        if(cashFlowItemList.contains(data.getCashFlowCode())){
            message = "存在相同现金流量编码 "+data.getCashFlowCode();
            return message;
        }
        return message;
    }

    /**
     * 现金流量批量录入
     * @Author lj
     * @Date:14:44 2019/10/23
     * @param
     * @return int
     **/
    @Override
    public int addBatch(List<BalanceCashFlowInitDto> datas) {
        int result = 0;
        if (CollectionUtils.isNotEmpty(datas)) {
            BalanceCashFlowInitDto balanceCashFlowInitDto = new BalanceCashFlowInitDto();
            balanceCashFlowInitDto.setBalanceCashFlowInits(datas);
            balanceCashFlowInitService.addCashFlowInitBatch(balanceCashFlowInitDto);
        }
        return result;
    }

    //todo 现金流量录入
    @Override
    public int add(BalanceCashFlowInitDto data) {
        return 0;
    }

    @Override
    public void writeSheet(ExcelWriter writer) {
        ExcelColumn[] excelColumnArr = new ExcelColumn[]{
                new ExcelColumn("code", "现金流量编码"),
                new ExcelColumn("name", "现金流量名称"),
                new ExcelColumn("balanceDirection", "方向", ExcelDataConstant.SYSTEM_DATA_BALANCE_DIRECTION),
                new ExcelColumn("", "期初余额")
        };
        //标题
        List<List<String>> excelHead = new ArrayList<>();
        for (ExcelColumn excelColumn : excelColumnArr) {
            List<String> list = new ArrayList<>();
            list.add(excelColumn.getTitle());
            excelHead.add(list);
        }
        List<CashFlowItemVo> cashFlowItemVoList = getCashFlowItemVos();
        DataGetGroup dataGetGroup = new DataGetGroup();
        List<List<Object>> excelData = dataGetGroup.get(cashFlowItemVoList, excelColumnArr);
        //
        Sheet sheet1 = new Sheet(1, 0);
        sheet1.setSheetName("现金流量");
        sheet1.setHead(excelHead);
        sheet1.setAutoWidth(Boolean.TRUE);
        //
        writer.write1(excelData,sheet1);

    }

    /**
     * @Author lj
     * @Description 获取现金流量列表
     * @Date:13:39 2019/10/23
     **/
    private List<CashFlowItemVo> getCashFlowItemVos() {
        CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
        cashFlowItemDto.setIsFinal(Constant.Is.YES);
        Page page = new Page();
        page.setSize(10000L);
        page.setSearchCount(false);
        cashFlowItemDto.setPage(page);
        Page<CashFlowItemVo> page1 = cashFlowReportClient.findCashFlowItemByPage(cashFlowItemDto).getData();
        return page1.getRecords();
    }

    /**
     * @description: 取消系统校验
     * @param: []
     * @return: boolean 
     * @author: xdy        
     * @create: 2019-10-22 10:08 
     */
    @Override
    public boolean isSystemCheck(){
        return false;
    }

    /**
     * @description: 是否分多批次，false全部一次性录入
     * @param: []
     * @return: boolean
     * @author: xdy
     * @create: 2019-10-23 10:49
     */
    @Override
    public boolean isMultiBatch(){return false;}
}
