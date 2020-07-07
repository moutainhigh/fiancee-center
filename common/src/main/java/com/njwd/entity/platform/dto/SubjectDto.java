package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.SubjectVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author lj
 * @Description //TODO
 * @Date:15:43 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectDto extends SubjectVo {
    private static final long serialVersionUID = 1081468739513934310L;
    private Page<SubjectVo> page = new Page<>();
    private Long[] accStandardIds;
    private Long[] elementIds;
    private String codeOrName;
    private Long[] auxiliaryIds;
    private List<SubjectDto> batchEditSubjects = new LinkedList<>();
    private MessageDto messageDto;
}

