package com.njwd.common;

import java.math.BigDecimal;

/**
 * @param
 * @author fancl
 * @description 总账常量类
 * @date 2019/8/27
 * @return
 */
public interface LedgerConstant {

    /**
     * 过账相关常量
     * fancl
     */
    interface PostPeriod {
        //检查项定义
        String isNull = "isNull";
        String isNull_message = "该账簿id不存在";
        String unOpen = "unOpen";
        String unOpen_message = "该账簿期间未开启";
        String settled = "settled";
        String settled_message = "该账簿期间已结账";
        String unAudit = "unAudit";
        String unAudit_message = "当期存在未审核凭证，共计%d条";
        String unReview = "unReview";
        String unReview_message = "当期存在未复核凭证，共计%d条";
        String broken = "broken";
        String broken_message = "当期存在断号凭证";
        String transferSQLFail = "transferSQLFail";
        String transferSQLFail_message = "过账执行SQL出现异常";
    }

    /***
     * Json字段更新类型
     */
    interface ManageInfoUpdateType {
        String transferItem = "transferItem";
        String transferItemUserId = "$.transferItemUserId";
        String transferItemUserName = "$.transferItemUserName";
        String transferItemTime = "$.transferItemTime";
    }

    /**
     * 凭证状态 -1：草稿、0：待审核、1：待过账、2：已过账
     **/
    interface VoucherStatus {
        byte DRAFT = -1;
        byte PENDING = 0;
        byte POSTING = 1;
        byte POST = 2;
    }

    /**
     * 凭证来源方式 0：手工、1：内部协同、2：损益结转、3：冲销、4：业务系统 5：公司间协同
     */
    interface SourceType {
        byte MANUAL = 0;
        byte COLLABORATE = 1;
        byte FORWARD = 2;
        byte RUSH = 3;
        byte BUSINESS_SYSTEM = 4;
        byte COMPANY_COLL = 5;
    }

    /***
     * 查询方案定义
     */
    interface QueryScheme {
        /**
         * 0等于1区间2包含
         */
        Byte OPERATOR_EQUAL = 0;
        Byte OPERATOR_RANGE = 1;
        Byte OPERATOR_CONTAINS = 2;
        /**
         * 值类型
         */
        Byte TYPE_STRING = 0;
        Byte TYPE_INTEGER = 1;
        Byte TYPE_DECIMAL = 2;
        Byte TYPE_JSON = 3;
        Byte TYPE_ARRAY = 4;

        /**
         * 区间
         */
        int RANGE_FIRST = 0;
        int RANGE_SECOND = 1;

        /**
         * 最大记录数
         */
        int MAX_RECORD_COUNT = 20;
    }


    /***
     * 凭证整理
     */
    interface VoucherAdjust {
        /**
         * key
         */
        String MAIN_KEY = "main_%d";
        String CHILD_KEY = "child_%d_%d";
        String KEY = "%s_%d";
        String REPORT_KEY = "report_%d";
        /**
         * key前缀
         */
        String MAIN_KEY_PRE = "main";
        String CHILD_KEY_PRE = "child";
        /**
         * 查询断号深度
         * 0全局一个，1分组各一个，2全部
         */
        Byte BROKEN_GLOBAL_ONE = 0;
        Byte BROKEN_GROUP_ONE = 1;
        Byte BROKEN_ALL = 2;
        /**
         * 第一个编码
         */
        Integer FIRST_CODE = 1;

        String CREDENTIAL = "%s%d-%d";
        String CREDENTIAL_CODE = "%d-%d";


        Byte ADJUST_STATUS_FAIL = 0;
        Byte ADJUST_STATUS_OK = 1;
        Byte ADJUST_STATUS_NORMAL = 2;

    }

    /**
     * 财务报告表名 资产负债表101,利润表 201,现金流量表 301
     **/
    interface FinancialReportCode {
        Integer INCOMESTAEMENTCODE = 201;
        Integer CASHFLOWTABLECODE = 301;
    }

    /**
     * 财务报告名称
     **/
    interface FinancialReportName {
        String BALANCEREPORT = "资产负债表";
        String PROFITREPORT = "利润表";
        String CASHFLOWREPORT = "现金流量表";
    }


