package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.SubjectAuxiliary;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author 周鹏
 * @Description 科目表辅助核算项
 * @Date:14:16 2019/8/23
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SubjectAuxiliaryVo extends SubjectAuxiliary {
    private static final long serialVersionUID = -5088483319146756190L;

    /**
     * 是否被使用 0:否 >0：是
     */
    private Integer ifUsed;
}