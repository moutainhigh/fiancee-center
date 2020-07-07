package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.BatchOperationDetails;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.SysSystemVo;

import java.util.List;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-25 16:52
 */
public interface AccountBookSystemService {

    /**
     * 查询 子系统及其状态 列表
     *
     * @param: [accountBookSystemDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:43
     */
    List<SysSystemVo> findList(AccountBookSystemDto accountBookSystemDto);

    /**
     * 查询 当前企业总帐列表
     *
     * @param: [accountBookSystemDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:43
     */
    Page<AccountBookSystemVo> findLedgerList(AccountBookSystemDto accountBookSystemDto);
    //根据子系统标识 查询当前企业用户已启用子系统列表
    Page<AccountBookSystemVo> findLedgerListByUserId(AccountBookSystemDto accountBookSystemDto);

    /**
     * 根据 账簿ID/子系统标识 查询已启用子系统
     *
     * @param: [accountBookSystemDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    List<AccountBookSystemVo> findEnableList(AccountBookSystemDto accountBookSystemDto);

    /**
     * 启用核子系统
     *
     * @param: [accountBookDto]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    int enableAccountBookSystem(AccountBookDto accountBookDto);

    /**
     * 批量 启用子系统
     *
     * @param: [accountBookDtos]
     * @return: com.njwd.entity.basedata.BatchOperationDetails
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    BatchOperationDetails enableAccountBookSystemBatch(List<AccountBookDto> accountBookDtos);

    /**
     * 反启用 子系统
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.BatchOperationDetails
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    BatchOperationDetails antiEnableAccountBookSystem(AccountBookDto accountBookDto);

    /**
     * 批量 反启用
     *
     * @param: [accountBookDtos]
     * @return: com.njwd.entity.basedata.BatchOperationDetails
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    BatchOperationDetails antiEnableAccountBookSystemBatch(List<AccountBookDto> accountBookDtos);

    /**
     * 根据ID 修改子系统初始化状态
     *
     * @param: [accountBookSystemDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    Integer updateById(AccountBookSystemDto accountBookSystemDto);

    /**
     * 初始化子系统
     *
     * @param: [accountBookSystemDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    Integer initSystemById(AccountBookSystemDto accountBookSystemDto);

    /**
     * 反初始化子系统
     *
     * @param: [accountBookSystemDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:44
     */
    Integer antiInitSystemById(AccountBookSystemDto accountBookSystemDto);


    /**
     * @Description 根据系统标识+账簿ID+会计年度+会计期间查询是否已初始化
     * @Author 朱小明
     * @Date 2019/8/14 16:54
     * @Param [accountBookSystemDto]
     * @return java.lang.Byte
     **/
    AccountBookSystem findInitStatusByCondition(AccountBookSystemDto accountBookSystemDto);

    /**
     * 获取账簿期间数据
     *
     * @param: [accountBookSystemDto]
     * @return: com.njwd.entity.basedata.AccountBookSystem
     * @author: zhuzs
     * @date: 2019-09-16 17:45
     */
    AccountBookSystem findAccountBookSystem(AccountBookSystemDto accountBookSystemDto);

    /**
     *
     *
     * @param: [platformSysSystemDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysSystemVo>
     * @author: zhuzs
     * @date: 2019-09-17 15:46
     */
    List<SysSystemVo> findAccountBookSystemAll(SysSystemDto platformSysSystemDto);

}
