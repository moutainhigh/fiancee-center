package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author 周鹏
 * @Description 科目表辅助核算项
 * @Date:14:16 2019/8/23
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectAuxiliaryDto<T> extends SubjectAuxiliaryVo {
    private static final long serialVersionUID = 6684189748516136175L;

    private Page<T> page = new Page<>();

    /**
     * 会计科目id
     */
    private Long accountSubjectId;

    /**
     * 辅助核算编码拼接
     */
    private String codes;

    /**
     * 启用的科目表辅助核算项集合
     */
    List<SubjectAuxiliaryDto> enableList;

    /**
     * 取消之前已启用的科目表辅助核算项集合
     */
    List<SubjectAuxiliaryDto> disableList;

}