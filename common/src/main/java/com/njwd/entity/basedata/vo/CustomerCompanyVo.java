package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.CustomerCompany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @Author: wuweiming
 * @Date: 2019-09-18 21:24
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomerCompanyVo extends CustomerCompany {
    /**
     * @description: 使用公司名称
     * @author wuweiming
     * @date 2019/8/19 17:40
     */
    private String useCompanyName;

    /**
     * @description: 使用公司名称
     * @author wuweiming
     * @date 2019/8/19 17:40
     */
    private String useCompanyIdString;
}
