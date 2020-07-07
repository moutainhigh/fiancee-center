package com.njwd.exception;

/**
 * feign Client异常信息
 *
 * @author xyyxhcj@qq.com
 * @since 2019-09-16
 */

public interface FeignClientErrorMsg {
	String GET_VOUCHER_CODE_ERROR = "未获取到凭证号";
	String GET_SUBJECT_DATA_ERROR = "未获取到会计科目数据";
	String GET_AUXILIARY_DATA_ERROR = "未获取到辅助核算资料";
	String GET_COMPANY_DATA_ERROR = "未获取到公司数据";
	String GET_ACCOUNT_SYS_INIT_DATA_ERROR = "未获取到账簿启用子系统记录";
	String GET_ACCOUNT_PERIOD = "未获取到需要的会计期间！";
	String GET_ACCOUNT_BOOK = "未获取到需要的账簿数据！";
	String GET_ACCOUNT_ENTITY_ERROR = "未获取到账簿下的核算主体数据！";
}
