package com.njwd.common;

import java.util.regex.Pattern;

public interface PlatformConstant {

    interface Character {
        Integer ZERO_I = 0;
        Integer ONE_I = 1;
        Integer TWO_I = 2;
        Integer THREE_I = 3;
        Integer TEN_I = 10;
        Byte THREE_B = 3;
        String COLON = ":";
        String ASTERISK = "*";
        String POINT = ".";
        String COMMA = ",";
        String EMPT_STR = "";
        Long ONE_L = 1l;
    }

    /**
     * 运营平台编码规则
     */
    interface PlatformCodeRule {
        //核算账簿分类    FL+账簿类型编码+3位流水号
        String ACCOUNTING_BOOK_TYPE = "FL";
        //会计准则   ZZ+2位流水号
        String ACCOUNTING_STANDARD = "ZZ";
        //科目表   KMB+会计准则最后2位编码+2位流水号
        String SUBJECT_TABLE = "KMB";
        int SUBJECT_TABLE_SERIAL_LENGTH = 2;
        //现金流量项目表    XMB+会计准则最后2位编码+2位流水号
        String CASH_FLOW = "XMB";
        //财务报表  　BB+会计准则最后2位编码+报表类型编码+2位流水号
        String FINANCIAL_REPORT = "BB";
        //税收制度    SZ + 2位流水号
        String TAX_SYSTEM = "SZ";
        //会计日历   2位流水号
        String ACCOUNTING_CALENDAR = "RL";
        //会计要素   2位流水号
        String ACCOUNT_ELEMENT = "YS";
        //核算项目    HSXM+3位流水号
        String ACCOUNTING_ITEMS = "HSXM";
        //币种    BZ+3位流水号
        String CURRENCY = "BZ";
        //常用摘要   ZY+3位流水号
        String COMMON_ABSTRACT = "ZY";
        //凭证字   PZZ+3位流水号
        String CREDENTIAL_WORD = "PZZ";
        //结算方式   JSFS+3位流水号
        String SETTLEMENT_METHOD = "JSFS";
        //银行类别    YH+3位流水号
        String BANK_CATEGORY = "YH";
        //费用项目   FYXM+3位流水号
        String COST_ITEM = "FYXM";
        //报表项目 BBXM+报表类型编码+4位流水号
        String FINANCIAL_REPORT_ITEM = "BBXM";
        int FINANCIAL_REPORT_ITEM_LENGTH = 4;
    }

    /**
     * 操作类型 0 删除 1 审核 2 反审核 3 发布
     */
    interface OperateType {
        Integer DELETE = 0;
        Integer APPROVED = 1;
        Integer DISAPPROVED = 2;
        Integer RELEASED = 3;
    }

    /**
     * 舍入类型
     */
    interface RoundingType {
        String ZERO = "四舍五入";
        String ONE = "向上舍入";
        String TWO = "向下舍入";
        String THREE = "四舍六入五成双";
    }

    /**
     * 是否增值税
     */
    interface IsVat {
        String NO = "否";
        String YES = "是";
    }

    /**
     * 对应审核状态中文
     */
    interface ApprovedStatus {
        String NO = "未审核";
        String YES = "已审核";
    }

    /**
     * 对应发布状态中文
     */
    interface ReleasedStatus {
        String NO = "未发布";
        String YES = "已发布";
    }

    /**
     * 现金流量
     */
    interface CashFlow {
        Integer CODE_LENGTH = 2;
        String MAX_LEVEL_PRE = "1";
        String MAX_LEVEL_CONCAT = "-2";
    }

    /**
     * 参数校验规则
     **/
    interface ParamRule {

        /**
         * 允许的科目最大级次
         */
        Byte[] ALLOW_SUBJECT_LEVEL = {3, 4, 5, 6};

        /**
         * 一个会计科目表最多启用50个核算项目
         **/
        int MAX_AUXILIARIES = 50;

        /**
         * 一个会计科目最多使用10个核算项目
         **/
        int MAX_AUXILIARY_USED = 10;
    }

    /**
     * 科目表常量
     **/
    interface Subject {
        String MAX_LEVEL_PRE = "4";
        String MAX_LEVEL_CONCAT = "-2";
    }

