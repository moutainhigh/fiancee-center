package com.njwd.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.common.Constant;
import com.njwd.entity.base.BaseBatchFail;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.ParameterSetSub;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import static org.dozer.loader.api.TypeMappingOptions.mapEmptyString;
import static org.dozer.loader.api.TypeMappingOptions.mapNull;

/**
 * 常用工具类
 *
 * @author xyyxhcj@qq.com
 * @since 2018-11-29
 */

public class FastUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(FastUtils.class);

    /**
     * 复制属性
     *
     * @param sources     源对象
     * @param destination 目标对象
     */
    public static void copyProperties(final Object sources, final Object destination) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        mapper.addMapping(new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(sources.getClass(), destination.getClass(), mapNull(true), mapEmptyString(true));
            }
        });
        mapper.map(sources, destination);
    }

    /**
     * 复制属性包含空字符串
     *
     * @param sources     源对象
     * @param destination 目标对象
     */
    public static void copyPropertiesExistsBlank(final Object sources, final Object destination) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        mapper.addMapping(new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(sources.getClass(), destination.getClass(), mapNull(false), mapEmptyString(true));
            }
        });
        mapper.map(sources, destination);
    }


    /**
     * 切割字符串
     *
     * @param src         被切割字符串
     * @param replacement 分割符
     * @param <T>         T
     * @return 切割后的结果集
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> splitStr(String src, String replacement) {
        ArrayList<T> list = new ArrayList<>();
        if (src == null || "".equals(src.trim())) {
            return list;
        }
        int index;
        int length = replacement.length();
        String str;
        while ((index = src.indexOf(replacement)) != -1) {
            str = src.substring(0, index);
            if (str.length() != 0) {
                list.add((T) str.trim());
            }
            src = src.substring(index + length);
        }
        if (src.length() != 0) {
            list.add((T) src.trim());
        }
        return list;
    }

    /**
     * 指定长度切割字符串
     *
     * @param src    被切割字符串
     * @param length 指定长度
     * @param <T>    T
     * @return 切割后的结果集
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> splitLength(String src, int length) {
        ArrayList<T> list = new ArrayList<>();
        if (src == null || "".equals(src.trim()) || length == 0 || src.length() < length) {
            return list;
        }
        while (src.length() >= length) {
            list.add((T) src.substring(0, length).trim());
            src = src.substring(length);
        }
        return list;
    }

    /**
     * 校验 不可为null
     *
     * @param objects 对象
     */
    public static void checkNull(Object... objects) {
        for (Object obj : objects) {
            if (obj == null) {
                throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
            }
        }
    }

    /**
     * 校验集合 为NULL 或 为空
     *
     * @param list 集合
     */
    public static boolean checkNullOrEmpty(List list) {
        if (null == list || Constant.Number.ZERO.equals(list.size())) {
            return true;
        }
        return false;
    }

    /**
     * 校验 多个list只要传了其中一个就Ok
     *
     * @param lists
     */
    public static void checkListNullOrEmpty(List... lists) {
        boolean flag = true;
        for (List list : lists) {
            if (null != list && !Constant.Number.ZERO.equals(list.size())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }

    }

    /**
     * 校验 必传参数
     *
     * @param objects 必传参数
     */
    public static void checkParams(Object... objects) {
        for (Object obj : objects) {
            if (obj == null) {
                throw new ServiceException(ResultCode.PARAMS_NOT);
            }
            if (obj instanceof Collection) {
                Collection coll = (Collection) obj;
                if (coll.isEmpty()) {
                    throw new ServiceException(ResultCode.PARAMS_NOT);
                }
            } else if (obj.getClass().isArray()) {//判断数组是否为空
                if (Array.getLength(obj) == 0) {
                    throw new ServiceException(ResultCode.PARAMS_NOT);
                }
            }
            if ("".equals(obj)) {
                throw new ServiceException(ResultCode.PARAMS_NOT);
            }
        }
    }
    /**
     * 校验数组参数
     *@param objects 必传参数
     */
    public static void checkArrParams(Object[] objects) {
        if (objects == null || objects.length == 0) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
    }
    /**
     * 校验 多个参数只要传了其中一个就Ok
     *
     * @param objects
     */
    public static void checkParamsForOr(Object... objects) {
        boolean flag = true;
        for (Object obj : objects) {
            if (!StringUtils.isEmpty(obj)) {
                flag = false;
                break;
            }
        }
        if (flag) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }

    }

    /**
     * 校验数据库
     *
     * @param resultCode   数据存在时所抛异常业务码
     * @param mapper       mapper
     * @param queryWrapper 查询条件
     */
    public static <T> void checkNotExist(ResultCode resultCode, BaseMapper<T> mapper, QueryWrapper<T> queryWrapper) {
        Integer count = mapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException(resultCode);
        }
    }

    /**
     * 获取请求参数
     */

    public static StringBuilder getRequestParams(HttpServletRequest request) {
        Set<Map.Entry<String, String[]>> entries = request.getParameterMap().entrySet();
        StringBuilder sb = new StringBuilder();
        if (entries.size() != 0) {
            for (Map.Entry<String, String[]> entry : entries) {
                sb.append(Constant.Character.AND).append(entry.getKey()).append(Constant.Character.EQUALS).append(String.join(Constant.Character.COMMA, entry.getValue()));
            }
        }
        return sb;
    }

    /**
     * 打印请求体
     */
    public static void loggerRequestBody(Logger logger, HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            // 从缓存中获取requestBody
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            logger.error("REQUEST_BODY：\n{}", org.apache.commons.lang3.StringUtils.toEncodedString(wrapper.getContentAsByteArray(), Charset.forName(wrapper.getCharacterEncoding())));
        }
    }

    /**
     * 根据配置生成code
     *
     * @param codePrefix code前缀
     * @param numLength  序号所占长度
     * @param codeNum    序号
     * @return code
     */
    public static String generateCode(String codePrefix, int numLength, Long codeNum) {
        StringBuilder sb = new StringBuilder(codePrefix);
        for (int i = codeNum.toString().length(); i < numLength; i++) {
            sb.append(0);
        }
        return sb.append(codeNum).toString();
    }

    /**
     * 筛选掉已物理删除的ids
     *
     * @param resultCode   数据存在时返回的异常业务码
     * @param mapper       mapper
     * @param queryWrapper 无效数据的查询条件
     * @param idColumnName 查询sql结果集的id列名
     * @param checkIds     被校验数据,校验后过滤掉不通过的数据
     * @param failList     将校验不通过的数据封装入该数据集
     * @return 数据库中存在的数据
     */
    public static <T> List<Long> filterRemovedIds(@NotNull ResultCode resultCode, @NotNull BaseMapper<T> mapper, @NotNull QueryWrapper<T> queryWrapper, @NotEmpty String idColumnName, @NotNull List<Long> checkIds, @NotNull List<ReferenceDescription> failList) {
        List<Object> existIds = mapper.selectObjs(queryWrapper.in(idColumnName, checkIds).select(idColumnName));
        List<Long> existIdList = new LinkedList<>();
        for (Object idObj : existIds) {
            Long id = idObj instanceof Long ? (Long) idObj : Long.parseLong(idObj.toString());
            // 放入数据库中未物理删除的id
            existIdList.add(id);
        }
        // 筛选出已物理删除的ids
        checkIds.removeAll(existIdList);
        for (Long id : checkIds) {
            ReferenceDescription failDescription = new ReferenceDescription();
            failDescription.setBusinessId(id);
            failDescription.setReferenceDescription(resultCode.message);
            failList.add(failDescription);
        }
        return existIdList;
    }

    /**
     * 查询已逻辑删除/禁用/启用的记录ids,筛选将被操作的id集合
     *
     * @param resultCode   数据存在时返回的异常业务码
     * @param mapper       mapper
     * @param queryWrapper 无效数据的查询条件
     * @param idColumnName 表id列名
     * @param checkIds     被校验数据,校验后过滤掉不通过的数据
     * @param failList     将校验不通过的数据封装入该数据集
     */
    public static <T> void filterIds(@NotNull ResultCode resultCode, @NotNull BaseMapper<T> mapper, @NotNull QueryWrapper<T> queryWrapper, @NotEmpty String idColumnName, @NotNull List<Long> checkIds, @NotNull List<ReferenceDescription> failList) {
        if (checkIds.isEmpty()) {
            return;
        }
        List<Object> invalidList = mapper.selectObjs(queryWrapper.in(idColumnName, checkIds).select(idColumnName));
        String failMessage = resultCode.message;
        addFailDesc(mapper, idColumnName, checkIds, failList, invalidList, failMessage);
    }

    /**
     * 添加失败详情
     *
     * @param mapper       mapper
     * @param idColumnName idColumnName
     * @param checkIds     checkIds
     * @param failList     failList
     * @param invalidList  invalidList
     * @param failMessage  failMessage
     * @author xyyxhcj@qq.com
     * @date 2019/9/10 11:02
     **/
    private static <T> void addFailDesc(@NotNull BaseMapper<T> mapper, @NotEmpty String idColumnName, @NotNull List<Long> checkIds, @NotNull List<ReferenceDescription> failList, List<Object> invalidList, String failMessage) {
        ReferenceDescription<T> failDescription;
        List<Long> failIds = new ArrayList<>();
        for (Object idObj : invalidList) {
            failDescription = new ReferenceDescription<>();
            Long id = idObj instanceof Long ? (Long) idObj : Long.parseLong(idObj.toString());
            failDescription.setBusinessId(id);
            failDescription.setReferenceDescription(failMessage);
            failList.add(failDescription);
            failIds.add(id);
            checkIds.remove(id);
        }
        if (Constant.ColumnName.ID.equals(idColumnName) && failIds.size() > 0) {
            //查询失败详情信息
            List<Map<String, Object>> failDescList = mapper.selectMaps(new QueryWrapper<T>().in(Constant.ColumnName.ID, failIds));
            MergeUtil.merge(failList, failDescList,
                    ReferenceDescription::getBusinessId, failDesc->failDesc.get("id"),
                    (failInfo, failDesc) -> failInfo.setInfo(failDesc));
        }
    }

    /**
     * 筛选掉版本号不一致的数据
     *
     * @param mapper       mapper
     * @param queryWrapper 前置查询条件
     * @param idColumnName 表id列名
     * @param checkIds     被校验数据,校验后过滤掉不通过的数据
     * @param versions     被校验数据的版本号
     * @param failList     将校验不通过的数据封装入该数据集
     */
    public static <T> void filterVersionIds(@NotNull final BaseMapper<T> mapper, @NotNull final QueryWrapper<T> queryWrapper, @NotEmpty final String idColumnName, @NotNull final List<Long> checkIds, @NotNull final List<Integer> versions, @NotNull final List<ReferenceDescription> failList) {
        if (checkIds.isEmpty() || versions.isEmpty()) {
            return;
        }
        if (checkIds.size() != versions.size()) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        queryWrapper.select(idColumnName);
        List<Long> idList = new ArrayList<>(checkIds);
        List<Integer> versionList = new ArrayList<>(versions);
        queryWrapper.and(tQueryWrapper -> {
            for (int i = 0; i < idList.size(); i++) {
                Long id = idList.get(i);
                Integer version = versionList.get(i);
                tQueryWrapper.or(wrapper -> wrapper.eq(idColumnName, id).ne(Constant.ColumnName.VERSION, version));
            }
            return tQueryWrapper;
        });
        List<Object> invalidList = mapper.selectObjs(queryWrapper);
        String failMessage = ResultCode.VERSION_ERROR.message;
        addFailDesc(mapper, idColumnName, checkIds, failList, invalidList, failMessage);
    }

    /**
     * 过滤集团共享的数据
     *
     * @param mapper   mapper
     * @param checkIds 被校验数据,校验后过滤掉不通过的数据
     * @param failList 将校验不通过的数据封装入该数据集
     */
    public static <T> void filterIdsByGroupId(@NotNull BaseMapper<T> mapper, @NotNull List<Long> checkIds, @NotNull List<ReferenceDescription> failList) {
        if (checkIds.isEmpty()) {
            return;
        }
        QueryWrapper<T> query = new QueryWrapper<T>().eq(Constant.ColumnName.COMPANY_ID, Constant.IsCompany.GROUP_ID)
                .in(Constant.ColumnName.ID, checkIds).select(Constant.ColumnName.ID);
        List<Object> adminIdList = mapper.selectObjs(query);
        for (Object idObj : adminIdList) {
            ReferenceDescription failDescription = new ReferenceDescription();
            Long id = idObj instanceof Long ? (Long) idObj : Long.parseLong(idObj.toString());
            failDescription.setBusinessId(id);
            failDescription.setReferenceDescription(ResultCode.GROUP_DATA_RULE.message);
            failList.add(failDescription);
            checkIds.remove(id);
        }
    }

    /**
     * 批量操作的数据
     *
     * @param mapper       mapper
     * @param model        操作结果实体
     * @param idColumnName 表id列名
     * @param updateIds    将被操作的id列表
     * @param <T>          T
     */
    public static <T> void updateBatch(@NotNull BaseMapper<T> mapper, @NotNull T model, @NotEmpty String idColumnName, @NotNull List<Long> updateIds,
                                       List<ReferenceDescription> successDetailList) {
        updateBatchWithWrapper(mapper, new QueryWrapper<>(), model, idColumnName, updateIds, successDetailList);
    }

    /**
     * 批量操作的数据
     *
     * @param mapper       mapper
     * @param model        操作结果实体
     * @param queryWrapper 自定义匹配条件
     * @param idColumnName 表id列名
     * @param updateIds    将被操作的id列表
     * @param <T>          T
     */
    public static <T> void updateBatchWithWrapper(@NotNull BaseMapper<T> mapper, @NotNull QueryWrapper<T> queryWrapper, @NotNull T model, @NotEmpty String idColumnName,
                                                  @NotNull List<Long> updateIds, List<ReferenceDescription> successDetailList) {
        if (updateIds.isEmpty()) {
            return;
        }
        SysUserVo operator = UserUtils.getUserVo();
        if (model instanceof BaseModel) {
            BaseModel baseModel = (BaseModel) model;
            baseModel.setUpdatorId(operator.getUpdatorId());
            baseModel.setUpdatorName(operator.getName());
        } else {
            try {
                Method setUpdatorId = model.getClass().getMethod("setUpdatorId", Long.class);
                Method setUpdatorName = model.getClass().getMethod("setUpdatorName", String.class);
                setUpdatorId.invoke(model, operator.getUserId());
                setUpdatorName.invoke(model, operator.getName());
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.error("反射设置操作人失败", e);
            }
        }
        mapper.update(model, queryWrapper.in(idColumnName, updateIds));
        if (null != successDetailList) {
            //查询操作成功的信息详情
            List<T> list = mapper.selectBatchIds(updateIds);
            ReferenceDescription successDetail;
            for (T info : list) {
                successDetail = new ReferenceDescription();
                successDetail.setInfo(info);
                successDetailList.add(successDetail);
            }
        }
    }

    /**
     * @param object 参数对象
     * @return java.math.BigDecimal
     * @description: bigDcimal为null的转换为0
     * @date 2019/7/30 15:07
     */
    public static BigDecimal Null2Zero(Object object) {
        return object == null ? new BigDecimal(Constant.Number.ZERO) : new BigDecimal(object.toString());
    }

    /**
     * @return java.lang.String
     * @description: 数字转中文
     * @Param [src]
     * @author LuoY
     * @date 2019/9/4 9:17
     */
    public static String int2chineseNum(int src) {
        final String num[] = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        final String unit[] = {"", "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千"};
        String dst = "";
        int count = 0;
        while (src > 0) {
            dst = (num[src % 10] + unit[count]) + dst;
            src = src / 10;
            count++;
        }
        return dst.replaceAll("零[千百十]", "零").replaceAll("零+万", "万")
                .replaceAll("零+亿", "亿").replaceAll("亿万", "亿零")
                .replaceAll("零+", "零").replaceAll("零$", "");
    }

    /**
     * 将manager转换成list
     *
     * @param managerInfo managerInfo
     * @return java.util.List<java.lang.Object>
     * @author xyyxhcj@qq.com
     * @date 2019/9/9 11:18
     **/
    public static List<Object> getManagerList(final ManagerInfo managerInfo) {
        List<Object> list = new ArrayList<>();
        Field[] fields = ManagerInfo.class.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            try {
                field.setAccessible(true);
                if (field.get(managerInfo) != null) {
                    list.add("$." + field.getName());
                    list.add(field.get(managerInfo));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return list;
    }

    /**
     * 校验字符串是否匹配正则表达式
     *
     * @param regex regex 正则表达式字符串
     * @param str   str 要匹配的字符串
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/9/21 10:19
     **/
    public static boolean match(String regex, String str) {
        return match(Pattern.compile(regex), str);
    }

    /**
     * 校验字符串是否匹配正则表达式
     *
     * @param pattern 编译好的正则表达式验证对象,用于循环调用时单独创建
     * @param str     str 要匹配的字符串
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/9/21 10:19
     **/
    public static boolean match(Pattern pattern, String str) {
        return pattern.matcher(str).matches();
    }

    /**
     * 校验状态 平台返回的is结果(原先为 true/false)->20190920 url添加参数&tinyInt1isBit=false后 返回Integer
     *
     * @param remoteDataMap remoteDataMap
     * @param isStatus      isStatus
     * @return java.lang.Boolean
     * @author xyyxhcj@qq.com
     * @date 2019/8/22 13:51
     **/
    public static Boolean isStatus(@NotNull Map<String, Object> remoteDataMap, String isStatus) {
        return Constant.Is.YES_INT.equals(remoteDataMap.get(isStatus));
    }

    /**
     * 获取系统参数值
     *
     * @param parameterSet    parameterSet
     * @param accountBookId   accountBookId
     * @param parameterSetKey parameterSetKey
     * @return com.njwd.entity.ledger.ParameterSetSub
     * @author xyyxhcj@qq.com
     * @date 2019/10/21 17:16
     **/
    public static @NotNull ParameterSetSub getParamSetSub(@NotNull ParameterSetVo parameterSet, Long accountBookId, String parameterSetKey) {
        Map<Long, ParameterSetSub> setSubDict = parameterSet.getParamDict().get(parameterSetKey);
        if (setSubDict == null || setSubDict.isEmpty()) {
            throw new ServiceException(ResultCode.PARAMETER_SET_NOT_EXIST, parameterSetKey);
        }
        ParameterSetSub paramSetSub = setSubDict.getOrDefault(accountBookId, setSubDict.get(Constant.IsCompany.GROUP_ID));
        if (paramSetSub == null) {
            throw new ServiceException(ResultCode.PARAMETER_SET_NOT_EXIST, parameterSetKey);
        }
        return paramSetSub;
    }

    /**
     * 优化批量操作数据过滤
     *
     * @param checkDataList 待操作的数据(前端传参,每条数据中包含版本号等)
     * @param failList      存放失败数据list
     * @param checkIds      successList 待操作的数据ids,过滤后返回,获取 => com.njwd.entity.base.BaseBatchFail#getCheckIds()
     * @param existDataMap  数据库数据,key为ID => com.njwd.entity.base.BaseBatchFail#getId()
     * @param batchFail     判断是否操作失败方法的实现
     * @author xyyxhcj@qq.com
     * @date 2019/11/21 15:19
     **/
    public static <T, E extends T> void filterIds(final List<E> checkDataList, final List<ReferenceDescription> failList, final LinkedHashSet<Long> checkIds, final Map<Long, T> existDataMap, final BaseBatchFail<T, E> batchFail) {
        ReferenceDescription failDesc;
        loop:
        for (E e : checkDataList) {
            Long id = batchFail.getId(e);
            T exist = existDataMap.get(id);
            for (BaseBatchFail.BaseEachFail<T,E> each : batchFail.checkFails) {
                if (each.isFail(exist, e)) {
                    failDesc = new ReferenceDescription();
                    failDesc.setBusinessId(id);
                    failDesc.setReferenceDescription(each.getFailMsg());
                    failList.add(failDesc);
                    checkIds.remove(id);
                    continue loop;
                }
            }
        }
    }
}
