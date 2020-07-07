package com.njwd.common;

import java.math.BigDecimal;

/**
 * 常量
 *
 * @author xyyxhcj@qq.com
 * @since 2018-08-26
 */

public interface Constant {

	/**
	 * 系统名相关
	 */
	interface Context {
		/**
		 * 基础资料系统的context
		 */
		String BASE_DATA = "financeback/";

		/**
		 * 基础资料系统的nacos注册服务名
		 */
		String BASE_DATA_FEIGN = "base-data";

		/**
		 * 基础资料系统的context
		 */
		String PLATFORM = "platform/";

		/**
		 * 运营平台系统的nacos注册服务名
		 */
		String PLATFORM_FEIGN = "platform";

		/**
		 * 总账系统的context
		 */
		String LEDGER = "ledger/";

		/**
		 * 总账系统的nacos注册服务名
		 */
		String LEDGER_FEIGN = "ledger";
	}

	/**
	 * 常用数值
	 */
	interface Number {
		Integer ZERO = 0;
		Integer ONE = 1;
		Integer TWO = 2;
		Integer THREE = 3;
		Integer FOUR = 4;
		Integer FIVE = 5;
		Integer SIX = 6;
		Integer SEVEN = 7;
		Integer EIGHT = 8;
		Integer NINE = 9;
		Integer TEN = 10;
		Integer LENGTH = 3;
		Integer ONEHUNDRED = 100;
		Long ZEROL = 0L;
		Long ONEL = 1L;
		Long COMPANY = 2L;
		Long TWOL = 2L;
		Long BUSINESS = 3L;
		Byte INITIAL = 1;
		Byte ANTI_INITLIZED = 0;
		Byte INITLIZED = 1;
		BigDecimal ZEROB = new BigDecimal(0);
		Double ZEROD = 0d;
		Byte ONEB = 1;
		Integer MINUS_ZERO = -1;
	}

	/**
	 * 当前环境 dev：开发, test：测试, prod：生产
	 */
	interface ProfileActive {
		String DEV = "dev";
		String TEST = "test";
		String PROD = "prod";
	}


	/**
	 * 常用字符
	 */
	interface Character {
		String GROUP_CODE = "0000";
		String QUESTION = "?";
		String EQUALS = "=";
		String AND = "&";
		String COLON = ":";
		String ASTERISK = "*";
		String POINT = ".";
		String COMMA = ",";
		String BRACKET_LEFT_B = "{";
		String ZERO = "00";
		String ONE = "001";
		String STRING_ZERO = "0";
		String NULL_VALUE = "";
		String UNDER_LINE = "_";
		String THROUGH_LINE = "-";
		String AND_CN= "与";
		String UNDER_LINE_ZERO = "_zero";
		String VIRGULE = "/";
		Byte FAIL = 0;
		Byte SUCCESS = 1;
		Byte IS_REFERENCE = 2;
		char ZERO_CHAR = '0';
	}

	/**
	 * 系统配置
	 */
	interface SysConfig {
		/**
		 * 登录验证所需key
		 */
		String SYSTEM_KEY = "njwd_finance";
		/**
		 * 同步锁自动超时时间(单位:秒)
		 */
		long REDIS_LOCK_TIMEOUT = 20;
		/**
		 * 凭证操作同步锁自动释放时间(单位:秒)
		 */
		long VOUCHER_LOCK_TIMEOUT = 60;
		/**
		 * 结账操作锁自动释放时间(单位:秒)
		 **/
		long SETTLE_ACCOUNT_LOCK_TIMEOUT = 60 * 20;
		/**
		 * 结账后是否允许修改现金流量
		 **/
		byte SETTLE_ALLOW_EDIT_CASH_FLOW = 1;
		/**
		 * 记录超时接口阈值
		 **/
		long LONG_TIME_THRESHOLD = 1000;
		/**
		 * 耗时日志打印
		 **/
		String LONG_TIME_LOG = "接口预警：{} 耗时：{}毫秒";
	}

	/**
	 * 请求url
	 */
	interface Url {
		/**
		 * 查询未引入的用户分页
		 */
		String FIND_NOT_IMPORT = "userApi.do?method=toFindUserListForPage&json_str={json}";
		String FINT_CUSTOMER_SUPPLIER_GLOBAL_ID = "customerSupplier.do?method=doAddByJson&json={json}";
	}

	/**
	 * 需要特殊处理的uri
	 **/
	interface RequestUri {
		String SETTLE = "/ledger/settle/settle";
		String CANCEL_SETTLE = "/ledger/settle/cancelSettle";
		String BALANCE_INIT = "/ledger/balanceInit/balanceInit";
		String BALANCE_DISINIT = "/ledger/balanceInit/balanceDisInit";
	}

	/**
	 * 同步锁key
	 */
	interface LockKey {
		/**
		 * 权限编码校验锁 %s code
		 */
		String SYS_MENU_CODE = "lock:sysMenuCode:%s";
		/**
		 * 岗位编码校验锁 %s:%s rootEnterpriseId:code
		 */
		String SYS_ROLE_CODE = "lock:sysRoleCode:%s:%s";
		/**
		 * 会计科目编码和名称校验锁 %s code name
		 */
		String ACCOUNT_SUBJECT = "lock:accountSubject:%s:%s";

		/**
		 * 会计科目编码校验锁 %s code
		 */
		String ACCOUNT_SUBJECT_CODE = "lock:accountSubjectCode:%s";

		/**
		 * 会计科目名称校验锁 %s name
		 */
		String ACCOUNT_SUBJECT_NAME = "lock:accountSubjectName:%s";

		/**
		 * 客户供应商校验锁 %s:%s:%s
		 * rootEnterpriseId:company_id:dataType
		 * 租户ID：公司_id：数据类型
		 */
		String CUSTOMER_SUPPLIER_UNIQUE = "lock:customer_supplier_unique:%s:%s:%s";

		/**
		 * 常用摘要 %s
		 * rootEnterpriseId
		 * 公司ID: 摘要内容
		 */
		String COMMON_ABSTRACT_UNIQUE = "lock:common_abstract_unique:%s:%s";

		/**
		 * 凭证-操作锁  %s:%s=账簿ID:凭证ID
		 */
		String VOUCHER = "lock:voucher:%s:%s";

		/**
		 * 凭证操作标记,表示该账簿有凭证正在处理,在进行结账时必须校验该标记不存在  %s:%s:%s=账簿ID:年度:期间
		 */
		String VOUCHER_OPER = "lock:voucherOper:%s:%s:%s";

		/**
		 * 凭证-账簿下的全局操作锁  %s=账簿ID
		 */
		String ACCOUNT_BOOK = "lock:accountBook:%s";

		/**
		 * 科目余额表 操作锁  :%s:%s:%s:%s=账簿ID:核算主体ID:年度:期间
		 */
		String BALANCE_SUBJECT = "lock:balanceSubject:%s:%s:%s:%s";

