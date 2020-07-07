package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.CompanyVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 公司
 *
 * @Author: Zhuzs
 * @Date: 2019-05-16 17:16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class  CompanyDto extends CompanyVo {
    private static final long serialVersionUID = 5901419542295311165L;
    private Page<CompanyVo> page = new Page<>();
    private String codeOrName;
    private Long userId;
    /**
     * 前端菜单code
     */
    private String menuCode;
    /**
     * 标识是否后台管理界面 0：否 1：是
     */
    private Byte isEnterpriseAdmin;

    /**
     * 公司ID 集合
     */
    private List<Long> idList;
}
