package com.njwd.common;

import com.njwd.entity.basedata.excel.ExcelColumn;

/**
 * @Description Excel常量类
 * @Author 朱小明
 * @Date 2019/7/1 14:12
 **/

public interface ExcelColumnConstant {

    /**
     * 共通字段
     */
    interface Common {
        ExcelColumn IS_ENABLE = new ExcelColumn("isEnable", "数据状态", ExcelDataConstant.SYSTEM_DATA_IS_ENABLE);
        ExcelColumn IS_DEL = new ExcelColumn("isDel", "删除标识");
        ExcelColumn CREATE_TIME = new ExcelColumn("createTime", "创建时间");
        ExcelColumn CREATOR_ID = new ExcelColumn("creatorId", "创建者ID");
        ExcelColumn CREATOR_NAME = new ExcelColumn("creatorName", "创建者");
        ExcelColumn UPDATE_TIME = new ExcelColumn("updateTime", "更新时间");
        ExcelColumn UPDATOR_ID = new ExcelColumn("updatorId", "更新者ID");
        ExcelColumn UPDATOR_NAME = new ExcelColumn("updatorName", "更新者");
    }

    /**
     * 客户供应商项目
     */
    interface CustomerSupplier {
        ExcelColumn DATA_TYPE = new ExcelColumn("dataType", "数据类型",ExcelDataConstant.SYSTEM_DATA_DATA_TYPE);
        ExcelColumn ROOT_ENTERPRISE_ID = new ExcelColumn("rootEnterpriseId", "企业ID");
        ExcelColumn COMPANY_ID = new ExcelColumn("companyId", "创建公司");
        ExcelColumn GLOBAL_ID = new ExcelColumn("globalId", "全局ID");
        ExcelColumn CODE_TYPE = new ExcelColumn("codeType", "编码类型");
        ExcelColumn CODE = new ExcelColumn("code", "编码");
        ExcelColumn NAME = new ExcelColumn("name", "名称");
        ExcelColumn CUSTOMER_TYPE = new ExcelColumn("customerType", "类型 ", ExcelDataConstant.SYSTEM_DATA_CUSTOMER_TYPE);
        ExcelColumn IS_INTERNAL_CUSTOMER = new ExcelColumn("isInternalCustomer", "是否内部客户",ExcelDataConstant.SYSTEM_DATA_IS_INTERNAL_CUSTOMER);
        ExcelColumn IS_INTERNAL_SUPPLIER = new ExcelColumn("isInternalCustomer", "是否内部供应商",ExcelDataConstant.SYSTEM_DATA_IS_INTERNAL_CUSTOMER);
        ExcelColumn UNIFIED_SOCIAL_CREDIT_CODE = new ExcelColumn("unifiedSocialCreditCode", "统一社会信用代码");
        ExcelColumn ID_CARD_NUM = new ExcelColumn("idCardNum", "身份证号");
        ExcelColumn BUSINESS_ADDRESS = new ExcelColumn("businessAddress", "经营地址");
        ExcelColumn LINKMAN = new ExcelColumn("linkman", "联系人");
        ExcelColumn CONTACT_NUMBER = new ExcelColumn("contactNumber", "联系电话");
        ExcelColumn COMPANY_NAME = new ExcelColumn("companyName", "创建公司");
        ExcelColumn USE_COMPANY_NAME = new ExcelColumn("useCompanyName", "使用公司");
    }

    /**
     * 员工
     */
    interface StaffSupplier {
        ExcelColumn CODE = new ExcelColumn("code", "编码");
        ExcelColumn NAME = new ExcelColumn("name", "名称");
        ExcelColumn CONTACT_NUMBER = new ExcelColumn("contactNumber", "联系电话");
        ExcelColumn EMAIL = new ExcelColumn("email", "电子邮箱");
        ExcelColumn USER_NAME = new ExcelColumn("name", "名称");
        ExcelColumn DEPT = new ExcelColumn("dept", "部门");
        ExcelColumn COMPANY = new ExcelColumn("company", "归属公司");
        ExcelColumn USER_COMPANY = new ExcelColumn("userCompany", "使用公司");
        ExcelColumn IS_ENABLE = new ExcelColumn("isEnable", "数据状态",ExcelDataConstant.SYSTEM_DATA_IS_ENABLE);
    }

