package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.SubjectAuxiliary;
import com.njwd.entity.basedata.dto.SubjectAuxiliaryDto;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 科目表辅助核算项mapper
 * @Author 周鹏
 * @Date 2019/8/23
 */
public interface SubjectAuxiliaryMapper extends BaseMapper<SubjectAuxiliary> {
    /**
     * 新增科目表辅助核算项
     *
     * @param list
     * @param subjectAuxiliaryDto
     * @return int
     * @author: 周鹏
     * @create: 2019/8/24
     */
    int addSubjectAuxiliary(@Param("list") List<SubjectAuxiliaryDto> list, @Param("subjectAuxiliaryDto") SubjectAuxiliaryDto subjectAuxiliaryDto);

    /**
     * 根据科目表id删除已配置辅助核算项
     *
     * @param subjectId
     * @param isInit
     * @return int
     * @author: 周鹏
     * @create: 2019/8/24
     */
    int deleteBySubjectId(@Param("subjectId") Long subjectId, @Param("isInit") Byte isInit);

    /**
     * 根据科目表id查询已配置辅助核算项
     *
     * @param subjectAuxiliaryDto
     * @return SubjectAuxiliaryVo
     * @author: 周鹏
     * @create: 2019/8/23
     */
    List<SubjectAuxiliaryVo> findSubjectAuxiliaryList(SubjectAuxiliaryDto subjectAuxiliaryDto);

}