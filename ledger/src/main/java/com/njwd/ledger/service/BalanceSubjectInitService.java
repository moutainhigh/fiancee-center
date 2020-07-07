package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.ledger.dto.BalanceSubjectInitDto;
import com.njwd.entity.ledger.vo.BalanceSubjectInitVo;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 期初余额-科目期初
 * @Date:9:50 2019/7/29
 **/
public interface BalanceSubjectInitService {

    /**
     * 科目期初录入
     * @Author lj
     * @Date:10:10 2019/8/2
     * @param balanceSubjectInitDto
     * @return int
     **/
    int addSubjectInitBatch(BalanceSubjectInitDto balanceSubjectInitDto);

    /**
     * 根据核算主体ID清空期初数据
     * @Author lj
     * @Date:15:42 2019/7/25
     * @param balanceSubjectInitDto
     * @return int
     **/
    int deleteBalSubjectBatch(BalanceSubjectInitDto balanceSubjectInitDto);

    /**
     * 试算平衡
     * @Author lj
     * @Date:11:43 2019/9/9
     * @param balanceSubjectInitDto
     * @return com.njwd.entity.ledger.vo.BalanceSubjectInitVo
     **/
    BalanceSubjectInitVo trialBalance(BalanceSubjectInitDto balanceSubjectInitDto);

    /**
     * 拉取当前所在公司账簿，自动带出账簿中有数据的核算主体及当前所在核算主体，账簿对应科目信息,账簿启用期间
     * @Author lj
     * @Date:11:00 2019/7/31
     * @param balanceSubjectInitDto
     * @return com.njwd.ledger.entity.vo.BalanceSubjectInitVo
     **/
    BalanceSubjectInitVo findAccountBookByComId(BalanceSubjectInitDto balanceSubjectInitDto);

    /**
     * a)	当前账簿启用二级核算，核算主体默认选中当前所在核算主体，科目中显示属性为内部往来的科目；
     * b)	当前账簿未启用二级核算，核算主体默认选中当前唯一核算主体且置灰不可选择，科目中不显示属性为内部往来的科目
     * @Author lj
     * @Date:10:12 2019/7/29
     * @param balanceSubjectInitDto
     * @return List<AccountSubjectVo>
     **/
    List<AccountSubjectVo> findSubject(BalanceSubjectInitDto balanceSubjectInitDto);

    /**
     * 查询期初辅助核算信息
     * @Author lj
     * @Date:17:25 2019/8/22
     * @param balanceSubjectInitDto
     * @return BalanceSubjectInitVo
     **/
    BalanceSubjectInitVo findAuxInfo(BalanceSubjectInitDto balanceSubjectInitDto);

    /**
     * 查询帐簿下的核算主体
     * @Author lj
     * @Date:17:04 2019/8/1
     * @param balanceSubjectInitDto
     * @return Page<AccountBookEntityVo>
     **/
    Page<AccountBookEntityVo> findBookEntityPageByComId(BalanceSubjectInitDto balanceSubjectInitDto);
    
    /**
     * @description: 导出excel
     * @param: [balanceSubjectInitDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-22 14:56 
     */
    void exportExcel(BalanceSubjectInitDto balanceSubjectInitDto, HttpServletResponse response);
}