    /**
     * 会计科目
     */
    interface AccountSubject {
        ExcelColumn CODE = new ExcelColumn("code", "科目编码");
        ExcelColumn NAME = new ExcelColumn("name", "科目名称");
        ExcelColumn BALANCE_DIRECTION = new ExcelColumn("balanceDirection", "余额方向", ExcelDataConstant.SYSTEM_DATA_BALANCE_DIRECTION);
        ExcelColumn ACCOUNT_CATEGORY_NAME = new ExcelColumn("accountCategoryName", "分类");
        ExcelColumn AUXILIARY_NAMES = new ExcelColumn("auxiliaryNames", "辅助核算");
        ExcelColumn CASH_FLOW_NAMES = new ExcelColumn("cashFlowNames", "现金流量项目预设");
        ExcelColumn IS_INTERIOR = new ExcelColumn("isInterior", "内部往来", ExcelDataConstant.SYSTEM_DATA_IS_INTERIOR);
        ExcelColumn COMPANY_NAME = new ExcelColumn("companyName", "归属公司");
        ExcelColumn USE_COMPANY_NAME = new ExcelColumn("useCompanyName", "使用公司");
    }

    /**
     * 银行账号
     */
    interface BankAccount {
        ExcelColumn CODE = new ExcelColumn("code", "账户编码");
        ExcelColumn ACCOUNT = new ExcelColumn("account", "银行账号");
        ExcelColumn NAME = new ExcelColumn("name", "账号名称");
        ExcelColumn DEPOSIT_BANK_NAME = new ExcelColumn("depositBankName", "开户银行");
        ExcelColumn ACC_TYPE_NAME = new ExcelColumn("accTypeName", "账户类型");
        ExcelColumn ACC_USAGE_NAME = new ExcelColumn("accUsageName", "账户用途");
        ExcelColumn COMPANY_NAME = new ExcelColumn("companyName", "归属公司");
        ExcelColumn USE_COMPANY_NAME = new ExcelColumn("useCompanyName", "使用公司");
        ExcelColumn AUXILIARY_NAME = new ExcelColumn("auxiliaryName", "核算主体");
        ExcelColumn IS_ENABLE_NAME = new ExcelColumn("isEnableStr", "数据状态");
    }

    /**
     * 自定义核算项目
     */
    interface AccountingItem {
        ExcelColumn CODE = new ExcelColumn("code", "编码");
        ExcelColumn NAME = new ExcelColumn("name", "名称");
        ExcelColumn VALUE_CODE = new ExcelColumn("code", "值编码");
        ExcelColumn VALUE_NAME = new ExcelColumn("name", "值名称");
        ExcelColumn COMPANY_NAME = new ExcelColumn("companyName", "归属公司");
        ExcelColumn USE_COMPANY_NAME = new ExcelColumn("useCompanyName", "使用公司");
        ExcelColumn STATUS_NAME = new ExcelColumn("statusName", "数据状态");
    }

    /**
     * 部门
     */
    interface Dept {
        ExcelColumn CODE = new ExcelColumn("code", "编码");
        ExcelColumn NAME = new ExcelColumn("name", "名称");
        ExcelColumn DEPTTYPENAME = new ExcelColumn("deptTypeName", "属性");
        ExcelColumn COMPANYNAME = new ExcelColumn("companyName", "归属公司");
        ExcelColumn USECOMPANYNAME = new ExcelColumn("useCompanyName", "使用公司");
        ExcelColumn BUSINESSUNITNAME = new ExcelColumn("businessUnitName", "业务单元");
        ExcelColumn ISENABLESTR = new ExcelColumn("isEnableStr", "数据状态");
    }

    /**
     * @Author Libao
     * @Description ExcelColumnConstant
     * @Date 2019/7/1 17:45
     */
    interface CashFlowItem {
        ExcelColumn CODE = new ExcelColumn("code", "编码");
        ExcelColumn NAME = new ExcelColumn("name", "名称");
        ExcelColumn CASHFLOWDIRECTION = new ExcelColumn("cashFlowDirection", "方向");
        ExcelColumn COMPANYNAME = new ExcelColumn("companyName", "归属公司");
        ExcelColumn USECOMPANYNAME = new ExcelColumn("useCompanyName", "使用公司");
        ExcelColumn ISENABLE = new ExcelColumn("isEnable", "数据状态");
    }

    /**
     * @Description 项目
     * @Author LuoY
     * @Date 2019/7/2 14:26
     * @Param
     * @return
     */
    interface  Project{
        ExcelColumn CODE=new ExcelColumn("code", "项目编码");
        ExcelColumn NAME=new ExcelColumn("name", "项目名称");
        ExcelColumn COMPANYNAME=new ExcelColumn("companyName", "归属公司");
        ExcelColumn USECOMPANYNAME=new ExcelColumn("useCompanyName", "使用公司");
        ExcelColumn ISENABLE=new ExcelColumn("isEnable", "数据状态",ExcelDataConstant.SYSTEM_DATA_IS_ENABLE);
        ExcelColumn DEPARTMENTNAME=new ExcelColumn("departmentName", "负责部门");
        ExcelColumn PERSONINCHARGE=new ExcelColumn("personInChargeName", "负责人");
        ExcelColumn PHONENUMBR=new ExcelColumn("phoneNumber", "手机号");
        ExcelColumn STARTDATE=new ExcelColumn("startDate", "开始日期");
        ExcelColumn INSPECTIONDATE=new ExcelColumn("inspectionDate", "验收日期");
        ExcelColumn REMARK=new ExcelColumn("remark", "备注");
    }

