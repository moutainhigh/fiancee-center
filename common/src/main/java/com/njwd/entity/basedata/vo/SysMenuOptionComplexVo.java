package com.njwd.entity.basedata.vo;

import com.njwd.entity.platform.vo.SysMenuOptionVo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/19 9:44
 */
@Getter
@Setter
public class SysMenuOptionComplexVo implements Serializable {

    List<SysMenuOptionVo> globalMenuOptionList;

    List<SysMenuOptionVo> localMenuOptionList;

}