    /**
     * 现金流量项目
     */
    interface CashFlowItem {
        Pattern PATTERN = Pattern.compile("[1-9]");
        Pattern PATTERN_TWO = Pattern.compile("[0-9][0-9]");
    }

    /**
     * 税率
     */
    interface TaxRate {
        String PERCENT = "%";
    }

    /**
     * 引用关系
     */
    interface Reference {
        /**
         * 会计准则
         */
        String PLAT_ACCOUNTING_STANDARD = "plat_accounting_standard";
        /**
         * 会计科目表（基准表）
         */
        String PLAT_SUBJECT_TABLE = "plat_subject_table";
        /**
         * 会计科目
         */
        String PLAT_ACCOUNT_SUBJECT = "plat_account_subject";
        /**
         * 现金流量项目表
         */
        String PLAT_CASH_FLOW = "plat_cash_flow";
        /**
         * 现金流量项目
         */
        String PLAT_CASH_FLOW_ITEM = "plat_cash_flow_item";
        /**
         * 税收制度
         */
        String PLAT_TAX_SYSTEM = "plat_tax_system";
        /**
         * 税种
         */
        String PLAT_TAX_CATEGORY = "plat_tax_category";
        /**
         * 会计日历
         */
        String PLAT_ACCOUNTING_CALENDAR = "plat_accounting_calendar";
        /**
         * 会计要素
         */
        String PLAT_ACCOUNT_ELEMENT = "plat_account_element";
        /**
         * 科目类别
         */
        String PLAT_SUBJECT_CATEGORY = "plat_subject_category";
        /**
         * 币种
         */
        String PLAT_CURRENCY = "plat_currency";
        /**
         * 银行类别
         */
        String PLAT_BANL_CATEGORY = "plat_bank_category";
        /**
         * 计量单位
         */
        String PLAT_UNIT = "plat_unit";
        /**
         * 财务报表
         */
        String PLAT_FINANCIAL_REPORT = "plat_financial_report";
        /**
         * 财务报表项目库
         */
        String PLAT_FINANCIAL_REPORT_ITEM = "plat_financial_report_item";
        /**
         * 财务报表的财务报表项目
         */
        String PLAT_FINANCIAL_REPORT_ITEM_SET = "plat_financial_report_item_set";
        /**
         * 用户
         */
        String PLAT_USER = "plat_user";
        /**
         * 核算项目
         */
        String PLAT_AUXILIARY_ITEM = "plat_auxiliary_item";

    }

    /**
     * 租户管理模块
     */
    interface EnterpriseAnduserManage {
        // 系统 code
        String SYSTEM_CODE = "finance";
        // 服务者给定值
        String ROOT_ENTERPRISE_ID = "572";

        // 登录 url
        String LOGIN_URL = "financeUserApi.do?method=doLogin&ajaxUrl=y";

        // 分页查询开通财务系统的租户列表 url
        String FIND_ENTERPRISE_LIST_FOR_PAGE = "financeUserApi.do?method=toFindEnterpriseListForPage&ajaxUrl=y";

        //
        String FIND_ENTERPRISE_BY_ID = "financeUserApi.do?method=toFindUser&ajaxUrl=y";

        // 新增用户
        String ADD_USER = "financeUserApi.do?method=doAddUser&ajaxUrl=y";
        // 修改用户
        String UPDATE_USER = "financeUserApi.do?method=doUpdateUser&ajaxUrl=y";
        // 分配用户
        String ASSIGN_ENTERPRISE = "financeUserApi.do?method=doUpdateAssignUser&ajaxUrl=y";
        // 分页查询运维用户列表
        String USER_PAGE = "financeUserApi.do?method=toFindUserListForPage&ajaxUrl=y";
        // 查询全部运维用户列表
        String USER_ALL = "financeUserApi.do?method=toFindUserList&ajaxUrl=y";
        // 查询具体的用户信息
        String TO_FIND_USER = "financeUserApi.do?method=toFindUser&ajaxUrl=y";
        // 删除用户：
        String DEL_USERS = "financeUserApi.do?method=doDeleteUser&ajaxUrl=y";