    /**
     * 总账初始化常量
     */
    interface BalanceInit {
        /**
         * 往来编码
         **/
        String INTERIOR_SUBJECT_CODE = "1999";
        /**
         * 最近账期是否结账
         **/
        String DIS_INIT_CHECK_CONTEXT = "启用期间未结账校验";

        /**
         * 最近账期未结账
         **/
        String DIS_INIT_DESCRIPTION = "启用期间已结账，请反结账后再进行反初始化";

        /**
         * 状态通过
         **/
        String CHECK_STATUS_PASS = "1";

        /**
         * 状态不通过
         **/
        String CHECK_STATUS_UN_PASS = "0";

        /**
         * 状态不通过
         **/
        String CHECK_STATUS_WARN = "警告";

        /**
         * 计算初始化值
         **/
        String BIG_DECIMAL_INIT = "0.00";

        /**
         * 核算主体试算平衡
         **/
        String ENTRY_CHECK_CONTEXT = "核算主体试算平衡校验";

        /**
         * 各核算主体试算平衡
         **/
        String ENTRY_DESCRIPTION = "试算不平衡";

        /**
         * 往来科目校验
         **/
        String INTERIOR_SUBJECT_CHECK_CONTEXT = "内部往来科目期初平衡校验";

        /**
         * 核算主体所有余额发生额合计为0
         **/
        String INTERIOR_SUBJECT_DESCRIPTION = "1999科目余额合计不为0";

        /**
         * 现金流量和资金科目期初
         **/
        String ACC_CHECK_CONTEXT = "现金流量和科目期初平衡校验";

        /**
         * 各核算主体相等
         **/
        String ACC_DESCRIPTION = "现金流量流入-流出 ≠ 科目期初现金类科目借方发生额-贷方发生额";

        /**
         * 往来现金流量
         **/
        String INTERIOR_CASH_CHECK_CONTEXT = "往来现金流量";

        /**
         * 核算主体所有流入流出合计为0
         **/
        String INTERIOR_CASH_DESCRIPTION = "核算主体所有流入流出合计为0";
    }

    /**
     * 期初辅助核算常量
     */
    interface AuxiliaryItem {
        /**
         * 期初余额
         */
        String OPENING_BALANCE_KEY = "openingBalance";

        /**
         * 本年借方
         */
        String THIS_YEAR_DEBIT_AMOUNT_KEY = "thisYearDebitAmount";

        /**
         * 本年贷方
         */
        String THIS_YEAR_CREDIT_AMOUNT_KEY = "thisYearCreditAmount";

        /**
         * 年初余额
         */
        String YEAR_OPENING_BALANCE_KEY = "yearOpeningBalance";
        /**
         * 期初余额
         */
        String OPENING_BALANCE_VALUE = "期初余额";

        /**
         * 本年借方
         */
        String THIS_YEAR_DEBIT_AMOUNT_VALUE = "本年借方";

        /**
         * 本年贷方
         */
        String THIS_YEAR_CREDIT_AMOUNT_VALUE = "本年贷方";

        /**
         * 年初余额
         */
        String YEAR_OPENING_BALANCE_VALUE = "年初余额";

        /**
         * auxiliarySourceTable
         */
        String AUXILIARY_SOURCE_TABLE = "auxiliarySourceTable";

        /**
         * auxiliaryCode
         */
        String AUXILIARY_CODE = "auxiliaryCode";

        /**
         * auxiliaryName
         */
        String AUXILIARY_NAME = "auxiliaryName";
    }


    /**
     * 结账处理
     */
    interface Settle {
        /**
         * 凭证行摘要(损益结转)
         */
        String VOUCHER_REMARK_LP = "%s年第%s期损益结转";

        /**
         * 凭证行摘要(以前年度损益调整结转)
         */
        String VOUCHER_REMARK_FP = "以前年度损益调整结转";

        /**
         * 以前期间
         */
        Byte BEFORE = -1;

        /**
         * 未来期间
         */
        Byte FUTURE = 1;

        /**
         * 总账参数设置的损益科目个数
         */
        int SET_SUBJECT = 3;
        /**
         * 配置错误
         */
        Byte PARAMETER_SET_ERROR = -1;

        /**
         * 第一期
         */
        Byte FIRST_NUM = 1;

