package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.SubjectAuxiliaryDto;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.AccountSubjectAuxiliaryDto;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectAuxiliaryVo;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.support.BatchResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Description 会计科目接口
 * @Author 周鹏
 * @Date 2019/6/12
 */
public interface AccountSubjectService {
    /**
     * 新增会计科目模板数据
     *
     * @param list
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int addRoot(List<AccountSubjectDto> list);

    /**
     * 新增会计科目相关信息
     *
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int add(AccountSubjectDto accountSubjectDto);

    /**
     * 新增会计科目相关信息(公共方法)
     *
     * @param accountSubject
     * @param accountSubjectDto
     * @param type
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int addInfo(AccountSubjectDto accountSubjectDto, String type);

    /**
     * 新增科目表辅助核算项
     *
     * @param subjectAuxiliaryDto
     * @return int
     * @author: 周鹏
     * @create: 2019/8/24
     */
    int addSubjectAuxiliary(SubjectAuxiliaryDto subjectAuxiliaryDto);

    /**
     * 更新会计科目相关信息
     *
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int update(AccountSubjectDto accountSubjectDto);

    /**
     * 根据id集合批量删除会计科目信息
     *
     * @param accountSubjectDto
     * @return UpdateResult
     * @author: 周鹏
     * @create: 2019/6/12
     */
    BatchResult updateDelete(AccountSubjectDto accountSubjectDto);

    /**
     * 根据id集合批量禁用和反禁用会计科目信息
     *
     * @param accountSubjectDto
     * @return UpdateResult
     * @author: 周鹏
     * @create: 2019/6/12
     */
    BatchResult updateEnable(AccountSubjectDto accountSubjectDto);

    /**
     * 查询租户下是否存在会计科目信息
     *
     * @param rootEnterpriseId
     * @return int
     * @author: 周鹏
     * @create: 2019/7/15
     */
    int findCount(Long rootEnterpriseId);

    /**
     * 根据id查询会计科目信息
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/6/12
     */
    AccountSubjectVo findInfoById(AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目基本信息及辅助核算项信息
     *
     * @param accountSubjectDto
     * @return
     */
    AccountSubjectVo findAccountSubjectById(AccountSubjectDto accountSubjectDto);

    /**
     * 分页查询会计科目列表信息
     *
     * @param accountSubjectDto
     * @return page
     * @author: 周鹏
     * @create: 2019/6/12
     */
    Page<AccountSubjectVo> findPage(AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目信息platformId
     *
     * @param accountSubjectDto
     * @return list
     * @author: 周鹏
     * @create: 2019/12/4
     */
    List<Long> findIds(AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目信息是否被引用
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/7/2
     */
    Boolean findIfIsCited(AccountSubjectDto accountSubjectDto);

    /**
     * 查询当前科目表是否已经增加新的科目
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/8/22
     */
    Boolean findIfExistNewInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据id查询所有上级科目信息
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/2
     */
    List<AccountSubjectVo> findAllParentInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询会计科目信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/6
     */
    List<AccountSubjectVo> findInfoForLedger(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询单个辅助核算来源表信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return Object
     * @author: 周鹏
     * @create: 2019/8/6
     */
    Map<Long, Map<String, Object>> findSourceTableInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询所有辅助核算来源表信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return Object
     * @author: 周鹏
     * @create: 2019/8/6
     */
    List<List<Map<String, Object>>> findAllSourceTableInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询相应辅助核算来源表信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return Object
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findSourceTableList(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询会计科目关联的辅助核算项组合(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/21
     */
    List<AccountSubjectAuxiliaryVo> findAuxiliaryGroup(AccountSubjectDto accountSubjectDto);

    /**
     * 根据会计科目查询辅助核算信息列表(支持总账需求)
     *
     * @param accountSubjectAuxiliaryDto
     * @return AccountSubjectAuxiliaryVo
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<AccountSubjectAuxiliaryVo> findAuxiliaryPage(AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto);

    /**
     * 根据科目表id查询已配置辅助核算项
     *
     * @param subjectAuxiliaryDto
     * @return SubjectAuxiliaryVo
     * @author: 周鹏
     * @create: 2019/8/23
     */
    List<SubjectAuxiliaryVo> findSubjectAuxiliaryList(SubjectAuxiliaryDto subjectAuxiliaryDto);

    /**
     * 根据条件查询会计科目信息
     *
     * @param dto
     * @return List<AccountSubjectVo>
     * @author: 周鹏
     * @create: 2019/08/09
     */
    List<AccountSubjectVo> findSubjectInfoByParam(AccountSubjectDto dto);

    /**
     * 根据code和subjectId查询所有下级科目信息
     *
     * @param accountSubjectDto
     * @return List<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/08/06
     */
    List<AccountSubjectVo> findAllChildInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询科目表信息
     *
     * @param subjectDto
     * @return Subject
     * @author: 周鹏
     * @create: 2019/8/21
     */
    Subject findSubjectByParam(SubjectDto subjectDto);

    /**
     * @description: 获取会计科目辅助核算信息
     * @param: [accountSubjectAuxiliaryDto]
     * @return: com.njwd.entity.platform.AccountSubjectAuxiliary
     * @author: xdy
     * @create: 2019-09-03 11:27
     */
    AccountSubjectAuxiliary findAccountSubjectAuxiliary(AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto);

    /**
     * 根据条件查询科目表信息
     *
     * @param subjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/11/12
     */
    Subject findSubjectInfo(SubjectDto subjectDto);

    /**
     * Excel 导出
     *
     * @param accountSubjectDto
     * @param response
     * @author: 周鹏
     * @create: 2019/6/12
     */
    void exportExcel(AccountSubjectDto accountSubjectDto, HttpServletResponse response);

    /**
     * 校验会计科目编码和名称是否重复
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/6/12
     */
    Boolean checkDuplicateInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 校验预置科目下是否存在下级预置科目
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/7/25
     */
    Boolean checkNextInitInfo(AccountSubjectDto accountSubjectDto);

    /**
     * 根据code集合校验批量禁用与反禁用
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/9/3
     */
    Boolean checkUpdateBatch(AccountSubjectDto accountSubjectDto);

}
