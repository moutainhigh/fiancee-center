package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SubjectSynergy;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/10/25
 */
@Getter
@Setter
public class SubjectSynergyVo extends SubjectSynergy {
	private String srcAccountSubjectCode;
	private String srcAccountSubjectName;
	private String destAccountSubjectCode;
	private String destAccountSubjectName;
	/**
	 * 会计科目表名
	 **/
	private String subjectName;
	/**
	 * 账簿类型名
	 **/
	private String accountBookTypeName;

}
