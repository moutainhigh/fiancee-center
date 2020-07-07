package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SubjectCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author lj
 * @Description 科目类别VO
 * @Date:11:19 2019/8/22
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectCategoryVo extends SubjectCategory {
    /**
     * 会计要素表 ->名称
     */
    private String elementName;
    /**
     * 会计要素项 ->名称
     */
    private String elementItemName;
}