        /**
         * 两个期间都有值
         */
        int TWO_PERIOD = 2;
        /**
         * 期初的期间数为0
         **/
        Byte INIT_PERIOD_NUM = 0;
    }


    /**
     * 总账报表导出文件名
     */
    interface LedgerExportName {
        //现金流量汇总
        String LEDGER_CASH_FLOW_ITEM_COLLECT = "cash_flow_item_collect";
        //现金流量明细
        String LEDGER_CASH_FLOW_ITEM_DETAIL = "cash_flow_item_detail";

        //现金流量
        String LEDGER_CASH_FLOW_ITEM = "cash_flow_item";
        //现金日记账
        String LEDGER_CASH_JOURNAL_DETAIL = "cash_journal_detail";
        //多栏明细账
        String LEDGER_MULTI_COLUMN_REPORT = "multi_column_report";
    }


    /**
     * 财务报告——导出
     */
    interface ExportConstant {

        Integer SIZE_TWO = 2;
        Integer SIZE_NINE = 9;
        Integer SIZE_SIXTEEN = 16;
        Integer SIZE_TEN = 10;

        /*现金流量表列数（2列,本期,本年累计）
         */
        Integer SINGLECASHFLOWCELLNUM = 2;

        /*单账簿 或者 单账簿单核算主体
         */
        String[] TITLE_ROW_OF_ONEBOOK_OR_ONEBOOK_WITH_ONEENTITY = {"资产: ", "行次", "期末余额", "年初余额",  "负债和所有者权益: ", "行次", "期末余额", "年初余额"};

        String ASSETS_COLUMN = "资产";

        String LIABILITY_COLUMN = "负责和所有者权益";

        String ITEM = "项目";

        String LINE = "行次";

        String ACCOUNTBOOK = "账簿";

        String ACCOUNTBOOKENTITY = "核算主体";

        String ACCOUNTBOOK_ENTITY = "项目 核算主体";

        String ASSETS_TOTAL = "资产总计";

        String INITILAL = "年初余额";

        String CLOSING = "期末余额";

        String CURRENTMONEY = "本期金额";

        String YEARCUMULATIVE = "本年累计";

        String YEARCOMPARE = "同比";

        String MONTHCOMPARE = "环比";

        String INCOMERATION = "占收入比";

        String GROWRATE = "增长率";

        String SPLIT = ",";

        String APPEND = "、";

        String ADD = "加: ";

        String AMONG = "其中: ";

        String SUB = "减: ";

        String BLANK = " ";

        String DASH = "-";

        String PERIOD ="期间:    ";

        String CURRENCY = "币种:  ";

        String SHEETPROFITNAME = "财务报告-利润表";

        String SHEETCASHFLOWNAME = "财务报告-现金流量表";
        /**
         * 项目属性:1、资产、2：负债、3：利润表、4：现金流量表
         * 资产
         */
        Byte ASSETS = 1;

        /**
         * 负债
         */
        Byte LIABILITY = 2;

        // 加粗、居中、灰色背景
        String BOLD_CENTER_GRAY = "boldCenterGray";
        // 加粗、居中、无背景色
        String BOLD_CENTER = "boldCenter";
        // 不加粗、居中、无背景色
        String CENTER = "center";
        // 加粗、不居中、无背景色
        String BOLD = "bold";
        // 缩进
        String INDENTATION= "indentation";
        // 默认
        String DEFAULT = "";
    }

    /**
     * 总帐
     */
    interface Ledger {
        // 核算主体ID为1时，表示合计数据
        Long TOTAL = -1L;

        String TOTAL_NAME = "合计";
        // 锁超时时间
        long LOCK_TIMEOUT = 10;
        // 是否末级科目
        Byte ISFINAL = 1;

        Byte LAST_MONTH = 12;
        // 一月
        Byte JANUARY = 1;

        Byte ZERO = 0;

        Byte ONE = 1;

        Byte INITIAL = 1;

        /**
         * 默认币种
         */
        Long DEFAULT_CURRENCY = 1L;

        /**
         * 默认币种
         */
        String DEFAULT_CURRENCYNAME = "人民币";
        /**
         * 默认单据张数
         */
        Byte DEFAULT_BILL_NUM = 0;

        /**
         * 坏账准备
         */
        String BAD_DEBT_CODE = "1231";

        String TOTAL_NAME_DAY = "本日合计";