    /**
     * 操作日志Excel常量
     */
    interface OperationLog{
        ExcelColumn SYS_NAME = new ExcelColumn("sysName","子系统");
        ExcelColumn MENU_NAME = new ExcelColumn("menuName","功能菜单");
        ExcelColumn OPERATION =new ExcelColumn("operation","记录内容");
        ExcelColumn CREATE_TIME =new ExcelColumn("createTime","操作时间");
        ExcelColumn PHONE = new ExcelColumn("phone","手机号码");
        ExcelColumn CREATOR_NAME = new ExcelColumn("createName","姓名");
        ExcelColumn IP_ADDRESS =new ExcelColumn("ipAddress","IP地址");
    }

    /**
     * 科目余额表、科目汇总表、辅助核算余额表
     */
    interface BalanceSubject {
        ExcelColumn ACCOUNT_BOOK_NAME = new ExcelColumn("accountBookName", "总账账簿");
        ExcelColumn ACCOUNT_BOOK_ENTITY_NAME = new ExcelColumn("accountBookEntityName", "核算主体");
        ExcelColumn PERIOD_YEAR = new ExcelColumn("periodYear", "会计年度", ExcelDataConstant.SYSTEM_DATA_BALANCE_DIRECTION);
        ExcelColumn NAME = new ExcelColumn("name", "科目名称");
        ExcelColumn CODE = new ExcelColumn("code", "科目编码");
        ExcelColumn AUXILIARY_CODE = new ExcelColumn("auxiliaryCode", "辅助核算编码");
        ExcelColumn AUXILIARY_NAME = new ExcelColumn("auxiliaryName", "辅助核算名称");
        ExcelColumn OPENING_DIRECTION_NAME = new ExcelColumn("openingDirectionName", "方向");
        ExcelColumn OPENING_BALANCE = new ExcelColumn("openingBalance", "期初余额");
        ExcelColumn DEBIT_AMOUNT = new ExcelColumn("debitAmount", "本期借方");
        ExcelColumn CREDIT_AMOUNT = new ExcelColumn("creditAmount", "本期贷方");
        ExcelColumn TOTAL_DEBIT_AMOUNT = new ExcelColumn("totalDebitAmount", "借方累计");
        ExcelColumn TOTAL_CREDIT_AMOUNT = new ExcelColumn("totalCreditAmount", "贷方累计");
        ExcelColumn CLOSING_DIRECTION_NAME = new ExcelColumn("closingDirectionName", "方向");
        ExcelColumn CLOSING_BALANCE = new ExcelColumn("closingBalance", "期末余额");
    }

    /**
     * 现金流量项目报表导出字段
     */
    interface CashFlowItemReport{
        ExcelColumn ACCOUNT_BOOK_NAME = new ExcelColumn("accountBookName", "总账账簿");
        ExcelColumn ACCOUNT_BOOK_ENTITY_NAME = new ExcelColumn("accountBookEntityName", "核算主体");
        ExcelColumn VOUCHER_DATE = new ExcelColumn("voucherDate", "日期");
        ExcelColumn CREDENTIAL_WORD_CODE = new ExcelColumn("credentialWordCode","凭证字号");
        ExcelColumn ABSTRACT_CONTENT = new ExcelColumn("abstractContent","摘要");
        ExcelColumn SUBJECT_CODE = new ExcelColumn("subjectCode","科目编码");
        ExcelColumn SUBJECT_FULL_NAME = new ExcelColumn("subjectFullName","科目名称");
        ExcelColumn SUBJECT_NAME = new ExcelColumn("subjectName","科目名称");
        ExcelColumn SUBJECT_FULL = new ExcelColumn("subjectFullName","科目");
        ExcelColumn SUBJECT = new ExcelColumn("subjectName","科目");
        ExcelColumn DEBIT_AMOUNT = new ExcelColumn("debitAmount","借方本币");
        ExcelColumn CREDIT_AMOUNT = new ExcelColumn("creditAmount","贷方本币");
        ExcelColumn CURRENCY_AMOUNT = new ExcelColumn("currencyAmount","流量金额");
        ExcelColumn CODE = new ExcelColumn("code","现金流量项目编码");
        ExcelColumn NAME = new ExcelColumn("name","现金流量项目名称");
        ExcelColumn AUXILIARY_NAMES = new ExcelColumn("auxiliaryNames","辅助核算");
        ExcelColumn OCCUR_AMOUNT = new ExcelColumn("occurAmount","本期数");
        ExcelColumn TOTAL_AMOUNT = new ExcelColumn("totalAmount","本年数");
    }

