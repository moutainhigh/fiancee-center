package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.platform.AccountSubjectCurrency;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author liuxiang
 *         前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountSubjectDto<T> extends AccountSubjectVo {
    private static final long serialVersionUID = 6684189748516136175L;

    private Page<T> page = new Page<>();

    /**
     * 科目表ID集合
     */
    private List<Long> subjectIds;

    /**
     * 会计科目ID集合
     */
    private List<Long> ids;

    /**
     * 被引用会计科目集合
     */
    private List<ReferenceDescription> isCitedList;

    /**
     * 会计科目code集合
     */
    private List<String> codes;

    /**
     * 原上级科目编码
     */
    private String originalUpCode;

    /**
     * 账簿类型ID 对应【账簿类型】表ID
     */
    private String accountBookTypeId;

    /**
     * 账簿类型名称 对应【账簿类型】表NAME
     */
    private String accountBookTypeName;

    /**
     * 会计准则ID 对应【会计准则】表ID
     */
    private Long accStandardId;

    /**
     * 会计准则名称 对应【会计准则】表NAME
     */
    private String accStandardName;

    /**
     * 科目模板表ID 对应【科目】表科目模板表ID
     */
    private Long templateSubjectId;

    /**
     * 科目模板表名称 对应【科目】表科目模板表NAME
     */
    private String templateSubjectName;

    /**
     * 内部往来名称 0：否、1：是
     */
    private String isInteriorName;

    /**
     * 编码或名称 查询条件
     */
    private String codeOrName;

    /**
     * 校验重复字段集合
     */
    private List<String> columnList;

    /**
     * 校验重复提示
     */
    private String message;

    /**
     * 是否是租户管理员
     */
    private Byte isEnterpriseAdmin;

    /**
     * 值来源表
     */
    private String sourceTable;

    /**
     * 会计科目基准表id
     */
    private Long baseSubjectId;

    /**
     * 会计科目编码查询类型
     */
    private Byte subjectCodeOperator;

    /**
     * 会计科目编码区间
     */
    private List<Long> subjectCodes;

    /**
     * 会计科目级次查询类型
     */
    private Byte subjectLevelOperator;

    /**
     * 科目级次区间
     */
    private List<Byte> subjectLevels;

    /**
     * 是否仅显示末级科目(0:否 1:是)
     */
    private Byte isFinal;

    /**
     * 是否显示科目全名(0:不显示 1:显示)
     */
    private Byte isShowFullName;

    /**
     * 是否包含禁用科目(0:不包含 1:包含)
     */
    private Byte isIncludeEnable;

    /**
     * 末级科目id集合
     */
    private List<Long> finalIds;

    /**
     * 是否只查询可新增下级科目的数据列表
     */
    private Boolean ifFindCouldInsertFlag;

    /**
     * 辅助核算项目名称
     */
    private String auxiliaryName;

    /**
     * 会计要素id
     */
    private Long elementId;

    /**
     * 数据类型 1：共享、2：分配、3：私有
     */
    private Byte dataType;

    /**
     * 是否查询辅助核算项列表 0:否 1:是
     */
    private Byte ifFindAuxiliary;

    /**
     * 是否只查询有辅助核算项信息的会计科目列表  null:否 1:是
     */
    private Byte ifFindHasAuxiliaryOnly;

    /**
     * 是否只查询下级科目 null:否 1:是
     */
    private Byte ifFindChildOnly;

    /**
     * 值来源表集合
     */
    private List<String> sourceTableList;

    /**
     * itemValueId值集合
     */
    private List<List<List<Long>>> idLists;

    /**
     * code值集合
     */
    private List<List<List<String>>> codeLists;

    /**
     * 版本号集合
     */
    private List<Integer> versionList;

    /**
     * 公司ID集合
     */
    private List<Long> companyIds;

    /**
     * 业务单元ID集合
     */
    private List<Long> businessUnitIds;

    /**
     * 属性
     */
    private String attribute;

    /**
     * 核算主体形态
     */
    private Integer form;

    /**
     * 是否只查询使用公司下的数据 0:否 1:是
     */
    private Byte ifFindUseCompanyDataOnly;

    /**
     * 是否只查询未删除数据  0:否 null:是
     */
    private Byte ifFindNotDelOnly;

    /**
     * 公司和核算主体关系集合
     */
    private List<Map<String, Object>> companyAndEntityList;

    /**
     * 科目类别 【科目类别】表ID 数组
     */
    private Long[] subjectCategories;

    private List<AccountSubjectCurrency> accSubjectCurrencyList = new LinkedList<>();

    private List<AccountSubjectDto> batchEditAccSubjects = new LinkedList<>();

    /**
     * 需要引入会计科目的科目表ID
     **/
    private Long needIntroductionId;

    private MessageDto messageDto;

    private List<Long> excludedIds = new ArrayList<>();
    /**
     * 查询id或platformId  null:id 1:platformId
     */
    private Byte ifFindPlatformId;

    /**
     * 平台会计科目ID集合
     */
    private List<Long> platformIds;
}
