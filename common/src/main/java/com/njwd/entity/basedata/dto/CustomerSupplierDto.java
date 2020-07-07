package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.entity.basedata.vo.CustomerSupplierVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author zxm
 * @date 2019/6/13
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomerSupplierDto extends CustomerSupplierVo {

    /**
     * check重复后传给前台的重复字段
     */
    private Set<String> rcSet = new HashSet<>();

    /**
     *  分页信息
     */
    private Page<CustomerSupplierVo> page = new Page<>();

    /**
     *  查询条件 编码/名称
     */
    private String codeName;

    /**
     *  查询条件 多公司查询
     */
    private List<Long> companyList;

    /**
     *  删除ID 批量删除
     */
    private List<Long> idS;

    /**
     *  被引用的ID
     */
    private List<Long> citeIds;

    /**
     *   是否业务管理员 0：否 1：是
     */
    private Byte isEnterpriseAdmin;

    /**
     * 创建公司编码
     */
    private String createCompanyCode;



    /**
     * 数据类型 0：客户、1：供应商
     */
    private Byte dataType;

    /**
     * 编码集合
     */
    private List<String> codes;

    /**
     * 名称集合
     */
    private List<String> names;

    /**
     * 资料类型集合
     */
    private List<Byte> dataTypeList;

    /**
     * 版本号集合
     */
    private List<Integer> versions;

    /**
     * 删除时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date deleteTime;

    /**
     * 删除人ID
     */
    private Long deleteId;

    /**
     * 删除人姓名
     */
    private String deleteName;

}
