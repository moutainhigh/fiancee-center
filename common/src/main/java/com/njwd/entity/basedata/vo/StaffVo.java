package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.Staff;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Zhuhc
 * @Date: 2019/6/12 9:18
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffVo extends Staff {

    private static final long serialVersionUID = 8274223730819908263L;

    /**
     * 部门
     */
    private String dept;

    /**
     * 归属公司
     */
    private String company;

    /**
     * 使用公司
     */
    private String userCompany;

    /**
     * 数据状态
     */
    private String dataStatus;

    /**
     * 用户名
     */
    private String userName;

    /**
     *  被引用数据
     **/
    private List<ReferenceDescription> references;

    /**
     * 操作成功条数
     **/
    private int dealCount;
    /**
     * 是否被引用 0 否 1是
     **/
    private int isDraw;
}
