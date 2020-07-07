package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.SubjectCategoryVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 科目类别dto
 * @Date:11:17 2019/8/22
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectCategoryDto extends SubjectCategoryVo {
    /** 用于分页 **/
    private Page<SubjectCategoryVo> page = new Page<>();
    /**
     * 会计要素表id 【会计要素】表id
     */
    private Long elementId;
    /**会计要素项id集合**/
    private List<Long> ids;
    /**
     * 会计要素表id集合
     */
    private List<Long> elementIds;
    /**
     * 编码 名称
     */
    private String codeOrName;

}
