package com.njwd.entity.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/11/14
 */
@Getter
@Setter
@ToString
public class SubjectAuxiliary {
    /**
    * 主键 默认自动递增
    */
    private Long id;

    /**
    * wd_subject表ID
    */
    private Long subjectId;

    /**
    * wd_auxiliary_item表ID
    */
    private Long auxiliaryId;
}
