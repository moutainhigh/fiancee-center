package com.njwd.entity.basedata.vo;


import com.njwd.annotation.ExcelCell;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.Dept;
import com.njwd.entity.basedata.DeptUseCompany;
import com.njwd.entity.basedata.ReferenceDescription;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * 部门
 *
 * @author jds
 * @create 2019-06-11 14:57
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeptVo extends Dept {

    /**
     *  是否被引用
     **/
    private Byte isUsed;

    /**
     *  上级部门数据状态
     **/
    private Byte parentIsEnable;

    /**
     *  被引用的详情
     **/
    private List<DeptVo> useList;


    /**
     *  版本列表
     **/
    private List<Integer> versionList;


    /**
     *  变更历史数量
     **/
    private Integer changeCount;


    /**
     *  查询条件
     **/
    private Integer queryConditions;


    /**
     *  被引用的说明
     **/
    private List<ReferenceDescription> references;


    /**
     *  使用公司变更历史
     **/
    private List<DeptUseCompany> DeptUseCompanyList;


    /**
     *  上级IDS
     **/
    private List<Long> preIds;

    /**
     * 删除条数
     **/
    private int delCount;

    /**
     *  被引用条数

     **/
    private int useCount;


    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    /**
     * 上级部门编码
     */
    @ExcelCell(index = 6)
    private String prarentCode;

    /**
     * 上级部门名称
     */
    @ExcelCell(index = 7)
    private String prarentName;



    /**
     * 业务单元名称
     */
    private String businessUnitName;

    /**
     * 使用公司名称
     */
    private String useCompanyName;

    /**
     * 创建公司名称
     */
    @ExcelCell(index = 1)
    private String companyName;


    /**
     * 创建公司编码
     */
    @ExcelCell(index = 0)
    private String companyCode;



    /**
     * 创建公司业务单元名称
     */
    @ExcelCell(index = 3)
    private String attrBusinessUnitName;

    /**
     * 创建公司业务单元编码
     */
    @ExcelCell(index = 2)
    private String attrBusinessUnitCode;


    /**
     * 历史表ID
     */
    private Long usedId;



    /**
     * 数据状态
     */
    private String status;

    /**
     * 变更人ID
     */
    private Long changePersonId;

    /**
     * 变更人name
     */
    private String changePersonName;

    /**
     * 变更时间
     */
    private Date changeTime;

    /**
     * 数据状态
     */
    private String isEnableStr;

    /**
     * 禁用人
     */
    private String disabledUserName;

    /**
     * 禁用时间
     */
    private Date disabledTime;
    /**
     * 启用人
     */
    private String enabledUserName;

    /**
     * 启用时间
     */
    private Date enabledTime;


    public String getDisabledUserName() {
        if (super.getManageInfo() == null) {
            return null;
        }
        return super.getManageInfo().getDisabledUserName();
    }

    public Date getDisabledTime() {
        if (super.getManageInfo() == null) {
            return null;
        }
        return super.getManageInfo().getDisabledTime();
    }

    public String getEnabledUserName() {
        if (super.getManageInfo() == null) {
            return null;
        }
        return super.getManageInfo().getEnabledUserName();
    }

    public Date getEnabledTime() {
        if (super.getManageInfo() == null) {
            return null;
        }
        return super.getManageInfo().getEnabledTime();
    }


    public String getIsEnableStr() {
        return this.getIsEnable() != null && Constant.Is.YES.equals(this.getIsEnable()) ? "已生效" : "已失效";
    }

    private static final long serialVersionUID =1L;
}
