package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.vo.AccountSubjectAuxiliaryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AccountSubjectAuxiliaryMapper extends BaseMapper<AccountSubjectAuxiliary> {
	/**
	 * 查会计科目下的辅助核算
	 *
	 * @param accSubjectId accSubjectId
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectAuxiliaryVo>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/27 14:52
	 **/
	List<AccountSubjectAuxiliaryVo> findList(@Param("accSubjectId") Long accSubjectId);
}
