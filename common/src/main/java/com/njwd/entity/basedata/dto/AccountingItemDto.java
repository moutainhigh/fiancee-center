package com.njwd.entity.basedata.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 自定义核算项目Dto 接收参数用
 * @Author 薛永利
 * @Date 2019/6/26 14:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingItemDto extends AccountingItemVo {

    private static final long serialVersionUID = 3249786861228776113L;
    private Page<AccountingItemVo> page = new Page<>();
    private String codeOrName;
    /**
     * 批量修改时传参
     */
    private List<Long> ids;
    /**
     * 批量导出时传参
     */
    private String exportIds;
    /**
     * 是否是租户管理员 0：否 1：是
     */
    private Byte isEnterpriseAdmin;
    /**
     * 版本号批量传参
     */
    private List<Integer> versions;
}
