package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.UserAccountBookEntity;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserAccountBookEntityVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/6/19
 */
public interface UserAccountBookEntityMapper extends BaseMapper<UserAccountBookEntity> {
	/**
	 * 批量插入用户关联的账簿主体
	 *
	 * @param companyId            公司id
	 * @param accountBookEntityIds 账簿主体ids
	 * @param sysUserDto           sysUserDto
	 * @param operator             操作人
	 * @return int
	 */
	int insertBatch(@Param("companyId") Long companyId, @Param("accountBookEntityIds") Long[] accountBookEntityIds, @Param("sysUserDto") SysUserDto sysUserDto, @Param("operator") SysUserVo operator);

	/**
	 * 获取用户的账簿主体列表
	 *
	 * @param sysUser          userId/companyId
	 * @param rootEnterpriseId rootEnterpriseId
	 * @return map
	 */
	List<UserAccountBookEntityVo> findAccountBookEntityList(@Param("sysUser") SysUserVo sysUser, @Param("rootEnterpriseId") Long rootEnterpriseId);
}