package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.DeptVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 部门
 *
 * @author jds
 * @create 2019-06-11 14:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptDto extends DeptVo implements Serializable {

    private Page<DeptVo> page = new Page<>();

    private List<DeptDto> deptDtoListeptDto;
    /**
     * 部门编码或部门名称
     */
    private String deptCodeOrDeptName;

    private String nameOrCode;

    private List<Long> companyIdList;

    private List<Long> idList;

    /**
     * 部门关联的员工ID
     */
    private List<Long> staffIdList;
    /**
     * 被引用的ID
     */
    private List<Long> useIdList;

    /**
     * 上级部门等级
     */
    private Byte prarentLevel;

    /**
     * 执行操作权限
     */
    private String menuDefine;

    /**
     * 是否管理员  0：否 1：是
     */
    private Byte isEnterpriseAdmin;

    /**
     * 用户id
     */
    private Long userId;
}