        String TOTAL_NAME_PERIOD = "本期合计";

        String TOTAL_NAME_YEAR = "本年累计";
    }


    /**
     * 查询会计期间的类型值
     */
    interface FindPeriodType {
        /**
         * 查询开始期间后最近的已结账期间
         */
        Integer BEGIN_PERIOD = 0;

        /**
         * 查询结束期间前最近的已结账期间
         */
        Integer END_PERIOD = 1;
    }


    /**
     * 凭证打印模板 0：发票、1：A4一版、2：A4二版、3：A4三版、4：A5
     * zhc
     */
    interface PrintModel {
        /**
         * 发票版
         */
        Byte FP = 0;
        /**
         * A4一版
         */
        Byte A41 = 1;
        /**
         * A4二版
         */
        Byte A42 = 2;
        /**
         * A4三版
         */
        Byte A43 = 3;
        /**
         * A5版
         */
        Byte A5 = 4;
    }

    /**
     * 凭证打印模板对应数据条数
     * zhc
     */
    interface PrintModelParameter {
        int A41_NUM = 23;
        int A42_NUM = 8;
        int A43_NUM = 5;
    }

    /**
     * 报表显示条件
     */
    interface ReportShowCondition {
        /**
         * 显示条件(0:本期无发生不显示 1:余额为零不显示 2:余额为零且本期无发生不显示)
         */
        Byte HAPPEN_NO = 0;
        Byte BALANCE_NO = 1;
        Byte HAPPEN_BALANCE_NO = 2;
    }


    /**
     * 会计期间查询类型  0 制单日期 1 会计区间
     */
    interface PeriodOperator {
        Byte PERIOD_YEAR_AND_NUM = 1;
        Byte VOUCHER_DATE = 0;
    }

    /**
     * 财务报告常用值
     */
    interface FinancialString {
        String MainBusinessIncome = "营业收入";
        String SUB_TOTAL = "小计";
        String TOTAL = "合计";
        String AllUseCompany = "全部";
        Long ACCOUNTBOOKID = -1L;

    }

    /**
     * 凭证字 1：记 、2：收、3：付、4：转
     **/
    interface CredentialWordName {
        String RECORD = "记";
        String RECEIVE = "收";
        String PAY = "付";
        String TRANSFER = "转";
    }

    /**
     * 现金流量常用值
     */
    interface CashFlowGroup {
        String CASH_FLOW_GROUPS = "1,2,3,4";
        String CASH_FLOW_GROUP_ONE = "1";
        String CASH_FLOW_GROUP_TWO = "2";
        String CASH_FLOW_GROUP_THR = "3";
        String CASH_FLOW_GROUP_FOU = "4";
        String CASH_FLOW_ONE_Z_ONE = "101";
        String CASH_FLOW_ONE_Z_TWO = "102";
        String CASH_FLOW_TWO_Z_ONE = "201";
        String CASH_FLOW_TWO_Z_TWO = "202";
        String CASH_FLOW_THR_Z_ONE = "301";
        String CASH_FLOW_THR_Z_TWO = "302";
        String CASH_FLOW_FOU_Z_ONE = "401";
        String CASH_FLOW_FOU_Z_TWO = "402";
    }

    /**
     * 财务报告级次0：标题、1：一级、2：二级、3：三级、4：小计、5：合计、6：总计
     */

