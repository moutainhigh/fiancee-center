package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.common.Constant;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 总账参数设置
 * @author xyyxhcj@qq.com
 * @date 2019/10/16 14:21
 **/
@Data
public class ParameterSet {
	/**
	 * 主键 默认自动递增
	 */
	private Long id;

	/**
	 * 租户ID
	 */
	private Long rootEnterpriseId;

	/**
	 * 组类别编码 如GLC001
	 */
	private String groupCode;

	/**
	 * 组名
	 */
	private String groupName;

	/**
	 * 组描述
	 */
	private String groupDesc;

	/**
	 * 参数的唯一标识(租户下唯一) 对应原来列字段名
	 */
	@TableField("`key`")
	private String key;

	/**
	 * 子栏目的名字(组名前端写固定值)
	 */
	@TableField("`name`")
	private String name;

	/**
	 * 后端注释
	 */
	@TableField("`desc`")
	private String desc;

	/**
	 * 枚举出判断类型,根据不同的类型走不同的修改判断逻辑
	 */
	private Byte modifyType;

	/**
	 * 是否个性化 1是 0否(只能租户级设置)
	 */
	private Byte isPersonal;

	/**
	 * 序号 用于排序
	 */
	private Byte rowNum;

	/**
	 * 版本号
	 **/
	private Integer version = 0;
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 创建人ID
	 */
	private Long creatorId;

	/**
	 * 创建人
	 */
	private String creatorName;

	/**
	 * 修改时间
	 */
	private Date updateTime;

	/**
	 * 修改人ID
	 */
	private Long updatorId;

	/**
	 * 修改人
	 */
	private String updatorName;

	public ParameterSet() {
	}
}