		/**
		 * 现金流量发生额表 操作锁  :%s:%s:%s:%s=账簿ID:核算主体ID:年度:期间
		 */
		String BALANCE_CASH_FLOW = "lock:balanceCashFlow:%s:%s:%s:%s";

		/**
		 * 总账参数设置锁  :%s=租户ID
		 */
		String PARAMETERSET = "lock:parameterSet:%s";

		/**
		 * 科目期初初始化 %s=核算主体ID
		 */
		String ACCOUNT_SUBJECT_INIT = "lock:accountSubjectInit:%s";

		/**
		 * 现金流量期初初始化 %s=核算主体ID
		 */
		String ACCOUNT_CASHFLOW_INIT = "lock:accountCashFlowInit:%s";

		/**
		 * 添加核算主体 %s=核算主体ID集合
		 */
		String BALANCE_INIT_RECORD = "lock:balanceInitRecord:%s";

		/**
		 * 公司间协同配置 %s=租户ID
		 **/
		String SUBJECT_SYNERGY = "lock:subjectSynergy:%s";

		/**
		 * 平台会计科目 %s=subjectId
		 **/
		String PLATFORM_ACC_SUBJECT = "lock:accountSubject:%s";

		/**
		 * 平台会计科目 %s=accStandardId
		 **/
		String PLATFORM_SUBJECT = "lock:subject:%s";
	}

	/**
	 * shiro配置
	 */
	interface ShiroConfig {
		/**
		 * Redis 缓存 key 前缀
		 */
		String CACHE_KEY_PREFIX = "backSysShiro:cache:";
		/**
		 * Redis Session key 前缀
		 */
		String SESSION_KEY_PREFIX = "backSysShiro:session:";
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
		String AUTH_CACHE_KEY_REMOVE = CACHE_KEY_PREFIX + Character.ASTERISK + "%s" + Character.ASTERISK;
		/**
		 * 统一redis使用的库
		 **/
		int DATABASE = 4;
	}

	/**
	 * 常用列名
	 */
	interface ColumnName {
		String CREATE_TIME = "create_time";
		String COMPANY_ID = "company_id";
		String USER_ID = "user_id";
		String ROOT_ENTERPRISE_ID = "root_enterprise_id";
		String MENU_ID = "menu_id";
		String IS_CHILD = "is_child";
		String CODE = "code";
		String IS_DEL = "is_del";
		String IS_FINAL = "is_final";
		String ROLE_ID = "role_id";
		String DATA_TYPE = "data_type";
		String NAME = "name";
		String TYPE = "type";
		String IS_ENABLE = "is_enable";
		String UNIFIED_SOCIAL_CREDIT_CODE = "unified_social_credit_code";
		String ID_CARD_NUM = "id_card_num";
		String CUSTOMER_TYPE = "customer_type";
		String ID = "id";
		String ACCOUNT_BOOK_ENTITY_ID = "account_book_entity_id";
		String ACCOUNT_BOOK_ID = "account_book_id";
		String APPROVE_STATUS = "approve_status";
		String REVIEW_STATUS = "review_status";
		String SOURCE_TYPE = "source_type";
		String DEPT_ID = "dept_id";
		String VERSION = "version";
		String CUSTOMERID = "customer_id";
	}

	/**
	 * 常用实体
	 */
	interface EntityName {
		String ID = "id";
		String CODE = "code";
		String NAME = "name";
		String UNIFIED_SOCIAL_CREDIT_CODE = "unifiedSocialCreditCode";
		String ID_CARD_NUM = "idCardNum";
	}

	/**
	 * 配置每个表匹配'或'的字段
	 */
	interface OrMatchColumn {
		String[] SYS_USER = {"name", "mobile"};
		String[] SYS_ROLE = {"name", "code"};
		String[] OPERATION_LOG_MATCH = {"mobile", "creator_name"};
		String[] PERIOD_NAME_CODE = {"account_book_name", "account_book_code"};
	}

	/**
	 * 拼接sql
	 */
	interface ConcatSql {
		String LIMIT_1 = "limit 1";
	}

	/**
	 * 常用属性
	 */
	interface PropertyName {
		/**
		 * 部门属性
		 */
		String DEPT_TYPE = "dept_type";
		String ACCOUNT_SUBJECT_AUXILIARY_LIST = "accountSubjectAuxiliaryList";
		String AUXILIARY_SOURCE_TABLE = "auxiliarySourceTable";
		String IS_INTERIOR = "isInterior";
		String ACCOUNT_CATEGORY = "accountCategory";
		String IS_ENABLE = "isEnable";
		String IS_DEL = "isDel";
		String IS_FINAL = "isFinal";
		String CASH_FLOW_DIRECTION = "cashFlowDirection";
		String CASH_INFLOW_ID = "cashInflowId";
		String CASH_OUTFLOW_ID = "cashOutflowId";
		String CODE = "code";
		String FULL_NAME = "fullName";
		String NAME = "name";
		String ID = "id";
		String BALANCE_DIRECTION = "balanceDirection";


		String IS_ENABLE_STRING = "已生效";
		String IS_DISABLE_STRING = "已失效";
	}

	/**
	 * 来源系统
	 **/
	interface SourceSystem {
		String LEDGER = "ledger";
	}

	/**
	 * 余额变更类型 1加 -1减
	 **/
	interface BalanceUpdateType {
		byte ADD = 1;
		byte SUBTRACT = -1;
	}

	/**
	 * 凭证新增日期设置类型：0系统日期 1上一张凭证日期
	 **/
	interface VoucherDateType {
		byte SYSTEM = 0;
		byte LAST_VOUCHER = 1;
	}

	/**
	 * 数据类型
	 **/
	interface VoucherDataType {
		String SUBJECT = "科目";
		String ARCHIVES = "档案";
		String CASH_FLOW_ITEM = "现金流量项目";
	}

	/**
	 * 分类code A01：现金科目 A02：银行科目 A03：现金等价物 B01：一般科目
	 * 以A开头为现金科目
	 **/
	interface AccountCategory {
		String A = "A";
		String CASH = "A01";
		String BANK = "A02";
		String VIK = "A03";
		String GENERAL = "B01";
	}

	/**
	 * 更新状态
	 */
	interface UpdateStatus {
		/**
		 * 删除处理
		 */
		int DELETE = 0;

		/**
		 * 启用处理
		 */
		int ENABLE = 1;

		/**
		 * 禁用处理
		 */
		int DISABLE = 2;
	}

	/**
	 * 返回结果 success：成功，fail：业务返回的失败，error：非业务异常失败
	 */
	interface ReqResult {
		String SUCCESS = "success";
		String FAIL = "fail";
		String ERROR = "error";
	}

	/**
	 * 凭证字类型设置 0：记 、1：收付转
	 **/
	interface CredentialWordSet {
		byte RECORD = 0;
		byte CASH_TRANSFER = 1;
	}

