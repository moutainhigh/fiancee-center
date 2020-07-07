package com.njwd.entity.platform.vo;

import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.Subject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 科目表
 * @Date:15:42 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectVo extends Subject {
    private static final long serialVersionUID = 383049895556171721L;

    /**
     * 会计准则名称
     */
    private String accStandardName;
    /**
     * 账簿类型名称
     */
    private String accountTypeName;
    /**
     * 归属科目表
     */
    private String parentName;
    /**
     * 会计要素表名称
     */
    private String elementName;
    private List<AuxiliaryItemVo> auxiliaryItemList;

    public String getIsApprovedStr() {
        if (this.getIsApproved() == null) {
            return null;
        }
        //审核状态
        if (this.getIsApproved().equals(Constant.Is.YES)) {
            return PlatformConstant.ApprovedStatus.YES;
        } else {
            return PlatformConstant.ApprovedStatus.NO;
        }
    }

    public String getIsReleasedStr() {
        if (this.getIsReleased() == null) {
            return null;
        }
        //发布状态
        if (this.getIsReleased().equals(Constant.Is.YES)) {
            return PlatformConstant.ReleasedStatus.YES;
        } else {
            return PlatformConstant.ReleasedStatus.NO;
        }
    }

    public String getIsBaseStr() {
        if (Constant.Is.YES.equals(getIsBase())) {
            return Constant.CustomerSupplier.IS_INTERNAL_YES_NAME;
        } else {
            return Constant.CustomerSupplier.IS_INTERNAL_NO_NAME;
        }
    }
}
