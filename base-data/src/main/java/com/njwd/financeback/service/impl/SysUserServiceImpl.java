package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.config.YmlProperties;
import com.njwd.entity.base.ManagerInfo;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.remote.req.UserPageReq;
import com.njwd.entity.basedata.remote.resp.UserLogin;
import com.njwd.entity.basedata.remote.resp.UserPageResp;
import com.njwd.entity.basedata.vo.SysMenuVo;
import com.njwd.entity.basedata.vo.SysRoleVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserAccountBookEntityVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.mapper.*;
import com.njwd.financeback.service.SysRoleService;
import com.njwd.financeback.service.SysUserService;
import com.njwd.handler.UserRealm;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/05/22
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl implements SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysUserEnterpriseMapper sysUserEnterpriseMapper;
    @Resource
    private CompanyMapper companyMapper;
    @Resource
    private UserAccountBookEntityMapper userAccountBookEntityMapper;
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private FileService fileService;
    @Resource
    private YmlProperties ymlProperties;

    @Override
    public SysUserVo login(UserLogin userLogin, String sessionId) {
        // 校验sign,将角色标识存入sysUserVo
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("system_code", Constant.SysConfig.SYSTEM_KEY);
        params.put("timestamp", userLogin.getTimestamp());
        params.put("root_enterprise_id", userLogin.getRoot_enterprise_id());
        params.put("user_id", userLogin.getUser_id());
        params.put("admin_type", userLogin.getAdmin_type());
        String sign = CheckUrlSignUtil.getSign(params, Constant.SysConfig.SYSTEM_KEY);
        if (!sign.equals(userLogin.getSign())) {
            throw new ServiceException(ResultCode.BAD_REQUEST);
        }
        Subject subject = SecurityUtils.getSubject();
        UserRealm.AutoLoginToken authenticationToken = new UserRealm.AutoLoginToken(userLogin);
        subject.login(authenticationToken);
        SysUserVo sysUserVo = UserUtils.getUserVo();
        fill(sysUserVo, sysUserVo.getRootEnterpriseId());
        sysUserVo.setAuthorization(sessionId);
        // 如果为租户/业务管理员 返回租户下的所有公司
        if (ShiroUtils.hasRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, sysUserVo.getRootEnterpriseId())) {
            sysUserVo.setAllCompanyList(companyMapper.findList(sysUserVo.getRootEnterpriseId()));
        }
        return sysUserVo;
    }

    @Override
    public void addUser(SysUserDto sysUserDto) {
        // 已存在时不插入
        SysUser existSysUser = sysUserMapper.selectById(sysUserDto.getUserId());
        if (existSysUser == null) {
            // 添加到财务用户表
            SysUser sysUser = new SysUser();
            FastUtils.copyProperties(sysUserDto, sysUser);
            sysUserMapper.insert(sysUser);
        }
        // 校验租户用户关系是否存在,不存在则插入
        SysUserEnterprise existSysUserEnterprise = sysUserEnterpriseMapper.selectOne(new QueryWrapper<SysUserEnterprise>().eq(Constant.ColumnName.USER_ID, sysUserDto.getUserId()).eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, sysUserDto.getRootEnterpriseId()));
        if (existSysUserEnterprise == null) {
            SysUserEnterprise sysUserEnterprise = new SysUserEnterprise();
            sysUserEnterprise.setUserId(sysUserDto.getUserId());
            sysUserEnterprise.setRootEnterpriseId(sysUserDto.getRootEnterpriseId());
            sysUserEnterprise.setCreatorId(sysUserDto.getCreatorId());
            sysUserEnterprise.setCreatorName(sysUserDto.getCreatorName());
            sysUserEnterpriseMapper.insert(sysUserEnterprise);
        }
    }

    private void httpAddUser(SysUserDto sysUserDto) {
        // 已存在时不插入
        SysUser existSysUser = sysUserMapper.selectById(sysUserDto.getUserId());
        if (existSysUser == null) {
            // 添加到财务用户表
            SysUser sysUser = new SysUser();
            FastUtils.copyProperties(sysUserDto, sysUser);
            sysUserMapper.insert(sysUser);
        }

        SysUserEnterprise sysUserEnterprise = new SysUserEnterprise();
        sysUserEnterprise.setUserId(sysUserDto.getUserId());
        sysUserEnterprise.setCreatorId(sysUserDto.getCreatorId());
        sysUserEnterprise.setCreatorName(sysUserDto.getCreatorName());
        for (Long rootEnterpriseId : sysUserDto.getToAssEnterIdList()) {
            // 校验租户用户关系是否存在,不存在则插入
            SysUserEnterprise existSysUserEnterprise = sysUserEnterpriseMapper.selectOne(new QueryWrapper<SysUserEnterprise>().eq(Constant.ColumnName.USER_ID, sysUserDto.getUserId()).eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, rootEnterpriseId));
            if (existSysUserEnterprise == null) {
                sysUserEnterprise.setRootEnterpriseId(rootEnterpriseId);
                sysUserEnterpriseMapper.insert(sysUserEnterprise);
            }
        }

    }

    @Override
    public Page<SysUserVo> findPage(SysUserDto sysUserDto) {
        Page<SysUserVo> page = sysUserDto.getPage();
        sysUserDto.getCommParams().setOrColumn(Constant.OrMatchColumn.SYS_USER);
        List<SysUserVo> records = sysUserMapper.findPage(sysUserDto, page, sysUserDto.getCommParams());
        // 放入核算主体
        List<Long> userIds = new LinkedList<>();
        records.forEach(record -> userIds.add(record.getUserId()));
        SysUserDto dto = new SysUserDto();
        dto.setUserIds(userIds);
        Map<Long, List<UserAccountBookEntityVo>> accountBookEntityDict = findAccountBookEntityByCompany(dto);
        for (SysUserVo record : records) {
            List<UserAccountBookEntityVo> userAccountBookEntityVos = accountBookEntityDict.get(record.getUserId());
            if (userAccountBookEntityVos != null && !userAccountBookEntityVos.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < userAccountBookEntityVos.size(); i++) {
                    UserAccountBookEntityVo entity = userAccountBookEntityVos.get(i);
                    sb.append(entity.getName());
                    if (i != userAccountBookEntityVos.size() - 1) {
                        sb.append(Constant.Character.COMMA);
                    }
                }
                record.setAccountBookEntityNames(sb.toString());
            }
        }
        return page.setRecords(records);
    }

    @Override
    public Page<SysUserVo> findPermPage(SysUserDto sysUserDto) {
        Page<SysUserVo> page = sysUserDto.getPage();
        return page.setRecords(sysUserMapper.findPermPage(sysUserDto, page));
    }

    @Override
    public Page<SysUserVo> findRoleUserPage(SysUserDto sysUserDto) {
        Page<SysUserVo> page = sysUserDto.getPage();
        return page.setRecords(sysUserMapper.findRoleUserPage(sysUserDto, page));
    }

    @Override
    public SysUserVo findDetail(SysUserDto sysUserDto) {
        FastUtils.checkParams(sysUserDto.getUserId(), sysUserDto.getRootEnterpriseId());
        SysUserVo sysUserVo = findUserVo(sysUserDto);
        FastUtils.checkNull(sysUserVo);
        if (sysUserDto.getRootEnterpriseId() == null) {
            // 表示用户已逻辑删除
            return sysUserVo;
        }
        return fill(sysUserVo, sysUserDto.getRootEnterpriseId());
    }

    @Override
    public SysUserVo findUserVo(SysUserDto sysUserDto) {
        return sysUserMapper.findUserVo(sysUserDto);
    }

    @Override
    public int updateUserEnterprise(SysUserEnterprise sysUserEnterprise, SysUserVo operator) {
        sysUserEnterprise.setUpdatorId(operator.getUserId());
        sysUserEnterprise.setUpdatorName(operator.getName());
        sysUserEnterprise.setUpdateTime(new Date());
        return sysUserEnterpriseMapper.updateById(sysUserEnterprise);
    }

    @Override
    public int updateEnterpriseUserBatch(SysUserDto sysUserDto, SysUserVo operator) {
        SysUserEnterprise sysUserEnterprise = new SysUserEnterprise();
        sysUserEnterprise.setIsEnable(sysUserDto.getIsEnable());
        sysUserEnterprise.setUpdatorId(operator.getUserId());
        sysUserEnterprise.setUpdatorName(operator.getName());
        sysUserEnterprise.setUpdateTime(new Date());
        return sysUserEnterpriseMapper.update(sysUserEnterprise, new QueryWrapper<SysUserEnterprise>().eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, sysUserDto.getRootEnterpriseId()).in(Constant.ColumnName.USER_ID, sysUserDto.getUserIds()));
    }

    @Override
    public Page<SysUserVo> findNotImportPage(SysUserDto sysUserDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        // 查询当前租户已引入的用户列表
        SysUserEnterprise sysEnterpriseUserQuery = new SysUserEnterprise();
        sysEnterpriseUserQuery.setRootEnterpriseId(operator.getRootEnterpriseId());
        List<SysUserEnterprise> importUsers = sysUserEnterpriseMapper.selectList(new QueryWrapper<>(sysEnterpriseUserQuery).select(Constant.ColumnName.USER_ID));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < importUsers.size(); i++) {
            sb.append(importUsers.get(i).getUserId());
            if (i != importUsers.size() - 1) {
                sb.append(Constant.Character.COMMA);
            }
        }
        // 查询统一门户接口
        UserPageReq reqParams = new UserPageReq();
        Page<SysUserVo> page = sysUserDto.getPage();
        reqParams.setPageNo(page.getCurrent());
        reqParams.setPageSize(page.getSize());
        reqParams.setUserIds(sb.toString());
        reqParams.setSystem_code(Constant.SysConfig.SYSTEM_KEY);
        reqParams.setName(sysUserDto.getName());
        reqParams.setMobile(sysUserDto.getMobile());
        reqParams.setName_mobile(sysUserDto.getCommParams().getOrMatch());
        reqParams.setRoot_enterprise_id(sysEnterpriseUserQuery.getRootEnterpriseId());
        reqParams.setTimestamp(System.currentTimeMillis() / 1000);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("system_code", Constant.SysConfig.SYSTEM_KEY);
        map.put("timestamp", reqParams.getTimestamp());
        map.put("root_enterprise_id", reqParams.getRoot_enterprise_id());
        reqParams.setSign(CheckUrlSignUtil.getSign(map, Constant.SysConfig.SYSTEM_KEY));
        String result = HttpUtils.restGetWithJson(ymlProperties.getNjwdCoreUrl() + Constant.Url.FIND_NOT_IMPORT, String.class, JsonUtils.object2Json(reqParams));
        // {"data":{"listData":[{"user_id":2284,"name":"12负者人","mobile":"12302111000","account":"ljx000","email":""},{"user_id":15284,"name":"lvjunxi1234","mobile":"12302111123","account":"lvjunxi1234","email":""}],"page":{"infoNum":10,"pageNo":1,"pageSize":10,"params":{"$ref":"$"},"totalPage":8,"totalRecord":78}},"status":"success"}
        // 转换为当前模块的page对象
        UserPageResp userPageResp = JsonUtils.json2Pojo(result, UserPageResp.class);
        if (userPageResp != null) {
            page.setTotal(userPageResp.getData().getPage().getTotalRecord());
            LinkedList<SysUserVo> sysUserVos = new LinkedList<>();
            page.setRecords(sysUserVos);
            userPageResp.getData().getListData().forEach(dataBean -> {
                SysUserVo vo = new SysUserVo();
                FastUtils.copyProperties(dataBean, vo);
                sysUserVos.add(vo);
            });
        }
        return page;
    }

    /**
     * 当前企业可选用户列表 分页
     *
     * @param sysUserDto
     * @return
     */
    @Override
    public Page<SysUserVo> findEnableList(SysUserDto sysUserDto) {
        Page<SysUserVo> page = sysUserDto.getPage();
        List<SysUserVo> sysUserVoList = sysUserMapper.selectEnableList(sysUserDto, page);
        return page.setRecords(sysUserVoList);
    }

    @Override
    public void exportExcel(SysUserDto sysUserDto, HttpServletResponse response) {
        Page<SysUserVo> page = sysUserDto.getPage();
        fileService.resetPage(page);
        sysUserDto.getCommParams().setOrColumn(Constant.OrMatchColumn.SYS_USER);
        List<SysUserVo> userVoList = sysUserMapper.findPage(sysUserDto, page, sysUserDto.getCommParams());
		/*fileService.exportExcel(response,userVoList
				,new ExcelColumn("mobile","手机号码")
				,new ExcelColumn("name","姓名")
				,new ExcelColumn("companyNames","公司")
				,new ExcelColumn("isEnableName","数据状态"));*/
        fileService.exportExcel(response, userVoList, MenuCodeConstant.SYS_USER);
    }

    @Override
    public int updateBySelf(SysUserDto sysUserDto, SysUserVo operator) {
        SysUserEnterprise sysUserEnterpriseQuery = new SysUserEnterprise();
        sysUserEnterpriseQuery.setUserId(operator.getUserId());
        sysUserEnterpriseQuery.setRootEnterpriseId(operator.getRootEnterpriseId());
        sysUserEnterpriseQuery.setIsEnable(Constant.Is.YES);
        SysUserEnterprise sysUserEnterprise = new SysUserEnterprise();
        sysUserEnterprise.setDefaultCompanyId(sysUserDto.getDefaultCompanyId());
        sysUserEnterprise.setIsLastAbstract(sysUserDto.getIsLastAbstract());
        sysUserEnterprise.setVoucherDateType(sysUserDto.getVoucherDateType());
        sysUserEnterprise.setVoucherListConfig(sysUserDto.getVoucherListConfig());
        sysUserEnterprise.setUpdatorId(operator.getUserId());
        sysUserEnterprise.setUpdatorName(operator.getName());
        sysUserEnterprise.setUpdateTime(new Date());
        return sysUserEnterpriseMapper.update(sysUserEnterprise, new QueryWrapper<>(sysUserEnterpriseQuery));
    }

    @Override
    public List<Long> addUserBatch(SysUserDto sysUserDto, SysUserVo operator) {
        List<Long> addUserIds = new LinkedList<>();
        for (SysUserDto userDto : sysUserDto.getSysUserDtoList()) {
            FastUtils.checkParams(userDto.getUserId(), userDto.getName());
            userDto.setCreatorId(operator.getUserId());
            userDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            userDto.setCreatorName(operator.getName());
            addUser(userDto);
            addUserIds.add(userDto.getUserId());
        }
        return addUserIds;
    }

    @Override
    public List<Long> httpAddUserBatch(SysUserDto sysUserDto, SysUserVo operator) {
        List<Long> addUserIds = new LinkedList<>();
        FastUtils.checkParams(sysUserDto.getUserId(), sysUserDto.getName());
        sysUserDto.setCreatorId(operator.getUserId());
        sysUserDto.setCreatorName(operator.getName());
        httpAddUser(sysUserDto);
        addUserIds.add(sysUserDto.getUserId());
        return addUserIds;
    }

    @Override
    public Map<Long, List<UserAccountBookEntityVo>> findAccountBookEntityByCompany(SysUserDto sysUserDto) {
        List<UserAccountBookEntityVo> accountBookEntityList = userAccountBookEntityMapper.findAccountBookEntityList(sysUserDto, UserUtils.getUserVo().getRootEnterpriseId());
        Map<Long, List<UserAccountBookEntityVo>> result = new LinkedHashMap<>();
        for (UserAccountBookEntityVo entityVo : accountBookEntityList) {
            List<UserAccountBookEntityVo> entityVoList = result.computeIfAbsent(entityVo.getUserId(), k -> new LinkedList<>());
            entityVoList.add(entityVo);
        }
        return result;
    }

    /**
     * 批量启用和禁用
     *
     * @param sysUserDto sysUserDto
     * @param operator   操作人
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/9/9 10:47
     **/
    @Override
    public BatchResult updateBatch(SysUserDto sysUserDto, SysUserVo operator) {
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        batchResult.setFailList(failList);
        List<Long> idList = sysUserDto.getUserIds();
        if (CollectionUtils.isNotEmpty(idList)) {
            // 筛选掉已物理删除的记录id
            Long rootEnterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
            idList = FastUtils.filterRemovedIds(ResultCode.IS_DEL, sysUserEnterpriseMapper, new QueryWrapper<SysUserEnterprise>().eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, rootEnterpriseId), Constant.ColumnName.USER_ID, idList, failList);
            // 筛选启用状态已变更成功的记录
            Byte isEnable = sysUserDto.getIsEnable();
            boolean disable = Constant.Is.NO.equals(isEnable);
            FastUtils.filterIds(disable ? ResultCode.IS_DISABLE : ResultCode.IS_ENABLE, sysUserEnterpriseMapper, new QueryWrapper<SysUserEnterprise>().eq(Constant.ColumnName.IS_ENABLE, isEnable).eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, rootEnterpriseId), Constant.ColumnName.USER_ID, idList, failList);
            if (!idList.isEmpty()) {
                //更新状态
                SysUserEnterprise sysUserEnterprise = new SysUserEnterprise();
                sysUserEnterprise.setIsEnable(isEnable);
                sysUserEnterprise.setRootEnterpriseId(rootEnterpriseId);
                sysUserEnterprise.setBatchIds(idList);
                ManagerInfo managerInfo = new ManagerInfo();
                sysUserEnterprise.setManageInfos(managerInfo);
                Date now = new Date();
                if (disable) {
                    managerInfo.setDisabledUserId(operator.getUserId());
                    managerInfo.setDisabledUserName(operator.getName());
                    managerInfo.setDisabledTime(now);
                } else {
                    managerInfo.setEnabledUserId(operator.getUserId());
                    managerInfo.setEnabledUserName(operator.getName());
                    managerInfo.setEnabledTime(now);
                }
                List<Object> list = FastUtils.getManagerList(managerInfo);
                sysUserEnterpriseMapper.batchDisableOrEnable(sysUserEnterprise, list, disable);

            }
            batchResult.setSuccessList(idList);
        }
        return batchResult;
    }

    @Override
    public BatchResult updateBatchDelete(SysUserDto sysUserDto) {
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        batchResult.setFailList(failList);
        List<Long> idList = sysUserDto.getUserIds();
        if (CollectionUtils.isNotEmpty(idList)) {
            Long rootEnterpriseId = sysUserDto.getRootEnterpriseId();
            FastUtils.checkParams(rootEnterpriseId);
            // 筛选掉已物理删除的记录id
            idList = FastUtils.filterRemovedIds(ResultCode.IS_DEL, sysUserEnterpriseMapper, new QueryWrapper<SysUserEnterprise>().eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, rootEnterpriseId), Constant.ColumnName.USER_ID, idList, failList);
            if (!idList.isEmpty()) {
                // 批量取消已分配的岗位
                sysUserDto.setUserIds(idList);
                sysRoleService.assignBatchDelete(sysUserDto, null, null);
                // 批量取消已分配的核算主体
                userAccountBookEntityMapper.delete(new LambdaQueryWrapper<UserAccountBookEntity>()
                        .in(UserAccountBookEntity::getUserId, idList)
                        .eq(UserAccountBookEntity::getRootEnterpriseId, rootEnterpriseId));
                // 批量删除
                sysUserEnterpriseMapper.delete(new QueryWrapper<SysUserEnterprise>().eq(Constant.ColumnName.ROOT_ENTERPRISE_ID, rootEnterpriseId).in(Constant.ColumnName.USER_ID, idList));
            }
            batchResult.setSuccessList(idList);
        }
        return batchResult;
    }

    @Override
    public SysUserEnterprise findConfig(SysUserVo operator) {
        return sysUserEnterpriseMapper.selectOne(new LambdaQueryWrapper<SysUserEnterprise>()
                .eq(SysUserEnterprise::getUserId, operator.getUserId())
                .eq(SysUserEnterprise::getRootEnterpriseId, operator.getRootEnterpriseId()));
    }

    private SysUserVo fill(@NotNull SysUserVo sysUserVo, Long rootEnterpriseId) {
        // 获取用户的所有角色,再封装到map
        Long userId = sysUserVo.getUserId();
        List<SysRoleVo> userRoleList = sysRoleMapper.findList(userId, rootEnterpriseId);
        Map<Long, List<SysRoleVo>> sysRoleMap = new LinkedHashMap<>();
        sysUserVo.setSysRoleMap(sysRoleMap);
        for (SysRoleVo sysRoleVo : userRoleList) {
            List<SysRoleVo> sysRoleVoList = sysRoleMap.computeIfAbsent(sysRoleVo.getCompanyId(), k -> new LinkedList<>());
            sysRoleVoList.add(sysRoleVo);
        }
        // 获取用户的所有权限,再封装到map
        List<SysMenuVo> userMenuList = sysMenuMapper.findListByUserId(userId, rootEnterpriseId);
        Map<Long, List<SysMenuVo>> sysMenuMap = new LinkedHashMap<>();
        // 封装有权的目录/模块/菜单并集
        Set<SysMenuVo> sysMenuSet = new LinkedHashSet<>();
        // 封装有权的按钮权限code,根据公司id(key)分类
        Map<Long, StringBuilder> sysButtonMap = new LinkedHashMap<>();
        sysUserVo.setSysButtonMap(sysButtonMap);
        sysUserVo.setSysMenuMap(sysMenuMap);
        // 用于判断是否属于目录/模块/菜单类型
        List<Byte> menus = Arrays.asList(Constant.MenuType.CATALOG, Constant.MenuType.MODULE, Constant.MenuType.MENU);
        for (SysMenuVo sysMenuVo : userMenuList) {
            if (Constant.MenuType.BUTTON == sysMenuVo.getType()) {
                StringBuilder sysButtonCodes = sysButtonMap.computeIfAbsent(sysMenuVo.getCompanyId(), k -> new StringBuilder());
                if (sysButtonCodes.length() > 0) {
                    sysButtonCodes.append(Constant.Character.COMMA);
                }
                sysButtonCodes.append(sysMenuVo.getCode());
            } else if (Collections.binarySearch(menus, sysMenuVo.getType()) > -1) {
                sysMenuSet.add(sysMenuVo);
            }
            List<SysMenuVo> sysMenuVoList = sysMenuMap.computeIfAbsent(sysMenuVo.getCompanyId(), k -> new LinkedList<>());
            sysMenuVoList.add(sysMenuVo);
        }
        String menuCodes = sysMenuSet.stream().map(SysMenu::getCode).collect(Collectors.joining(Constant.Character.COMMA));
        sysUserVo.setSysMenuCodes(menuCodes);
        // 获取用户的账簿主体列表,以公司为key
        List<UserAccountBookEntityVo> accountBookEntityList = userAccountBookEntityMapper.findAccountBookEntityList(sysUserVo, rootEnterpriseId);
        Map<Long, List<UserAccountBookEntityVo>> accountBookEntityMap = new LinkedHashMap<>();
        sysUserVo.setUserAccountBookEntityVoMap(accountBookEntityMap);
        for (UserAccountBookEntityVo userAccountBookEntityVo : accountBookEntityList) {
            List<UserAccountBookEntityVo> userAccountBookEntityVos = accountBookEntityMap.computeIfAbsent(userAccountBookEntityVo.getCompanyId(), k -> new LinkedList<>());
            userAccountBookEntityVos.add(userAccountBookEntityVo);
        }
        // 获取用户有权的公司列表,以公司id为key
        sysUserVo.setCompanyVoMap(companyMapper.findCompanyMap(userId, rootEnterpriseId));
        return sysUserVo;
    }
}