	/**
	 * 凭证字类型 1：记 、2：收、3：付、4：转
	 **/
	interface CredentialWordType {
		byte RECORD = 1;
		byte RECEIVE = 2;
		byte PAY = 3;
		byte TRANSFER = 4;
	}

	/**
	 * 级别
	 */
	interface Level {
		Byte ONE = 1;
		Byte TWO = 2;
		Byte THREE = 3;
		Byte FOUR = 4;
		Byte FIVE = 5;
		Byte SIX = 6;
		Byte SEVEN = 7;
		Byte EIGHT = 8;
	}


	/**
	 * 是否
	 */
	interface Is {
		Byte YES = 1;
		Byte NO = 0;
		Integer YES_INT = 1;
		Integer NO_INT = 0;
	}

	/**
	 * 是否核本部
	 */
	interface IsCompany {
		Byte YES = 1;
		Byte NO = 0;
		// 表示租户级数据
		Long GROUP_ID = 0L;
	}


	/**
	 * 主体类别
	 */
	interface EntityType {
		Byte ENTERPRISE = 1;
		Byte COMPANY = 0;
	}

	/**
	 * 子系统标识
	 */
	interface SystemSign {
		// 总账
		String LEDGER = "ledger";
		// 资产
		String ASSETS = "assets";
		// 应收
		String RECEIVABLE = "receivable";
	}

	/**
	 * 子系统标识Value
	 */
	interface SystemSignValue {
		// 总账
		String LEDGER = "ledger";
		// 资产
		String ASSETS = "asset";
		// 应收
		String RECEIVABLE = "receive";
	}

	/**
	 * 编码类型 0系统生成编码 1用户自定义编码
	 */
	interface CodeType {
		byte SYSTEMCODE = 0;
		byte USERDIYCODE = 1;
	}

	/**
	 * excel错误信息
	 */
	interface ExcelErrorMsg {
		String ERROR_LENGTH = "不能超过长度%d";
		String ERROR_EMPTY = "不能为空";
		String ERROR_INSERT = "数据入库异常:%s";
		String ERROR_CONVERT = "数据转换失败:%s";
	}

	/**
	 * 状态 0：关闭、1：打开
	 **/
	interface Status {
		byte OFF = 0;
		byte ON = 1;
	}

	/**
	 * shiro角色标识
	 */
	interface ShiroAdminDefi {
		/**
		 * 平台管理员
		 */
		String SYS_ADMIN = "sysAdmin";
		/**
		 * 租户管理员
		 */
		String ENTERPRISE_ADMIN = "enterpriseAdmin";
		/**
		 * 业务管理员
		 */
		String BUSINESS_ADMIN = "businessAdmin";
	}

	/**
	 * 管理员类型: 1平台管理员,2租户管理员
	 */
	interface AdminType {
		int SYS_ADMIN = 1;
		int ENTERPRISE_ADMIN = 2;
		Integer ADMIN_ENTERPRISE = new Integer(2);
	}

	/**
	 * 统一门户传递的用户类型：-1：一般员工、0：平台管理员、1：企业/租户管理员；
	 */
	interface CoreAdminType {
		long USER = -1;
		long ADMIN_ROLE = 0;
		long ENTERPRISE_ROLE = 1;
	}

	/**
	 * menu权限类型: 1目录 2模块 3菜单 4权限组 5按钮
	 */
	interface MenuType {
		byte CATALOG = 1;
		byte MODULE = 2;
		byte MENU = 3;
		byte GROUP = 4;
		byte BUTTON = 5;
	}

	/**
	 * 现金流量类型 0不需要指定现金流量 1未指定 2已指定
	 **/
	interface CashFlowType {
		byte NEEDLESS = 0;
		byte UNSPECIFIED = 1;
		byte SPECIFIED = 2;
	}

	/**
	 * 往来类型 0不需要生成协同凭证 1未生成 2已生成
	 **/
	interface InteriorType {
		byte NEEDLESS = 0;
		byte NOT_GENERATE = 1;
		byte GENERATE = 2;
	}

	/**
	 * 现金流量 方向 0：现金流出 1：现金流入
	 **/
	interface CashFlowDirection {
		byte OUT = 0;
		byte IN = 1;
	}

	/**
	 * 现金流量检查类型: -1 非现金类凭证 0 未检查 1 已检查
	 **/
	interface CashFlowCheckType {
		byte NEEDLESS = -1;
		byte UNEXAMINED = 0;
		byte EXAMINED = 1;
	}

	/**
	 * 内部往来平衡状态: -1 未启用分账核算
	 **/
	interface BalancedStatus {
		Byte NEEDLESS = new Byte((byte) -1);
	}

	interface ExcelConfig {
		/**
		 * excel缓存
		 */
		String EXCEL_KEY_PREFIX = "excel:cache:%s";
	}

	/**
	 * 客户供应商
	 */
	interface CustomerSupplier {
		/**
		 * 客户供应商类型：客户
		 */
		Byte CUSTOMER = 0;
		/**
		 * 客户供应商类型：供应商
		 */
		Byte SUPPLIER = 1;

		/**
		 * 企业类型：企业
		 */
		Byte ENTERPRISE = 0;

		/**
		 * 企业类型：企业
		 */
		String ENTERPRISE_NAME = "企业";

		/**
		 * 企业类型：个人
		 */
		Byte PERSONAL = 1;

		/**
		 * 企业类型：个人
		 */
		String PERSONAL_NAME = "个人";

		/**
		 * 是否：是
		 */
		Byte IS_INTERNAL_YES = new Byte((byte) 1);

		/**
		 * 是否：是
		 */
		String IS_INTERNAL_YES_NAME = "是";

		/**
		 * 是否：否
		 */
		Byte IS_INTERNAL_NO = new Byte((byte) 0);

		/**
		 * 是否：否
		 */
		String IS_INTERNAL_NO_NAME = "否";

		/**
		 * 统一社会信用代码匹配规则
		 */
		String REGEX_CREDIT_CODE = "^(\\d|[a-zABCDEFGHJKLMNPQRTUWXY]){18}$";

	}



	/**
	 * 基础资料系统编码规则
	 */
	interface BaseCodeRule {
		/**
		 * 客户 kh+四位流水号
		 */
		String CUSTOMER = "KH";

		/**
		 * 供应商 gys+四位流水号
		 */
		String SUPPLIER = "GYS";

		/**
		 * XM+公司编码+4位流水号
		 */
		String PROJECT = "XM";

		/**
		 * 公司编码+3位流水号
		 */
		String BUSINESS_UNIT = "business_unit";

		/**
		 * BM+公司编码+4位流水号
		 */
		String DEPT = "BM";

		/**
		 * 自定义核算 FZ+3位流水号
		 */
		String ACC_ITEM = "Z";

		/**
		 * 银行账号 BN+3位流水号
		 */
		String BANK_ACCOUNT = "BN";

