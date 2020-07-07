package com.njwd.entity.ledger.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author lj
 * @Description 总账初始化校验
 * @Date:11:25 2019/8/12
 **/
@Data
public class BalanceInitCheckVo {
    /**
     *检查项
     **/
    private String checkContext;
    /**
     *状态
     **/
    private String checkStatus;
    /**
     *检查结果集合
     **/
    private List<String> description;

    /**
     *检查结果
     **/
    private String checkDescription;
}
