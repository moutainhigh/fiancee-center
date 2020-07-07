package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
* @description: 项目分配使用公司
* @author LuoY
* @date 2019/8/19 11:17
*/
@Getter
@Setter
public class ProjectUseCompany extends BaseModel {

    /**
    * 主键id
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目id 【项目】表id
     */
    private Long projectId;

    /**
     * 使用公司id
     */
    private Long useCompanyId;

    /**
     * 是否删除
     */
    private Byte isDel;

    /**
     * 是否删除
     */
    private Integer version;
}