		/**
		 * 岗位权限 GWQX+5位流水号
		 **/
		String ROLE = "GWQX";

		/**
		 * 公司间协同配置-租户
		 **/
		String SUBJECT_SYNERGY = "XTKM";

		/**
		 * 费用项目
		 */
		String COST_ITEM = "FYXM";

		/**
		 * 流水号补位：2位
		 */
		int LENGTH_TWO = 2;

		/**
		 * 流水号补位：3位
		 */
		int LENGTH_THREE = 3;

		/**
		 * 流水号补位：4位
		 */
		int LENGTH_FOUR = 4;

		/**
		 * 流水号补位：5位
		 */
		int LENGTH_FIVE = 5;

		/**
		 * 凭证流水号
		 */
		String VOUCHER_NO = "voucherNo";

		/**
		 * 凭证子流水号
		 */
		String VOUCHER_SUN_NO = "voucherSunNo";

		/**
		 * 带上核算主体
		 */
		Boolean IS_ADD_TRUE = true;

		/**
		 * 不带上核算主体
		 */
		Boolean IS_ADD_FALSE = false;

		/**
		 * 核算主体为企业
		 */
		Byte ENTERPRISE = 1;

		/**
		 * 核算主体为公司
		 */
		Byte COMPANY = 0;
	}

	/**
	 * 导入模板名称
	 */
	interface TemplateType {
		/**
		 * 会计科目
		 */
		String ACCOUNT_SUBJECT = "account_subject";

		/**
		 * 项目
		 */
		String PROJECT = "project";

		/**
		 * 银行账号
		 */
		String BANK_ACCOUNT = "bank_account";

	}

	/**
	 * 会计科目模块常量
	 */
	interface AccountSubjectData {
		/**
		 * 集团创建,集团共享
		 */
		Long GROUP_ID = Long.valueOf(0);

		/**
		 * 新增下级,导入excel
		 */
		String ADD_TYPE = "add";

		/**
		 * 引入模板
		 */
		String ADD_ROOT_TYPE = "addRoot";

		/**
		 * 最大级次
		 */
		String MAX_LEVEL = "4-2-2-2-2-2";

		/**
		 * 校验编码是否全为数字
		 **/
		String CHECK_ALL_NUM_REGEX = "^\\d{%s}$";

		/**
		 * 校验编码是否全为0
		 **/
		String CHECK_ALL_ZERO_REGEX = "^0{%s}$";

		/**
		 * 不允许使用1999科目
		 **/
		String[] NOT_ALLOW_CODES = {"1999"};

		/**
		 * base_acc_subject_id 以零标记自身
		 **/
		Long BASE_ACC_SUBJECT_ID_SELF = 0L;
	}

	/**
	 * 辅助资料code
	 */
	interface SysAuxDataCode {
		/**
		 * 会计科目分类
		 */
		String ACCOUNT_CATEGORY = "account_category";

		/**
		 * 类别属性
		 */
		String TYPE_ATTRIBUTE = "type_attribute";

		/**
		 * 账号币种
		 */
		String ACCOUNT_CURRENCY = "accounting_currency";

		/**
		 * 账号用途
		 */
		String ACCOUNT_USAGE = "acc_usage";
	}

	/**
	 * 账簿类型
	 */
	interface AccountBookType {
		Long ACCOUNT_BOOK_ID = 1L;
		String ACCOUNT_BOOK_NAME = "核算账簿";
	}

	/**
	 * 现金流入模块常量
	 */
	interface CashFlowItemData {
		/**
		 * 现金流入方向
		 */
		Byte INFLOW_DIRECTION = 1;

		/**
		 * 现金流入方向
		 */
		String CASH_CODE = "01";

		/**
		 * 现金流出方向
		 */
		Byte OUTFLOW_DIRECTION = 0;

		/**
		 * 一级会计科目等级
		 */
		Byte ROOT_LEVEL = 1;

		/**
		 * 预置数据  是
		 */
		Byte IS_INIT_YES = 1;


		/**
		 * 预置数据  否
		 */
		Byte IS_INIT_NO = 0;

		/**
		 * 集团创建,集团共享
		 */
		Long GROUP_ID = Long.valueOf(0);

		/**
		 * redis存储id（uuid）
		 */
		String UUID = "uuid";

		String CASHFLOWITEMADD = "cashFlowItemAdd";

		String CASHFLOWADD = "cashFlowAdd";

		String CASHFLOWITEMUPDATE = "cashFlowItemUpdate";

		String CASHFLOWITEMDELBATCH = "cashFlowItemDelBatch";

		String CASHFLOWITEMUPDATEBATCH = "cashFlowItemUpdateBatch";
	}

	interface Reference {
		/**
		 * 公司
		 */
		String COMPANY = "company";
		/**
		 * 业务单元
		 */
		String BUSINESS_UNIT = "business_unit";
		/**
		 * 岗位权限
		 */
		String ROLE = "role";
		/**
		 * 核算账簿
		 */
		String ACCOUNT_BOOK = "account_book";
		/**
		 * 账簿子系统
		 */
		String ACCOUNT_BOOK_SYSTEM = "account_book_system";
		/**
		 * 核算主体
		 */
		String ACCOUNT_BOOK_ENTITY = "account_book_entity";
		/**
		 * 会计科目
		 */
		String ACCOUNT_SUBJECT = "account_subject";
		/**
		 * 现金流量项目
		 */
		String CASH_FLOW_ITEM = "cash_flow_item";
		/**
		 * 部门
		 */
		String DEPT = "dept";
		/**
		 * 员工
		 */
		String STAFF = "staff";
		/**
		 * 自定义核算项目
		 */
		String ACCOUNTING_ITEM = "accounting_item";
		/**
		 * 自定义核算项目值
		 */
		String ACCOUNTING_ITEM_VALUE = "accounting_item_value";
		/**
		 * 银行账号
		 */
		String BANK_ACCOUNT = "bank_account";

		/**
		 * 客户供应商
		 */
		String CUSTOMER = "customer";

		String SUPPLIER = "supplier";

		/**
		 * 客户项目
		 */
		String PROJECT = "PROJECT";

		String TABLE_VOUCHER = "wd_voucher";
		String TABLE_VOUCHER_ENTRY = "wd_voucher_entry";
		String TABLE_VOUCHER_ENTRY_AUXILIARY = "wd_voucher_entry_auxiliary";
		String TABLE_VOUCHER_ENTRY_CASH_FLOW = "wd_voucher_entry_cash_flow";
	}

