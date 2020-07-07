package com.njwd.financeback.controller;


import com.njwd.basedata.cloudclient.SysTabColumnFeignClient;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.SysMenuTabColumn;
import com.njwd.entity.basedata.dto.TableConfigComplexDto;
import com.njwd.entity.basedata.dto.TableConfigCreateDto;
import com.njwd.entity.basedata.dto.query.TableConfigQueryDto;
import com.njwd.entity.basedata.vo.SysMenuTabColumnVo;
import com.njwd.entity.platform.dto.SysTabColumnDto;
import com.njwd.entity.platform.vo.SysTabColumnVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.TableConfigService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 表格配置Controller
 * @author: fancl
 * @create: 2019-05-21
 */
@RestController
@RequestMapping("tableConfig")

public class TableConfigController extends BaseController {
    @Autowired
    SenderService senderService;
    @Autowired
    TableConfigService tableConfigService;

    @Autowired
    SysTabColumnFeignClient sysTabColumnFeignClient;


    @RequestMapping("findList")
    public Result findList(@RequestBody TableConfigQueryDto queryDto) {
        //检验参数
        FastUtils.checkParams(queryDto.getMenuCode(), queryDto.getIsEnterpriseAdmin());
        //组装参数
        SysTabColumnDto platformSysTabColumnDto = new SysTabColumnDto();
        platformSysTabColumnDto.setMenuCode(queryDto.getMenuCode());
        platformSysTabColumnDto.setIsEnterpriseAdmin(queryDto.getIsEnterpriseAdmin());
        //调运营平台接口
        Result<List<SysTabColumnVo>> recJsonStr = sysTabColumnFeignClient.findSysTabColumnList(platformSysTabColumnDto);
        List<SysTabColumnVo> columnVos = recJsonStr.getData();
        List<SysMenuTabColumnVo> globalConfigList = new ArrayList<>();
        for(SysTabColumnVo columnVo:columnVos){
            SysMenuTabColumnVo sysMenuTabColumn = new SysMenuTabColumnVo();
            FastUtils.copyProperties(columnVo,sysMenuTabColumn);
            globalConfigList.add(sysMenuTabColumn);
        }
        //list为null说明运营平台返回码非200
        if (globalConfigList == null) {
            throw new ServiceException(ResultCode.PLATFORM_FAILURE);
        }
        //转换为对象
        List<SysMenuTabColumnVo> localConfigList = tableConfigService.findList(queryDto);
        //查询方案不存在时
        if((localConfigList==null||localConfigList.isEmpty())&&queryDto.getSchemeId()!=null){
            queryDto.setSchemeId(-1L);
            localConfigList = tableConfigService.findList(queryDto);
        }
        TableConfigComplexDto complexDto = new TableConfigComplexDto();
        complexDto.setGlobalConfigList(globalConfigList);
        complexDto.setLocalConfigList(localConfigList);

        return ok(complexDto);
    }


    /**
     * 新增或修改表格
     *
     * @param configCreateDto
     * @return
     */
    @RequestMapping("batchUpdate")
    public Result batchUpdate(@RequestBody TableConfigCreateDto configCreateDto) {
        //校验非空
        FastUtils.checkParams(configCreateDto,configCreateDto.getTableConfigQueryDto(), configCreateDto.getTabColumnDtoList()
                , configCreateDto.getTableConfigQueryDto().getIsEnterpriseAdmin());
        //业务处理
        //修改标识为true时才进行内容修改
        if(Constant.IsHasUpdated.Yes.equals(configCreateDto.getTableConfigQueryDto().getUpdateFlag())){
            tableConfigService.batchUpdate(configCreateDto.getTableConfigQueryDto(), configCreateDto.getTabColumnDtoList());
        }

        return ok("");
    }

    /**
     *  获取表格设置选项
     * @param queryDto
     * @return
     */
    @RequestMapping("findUserList")
    public Result<List<SysMenuTabColumn>> findUserList(@RequestBody TableConfigQueryDto queryDto){
        return ok(tableConfigService.findUserList(queryDto));
    }

}
