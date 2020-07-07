package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Description 常用摘要
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wd_common_abstract")
public class CommonAbstract extends BaseModel implements Serializable {

	/**
	 * 租户id
	 */
	private Long rootEnterpriseId;

	/**
	 * 创建租户id
	 */
	private Long createEnterpriseId;

	/**
	 * 使用租户id
	 */
	private Long useEnterpriseId;

	/**
	 * 编码
	 */
	private String code;

	/**
	 * 摘要内容
	 */
	private String abstractContent;

	/**
	 * 助记码
	 */
	private String abstractCode;

	/**
	 * 审核状态 0：未审核、1：已审核
	 */
	private Byte isApproved;

	/**
	 * 发布状态 0：未发布、1：已发布
	 */
	private Byte isReleased;

	/**
	 * 管理信息
	 */
	private ManagerInfo manageInfo;

}