	/**
	 * 项目
	 */
	interface Project {
		/**
		 * 添加项目
		 */
		String PROJECT_CRUD_METHOD_ADDPROJECT = "addProject";
		/**
		 * 反禁用项目
		 */
		String PROJECT_CRUD_METHOD_ENABLEPROJECT = "updateProjectEnable";
		/**
		 * 禁用项目
		 */
		String PROJECT_CRUD_METHOD_DISABLEPROJECT = "updateProjectDisable";
		/**
		 * 删除项目
		 */
		String PROJECT_CRUD_METHOD_DELETEPROJECT = "deleteProject";
		/**
		 * 删除项目
		 */
		String PROJECT_CRUD_METHOD_UPDATEPROJECTINFO = "updateProjectInfo";
		/**
		 * 分配项目
		 */
		String PROJECT_CRUD_METHOD_UPDATEPROJECTUSECOMPANY = "updateProjectUseCompany";
		/**
		 * 取消分配项目
		 */
		String PROJECT_CRUD_METHOD_UNDOUPDATEPROJECTUSECOMPANY = "unDoUpdateProjectUseCompany";
		String PROJECT_CRUD_METHOD_UPGRADEPROJECTUSECOMPANY = "upgradeProjectDataType";
		/**
		 * 项目名称长度
		 */
		int PROJECT_NAME_LENGTH = 50;
		/**
		 * 项目名称长度
		 */
		String PROJECT_FILE_NAME = "基础资料项目导出结果";
		/**
		 * 手机号匹配规则
		 */
		String REGEX_PHONE_NUMBER = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
	}

	/**
	 * 员工
	 */
	interface Staff {
		/**
		 * 银行号匹配规则
		 */
		String REGEX_BANK_ACCOUNT = "^(\\d{16}|\\d{19})$";

		/**
		 * 邮箱匹配规则
		 */
		String REGEX_EMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		/**
		 * 员工姓名匹配规则  字母数字汉字
		 */
		String REGEX_STAFF_NAME = "^[A-Za-z0-9\u4e00-\u9fa5]+$";

		/**
		 * 身份证号匹配规则 18位数字，最后一位可为英文字母
		 */
		String REGEX_ID_CARD_NUMBER = "^\\d{17}([0-9]|X|x)$";
	}

	/**
	 * 银行账号批量操作类型
	 */
	interface OperateType {
		Integer DELETE = 0;
		Integer ENABLE = 1;
		Integer DISABLE = 2;
	}

	/**
	 * 归属公司、使用公司/归属业务单元、使用业务单元
	 */
	interface CompanyAndBusinessUnit {
		String COMPANY = "companyName";
		String USECOMPANYNAME = "useCompanyName";
		String ATTRBUSINESSUNITNAME = "attrBusinessUnitName";
		String BUSINESSUNITNAME = "businessUnitName";
		String DEPT_ID = "dept_id";
	}

	/**
	 * 删除、禁用
	 */
	interface IsDelOrDisable {
		String IS_DEL = "数据已删除";
		String DISABLE = "数据已禁用";
		String IS_ENABLE = "数据已启用";
	}

	/**
	 * 符号
	 */
	interface Symbol {
		String UNDERLINE = "_";
	}

	/**
	 * 内容是否有修改 true有修改  false 无修改
	 */
	interface IsHasUpdated {
		Boolean Yes = true;
		Boolean No = false;
	}

	/**
	 * Service缓存value
	 */
	interface RedisCache {
		/**
		 * 会计科目value
		 */
		String ACCOUNT_SUBJECT = "accountSubject";
		/**
		 * 客户供应商缓存value
		 */
		String CUSTOMER = "customer";

		String SUPPLIER = "supplier";
		/**
		 * 银行账号value
		 */
		String BANK_ACCOUNT = "bankAccount";

		/**
		 * 部门缓存value
		 */
		String DEPT = "dept";

		/**
		 * 自定义核算项目value
		 */
		String ACCOUNTING_ITEM = "accountingItem";

		/**
		 * 自定义核算项目区值value
		 */
		String ACCOUNTING_ITEM_VALUE = "accountingItemValue";

		/**
		 * 公司
		 */
		String COMPANY = "company";
		/**
		 * 业务单元
		 */
		String BUSINESS_UNIT = "businessUnit";
		/**
		 * 核算账簿
		 */
		String ACCOUNT_BOOK = "accountBook";

		/**
		 * 账簿子系统
		 */
		String ACCOUNT_BOOK_SYSTEM = "accountBookSystem";
		/**
		 * 员工缓存value
		 */
		String STAFF = "staff";
		/**
		 * 项目
		 */
		String PROJECT = "project";
		/**
		 * 权限缓存名
		 */
		String MENU_PERM = "menuPerm";

		/**
		 * 常用摘要
		 */
		String COMMON_ABSTRACT = "commonAbstract";

		/**
		 * 总账参数
		 */
		String PARAMETER_SET = "parameterSet";

		/**
		 * 总账参数
		 */
		String PARAMETER_SET_VALUE = "parameterSetValue";

		/**
		 * 多栏账方案
		 */
		String MULTI_COLUMN_SCHEME = "multiColumnScheme";

		/**
		 * 多栏账方案明细
		 */
		String MULTI_COLUMN_SCHEME_ITEM = "multiColumnSchemeItem";

		/**
		 * 推送
		 */
		String PUSH = "push";

		/**
		 * Redis key 分隔符（@Cacheable自动生成的分隔符）
		 */
		String redisSeparator = "::";
	}

	/**
	 * 权限标识
	 */
	interface MenuDefine {
		String BUSINESS_UNIT_FIND = "glAdmin:userAuth:bussUnit:comm:find";
		String BUSINESS_UNIT_UPDATE = "glAdmin:userAuth:bussUnit:comm:edit";

		/**
		 * 银行账号（查询  编辑  删除  禁用  反禁用  打印  导出  导入）
		 */
		String BANK_ACCOUNT_FIND = "baseData:main:bankAcco:comm:find";
		String BANK_ACCOUNT_EDIT = "baseData:main:bankAcco:comm:edit";
		String BANK_ACCOUNT_DELETE = "baseData:main:bankAcco:comm:delete";
		String BANK_ACCOUNT_DISABLE = "baseData:main:bankAcco:comm:disable";
		String BANK_ACCOUNT_ENABLE = "baseData:main:bankAcco:comm:enable";
		String BANK_ACCOUNT_PRINT = "baseData:main:bankAcco:comm:print";
		String BANK_ACCOUNT_EXPORT = "baseData:main:bankAcco:comm:export";
		String BANK_ACCOUNT_IMPORT = "baseData:main:bankAcco:comm:import";

		/**
		 * 凭证摘要（查询  编辑  删除  禁用  反禁用  打印  导出  导入）
		 */
		String COMMON_ABSTRACT_FIND = "ledgerManage:baseConf:abst:comm:find";
		String COMMON_ABSTRACT_EDIT = "ledgerManage:baseConf:abst:comm:edit";
		String COMMON_ABSTRACT_DELETE = "ledgerManage:baseConf:abst:comm:delete";
		String COMMON_ABSTRACT_DISABLE = "ledgerManage:baseConf:abst:comm:disable";
		String COMMON_ABSTRACT_ENABLE = "ledgerManage:baseConf:abst:comm:enable";
		String COMMON_ABSTRACT_PRINT = "ledgerManage:baseConf:abst:comm:print";
		String COMMON_ABSTRACT_EXPORT = "ledgerManage:baseConf:abst:comm:export";
		String COMMON_ABSTRACT_IMPORT = "ledgerManage:baseConf:abst:comm:import";