        String BASEDATA_ADD_USER = ":8183/financeback/sysUser/httpAddUserBatch";

    }

    interface ShiroConfig {
        /**
         * Redis 缓存 key 前缀
         */
        String CACHE_KEY_PREFIX = "platformShiro:cache:";
        /**
         * Redis Session key 前缀
         */
        String SESSION_KEY_PREFIX = "platformShiro:session:";
        /**
         * 从请求头中获取sessionId,区分平台的token和当前系统的token
         */
        String SESSION_ID_KEY = "Authorization";
        /**
         * session 过期时间
         */
        int EXPIRE = 60 * 60 * 4;
        /**
         * AuthCacheKey 租户id:用户id:用户类型:adminType
         */
        String AUTH_CACHE_KEY = "%d:%d:%d";
        /**
         * AuthCacheKey删除租户下的缓存
         */
        String AUTH_CACHE_KEY_REMOVE = CACHE_KEY_PREFIX + Constant.Character.ASTERISK + "%s" + Constant.Character.ASTERISK;
        /**
         * 统一redis使用的库
         **/
        int DATABASE = 4;
    }

	/**
	 * Service缓存value
	 */
	interface RedisCache {
		/**
		 * 币种
		 */
		String CURRENCY = "platformCurrency";

		/**
		 * 凭证字
		 */
		String CREDENTIAL_WORD = "platformCredentialWord";

		/**
		 * 常用摘要
		 */
		String COMMON_ABSTRACT = "platformCommonAbstract";

		/**
		 * 税制
		 */
		String TAX_SYSTEM = "platformTaxSystem";

		/**
		 * 税种
		 */
		String TAX_CATEGORY = "platformTaxCategory";

		/**
		 * 税率
		 */
		String TAX_RATE = "platformTaxRate";
	}

    /**
     * 返回结果 success：成功，fail：业务返回的失败，error：非业务异常失败
     */
    interface ResResult {
        String SUCCESS = "success";
        String FAIL = "fail";
        String ERROR = "error";

        String ALL = "all";

        String DATA = "data";
    }

    /**
     * 返回结果状态码
     */
    interface StatusCode {
        String EXIT = "2";
        String EXIT_TWO = "2";
        String EXIT_ONE = "1";
    }

    /**
     * 状态
     */
    interface Status{
        String SUCCESS = "success";
        String FAIL = "fail";
    }


    /**
     * 调用Core接口新增用户，所需参数
     */
    interface AddType {
        String ADD = "add";
        String ADD_UPDTE = "add_update";
    }

    /**
     * 批量处理表名
     */
    interface TableName {
        String FINANCIAL_REPORT = "wd_financial_report";
        String CASH_FLOW_ITEM = "wd_cash_flow_item";
        String CASH_FLOW = "wd_cash_flow";
        String FINANCIAL_REPORT_ITEM = "wd_financial_report_item";
        String FINANCIAL_REPORT_ITEM_SET = "wd_financial_report_item_set";
        String CURRENCY = "wd_currency";
        String CREDENTIAL_WORD = "wd_credential_word";
        String COMMON_ABSTRACT = "wd_common_abstract";
        String TAX_SYSTEM = "wd_tax_system";
        String TAX_CATEGORY = "wd_tax_category";
        String TAX_RATE = "wd_tax_rate";
        String AUXILIARY_ITEM = "wd_auxiliary_item";
        String UNIT = "wd_unit";
        String COST_ITEM = "wd_cost_item";
        String ACCOUNTING_CALENDAR = "wd_accounting_calendar";
        String ACCOUNT_BOOK_CATEGORY = "wd_account_book_category";
        String SUBJECT_SYNERGY = "wd_subject_synergy";
    }

    /**
     * menu:查询非按钮菜单权限;button:按钮权限
     */
    interface SelectType{
        String MENU = "menu";
        String BUTTON = "button";
    }

    /**
     * 消息类型 0系统公告1资料更新
     */
    interface MessageType{
        Byte SYSTEM_NOTICE = 0;
        Byte DATA_UPDATE = 1;
    }

}
