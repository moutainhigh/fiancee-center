package com.njwd.financeback.service.impl;

import com.njwd.basedata.cloudclient.SysAuxDataFeignClient;
import com.njwd.entity.platform.SysAuxData;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.financeback.service.SysAuxDataService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 辅助资料
 * @author: zhuzs
 * @create: 2019/5/25
 */
@Service
public class SysAuxDataServiceImpl implements SysAuxDataService {

    @Resource
    private SysAuxDataFeignClient sysAuxDataFeignClient;

    /**
     * 根据类型查询辅助资料列表
     * @param dataType 形态：form
     *                 公司类型：company_type
     *                 纳税人资质：tax_qualification
     *                 账户类型：acc_type
     *                 账户用途：acc_usage
     * @return
     */
    @Override
    public List<SysAuxDataVo> findAuxDataList(SysAuxDataDto dataType) {
        FastUtils.checkParams(dataType);
        Result<List<SysAuxDataVo>> list = sysAuxDataFeignClient.findSysAuxDataListByType(dataType);
        List<SysAuxDataVo> sysAuxDataList = list.getData();
        FastUtils.checkNull(sysAuxDataList);
        return sysAuxDataList;
    }


    /**
     * 根据类型和名称或者编码  分页  查询辅助资料列表
     * @param dataTypeAndCodeOrName 形态：form
     *                 公司类型：company_type
     *                 纳税人资质：tax_qualification
     *                 账户类型：acc_type
     *                 账户用途：acc_usage
     *                 部门属性：dept_type
     * @return
     */
    @Override
    public Result findAuxDataListByCodeOrName(SysAuxDataDto dataTypeAndCodeOrName) {
        FastUtils.checkParams(dataTypeAndCodeOrName);
        return sysAuxDataFeignClient.findAuxDataPageByNameCodeType(dataTypeAndCodeOrName);
    }

    /**
     * 根据类型和名称    查询辅助资料表
     * @param dataTypeName 形态：form
     *                 公司类型：company_type
     *                 账簿类型：account_book_type
     *                 纳税人资质：tax_qualification
     *                 会计准则：accounting_standard
     *                 税制：tax_system
     *                 记账本位币：accounting_currency
     *                 账户类型：acc_type
     *                 账户用途：acc_usage
     *                 部门属性：dept_type
     * @return
     */
    @Override
    public Result findAuxDataByName(SysAuxDataDto dataTypeName) {
        FastUtils.checkParams(dataTypeName);
        return sysAuxDataFeignClient.findSysAuxDataListByNameType(dataTypeName);
    }


}
