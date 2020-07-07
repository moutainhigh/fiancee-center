package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.SubjectAuxiliaryDto;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.dto.AccountSubjectAuxiliaryDto;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectAuxiliaryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 会计科目与辅助核算项目关系mapper
 * @Author 周鹏
 * @Date 2019/6/12
 */
public interface AccountSubjectAuxiliaryMapper extends BaseMapper<AccountSubjectAuxiliary> {
    /**
     * 删除会计科目与辅助核算关系信息
     *
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/26
     */
    int delete(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 删除会计科目与辅助核算关系信息
     *
     * @param accountSubjectAuxiliary
     * @return count
     */
    int deleteByParam(@Param("accountSubjectAuxiliary") AccountSubjectAuxiliary accountSubjectAuxiliary);

    /**
     * 更新会计科目与辅助核算关系信息
     *
     * @param accountSubjectAuxiliaryDto
     * @return int
     * @author: 周鹏
     * @create: 2019/8/27
     */
    int updateAccountSubjectId(@Param("accountSubjectAuxiliaryDto") AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto);

    /**
     * 查询会计科目与币种关系信息数量
     *
     * @param accountSubjectAuxiliary
     * @return count
     */
    int findCountByParam(@Param("accountSubjectCurrency") AccountSubjectAuxiliary accountSubjectAuxiliary);

    /**
     * 查询已被会计科目使用的辅助核算项名称
     *
     * @param list
     * @param subjectId
     * @return count
     */
    AccountSubjectAuxiliary findUsedAuxiliaryName(@Param("list") List<SubjectAuxiliaryDto> list, @Param("subjectId") Long subjectId);

    /**
     * 根据会计科目id查询辅助核算信息
     *
     * @param accountSubjectDto
     * @return AccountSubjectAuxiliary
     */
    List<AccountSubjectAuxiliary> findListById(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询会计科目关联的辅助核算项组合(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/21
     */
    List<AccountSubjectAuxiliaryVo> findAuxiliaryGroup(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据会计科目查询辅助核算信息列表(支持总账需求)
     *
     * @param accountSubjectAuxiliaryDto
     * @param page
     * @return AccountSubjectAuxiliaryVo
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<AccountSubjectAuxiliaryVo> findAuxiliaryPage(@Param("page") Page<AccountSubjectAuxiliaryVo> page, @Param("accountSubjectAuxiliaryDto") AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto);
}