		/**
		 * 部门（查询  新增/编辑  删除  禁用  反禁用  打印  导出  导入）
		 */
		String DEPT_FIND = "baseData:main:dept:comm:find";
		String DEPT_EDIT = "baseData:main:dept:comm:edit";
		String DEPT_DELETE = "baseData:main:dept:comm:delete";
		String DEPT_DISABLE = "baseData:main:dept:comm:disable";
		String DEPT_ENABLE = "baseData:main:dept:comm:enable";
		String DEPT_PRINT = "baseData:main:dept:comm:print";
		String DEPT_EXPORT = "baseData:main:dept:comm:export";
		String DEPT_IMPORT = "baseData:main:dept:comm:import";

		/**
		 * 自定义核算项目（查询  新增  编辑  禁用  反禁用  打印  导出  导入）
		 */
		String ACCOUNTING_ITEM_FIND = "baseData:main:accoItem:comm:find";
		String ACCOUNTING_ITEM_UPDATE = "baseData:main:accoItem:comm:edit";
		String ACCOUNTING_ITEM_DISABLE = "baseData:main:accoItem:comm:disable";
		String ACCOUNTING_ITEM_ENABLE = "baseData:main:accoItem:comm:enable";
		String ACCOUNTING_ITEM_EXPORT = "baseData:main:accoItem:comm:export";
		String ACCOUNTING_ITEM_IMPORT = "baseData:main:accoItem:comm:import";
		String ACCOUNTING_ITEM_PRINT = "baseData:main:accoItem:comm:print";
		String ACCOUNTING_ITEM_DELETE = "baseData:main:accoItem:comm:delete";

		/**
		 * 员工（查询  新增  删除 编辑  禁用  反禁用  打印  导出  导入）
		 */
		String STAFF_FIND = "baseData:main:staff:comm:find";
		String STAFF_DELETE = "baseData:main:staff:comm:delete";
		String STAFF_EDIT = "baseData:main:staff:comm:edit";
		String STAFF_DISABLE = "baseData:main:staff:comm:disable";
		String STAFF_ENABLE = "baseData:main:staff:comm:enable";
		String STAFF_PRINT = "baseData:main:staff:comm:print";
		String STAFF_EXPORT = "baseData:main:staff:comm:export";
		String STAFF_IMPORT = "baseData:main:staff:comm:import";

		/**
		 * 会计科目（查询  新增  删除 编辑  禁用  反禁用  打印  导出  导入）
		 */
		String ACCOUNT_SUBJECT_FIND = "baseData:accInfo:accoSubj:comm:find";
		String ACCOUNT_SUBJECT_DELETE = "baseData:accInfo:accoSubj:comm:delete";
		String ACCOUNT_SUBJECT_EDIT = "baseData:accInfo:accoSubj:comm:edit";
		String ACCOUNT_SUBJECT_DISABLE = "baseData:accInfo:accoSubj:comm:disable";
		String ACCOUNT_SUBJECT_enable = "baseData:accInfo:accoSubj:comm:enable";
		String ACCOUNT_SUBJECT_PRINT = "baseData:accInfo:accoSubj:comm:print";
		String ACCOUNT_SUBJECT_EXPORT = "baseData:accInfo:accoSubj:comm:export";
		String ACCOUNT_SUBJECT_IMPORT = "baseData:accInfo:accoSubj:comm:import";


		/**
		 * 现金流量项目（查询  新增  删除 编辑  禁用  反禁用  打印  导出  导入）
		 */
		String CASH_FLOW_ITEM_DELETE = "baseData:accInfo:cashFlow:comm:delete";
		String CASH_FLOW_ITEM_EDIT = "baseData:accInfo:cashFlow:comm:edit";
		String CASH_FLOW_ITEM_FIND = "baseData:accInfo:cashFlow:comm:find";
		String CASH_FLOW_ITEM_DISABLE = "baseData:accInfo:cashFlow:comm:disable";
		String CASH_FLOW_ITEM_ENABLE = "baseData:accInfo:cashFlow:comm:enable";
		String CASH_FLOW_ITEM_PRINT = "baseData:accInfo:cashFlow:comm:print";
		String CASH_FLOW_ITEM_EXPORT = "baseData:accInfo:cashFlow:comm:export";
		String CASH_FLOW_ITEM_IMPORT = "baseData:accInfo:cashFlow:comm:import";
		String CASH_FLOW_ITEM_ALL = "baseData:accInfo:cashFlow:comm";

		/**
		 * 项目
		 */
		String PROJECT_DELETE = "baseData:main:project:comm:delete";
		String PROJECT_FIND = "baseData:main:project:comm:find";
		String PROJECT_EDIT = "baseData:main:project:comm:edit";
		String PROJECT_DISABLE = "baseData:main:project:comm:disable";
		String PROJECT_ENABLE = "baseData:main:project:comm:enable";
		String PROJECT_PRINT = "baseData:main:project:comm:print";
		String PROJECT_EXPORT = "baseData:main:project:comm:export";
		String PROJECT_IMPORT = "baseData:main:project:comm:import";

		/**
		 * 凭证
		 **/
		String VOUCHER_EDIT = "ledgerManage:voucMain:list:comm:edit";
		String VOUCHER_DELETE = "ledgerManage:voucMain:list:comm:delete";
		String VOUCHER_OFFSET = "ledgerManage:voucMain:list:comm:offset";
		String VOUCHER_CHECK = "ledgerManage:voucMain:list:comm:check";
		String VOUCHER_UNCHECK = "ledgerManage:voucMain:list:comm:uncheck";
		String VOUCHER_REVIEW = "ledgerManage:voucMain:list:comm:review";
		String VOUCHER_UNREVIEW = "ledgerManage:voucMain:list:comm:unreview";

		/**
		 * 客户（查询  新增  删除 编辑  禁用  反禁用  打印  导出  导入）
		 */
		String CUSTOMER_ITEM_FIND = "baseData:main:customer:comm:find";
		String CUSTOMER_ITEM_DELETE = "baseData:main:customer:comm:delete";
		String CUSTOMER_ITEM_EDIT = "baseData:main:customer:comm:edit";
		String CUSTOMER_ITEM_DISABLE = "baseData:main:customer:comm:disable";
		String CUSTOMER_ITEM_ENABLE = "baseData:main:customer:comm:enable";
		String CUSTOMER_ITEM_PRINT = "baseData:main:customer:comm:print";
		String CUSTOMER_ITEM_EXPORT = "baseData:main:customer:comm:export";
		String CUSTOMER_ITEM_IMPORT = "baseData:main:customer:comm:import";
		String CUSTOMER_ITEM_ALL = "baseData:main:customer:comm";

