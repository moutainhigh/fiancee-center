package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.AccountingStandardVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 会计准则
 * @Date:15:34 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingStandardDto extends AccountingStandardVo {
    private static final long serialVersionUID = -8271579409998254055L;
    private Page<AccountingStandardVo> page = new Page<>();
    private String codeOrName;
    private List<String> areas;
}
