package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.vo.CommonAbstractVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 常用摘要 dto.
 * @Date 2019/7/25 9:48
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonAbstractDto extends CommonAbstractVo {

	private Page<CommonAbstractVo> page = new Page<>();

	/**
	 * 批量操作 常用摘要 id list
	 */
	private List<Long> idList;

	/**
	 * 查询使用公司id list
	 */
	private List<Long> companyIdList;

	/**
	 * 查询 摘要内容 或 助记码
	 */
	private String contentOrCode;

	/**
	 * 是否管理员 0:否 1:是
	 */
	private Byte isEnterpriseAdmin = Constant.Is.NO;

	/**
	 * user　用户id
	 */
	private Long userId;

	/**
	 * 账簿id
	 */
	private Long accountBookId;
}