		/**
		 * 供应商（查询  新增  删除 编辑  禁用  反禁用  打印  导出  导入）
		 */
		String SUPPLIER_ITEM_FIND = "baseData:main:supplier:comm:find";
		String SUPPLIER_ITEM_DELETE = "baseData:main:supplier:comm:delete";
		String SUPPLIER_ITEM_EDIT = "baseData:main:supplier:comm:edit";
		String SUPPLIER_ITEM_DISABLE = "baseData:main:supplier:comm:disable";
		String SUPPLIER_ITEM_ENABLE = "baseData:main:supplier:comm:enable";
		String SUPPLIER_ITEM_PRINT = "baseData:main:supplier:comm:print";
		String SUPPLIER_ITEM_EXPORT = "baseData:main:supplier:comm:export";
		String SUPPLIER_ITEM_IMPORT = "baseData:main:supplier:comm:import";
		String SUPPLIER_ITEM_ALL = "baseData:main:supplier:comm";

		/**
		 * 总账期初（初始化 反初始化）
		 */
		String LEDGER_INIT = "ledgerManage:initTreat:ledger:comm:init";
		String LEDGER_REVERSE = "ledgerManage:initTreat:ledger:comm:reverse";
		String SUBJECT_EDIT = "ledgerManage:initTreat:init:comm:subj";
		String CASH_EDIT = "ledgerManage:initTreat:init:comm:cash";
		String CASH_DELETE = "ledgerManage:initTreat:init:comm:delete";

		/**
		 * 结账 反结账
		 **/
		String SETTLE = "ledgerManage:final:settle:comm:settle";
		String UNSETTLE = "ledgerManage:final:settle:comm:unsettle";
	}


	interface DataInit {

		/**
		 * 数据同步状态
		 * 0同步失败(未同步)、1同步成功
		 */
		Byte STATUS_FAIL = 0;
		Byte STATUS_OK = 1;

		/**
		 * 模块名称
		 * common_abstract常用摘要、all所有模块、menu_option菜单选项、create_table创建表、parameter_set参数设置
		 * currency币种、tax_category税种、cost_item费用项目、unit计量单位、account_book_category核算账簿分类
		 */

		String MODULE_CURRENCY = "currency";
		String MODULE_TAX_CATEGORY = "tax_category";
		String MODULE_COMMON_ABSTRACT = "common_abstract";
		String MODULE_COST_ITEM = "cost_item";
		String MODULE_UNIT = "unit";
		String MODULE_ACCOUNT_BOOK_CATEGORY = "account_book_category";
		String MODULE_CASH_FLOW = "cash_flow";
		String MODULE_SUBJECT = "subject";
		String MODULE_PARAMETER_SET = "parameter_set";
		String MODULE_MENU_OPTION = "menu_option";
		String MODULE_CREATE_TABLE = "create_table";
		String MODULE_SYS_SYSTEM = "sys_system";
		String MODULE_ALL = "all";
		/**
		 * 数据初始化key
		 */
		String KEY = "init_data_key:%d";
		/**
		 * 数据初始化lock
		 */
		String LOCK = "init_data_lock:%d:%s";
		/**
		 * 锁超时时间
		 */
		long LOCK_TIMEOUT = 60;

		/**
		 * 凭证相关表
		 */
		String VOUCHER = "wd_voucher_%d";
		String VOUCHER_ENTRY = "wd_voucher_entry_%d";
		String VOUCHER_ENTRY_AUXILIARY = "wd_voucher_entry_auxiliary_%d";
		String VOUCHER_ENTRY_CASH_FLOW = "wd_voucher_entry_cash_flow_%d";
		String VOUCHER_ENTRY_INTERIOR = "wd_voucher_entry_interior_%d";

		String KEY_CASH_FLOW = "cash_flow_%d";
		String KEY_CASH_FLOW_ITEM = "cash_flow_item_%d";

	}

	/**
	 * 微服务名称
	 */
	interface ServiceName {
		String SERVICE_BASE_DATA = "base-data";
	}

	/**
	 * 表名
	 */
	interface TableName {
		String ACCOUNT_SUBJECT = "wd_account_subject";
		String CASH_FLOW_ITEM = "wd_cash_flow_item";
		String ACCOUNT_BOOK_ENTITY = "wd_account_book_entity";
		String PROJECT = "wd_project";
		String BANK_ACCOUNT = "wd_bank_account";
		String CUSTOMER = "wd_customer";
		String SUPPLIER = "wd_supplier";
		String STAFF = "wd_staff";
		String DEPT = "wd_dept";
		String ACCOUNTING_ITEM = "wd_accounting_item";
		String ACCOUNTING_ITEM_VALUE = "wd_accounting_item_value";
		String SUBJECT = "wd_subject";
	}

	interface Auxiliary{
		String PROJECT_CODE = "项目编码";
		String PROJECT_NAME = "项目名称";
		String BANK_ACCOUNT_CODE = "银行账号";
		String BANK_ACCOUNT_NAME  = "银行账号名称";
		String CUSTOMER_CODE = "客户编码";
		String CUSTOMER_NAME  = "客户名称";
		String SUPPLIER_CODE = "供应商编码";
		String SUPPLIER_NAME  = "供应商名称";
		String STAFF_CODE = "员工编码";
		String STAFF_NAME  = "员工名称";
		String DEPT_CODE= "部门编码";
		String DEPT_NAME  = "部门名称";
		String ACCOUNTING_ITEM_VALUE_CODE = "自定义核算编码";
		String ACCOUNTING_ITEM_VALUE_NAME  = "自定义核算名称";
		String ACCOUNT_BOOK_ENTITY_CODE = "核算主体编码";
		String ACCOUNT_BOOK_ENTITY_NAME="核算主体名称";
	}


	/**
	 * 余额方向 0：借方、1：贷方
	 */
	interface BalanceDirection {
		Byte DEBIT = 0;
		Byte CREDIT = 1;
		Byte FLAT = 2;
	}

	/**
	 * 余额方向名称
	 */
	interface BalanceDirectionName {
		String DEBIT = "借";
		String CREDIT = "贷";
		String FLAT = "平";
	}

	/**
	 * 余额方向类型  0：借 、1：贷、2：平
	 */
	interface BalanceDirectionType {
		int DEBIT = 0;
		int CREDIT = 1;
		int FLAT = 2;
	}

	/**
	 * 余额方向
	 */
	interface BalanceDirectionOwner {
		String DEBIT = "借方";
		String CREDIT = "贷方";
	}

	/**
	 * 会计期间常用值
	 */
	interface PeriodNum {
		Byte ZERO = 0;
		Byte January = 1;
		Byte December = 12;
	}

