package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 会计科目mapper
 * @Author 周鹏
 * @Date 2019/6/12
 */
public interface AccountSubjectMapper extends BaseMapper<AccountSubject> {
    /**
     * 删除科目表信息
     *
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/26
     */
    int delete(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 更新是否末级字段
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/8/27
     */
    int updateIsFinal(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 单条更新删除状态
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int updateSingleDelete(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 批量更新删除状态
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int updateBatchDelete(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 单条更新启用状态
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int updateSingleEnable(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 批量更新启用状态
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/6/12
     */
    int updateBatchEnable(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据id查询会计科目信息
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/6/12
     */
    AccountSubjectVo findInfoById(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询上级会计科目信息
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/28
     */
    AccountSubjectVo findParentInfoByParam(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 分页查询会计科目列表信息
     *
     * @param accountSubjectDto
     * @param page
     * @return list
     * @author: 周鹏
     * @create: 2019/6/12
     */
    Page<AccountSubjectVo> findPage(@Param("page") Page<AccountSubjectVo> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询会计科目信息id
     *
     * @param accountSubjectDto
     * @return list
     * @author: 周鹏
     * @create: 2019/7/2
     */
    List<Long> findIds(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询所有需要禁用/反禁用的会计科目id
     *
     * @param accountSubjectDto
     * @return list
     * @author: 周鹏
     * @create: 2019/8/22
     */
    List<Long> findOperateIdsByParam(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询会计科目信息
     *
     * @param accountSubjectDto
     * @return list
     * @author: 周鹏
     * @create: 2019/6/24
     */
    List<AccountSubjectVo> findByParam(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询可以被更新为末级科目的数据id
     *
     * @param list
     * @param rootEnterpriseId
     * @return list
     * @author: 周鹏
     * @create: 2019/7/22
     */
    List<Long> findToBeFinalList(@Param("list") List<Long> list, @Param("rootEnterpriseId") Long rootEnterpriseId);

    /**
     * 根据条件查询会计科目信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/6
     */
    List<AccountSubjectVo> findInfoForLedger(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询单个辅助核算来源表信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/6
     */
    @MapKey("id")
    Map<Long, Map<String, Object>> findSourceTableInfo(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询核算主体的entityId集合
     * @param accountSubjectDto
     * @return AccountBookEntityVo
     */
    List<AccountBookEntityVo> findEntityIdList(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询员工表信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findStaffInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询部门表信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findDeptInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询项目信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findProjectInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询客户信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findCustomerInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询供应商信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findSupplierInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询银行账号信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findBankAccountInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询核算主体表信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findAccountBookEntityInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 查询自定义辅助核算项表信息(支持总账需求)
     *
     * @param page
     * @param accountSubjectDto
     * @return Map
     * @author: 周鹏
     * @create: 2019/8/28
     */
    Page<Map<String, Object>> findAccountingItemValueInfo(@Param("page") Page<Map<String, Object>> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 根据条件查询会计科目信息
     *
     * @param dto
     * @return List<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/08/06
     */
    List<AccountSubjectVo> findSubjectInfoByParam(@Param("dto") AccountSubjectDto dto);

    /**
     * 校验科目编码是否重复
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @SqlParser(filter=true)
    int checkDuplicateCode(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 校验科目名称是否重复
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @SqlParser(filter=true)
    int checkDuplicateName(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

    /**
     * 校验预置科目下是否存在下级预置科目
     *
     * @param accountSubjectDto
     * @return count
     * @author: 周鹏
     * @create: 2019/7/25
     */
    int checkNextInitInfo(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

}