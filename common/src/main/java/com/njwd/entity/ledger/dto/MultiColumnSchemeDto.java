package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.vo.MultiColumnSchemeVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 多栏账方案
 * @Date:16:08 2019/7/29
 **/
@Getter
@Setter
public class MultiColumnSchemeDto extends MultiColumnSchemeVo {
    private static final long serialVersionUID = 1487627074724020937L;

    private Page<MultiColumnSchemeVo> page = new Page<>();

    /**
     * 批量操作 多栏账方案 idList
     */
    private List<Long> idList;

    /**
     * 查询 多栏账方案 codeOrName
     */
    private String codeOrName;


    /**
     * 查询 多栏账方案 账簿codeOrName
     */
    private String bookCodeOrName;

    /**
     * 查询 多栏账方案 入参账簿ID集合
     */
    private List<Long> accountBookIds;

    /**
     * 新增 多栏账方案 schemeId
     */
    private Byte schemeId;

    /**
     * 新增 多栏账方案 direction
     */
    private Byte direction;

    /**
     * 新增 多栏账方案 项目编码
     */
    private String itemCode;

    /**
     * 新增 多栏账方案 项目名称
     */
    private String itemName;

}