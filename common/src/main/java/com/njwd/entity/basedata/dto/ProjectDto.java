package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.basedata.vo.ProjectVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
* @description: 项目dto
* @author LuoY
* @date 2019/8/20 13:41
*/
@Data
@EqualsAndHashCode(callSuper=false)
public class ProjectDto extends ProjectVo {
    private static final long serialVersionUID = 3869947828917839946L;
    private Page<ProjectVo> page = new Page<>();

    /**
     * 项目编号或名称
     */
    private String codeOrName;

    /**
     * 批量删除id
     */
    private List<Long> ids;

    /**
     * 批量处理数据的版本号
     */
    private List<Integer> versionIds;

    /**
     * 归属公司ids
     */
    private List<Long> companyIds;

    /**
     * 分配使用的公司ids
     */
    private List<Long> useCompanyIdArr;

    /**
     * 查询使用公司ids
     */
    private List<Long> useCompanyIds;

    /**
     * 是否管理员 0：否 1：是
     */
    private byte isEnterpriseAdmin;

    /**
     * 项目负责人员工编码
     */
    @ExcelCell(index = 6)
    private String personInChargeCode;

    /**
     * 创建公司编码
     */
    @ExcelCell(index = 0)
    private String createCompanyCode;

    /**
     * 项目负责部门编码
     */
    @ExcelCell(index = 4)
    private String departmentCode;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 前端菜单code
     */
    private String menuCode;

    /**
     * 资料类型 1.共享型,2.分配型,3.私有型，用于跟新比对
     */
    private Byte oldDataType;
}
