package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBook;
import com.njwd.entity.basedata.vo.AccountBookVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

/**
 * 核算账簿
 * 、
 * @Author: Zhuzs
 * @Date: 2019-05-16 17:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBookDto extends AccountBook {
    private static final long serialVersionUID = -6274218085013554433L;

    /**
     * 账簿分页信息
     */
    Page<AccountBookVo> page = new Page<>();

    /**
     * 根据idSet查询
     */
    private Set<Long> idSet;

    /**
     * 核算账簿ID 集合
     */
    private List<Long> accountBookIdList;

    /**
     * 核算账簿ID 集合
     */
    private List<Long> idList;

    /**
     * 账簿编码或名称
     */
    private String codeOrName;

    /**
     * 公司是启用否分账核算 0：否；1：是
     */
    private Byte companyHasSubAccount ;

    /**
     * 子系统集合
     */
    List<AccountBookSystemDto> accountBookSystemDtoList;

    /**
     * 总帐启用状态 0:未启用;1:已启用
     */
    Byte ledgerStatus;

    /**
     * 资产启用状态 0:未启用;1:已启用
     */
    Byte assetsStatus;

    /**
     * 应收启用状态 0:未启用;1:已启用
     */
    Byte receivableStatus;

    /**
     * operator ID
     */
    private Long userId;

    /**
     * 是否是默认核算主体 0：否，1：是
     */
    private Byte isDefault;

    /**
     * 前端菜单code
     */
    private String menuCode;

    /**
     * 标识是否后台管理界面 0：否 1：是
     */
    private Byte isEnterpriseAdmin;

}
