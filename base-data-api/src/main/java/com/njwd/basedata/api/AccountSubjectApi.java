package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.dto.AccountSubjectAuxiliaryDto;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.*;
import com.njwd.exception.ResultCode;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * 会计科目
 *
 * @author 朱小明
 * @date 2019/6/12
 */
@RequestMapping("financeback/accountSubject")
public interface AccountSubjectApi {
    /**
     * 初始化会计科目基准数据.
     *
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/7/12
     */
    @PostMapping("addInitInfo")
    Result<Long> addInitInfo();

    /**
     * 引入会计科目模板数据.
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("addRoot")
    Result<Long> addRoot(AccountSubjectDto accountSubjectDto);

    /**
     * 新增会计科目相关信息.
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("add")
    Result<Long> add(AccountSubjectDto accountSubjectDto);

    /**
     * 更新会计科目相关信息.
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("update")
    Result<Long> update(AccountSubjectDto accountSubjectDto);

    /**
     * 根据id集合批量删除会计科目信息
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("updateBatchDelete")
    Result<BatchResult> updateBatchDelete(AccountSubjectDto accountSubjectDto);

    /**
     * 根据code集合禁用会计科目信息
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("updateBatchDisable")
    Result<BatchResult> updateBatchDisable(AccountSubjectDto accountSubjectDto);

    /**
     * 根据code集合反禁用会计科目信息
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("updateBatchEnable")
    Result<BatchResult> updateBatchEnable(AccountSubjectDto accountSubjectDto);

    /**
     * 分页查询会计科目模板数据列表
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("findRootPage")
    Result<List<AccountSubjectVo>> findRootPage(AccountSubjectDto accountSubjectDto);

    /**
     * 账簿类型下拉框
     *
     * @param
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/25
     */
    @PostMapping("findAccountBookTypeSelection")
    Result<List<AccountBookTypeVo>> findAccountBookTypeSelection();

    /**
     * 会计准则下拉框
     *
     * @param
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/25
     */
    @PostMapping("findAccountingStandardSelection")
    Result<List<AccountingStandardVo>> findAccountingStandardSelection();

    /**
     * 科目表下拉框
     *
     * @param subjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("findSubjectSelection")
    Result<List<SubjectVo>> findSubjectSelection(SubjectDto subjectDto);

    /**
     * 会计要素下拉框
     *
     * @param subjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/21
     */
    @PostMapping("findSelectionList")
    Result<Map<String, Object>> findSelectionList(@RequestBody SubjectDto subjectDto);

    /**
     * 查询辅助核算设置列表
     *
     * @return Result
     * @author 周鹏
     * @date 2019/8/23
     */
    @PostMapping("findAuxiliaryItemList")
    Result<Map<String, Object>> findAuxiliaryItemList(@RequestBody SubjectDto subjectDto);

    /**
     * 查询已配置辅助核算项目列表.
     *
     * @param subjectDto
     * @return java.lang.String
     * @author 周鹏
     * @date 2019/8/27
     */
    @PostMapping("findSubjectAuxiliaryList")
    Result<List<SubjectAuxiliaryVo>> findSubjectAuxiliaryList(SubjectDto subjectDto);

    /**
     * 根据id查询会计科目信息.
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("findInfoById")
    Result<AccountSubjectVo> findInfoById(AccountSubjectDto accountSubjectDto);

    /**
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @Description 根据id查询会计科目信息
     * @Author liuxiang
     * @Date:14:35 2019/8/15
     * @Param [accountSubjectDto]
     **/
    @PostMapping("findNamesByIds")
    Result<List<AccountSubjectVo>> findNamesByIds(AccountSubjectDto accountSubjectDto);

