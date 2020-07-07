package com.njwd.entity.platform.dto;

import com.njwd.entity.platform.vo.AccountBookTypeVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author lj
 * @Description 账簿类型
 * @Date:17:01 2019/6/25
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBookTypeDto extends AccountBookTypeVo {

    private static final long serialVersionUID = -2813673038755180468L;
}
