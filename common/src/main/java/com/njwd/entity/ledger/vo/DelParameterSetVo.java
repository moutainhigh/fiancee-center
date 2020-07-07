package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.DelParameterSet;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @Author liuxiang
 * @Description 总账参数设置
 * @Date:9:27 2019/7/25
 **/
@Getter
@Setter
public class DelParameterSetVo extends DelParameterSet {
    private static final long serialVersionUID = 7387574611587581116L;
    /**
     * 损益类科目明细数据
     **/
    private Map<Long, Map<String, Object>> carrySubjects;
}