    interface FinancialReportItemLevel {
        Byte LEVEL_ZERO = 0;
        Byte LEVEL_ONE = 1;
        Byte LEVEL_TWO = 2;
        Byte LEVEL_THREE = 3;
        Byte LEVEL_FOUR = 4;
        Byte LEVEL_FIVE = 5;
        Byte LEVEL_SIX = 6;
    }
    /**
     * 刘遵通
    * 审核 反审核  复核  反复核提示语信息
    * */
    interface MessAge{
        String USER_BEING_OPERATION = "%s号凭证其他用户正在操作,请稍候再使用!";
        String VOUCHER_POST = "%s号凭证已过账，不允许审核!";
        String VOUCHER_POSTING = "%s号凭证已审核,不允许重复审核!";
        String COORDINATION_VOUCHER = "协同凭证%s号请先复核!";
        String VOUCHER_PLEASE_REVIEW = "%s号凭证,请先复核!";
        String COORDINATION_VOUCHER_NOTAPPROVE = "协同凭证不能审核!";
        String APPROVEUSER_MAKINGUSER_DIFFERENCE = "审核用户和制单用户不可以是同一人!";
        String APPROVE_SUCCESS = "%s号凭证，审核成功!";
        String VOUCHER_PLEASE_APPROVE = "%s号凭证,请先审核!";
        String VOUCHER_ALREADY_POSTING = "%s号凭证已过账,不允许反审核!";
        String COORDINATION_VOUCHER_NOT_BACKAPPROVE = "协同凭证不能反审核!";
        String APPROVEUSER_BACKAPPROVEUSER_MUST_IDENTICAL = "%s号凭证,反审核用户和审核必须为同一人,不允许反审核!";
        String BACKAPPROVE_SUCCESS = "%s号凭证，反审核成功!";
        String NONEED_REVIEW = "%s号凭证无需复核!";
        String VOUCHER_ALREADY_POSTING_REVIEW  = "%s号凭证已过账,不允许复核!";
        String VOUCHER_ALREADY_REVIEW = "%s号凭证已复核,不允许重复出纳复核!";
        String REVIEW_SUCCESS = "%s号凭证，复核成功!";
        String BACKREVIEWUSER_REVIEWUSER_MUST_IDENTICAL = "%s号凭证,反复核用户和复核必须为同一人,不允许反复核!";
        String BACKREVIEW_IN_BACKAPPROVE_AFTER = "%s号凭证,反复核需要在反审核之后!";
        String BACKREVIEW_SUCCESS = "%s号凭证，反复核成功!";
    }

    /**
     * 同比或环比 0,同比,1,环比
     **/
    interface YearOrMonth {
        Byte YEAR = 0;
        Byte MONTH = 1;
    }


    /**
     * 财务报告百分比小数位 FinancialReportPercentDecimalDigits
     **/
    interface FRPDecimalDigits {
        Integer FINANCIALDECIMAL = 2;
        Integer FINANCIALDECIMALPERCENT = 4;
    }

    /**
     * 利润表计算类型
     **/
    interface IncomeRationType {
        /**
         * 本期
         **/
        String CURRENTISSUE = "currentIssue";
        /**
         * 本年累计
         **/
        String YEARCUMULATIVE = "yearCumulative";
        /**
         * 同比
         **/
        String YEARCOMPARE = "yearCompare";
        /**
         * 环比
         **/
        String MONTHCOMPARE = "monthCompare";
    }

    /**
     * 公式类型 0：科目或项目、1：项目行、2：现金流量特殊项
     **/
    interface FormulaType {
        Byte SUBECTORITEM = 0;
        Byte ITEMLINE = 1;
        Byte SPECIALCASHFLOW = 2;
    }

    /**
     * 运算标识 0：加、1：减
     **/
    interface Operator {
        Byte ADD = 0;
        Byte SUBTRACT = 1;
    }

    /**
     * 凭证排序凭什么小到大
     */
    interface ROWNUM {
        int SORT_ONE = 1;
        int SORT_TWO = 2;
        int SORT_THREE = 3;
        int SORT_FOUR = 4;

    }

    /**
     * 汇率
     **/
    interface ExchangeRate {
        // 本位币汇率
        BigDecimal DEFAULT = BigDecimal.ONE;
    }

    /**
     * 科目余额期初状态 0：未录入，1：未平衡:2：已平衡:3：已初始化
     **/
    interface SubjectStatus {
        /**
         * 未录入
         **/
        Byte UN_RECORD = 0;
        /**
         * 未平衡
         **/
        Byte  UN_BALANCE = 1;
        /**
         * 已平衡
         **/
        Byte BALANCED =2;
        /**
         * 已初始化
         **/
        Byte INIT_ED = 3;
    }

    /**
     * 现金流量期初状态 0：未录入，1：已录入，2：未启用，3：已初始化
     **/
    interface CashStatus {
        /**
         * 未录入
         **/
        Byte UN_RECORD = 0;
        /**
         * 已录入
         **/
        Byte RECORDED = 1;
        /**
         * 未启用
         **/
        Byte UNUSED =2;
        /**
         * 已初始化
         **/
        Byte INIT_ED = 3;
    }
}
