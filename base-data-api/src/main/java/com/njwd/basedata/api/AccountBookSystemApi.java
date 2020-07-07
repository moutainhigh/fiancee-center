package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-26 16:36
 */
@RequestMapping("financeback/accountBookSystem")
public interface AccountBookSystemApi {

    /**
     * 根据子系统标识 查询当前企业已启用子系统列表
     *
     * @param accountBookSystemDto
     * @return
     */
    @RequestMapping("findLedgerList")
    Result<Page<AccountBookSystemVo>> findLedgerList(@RequestBody AccountBookSystemDto accountBookSystemDto);
    //根据子系统标识 查询当前企业用户已启用子系统列表
    @RequestMapping("findLedgerListByUserId")
    Result<Page<AccountBookSystemVo>> findLedgerListByUserId(@RequestBody AccountBookSystemDto accountBookSystemDto);

    /**
     * 初始化子系统
     *
     * @param accountBookSystemDto
     * @return
     */
    @RequestMapping("initSystemById")
    Result<Integer> initSystemById(@RequestBody AccountBookSystemDto accountBookSystemDto);

    /**
     * 反初始化子系统
     *
     * @param accountBookSystemDto
     * @return
     */
    @RequestMapping("antiInitSystemById")
    Result<Integer> antiInitSystemById(@RequestBody AccountBookSystemDto accountBookSystemDto);

    /**
     * @Description 根据系统标识+账簿ID+会计年度+会计期间查询是否已初始化
     * @Author 朱小明
     * @Date 2019/8/14 16:16
     * @Param [accountBookSystemDto]
     * @return com.njwd.support.Result<java.lang.Integer>
     **/
    @RequestMapping("findInitStatusByCondition")
    Result<AccountBookSystem> findInitStatusByCondition(@RequestBody AccountBookSystemDto accountBookSystemDto);

    /**
     * 根据 账簿ID/子系统标识 查询已启用子系统
     *
     * @param accountBookSystemDto
     * @return
     */
    @RequestMapping("findEnableList")
    Result<List<AccountBookSystemVo>> findEnableList(AccountBookSystemDto accountBookSystemDto);

}