    /**
     * 分页查询会计科目列表信息
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("findPage")
    Result<Page<AccountSubjectVo>> findPage(AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目信息是否被引用
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/7/2
     */
    @PostMapping("findIfIsCited")
    Result<Boolean> findIfIsCited(AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目信息是否末级
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/7/25
     */
    @PostMapping("findIfIsFinal")
    Result<Boolean> findIfIsFinal(AccountSubjectDto accountSubjectDto);

    /**
     * 根据id查询所有上级科目信息
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 周鹏
     * @date 2019/8/6
     */
    @PostMapping("findAllParentInfo")
    Result<List<AccountSubjectVo>> findAllParentInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询会计科目信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/8/6
     */
    @PostMapping("findInfoForLedger")
    Result<List<AccountSubjectVo>> findInfoForLedger(@RequestBody AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询单个辅助核算来源表信息(支持总账需求,后端用)
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/8/6
     */
    @PostMapping("findSourceTableInfo")
    Result<Map<Long, Map<String, Object>>> findSourceTableInfo(@RequestBody AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询所有辅助核算来源表信息(支持总账需求,后端用)
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/8/31
     */
    @PostMapping("findAllSourceTableInfo")
    Result<List<List<Map<String, Object>>>> findAllSourceTableInfo(@RequestBody AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询相应辅助核算来源表信息列表(支持总账需求,前端用)
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/28
     */
    @PostMapping("findSourceTableList")
    Result<Page<Map<String, Object>>> findSourceTableList(@RequestBody AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询会计科目关联的辅助核算项组合(支持总账需求)
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/21
     */
    @PostMapping("findAuxiliaryGroup")
    Result<List<AccountSubjectAuxiliaryVo>> findAuxiliaryGroup(@RequestBody AccountSubjectDto accountSubjectDto);

    /**
     * 校验会计科目编码和名称是否重复.
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/6/12
     */
    @PostMapping("checkDuplicateInfo")
    Result<ResultCode> checkDuplicateInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 校验预置科目下是否存在下级预置科目.
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 朱小明
     * @date 2019/7/25
     */
    @PostMapping("checkNextInitInfo")
    Result<ResultCode> checkNextInitInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据code和subjectId查询所有下级科目信息
     *
     * @param accountSubjectDto
     * @return Result<List<AccountSubjectVo>>
     * @author: wuweiming
     * @create: 2019/08/06
     */
    @PostMapping("findAllChildInfo")
    Result<List<AccountSubjectVo>> findAllChildInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目信息
     *
     * @param accountSubjectDto
     * @return Result<List<AccountSubjectVo>>
     * @author: 朱小明
     * @create: 2019/08/23
     */
    @PostMapping("findAccountSubjectByElement")
    Result<List<AccountSubjectVo>> findAccountSubjectByElement(AccountSubjectDto accountSubjectDto);

    /**
     * 根据会计科目查询辅助核算信息列表(支持总账需求)
     *
     * @param accountSubjectAuxiliaryDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/28
     */
    @PostMapping("findAuxiliaryList")
    Result<List<AccountSubjectAuxiliaryVo>> findAuxiliaryList(AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto);

    /**
     * @description: 获取会计科目辅助核算
     * @param: [accountSubjectAuxiliaryDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.AccountSubjectAuxiliary>
     * @author: xdy
     * @create: 2019-09-03 15:35
     */
    @PostMapping("findAccountSubjectAuxiliary")
    Result<AccountSubjectAuxiliary> findAccountSubjectAuxiliary(@RequestBody AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto);

    /**
     * 根据code集合校验批量禁用与反禁用
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/9/3
     */
    @PostMapping("checkUpdateBatch")
    Result<Boolean> checkUpdateBatch(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询科目信息
     *
     * @param dto
     * @return
     */
    @PostMapping("findSubjectInfoByParam")
    Result<AccountSubjectVo> findSubjectInfoByParam(@RequestBody AccountSubjectDto dto);

    /**
     * 根据条件查询科目信息
     *
     * @param dto
     * @return
     */
    @PostMapping("findSubjectInfoByParamWithCodes")
    Result<AccountSubjectVo> findSubjectInfoByParamWithCodes(@RequestBody AccountSubjectDto dto);
}