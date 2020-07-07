package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper;
import com.njwd.basedata.mapper.BaseCustomMapper;
import com.njwd.basedata.service.BaseCustomService;
import com.njwd.common.Constant;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @Description 共通代码的service
 * @Date 2019/8/15 16:00
 * @Author 朱小明
 */
@Service
public class BaseCustomServiceImpl implements BaseCustomService {

    @Resource
    private BaseCustomMapper baseCustomMapper;

    private Logger log = LoggerFactory.getLogger(BaseCustomServiceImpl.class);

    /**
     * 批量操作启用禁用
     * isEnable 1:启用 0:禁用
     *
     * @param entity 为要启用禁用表的实体类,使用DTO也可以
     */
    @Override
    public <T extends BaseModel> int batchEnable(@NotNull T entity, Byte isEnable, BaseMapper<T> mapper, List<ReferenceDescription> successDetailsList) {
        FastUtils.checkParams(entity.getBatchIds());
        entity.setTableName(TableInfoHelper.getTableInfo(entity.getClass()).getTableName());
        SysUserVo suv = UserUtils.getUserVo();
        ManagerInfo managerInfo = new ManagerInfo();
        if (Constant.Is.YES.equals(isEnable)) {
            managerInfo.setEnabledUserId(suv.getUserId());
            managerInfo.setEnabledUserName(suv.getName());
            managerInfo.setEnabledTime(new Date());
        } else {
            managerInfo.setDisabledUserId(suv.getUserId());
            managerInfo.setDisabledUserName(suv.getName());
            managerInfo.setDisabledTime(new Date());
        }
        entity.setManageInfos(managerInfo);
        List<Object> list = FastUtils.getManagerList(managerInfo);
        int result;
        if (Constant.Is.YES.equals(isEnable)) {
            result = baseCustomMapper.batchEnabled(entity, list);
        } else {
            result = baseCustomMapper.batchDisabled(entity, list);
        }
        if (null != successDetailsList) {
            //查询操作成功的信息详情
            List<T> successList = mapper.selectBatchIds(entity.getBatchIds());
            ReferenceDescription successDetail;
            for (T info : successList) {
                successDetail = new ReferenceDescription();
                successDetail.setInfo(info);
                successDetailsList.add(successDetail);
            }
        }
        return result;
    }

}