    /**
     * 总分类账/明细分类账导出字段
     */
    interface GeneralLedger {
        ExcelColumn SUBJECT_CODE = new ExcelColumn("subjectCode", "科目编码");
        ExcelColumn SUBJECT_NAME = new ExcelColumn("subjectName", "科目名称");
        ExcelColumn ACCOUNT_BOOK_NAME = new ExcelColumn("accountBookName", "总账账簿");
        ExcelColumn ACCOUNT_BOOK_ENTITY_NAME = new ExcelColumn("accountBookEntityName", "核算主体");
        ExcelColumn PERIOD_YEAR = new ExcelColumn("periodYear", "会计年度");
        ExcelColumn PERIOD_NUM = new ExcelColumn("periodNum", "期间");
        ExcelColumn SUMMARY = new ExcelColumn("summary", "摘要");
        ExcelColumn DEBIT = new ExcelColumn("debit", "借方");
        ExcelColumn CREDIT = new ExcelColumn("credit", "贷方");
        ExcelColumn BALANCE_DIRECTION = new ExcelColumn("balanceDirection", "方向", ExcelDataConstant.SYSTEM_DATA_BALANCE_DIRECTION);
        ExcelColumn BALANCE = new ExcelColumn("balance", "余额");
        ExcelColumn VOUCHER_DATE = new ExcelColumn("voucherDate", "日期");
        ExcelColumn VOUCHER_WORD = new ExcelColumn("voucherWord", "凭证字号");
    }

    /**
     * 多栏明细账
     */
    interface MultiColumn{
        ExcelColumn ACCOUNT_BOOK_ENTITY_NAME = new ExcelColumn("accountBookEntityName","核算主体");
        ExcelColumn VOUCHER_DATE    = new ExcelColumn("voucherDate","日期");
        ExcelColumn CREDENTIAL_WORD_CODE = new ExcelColumn("credentialWordCode","凭证字号");
        ExcelColumn ABSTRACT_CONTENT = new ExcelColumn("abstractContent","摘要");
        ExcelColumn TOTAL_DEBIT_AMOUNT = new ExcelColumn("totalDebitAmount","合计借方");
        ExcelColumn TOTAL_CREDIT_AMOUNT = new ExcelColumn("totalCreditAmount","合计贷方");
        ExcelColumn BALANCE_DIRECTION_NAME = new ExcelColumn("balanceDirectionName","方向");
        ExcelColumn TOTAL_BALANCE = new ExcelColumn("totalBalance","合计余额");
    }

    /**
     * 现金流量期初
     */
    interface CashFlowInit{
        ExcelColumn CODE = new ExcelColumn("code","现金流量项目编码");
        ExcelColumn NAME    = new ExcelColumn("name","现金流量项目名称");
        ExcelColumn CASH_FLOW_DIRECTION = new ExcelColumn("cashFlowDirection","方向", ExcelDataConstant.SYSTEM_DATA_CASH_FLOW_DIRECTION);
        ExcelColumn OPENING_BALANCE = new ExcelColumn("openingBalance","期初余额");
    }

    /**
     * 科目期初
     */
    interface SubjectInit{
        ExcelColumn CODE = new ExcelColumn("code","科目编码");
        ExcelColumn NAME    = new ExcelColumn("name","科目名称");
        ExcelColumn BALANCE_DIRECTION = new ExcelColumn("balanceDirection","方向", ExcelDataConstant.SYSTEM_DATA_BALANCE_DIRECTION);
        ExcelColumn OPENING_BALANCE = new ExcelColumn("openingBalance","期初累计");
        ExcelColumn THIS_YEAR_DEBIT_AMOUNT = new ExcelColumn("thisYearDebitAmount","本年借方");
        ExcelColumn THIS_YEAR_CREDIT_AMOUNT = new ExcelColumn("thisYearCreditAmount","本年贷方");
        ExcelColumn YEAR_OPENING_BALANCE = new ExcelColumn("yearOpeningBalance","年初余额");
    }

    /**
     * 总账初始化
     */
    interface BalanceInit{
        String ACCOUNT_BOOK_NAME = "总账初始化异常明细";
        ExcelColumn CHECK_CONTEXT    = new ExcelColumn("checkContext","检查项");
        ExcelColumn CHECK_STATUS = new ExcelColumn("checkStatus","状态");
        ExcelColumn CHECK_DESCRIPTION = new ExcelColumn("checkDescription","检查结果");
    }
}
