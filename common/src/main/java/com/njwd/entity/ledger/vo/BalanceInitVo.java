package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.dto.BalanceInitDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author lj
 * @Description 总账初始化
 * @Date:11:25 2019/8/12
 **/
@Data
public class BalanceInitVo {

    List<BalanceInitVo> balanceInitVoList;
    private String accountBookName;
    private String accountStatus;
    private Boolean checkFlag;
    private List<Long> accountBookIds = new ArrayList<Long>();
    private List<BalanceInitDto> successList = new ArrayList<BalanceInitDto>();
    private List<BalanceInitDto> failList = new ArrayList<BalanceInitDto>();
    private List<BalanceInitCheckVo> balanceInitCheckVos;
}
