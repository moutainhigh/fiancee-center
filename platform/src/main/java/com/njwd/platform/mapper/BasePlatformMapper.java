package com.njwd.platform.mapper;

import com.njwd.entity.platform.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * platform批量处理mapper
 *
 * @author xyyxhcj@qq.com
 * @since 2019-11-15
 */

public interface BasePlatformMapper {
	/**
	 * 批量处理mapper
	 *
	 * @param idList idList
	 * @param operator  operator
	 * @param type      操作类型 0删除 1审核 2反审核 3发布
	 * @param tableName Constant.TableName
	 * @return int
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/15 15:47
	 **/
	int batchProcess(@Param("idList") @NotEmpty Collection<Long> idList, @Param("type") @NotNull int type, @Param("operator") @NotNull SysUserVo operator, @Param("tableName") String tableName);
}
