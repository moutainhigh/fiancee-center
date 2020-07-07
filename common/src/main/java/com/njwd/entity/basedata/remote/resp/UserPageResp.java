package com.njwd.entity.basedata.remote.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 查询统一门户未引入的用户参数
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/27
 */
@Data
public class UserPageResp {
	/**
	 * status : success
	 * data : {"page":{"infoNum":10,"pageNo":1,"pageSize":10,"params":{"$ref":"$"},"totalPage":8,"totalRecord":78},"listData":[{"email":"","name":"12负者人","account":"ljx000","user_id":2284,"mobile":"12302111000"},{"email":"","name":"lvjunxi1234","account":"lvjunxi1234","user_id":15284,"mobile":"12302111123"},{"email":"","name":"lvjunxi1234","account":"lvjunxi1234","user_id":15284,"mobile":"12302111123"},{"email":"","name":"LV采购1","account":"ljx777","user_id":12931,"mobile":"12302000777"},{"email":"","name":"LV采购2","account":"ljx888","user_id":12932,"mobile":"12302000888"},{"email":"","name":"LV采购3","account":"ljx999","user_id":12933,"mobile":"12302000999"},{"email":"","name":"LV出纳1","account":"ljx444","user_id":12901,"mobile":"12302000444"},{"email":"","name":"LV出纳2","account":"ljx555","user_id":12902,"mobile":"12302000555"}]}
	 */

	private String status;
	private DataBean data;
	private String msg;
	@Data
	public static class DataBean {
		/**
		 * page : {"infoNum":10,"pageNo":1,"pageSize":10,"params":{"$ref":"$"},"totalPage":8,"totalRecord":78}
		 * listData : [{"email":"","name":"12负者人","account":"ljx000","user_id":2284,"mobile":"12302111000"},{"email":"","name":"lvjunxi1234","account":"lvjunxi1234","user_id":15284,"mobile":"12302111123"},{"email":"","name":"lvjunxi1234","account":"lvjunxi1234","user_id":15284,"mobile":"12302111123"},{"email":"","name":"LV采购1","account":"ljx777","user_id":12931,"mobile":"12302000777"},{"email":"","name":"LV采购2","account":"ljx888","user_id":12932,"mobile":"12302000888"},{"email":"","name":"LV采购3","account":"ljx999","user_id":12933,"mobile":"12302000999"},{"email":"","name":"LV出纳1","account":"ljx444","user_id":12901,"mobile":"12302000444"},{"email":"","name":"LV出纳2","account":"ljx555","user_id":12902,"mobile":"12302000555"}]
		 */

		private PageBean page;
		private List<ListDataBean> listData;
		@Data
		public static class PageBean {
			/**
			 * infoNum : 10
			 * pageNo : 1
			 * pageSize : 10
			 * params : {"$ref":"$"}
			 * totalPage : 8
			 * totalRecord : 78
			 */

			private int infoNum;
			private int pageNo;
			private int pageSize;
			private int totalPage;
			private int totalRecord;
		}
		@Data
		public static class ListDataBean {
			/**
			 * email :
			 * name : 12负者人
			 * account : ljx000
			 * user_id : 2284
			 * mobile : 12302111000
			 */

			private String email;
			private String name;
			private String account;
			@JsonProperty("user_id")
			private Long userId;
			private String mobile;
		}
	}
}
