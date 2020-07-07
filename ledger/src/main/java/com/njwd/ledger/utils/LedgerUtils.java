package com.njwd.ledger.utils;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.exception.FeignClientErrorMsg;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.CompanyFeignClient;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 总账工具类
 *
 * @author xyyxhcj@qq.com
 * @since 2019/07/26
 */
@SuppressWarnings("all")
public class LedgerUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(LedgerUtils.class);
    /**
     * 切割数组
     **/
    private final static String SPLIT_SCRIPT = "" +
            "local function split(str, split_char)\n" +
            "    if (split_char=='') then return false end\n" +
            "    local pos,arr = 0, {}\n" +
            "    for st,sp in function()\n" +
            "                    return string.find(str, split_char, pos, true)\n" +
            "                 end do\n" +
            "        table.insert(arr, string.sub(str, pos, st - 1))\n" +
            "        pos = sp + 1\n" +
            "    end\n" +
            "    table.insert(arr, string.sub(str, pos))\n" +
            "    return arr\n" +
            "end ";
    /**
     * 凭证操作前上锁,如果账簿已被锁返回-1
     **/
    private final static String LOCK_VOUCHER_SCRIPT = SPLIT_SCRIPT +
            "local count = 0\n" +
            "local token = ARGV[1]\n" +
            "local voucher_sign_keys = split(ARGV[2], ',')\n" +
            "local valid_time = tonumber(ARGV[3])\n" +
            "local account_book_keys = split(ARGV[4], ',')\n" +
            "local sign_key_valid_time = tonumber(ARGV[5])\n" +
            "for k, v in pairs(account_book_keys) do\n" +
            "    if (redis.call('exists', v) ~= 0) then\n" +
            "        return -1\n" +
            "    end\n" +
            "end\n" +
            "for k, v in pairs(KEYS) do\n" +
            "    if (redis.call('exists', v) == 0 or redis.call('get', v) == token) then\n" +
            "        if (not redis.call('setex', v, valid_time, token)) then\n" +
            "            return 0\n" +
            "        end\n" +
            "    else\n" +
            "        return 0\n" +
            "    end\n" +
            "end\n" +
            "-- 标记当前账簿期间正在处理凭证\n" +
            "for k, v in pairs(voucher_sign_keys) do\n" +
            "    redis.call('incr', v)\n" +
            "    redis.call('expire', v, sign_key_valid_time)\n" +
            "end\n" +
            "return 1 ";
    /**
     * 凭证操作后释放锁
     **/
    private final static String UNLOCK_VOUCHER_SCRIPT = SPLIT_SCRIPT +
            "local count = 0 " +
            "local token = ARGV[1] " +
            "local voucher_sign_keys = split(ARGV[2], ',') " +
            "for k, v in pairs(KEYS) do " +
            "    if (redis.call('get', v) == token) then " +
            "        if (redis.call('del', v)) then " +
            "            count = count + 1 " +
            "        end " +
            "    end " +
            "end " +
            "for k, v in pairs(voucher_sign_keys) do " +
            "    redis.call('decr', v) " +
            "end " +
            "return count ";
    /**
     * 结账操作前上锁 自释放时间暂定20分钟 存在正在处理的凭证时返回-1
     **/
    private final static String LOCK_ACCOUNT_BOOK_SCRIPT = SPLIT_SCRIPT +
            "local token = ARGV[1] \n" +
            "local valid_time = tonumber(ARGV[2]) \n" +
            "local voucher_sign_keys = split(ARGV[3], ',') \n" +
            "for k, v in pairs(voucher_sign_keys) do \n" +
            "    local redis_val = redis.call('get', v) \n" +
            "    if (type(redis_val) == 'number' and tonumber(redis_val) > 0) then \n" +
            "        return -1 \n" +
            "    end \n" +
            "end \n" +
            "for k, v in pairs(KEYS) do \n" +
            "    if (redis.call('exists', v) == 0 or redis.call('get', v) == token) then \n" +
            "        if (not redis.call('setex', v, valid_time, token)) then \n" +
            "            return 0 \n" +
            "        end \n" +
            "    else \n" +
            "        return 0 \n" +
            "    end \n" +
            "end \n" +
            "return 1 \n";
    /**
     * 结账操作后释放锁
     **/
    private final static String UNLOCK_ACCOUNT_BOOK_SCRIPT = SPLIT_SCRIPT +
            "local count = 0 " +
            "local token = ARGV[1] " +
            "for k, v in pairs(KEYS) do " +
            "    if (redis.call('get', v) == token) then " +
            "        if (redis.call('del', v)) then " +
            "            count = count + 1 " +
            "        end " +
            "    end " +
            "end " +
            "return count ";

    /**
     * 在凭证整理，结账操作时对账簿上锁
     *
     * @param process        process
     * @param accountPeriods 必有参数:账簿ID accountBookId,年度 periodYear,期间 periodNum
     * @return T
     * @author xyyxhcj@qq.com
     * @date 2019/7/26 16:07
     **/
    public static <T> T lockAccountBook(RedisUtils.LockProcess<T> process, AccountBookPeriod... accountPeriods) {
        FastUtils.checkArrParams(accountPeriods);
        // 所有账簿均没有正在操作的凭证时才能上锁成功
        String token = UUID.randomUUID().toString();
        Set<String> accountBookKeys = new HashSet<>();
        Set<String> voucherSignKeys = new HashSet<>();
        for (AccountBookPeriod accountPeriod : accountPeriods) {
            if (accountPeriod == null) {
                continue;
            }
            FastUtils.checkParams(accountPeriod.getAccountBookId(), accountPeriod.getPeriodYear(), accountPeriod.getPeriodNum());
            accountBookKeys.add(String.format(Constant.LockKey.ACCOUNT_BOOK, accountPeriod.getAccountBookId()));
            voucherSignKeys.add(String.format(Constant.LockKey.VOUCHER_OPER, accountPeriod.getAccountBookId(), accountPeriod.getPeriodYear(), accountPeriod.getPeriodNum()));
        }
        Long lock = lockAccountBook(token, accountBookKeys, voucherSignKeys);
        try {
            if (lock == null) {
                throw new ServiceException(ResultCode.TIME_OUT);
            } else if (lock > 0) {
                return process.execute();
            } else if (lock < 0) {
                throw new ServiceException(ResultCode.VOUCHER_OPER_EXIST);
            } else {
                throw new ServiceException(ResultCode.TIME_OUT);
            }
        } finally {
            releaseLockAccountBook(token, accountBookKeys);
        }
    }

    private static void releaseLockAccountBook(String token, Set<String> accountBookKeys) {
        List<String> keyList = new ArrayList<>(accountBookKeys);
        List<String> args = Collections.singletonList(token);
        RedisCallback<Long> callback = RedisUtils.buildRedisCallback(keyList, args, UNLOCK_ACCOUNT_BOOK_SCRIPT);
        LOGGER.info("释放账簿锁, accountBookKeys：{}，token：{}", keyList.toString(), token);
        RedisUtils.CLIENT.execute(callback);
    }

    private static Long lockAccountBook(String token, Set<String> accountBookKeys, Set<String> voucherSignKeys) {
        // 记录上锁失败次数
        int count = 0;
        List<String> args = new ArrayList<>();
        args.add(token);
        long timeout = Constant.SysConfig.SETTLE_ACCOUNT_LOCK_TIMEOUT;
        args.add(String.valueOf(timeout));
        args.add(String.join(Constant.Character.COMMA, voucherSignKeys));
        ArrayList<String> accountBookKeyList = new ArrayList<>(accountBookKeys);
        String keysStr = accountBookKeys.toString();
        String signKeysStr = voucherSignKeys.toString();
        LOGGER.info("开始上账簿锁, accountBookKeys：{}，token：{}", keysStr, token);
        try {
            while (true) {
                RedisCallback<Long> callback = RedisUtils.buildRedisCallback(accountBookKeyList, args, LOCK_ACCOUNT_BOOK_SCRIPT);
                Long execute = RedisUtils.CLIENT.execute(callback);
                if (execute != null && execute != 0) {
                    return execute;
                }
                if (count == timeout) {
                    LOGGER.info("尝试上账簿锁超时，手动退出，count：{}，accountBookKeys：{},voucherSignKeys：{}", ++count, keysStr, signKeysStr);
                    return execute;
                }
                LOGGER.info("没拿到账簿锁，等待下次尝试，count：{}，accountBookKeys：{}", ++count, keysStr);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            LOGGER.info("账簿上锁失败，accountBookKeys：{}", keysStr, e);
            return 0L;
        }
    }

    /**
     * 对多张凭证上锁
     *
     * @param operType 操作类型
     * @param process  process
     * @param vouchers 每条凭证都必须有accountBookId,periodYear,periodNum,无id时不上凭证锁,只标记正在处理
     * @return T
     * @author xyyxhcj@qq.com
     * @date 2019/7/26 17:11
     **/
    public static <T> T lockVoucher(String operType, RedisUtils.LockProcess<T> process, Voucher... vouchers) {
        FastUtils.checkArrParams(vouchers);
        // 所有账簿均没有上锁时才能操作凭证
        String token = UUID.randomUUID().toString();
        Set<String> voucherKeys = new HashSet<>();
        Set<String> voucherSignKeys = new HashSet<>();
        Set<String> accountBookKeys = new HashSet<>();
        for (Voucher voucher : vouchers) {
            FastUtils.checkParams(voucher.getAccountBookId(), voucher.getPostingPeriodYear(), voucher.getPostingPeriodNum());
            if (voucher.getId() != null) {
                voucherKeys.add(String.format(Constant.LockKey.VOUCHER, voucher.getAccountBookId(), voucher.getId()));
            }
            voucherSignKeys.add(String.format(Constant.LockKey.VOUCHER_OPER, voucher.getAccountBookId(), voucher.getPostingPeriodYear(), voucher.getPostingPeriodNum()));
            accountBookKeys.add(String.format(Constant.LockKey.ACCOUNT_BOOK, voucher.getAccountBookId()));
        }
        Long lock = lockVoucher(token, voucherKeys, voucherSignKeys, accountBookKeys);
        try {
            if (lock == null) {
                throw new ServiceException(ResultCode.TIME_OUT);
            } else if (lock > 0) {
                return process.execute();
            } else if (lock < 0) {
                throw new ServiceException(String.format(ResultCode.ACCOUNT_BOOK_LOCK.message, operType), ResultCode.ACCOUNT_BOOK_LOCK);
            } else {
                throw new ServiceException(ResultCode.TIME_OUT);
            }
        } finally {
            releaseLockVoucher(token, voucherKeys, voucherSignKeys);
        }
    }

    private static void releaseLockVoucher(String token, Set<String> voucherKeys, Set<String> voucherSignKeys) {
        String voucherSignStrs = String.join(Constant.Character.COMMA, voucherSignKeys);
        List<String> keyList = new ArrayList<>(voucherKeys);
        List<String> args = new ArrayList<>();
        args.add(token);
        args.add(voucherSignStrs);
        RedisCallback<Long> callback = RedisUtils.buildRedisCallback(keyList, args, UNLOCK_VOUCHER_SCRIPT);
        LOGGER.info("释放凭证锁，token：{}， voucherKeys：{}，voucherSignKeys：{}", token, keyList.toString(), voucherSignStrs);
        RedisUtils.CLIENT.execute(callback);
    }

    private static Long lockVoucher(String token, Set<String> voucherKeys, Set<String> voucherSignKeys, Set<String> accountBookKeys) {
        // 记录上锁失败次数
        int count = 0;
        ArrayList<String> voucherKeyList = new ArrayList<>(voucherKeys);
        String keysStr = voucherKeys.toString();
        String signKeysStr = voucherSignKeys.toString();
        String accountBookKeysStr = accountBookKeys.toString();
        long timeout = Constant.SysConfig.VOUCHER_LOCK_TIMEOUT;
        List<String> args = new ArrayList<>();
        args.add(token);
        args.add(String.join(Constant.Character.COMMA, voucherSignKeys));
        args.add(String.valueOf(timeout));
        args.add(String.join(Constant.Character.COMMA, accountBookKeys));
        args.add(String.valueOf(Constant.SysConfig.SETTLE_ACCOUNT_LOCK_TIMEOUT));
        LOGGER.info("开始上凭证锁, token：{}，voucherKeys：{}，voucherSignKeys：{}，accountBookKeys：{}", token, keysStr, signKeysStr, accountBookKeysStr);
        try {
            while (true) {
                RedisCallback<Long> callback = RedisUtils.buildRedisCallback(voucherKeyList, args, LOCK_VOUCHER_SCRIPT);
                Long execute = RedisUtils.CLIENT.execute(callback);
                if (execute != null && execute != 0) {
                    return execute;
                }

                if (count == timeout) {
                    LOGGER.info("尝试上凭证锁超时，手动退出，count：{}，voucherKeys：{}，voucherSignKeys：{}，accountBookKeys：{}", ++count, keysStr, signKeysStr, accountBookKeys);
                    return execute;
                }
                LOGGER.info("没拿到凭证锁，等待下次尝试，count：{}，voucherKeys：{}，voucherSignKeys：{}，accountBookKeys：{}", ++count, keysStr, signKeysStr, accountBookKeys);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            LOGGER.info("凭证上锁失败，voucherKeys：{}", keysStr, e);
            return 0L;
        }
    }

    /**
     * 更新科目余额表前上锁 (包括辅助核算科目余额表)
     *
     * @param periodLock 标识账簿 主体 年度 期间
     * @param process    process
     * @return T
     * @author xyyxhcj@qq.com
     * @date 2019/8/13 19:07
     **/
    public static <T> T lockSubject(Voucher periodLock, RedisUtils.LockProcess<T> process) {
        FastUtils.checkParams(periodLock);
        String key = String.format(Constant.LockKey.BALANCE_SUBJECT, periodLock.getAccountBookId(), periodLock.getAccountBookEntityId(), periodLock.getPostingPeriodYear(), periodLock.getPostingPeriodNum());
        return RedisUtils.lock(key, Constant.SysConfig.VOUCHER_LOCK_TIMEOUT, process);
    }

    /**
     * 更新现金流量发生额表前上锁
     *
     * @param periodLock 标识账簿 主体 年度 期间
     * @param ids        主键s
     * @param process    process
     * @return T
     * @author xyyxhcj@qq.com
     * @date 2019/8/13 19:07
     **/
    public static <T> T lockCashFlow(Voucher periodLock, RedisUtils.LockProcess<T> process) {
        FastUtils.checkParams(periodLock);
        String key = String.format(Constant.LockKey.BALANCE_CASH_FLOW, periodLock.getAccountBookId(), periodLock.getAccountBookEntityId(), periodLock.getPostingPeriodYear(), periodLock.getPostingPeriodNum());
        return RedisUtils.lock(key, Constant.SysConfig.VOUCHER_LOCK_TIMEOUT, process);
    }

    /**
    * @Description 凭证字使用和类型之间的转换
    * @Author 朱小明
    * @Date 2019/8/23
    * @param [source]
    * @return java.lang.Byte
    **/

    public static Byte ConvertCredentialWord(Byte source) {
        if(source == null) {
            return source;
        }
        Byte des;
        switch (source) {
            case Constant.CredentialWordSet.RECORD:
                des = Constant.CredentialWordType.RECORD;
                break;
            case Constant.CredentialWordSet.CASH_TRANSFER:
                des = Constant.CredentialWordType.TRANSFER;
                break;
            default:
                des = Constant.CredentialWordType.RECORD;
        }
        return des;
    }

    /**
     * 获取单个账簿的公司id
     *
     * @param companyFeignClient companyFeignClient
     * @param accountBookId      accountBookId
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/8/29 19:48
     **/
    public static Long getCompanyId(CompanyFeignClient companyFeignClient, Long accountBookId) {
        AccountBookDto accountBookDto = new AccountBookDto();
        FastUtils.checkParams(accountBookId);
        accountBookDto.setId(accountBookId);
        Result<CompanyVo> companyVoResult = companyFeignClient.checkHasSubAccount(accountBookDto);
        if (companyVoResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_COMPANY_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        return companyVoResult.getData().getId();
    }

    /**
     * 获取多个账簿的公司ids
     *
     * @param companyFeignClient companyFeignClient
     * @param accountBookIds     accountBookIds
     * @return java.util.List<java.lang.Long>
     * @author xyyxhcj@qq.com
     * @date 2019/8/29 19:48
     **/
    public static List<Long> getCompanyIds(CompanyFeignClient companyFeignClient, Set<Long> accountBookIds) {
        CompanyVo companyVo = getCompanyVoResult(companyFeignClient, accountBookIds);
        return companyVo.getBatchIds();
    }

    /**
     * 获取账簿对应的公司字典,key为账簿ID,value为公司ID,用于过滤无权限的账簿数据
     *
     * @param companyFeignClient companyFeignClient
     * @param accountBookIds accountBookIds
     * @return java.util.List<java.lang.Long>
     * @author xyyxhcj@qq.com
     * @date 2019/8/29 19:48
     **/
    public static Map<Long, Long> getAccountBookCompanyDict(CompanyFeignClient companyFeignClient, Set<Long> accountBookIds) {
        CompanyVo companyVo = getCompanyVoResult(companyFeignClient, accountBookIds);
        Map<Long, Long> accountBookCompanyDict = new LinkedHashMap<>();
        List<CompanyVo> companyVoList = companyVo.getCompanyVoList();
        if (companyVoList != null) {
            companyVoList.forEach(vo -> accountBookCompanyDict.put(vo.getAccountBookId(), vo.getId()));
        }
        return accountBookCompanyDict;
    }

    /**
     * 获取账簿的公司数据
     *
     * @param companyFeignClient companyFeignClient
     * @param accountBookIds     accountBookIds
     * @return com.njwd.support.Result<com.njwd.entity.basedata.vo.CompanyVo>
     * @author xyyxhcj@qq.com
     * @date 2019/9/10 15:26
     **/
    public static CompanyVo getCompanyVoResult(CompanyFeignClient companyFeignClient, Set<Long> accountBookIds) {
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setIdSet(accountBookIds);
        Result<CompanyVo> companyVoResult = companyFeignClient.checkHasSubAccount(accountBookDto);
        if (companyVoResult.getData() == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_COMPANY_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        return companyVoResult.getData();
    }

    /**
     * 获取科目字典
     *
     * @param client     client
     * @param subjectIds subjectIds
     * @param dto        accountSubjectDto 不可有sourceTable
     * @return java.util.Map<java.lang.Long, java.util.Map < java.lang.String, java.lang.Object>>
     * @author xyyxhcj@qq.com
     * @date 2019/9/21 10:42
     **/
    public static @Nullable
    Map<Long, Map<String, Object>> getSubjectDict(AccountSubjectFeignClient client, List<Long> subjectIds, @Nullable AccountSubjectDto dto) {
        if (dto == null) {
            dto = new AccountSubjectDto();
        }
        dto.setIds(subjectIds);
        return client.findSourceTableInfo(dto).getData();
    }

    /**
     * 校验科目数据是否可用
     *
     * @param subjectMap   subjectMap
     * @param errorData    errorData
     * @param errorCode    errorCode
     * @param propertyName propertyName
     * @param yes          true是->是则抛异常 false否->否抛异常
     * @author xyyxhcj@qq.com
     * @date 2019/9/21 11:04
     **/
    public static void checkSubjectValid(Map<String, Object> subjectMap, Object errorData, ResultCode errorCode, String propertyName, boolean yes) {
        Boolean status = yes ? FastUtils.isStatus(subjectMap, propertyName) : !FastUtils.isStatus(subjectMap, propertyName);
        if (subjectMap == null || status) {
            throw new ServiceException(errorCode, errorData);
        }
    }
}