	interface SysMenuOption {
		String DATA_TYPE_CODE = "code";
		String DATA_TYPE_RULE = "rule";
	}


	interface dataType {
		//共享型
		Byte SHRETYPE = 1;
		String SHRETYPE_NAME = "共享型";
		//分配型
		Byte DISTRIBUTION = 2;
		String DISTRIBUTION_NAME = "分配型";
		//私有型
		Byte PRIVATE = 3;
		String PRIVATE_NAME = "私有型";
	}


	/**
	 * 数据排序 0 期初余额 1 明细 2本期合计 3本年累计 4本日合计
	 */
	interface SortNum {
		Integer OPENING = 0;
		Integer DETAIL = 1;
		Integer PERIOD_NUM = 2;
		Integer PERIOD_YEAR = 3;
		Integer PERIOD_DAY = 4;
	}

	/**
	 * 报表数据行类型
	 */
	interface ReportFormRowType {
		String OPENING = "期初余额";
		String PERIOD_NUM = "本期合计";
		String PERIOD_YEAR = "本年累计";
		String PERIOD_DAY = "本日合计";
	}

	/**
	 * 多栏账方案
	 * 0：科目多栏账、1：辅助核算多栏账
	 */
	interface MultiColumnScheme {
		Byte SCHEME_TYPE_SUBJECT = 0;
		Byte SCHEME_TYPE_AUXILIAY = 1;
	}

	/**
	 * 集团信息
	 */
	interface BlocInfo {
		//集团id
		Long BLOCID = 0L;
		//集团编码
		String BLOCCODE = "0000";

		String BLOCNAME = "集团";
	}

	/**
	 * 数据状态标识:1启用,0禁用
	 */
	interface IsEnable {
		Byte ENABLE = 1;
		Byte DISABLE = 0;
	}

	/**
	 * 辅助核算项来源
	 */
	interface SubjectAuxiliarySource {
		/**
		 * 平台
		 */
		Byte PLATFORM = 0;
		/**
		 * 自定义辅助核算
		 */
		Byte CUSTOMIZE = 1;
	}

	/**
	 * 辅助核算项核算主体信息
	 */
	interface AccountBookEntityInfo {
		String CODE = "00005";
		String NAME = "核算主体";
		String SOURCE_TABLE = "wd_account_book_entity";
		Long FORM_COMPANY = 2L;
		Long FORM_UNIT = 3L;
	}

	/**
	 * 显示条件(0:本期无发生不显示 1:余额为零不显示 2:余额为零且本期无发生不显示)
	 */
	interface ShowCondition {
		Byte NO_HAPPEN = 0;
		Byte BALANCE_ZERO = 1;
		Byte NO_HAPPEN_AND_BALANCE_ZERO = 2;
	}

	/**
	 * 正则验证
	 */
	interface Regex {
		String RULER = "^[A-z0-9\\u4e00-\\u9fa5]*$";
		String HAS_SPACE = "(^\\s+)|(\\s+$)|\\s+";
		String HAS_SPECIAL = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]";
	}

	/**
	 * 会计要素项名称
	 */
	interface AccountElementItemName {
		String ASSETS = "资产";
		String COST = "成本";
		String COMMON = "共同";
		String DEBT = "负债";
		String RIGHT = "权益";
		String PROFIT = "损益";
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
	 * 参数设置 key
	 */
	interface ParameterSetKey {
		/**
		 * 科目
		 */
		String ACC_SUBJECT_ID = "acc_subject_id";
		/**
		 * 本年利润科目
		 */
		String LR_ACC_SUBJECT_ID = "lr_acc_subject_id";
		/**
		 * 利润分配科目
		 */
		String FP_ACC_SUBJECT_ID = "fp_acc_subject_id";
		/**
		 * 以前年度损益调整科目
		 */
		String SY_ACC_SUBJECT_ID = "sy_acc_subject_id";
		/**
		 * 损益科目是否合并结转
		 */
		String CREDENTIAL_TYPE = "credential_type";
		/**
		 * 设置结账时是否同时结转损益
		 */
		String IS_CARRYOVER = "is_carryover";
		/**
		 * 凭证字类型 凭证字为'记'或'收付转'
		 */
		String CREDENTIAL_WORD_TYPE = "credential_word_type";
		/**
		 * 未来期间数
		 */
		String FUTURE_PERIOD_NUM = "future_period_num";
		/**
		 * 允许凭证制单人和审核人是同一人
		 */
		String IS_ADD_APPROVE_SAME = "is_add_approve_same";
		/**
		 * 允许审核人和反审核人不是同一人
		 */
		String IS_APPROVE_NOT_SAME = "is_approve_not_same";
		/**
		 * 是否需要出纳复核
		 */
		String IS_CASHIER_REVIEW = "is_cashier_review";
		/**
		 * 出纳复核是否在审核之前
		 */
		String IS_REVIEW_BEFORE_APPROVE = "is_review_before_approve";
		/**
		 * 允许出纳复核和反复核不是同一人
		 */
		String IS_REVIEW_NOT_SAME = "is_review_not_same";
		/**
		 * 非制单人是否可以修改凭证
		 */
		String IS_CAN_UPDATE_OTHER = "is_can_update_other";
		/**
		 * 是否允许反结账
		 */
		String IS_OPEN_ACCOUNTS = "is_open_accounts";
		/**
		 * 结账时检查现金流量分析
		 */
		String IS_CHECK_CASH_FLOW = "is_check_cash_flow";
		/**
		 * 凭证保存必须指定现金流量项目
		 */
		String IS_MUST_SET_CASH_FLOW = "is_must_set_cash_flow";
		/**
		 * 允许结账后修改现金流量
		 */
		String IS_UPDATE_CASH = "is_update_cash";
		/**
		 * 凭证打印方式 打印模板 0：发票、1：A4一版、2：A4二版、3：A4三版、4：A5
		 */
		String PRINT_MODEL = "print_model";
		/**
		 * 凭证打印时科目名称显示方式
		 */
		String PRINT_SUBJECT_TYPE = "print_subject_type";
	}

	/**
	 * 参数设置 判断类型
	 */
	interface ParameterSetModifyType {
		/**
		 * 不可改
		 */
		Byte DISABLE = -1;
		/**
		 * 正常不控制
		 */
		Byte ENABLE = 0;
		/**
		 * 存在不可编辑无法删除
		 */
		Byte CAN_NOT_EDIT = 1;
	}

	/**
	 * code编码前缀
	 **/
	interface CodePrefix {
		/**
		 * 公司间协同配置-平台
		 **/
		String SUBJECT_SYNERGY_PLATFORM = "XTGX";
	}

	/**
	 * code 后缀(自增数字长度)
	 */
	interface CodeSuffix {
		/**
		 * 公司间协同配置-平台/租户
		 */
		int SUBJECT_SYNERGY = 4;
	}
}
