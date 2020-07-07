package com.njwd.common;

/**
 * 日志常量定义
 */
public interface LogConstant {
    /**
     * 子系统名称
     */
    interface sysName{
        String FinanceBackSys = "基础资料";
        String LedgerSys = "总账";
        String PlatformSys="运营平台";
    }

    /**
     *菜单名 (对应三级菜单)
     */
    interface menuName{
        //权限、公司
        String company = "公司";
        String busiUnit = "业务单元";
        String postAuth = "岗位权限";
        String users = "用户";
        String userRights = "用户权限表";
        String accountbook = "核算账簿";

        //基础资料部分
        String accoutSubject = "会计科目";
        String cashFlowItems = "现金流量项目";
        String department = "部门";
        String employee = "员工";
        String customer = "客户";
        String supplier = "供应商";
        String project = "项目";
        String bankAccount = "银行账号";
        String customerAccoutingItem = "自定义核算项目";
        String customerAccoutingItemValue = "自定义核算项目大区值";
        String subjectAuxiliary = "科目表辅助核算项";
        String subjectSynergy = "科目协同配置";

        //财务总账
        String commonAbstract = "常用摘要";
        String parameterSet="总账参数";
        String multiColumnScheme="多栏账方案";
        String multiColumnSchemeItem="多栏账明细";
        String voucher="凭证";
        String postPeriod = "过账";
        String cashFlowInit = "现金流量期初";
        String subjectInit = "科目期初";
        String balanceInit = "总账期初";
        String balanceSettle = "期末处理";
        String balanceInitRecord = "添加核算主体";

        //运营平台
        String costItem = "费用项目";
        String unit="计量单位";
        String auxiliaryItem="核算项目";
        String currency = "币种";
        String credentialWord = "凭证字";
        String taxSystem = "税制";
        String taxCategory = "税种";
        String taxRate = "税率";
        String accountBookCategory = "核算账簿分类";
        String accountingCalendar = "会计日历";
        String cashFlow = "现金流量项目表";
        String financialReport = "财务报表";
        String financialReportItem = "财务报表项目库";
        String financialReportItemSet = "财务报表项目";
        String subject = "会计科目表";
        String userManage = "用户管理";
        String enterpriseManage = "用户管理";
    }

    /**
     * 操作：
     *  新增、修改、删除、授权、批量授权、禁用、反禁用、新增下级、导入、建账
     */
    interface operation{
        String add = "新增";
        String add_type = "add";
        String update = "修改";
        String update_type = "update";
        String delete = "删除";
        String delete_type = "delete";
        String accCreation = "建账";
        String accCreation_type = "accCreation";
        String forbidden = "禁用";
        String forbidden_type = "forbidden";
        String antiForbidden = "反禁用";
        String antiForbidden_type = "antiForbidden";
        String deleteBatch = "批量删除";
        String deleteBatch_type = "deleteBatch";
        String forbiddenBatch = "批量禁用";
        String forbiddenBatch_type = "forbiddenBatch";
        String antiForbiddenBatch = "批量反禁用";
        String antiForbiddenBatch_type = "antiForbiddenBatch";
        String addAccounting = "建账";
        String addAccounting_type = "addAccounting";
        String addAccountingBatch = "批量建账";
        String addAccountingBatch_type = "addAccountingBatch";
	    String auth = "授权";
	    String auth_type = "assign";
	    String authBatch = "批量授权";
	    String authBatch_type = "assignBatch";
        String unAuthBatch = "批量取消授权";
        String unAuthBatch_type = "unAssignBatch";
        String authAdmin = "业务管理员授权";
        String authAdmin_type = "assignAdmin";
	    String addBatch = "批量新增";
	    String addBatch_type = "addBatch";
	    String save = "保存";
	    String save_type = "save";
        String approve = "审核";
        String approve_type = "approve";
        String reversalApprove = "反审核";
        String reversalApprove_type = "reversalApprove";
        String review = "复核";
        String review_type = "review";
        String reversalReview = "反复核";
        String reversalReview_type = "reversalReview";
        String draft = "暂存";
        String draft_type = "draft";
        String generateOffset = "冲销";
        String generateOffset_type = "generateOffset";
        String saveCashFlow = "保存现金流量";
        String saveCashFlow_type = "saveCashFlow";
        String postPeriod = "过账";
        String postPeriod_type = "postPeriod";
        String clear = "清空";
        String clear_type = "clear";
        String init = "初始化";
        String init_type = "init";
        String disInit = "反初始化";
        String disInit_type = "disInit";
        String initBatch = "批量初始化";
        String initBatch_type = "initBatch";
        String disInitBatch = "批量反初始化";
        String disInitBatch_type = "disInitBatch";
        String settle = "结账";
        String settle_type = "settle";
        String cancelSettle = "反结账";
        String cancleSettleType = "cancleSettle";
        String release="发布";
        String release_type="release";
        String introduction="引入";
        String introduction_type="introduction";
    }

}
