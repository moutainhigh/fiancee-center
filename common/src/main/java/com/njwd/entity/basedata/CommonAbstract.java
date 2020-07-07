package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
@Getter
@Setter
public class CommonAbstract implements Serializable {
	/**
	 * 主键 默认自动递增
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 租户ID
	 */
	private Long rootEnterpriseId;

	/**
	 * 创建公司ID
	 */
	private Long createEnterpriseId;

	/**
	 * 使用公司ID 0：共享、非0：使用公司ID
	 */
	private Long useEnterpriseId;

	/**
	 * 摘要内容
	 */
	private String abstractContent;

	/**
	 * 助记码 默认空字符串
	 */
	private String abstractCode;

	/**
	 * 是否删除 0：否、1：是
	 */
	private Byte isDel;

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

	private static final long serialVersionUID = 1L;

	/**
	 * 平台ID
	 */
	private Long platformId;

}
