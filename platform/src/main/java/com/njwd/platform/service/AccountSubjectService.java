package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.FindAccountSubjectListVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计科目
 * @Date:15:47 2019/7/2
 **/
public interface AccountSubjectService{
    /**
     * @Description 更多科目模板分页查询
     * @Author liuxiang
     * @Date:15:47 2019/7/2
     * @Param [accountSubjectDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.FindAccountSubjectListVo>
     **/
    Page<FindAccountSubjectListVo> findAccountSubjectPage(AccountSubjectDto accountSubjectDto);

    /**
     * @Description 更多科目模板列表查询
     * @Author liuxiang
     * @Date:15:48 2019/7/2
     * @Param [accountSubjectVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FindAccountSubjectListVo>
     **/
    List<FindAccountSubjectListVo> findAccountSubjectList(AccountSubjectDto accountSubjectDto);

    /**
     * 分页
     *
     * @param accountSubjectDto accountSubjectDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/13 15:23
     **/
    Page<AccountSubjectVo> findPage(AccountSubjectDto<AccountSubjectVo> accountSubjectDto);

    /**
     * add
     *
     * @param accSubjectDto accSubjectDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/11/19 9:33
     **/
    Long add(AccountSubjectDto<AccountSubjectVo> accSubjectDto);

    /**
     * update
     *
     * @param accSubjectDto accSubjectDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 10:31
     **/
    Long update(AccountSubjectDto accSubjectDto);

    /**
     * delete
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 11:19
     **/
    BatchResult delete(AccountSubjectDto<AccountSubjectVo> accSubjectDto);

    /**
     * approve
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 14:03
     **/
    BatchResult approve(AccountSubjectDto<AccountSubjectVo> accSubjectDto);

    /**
     * reversalApprove
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 14:12
     **/
    BatchResult reversalApprove(AccountSubjectDto<AccountSubjectVo> accSubjectDto);

    /**
     * findDetail
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.entity.platform.vo.AccountSubjectVo
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 14:18
     **/
    AccountSubjectVo findDetail(AccountSubjectDto<AccountSubjectVo> accSubjectDto);

    /**
     * release
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 15:18
     **/
    BatchResult release(AccountSubjectDto<AccountSubjectVo> accSubjectDto);

    /**
     * introduction
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/21 10:38
     **/
	BatchResult introduction(AccountSubjectDto<AccountSubjectVo> accSubjectDto);

    /**
     * export
     *
     * @param accountSubjectDto accountSubjectDto
     * @param response          response
     * @author xyyxhcj@qq.com
     * @date 2019/11/22 17:54
     **/
    void exportExcel(AccountSubjectDto<AccountSubjectVo> accountSubjectDto, HttpServletResponse response);
}
