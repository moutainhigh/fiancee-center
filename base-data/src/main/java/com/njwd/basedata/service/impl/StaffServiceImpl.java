package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.mapper.StaffMapper;
import com.njwd.basedata.service.*;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.Staff;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.dto.StaffDto;
import com.njwd.entity.basedata.dto.StaffUseCompanyDto;
import com.njwd.entity.basedata.vo.DeptVo;
import com.njwd.entity.basedata.vo.StaffVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Author ZhuHC
 * @Date  2019/7/3 14:58
 * @Description  员工service
 */
@Service
public class StaffServiceImpl implements StaffService{

    @Resource
    private StaffMapper staffMapper;

    @Resource
    private FileService fileService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private DeptService deptService;

    @Resource
    private SequenceService sequenceService;

    @Resource
    private StaffUseCompanyService staffUseCompanyService;

    @Resource
    private BaseCustomService baseCustomService;

    @Resource
    private StaffService staffService;

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 16:19
     * @Param [staffDto]
     * @return Long
     * @Description 新增员工
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addStaff(StaffDto staffDto) {
        if (Constant.Is.NO.equals(staffDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.STAFF_EDIT,staffDto.getCompanyId());
        }
        //设置基础数据
        SysUserVo userVo = UserUtils.getUserVo();
        staffDto.setCreatorId(userVo.getUserId());
        staffDto.setCreatorName(userVo.getName());
        staffDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        staffDto.setIsDel(Constant.Is.NO);
        staffDto.setIsEnable(Constant.Is.YES);
        //创建公司
        staffDto.setCreateCompanyId(staffDto.getCompanyId());
        //校验部门 是否是末级部门 否 则无法引用
        checkDeptIsEnd(staffDto);
        //校验 手机号 是否唯一
        if(StringUtil.isNotEmpty(staffDto.getContactNumber())){
            checkContactNumber(staffDto);
        }
        //校验 银行号 是否唯一
        if(StringUtil.isNotEmpty(staffDto.getBankAccount())){
            checkBankAccount(staffDto);
        }
        //如果codeType==0，需要系统生成项目编号
        if (staffDto.getCodeType() == Constant.CodeType.SYSTEMCODE) {
            getStaffCode(staffDto);
        }
        //校验 该公司员工编码唯一性 部门内唯一
        checkStaffUniqueness(staffDto);
        Staff staff = new StaffDto();
        FastUtils.copyProperties(staffDto,staff);
        staffMapper.insertStaff(staff);
        Long staffId = staff.getId();
        //员工使用公司表同步新增数据
        StaffUseCompanyDto staffUserCompanyDto = setStaffUserCompanyDto(staffDto, staffId);
        staffUseCompanyService.insertStaffUseCompany(staffUserCompanyDto);
        return staffId;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 14:55
     * @Param [staffDto]
     * @return int
     * @Description 新增
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertStaff(StaffDto staffDto) {
        //校验 该公司员工编码唯一性 部门内唯一
        checkStaffUniqueness(staffDto);
        int flag = staffMapper.insertStaff(staffDto);
        //员工使用公司表同步新增数据
        StaffUseCompanyDto staffUserCompanyDto = setStaffUserCompanyDto(staffDto, staffDto.getId());
        staffUseCompanyService.insertStaffUseCompany(staffUserCompanyDto);
        return flag;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/2 17:47
     * @Param [staffDtoList]
     * @return int
     * @Description 批量插入
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addBatchStaff(List<StaffDto> staffDtoList){
        for(StaffDto staffDto : staffDtoList){
            //校验 该公司员工编码唯一性 部门内唯一
            checkStaffUniqueness(staffDto);
        }
        int flag = staffMapper.addBatchStaff(staffDtoList);
        List<StaffUseCompanyDto> staffUseCompanyDtos = new ArrayList<>();
        for(StaffDto staffDto : staffDtoList){
            StaffUseCompanyDto staffUserCompanyDto = setStaffUserCompanyDto(staffDto, staffDto.getId());
            staffUseCompanyDtos.add(staffUserCompanyDto);
        }
        staffUseCompanyService.insertStaffUseCompanyList(staffUseCompanyDtos);
        return flag;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/26 13:41
     * @Param [staffDto]
     * @return BatchResult
     * @Description 删除员工信息
     */
    @Override
    public BatchResult deleteStaff(StaffDto staffDto) {
        RedisUtils.removeBatch(Constant.RedisCache.STAFF,staffDto.getIds());
        return getBatchResult(staffDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 16:36
     * @Param [staffDto]
     * @return int
     * @Description 根据ID删除员工
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.STAFF ,key = "#staffDto.id")
    public BatchResult deleteStaffById(StaffDto staffDto) {
        List<Long> ids = new ArrayList<>();
        ids.add(staffDto.getId());
        List<Integer> versions = new ArrayList<>();
        versions.add(staffDto.getVersion());
        staffDto.setIds(ids);
        staffDto.setVersions(versions);
        return getBatchResult(staffDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/4 15:25
     * @Param [staffDto]
     * @return com.njwd.support.BatchResult
     * @Description 禁用
     */
    @Override
    public BatchResult disableBatch(StaffDto staffDto) {
        //判断 是否有禁用权限
        BatchResult batchResult = new BatchResult();
        if (Constant.Is.NO.equals(staffDto.getIsEnterpriseAdmin())) {
            //验证权限
            batchResult=batchVerifyPermission(staffDto,Constant.MenuDefine.STAFF_DISABLE);
            //根据删选后的数据 获得可删除的员工ID和版本号
            structuredData(staffDto,batchResult);
        }
        RedisUtils.removeBatch(Constant.RedisCache.STAFF,staffDto.getIds());
        String msg = ResultCode.STAFF_DELETED_OR_DISABLED.message;
        updateBatdh(staffDto, msg, batchResult);
        return batchResult;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/9 15:30
     * @Param [staffDto]
     * @return com.njwd.support.BatchResult
     * @Description 单个禁用
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.STAFF ,key = "#staffDto.getIds().get(0)")
    public BatchResult disableStaff(StaffDto staffDto) {
        String msg = ResultCode.STAFF_DELETED_OR_DISABLED.message;
        BatchResult batchResult = new BatchResult();
        updateBatdh(staffDto, msg, batchResult);
        return batchResult;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/4 15:25
     * @Param [staffDto]
     * @return com.njwd.support.BatchResult
     * @Description 反禁用
     */
    @Override
    public BatchResult enableBatch(StaffDto staffDto) {
        BatchResult batchResult = new BatchResult();
        if (Constant.Is.NO.equals(staffDto.getIsEnterpriseAdmin())) {
            //验证权限
            batchResult=batchVerifyPermission(staffDto,Constant.MenuDefine.STAFF_ENABLE);
            //根据删选后的数据 获得可删除的员工ID和版本号
            structuredData(staffDto,batchResult);
        }
        RedisUtils.removeBatch(Constant.RedisCache.STAFF,staffDto.getIds());
        String msg = ResultCode.STAFF_DELETED_OR_ENABLED.message;
        updateBatdh(staffDto, msg, batchResult);
        return batchResult;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/9 15:31
     * @Param [staffDto]
     * @return com.njwd.support.BatchResult
     * @Description 单个反禁用
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.STAFF ,key = "#staffDto.getIds().get(0)")
    public BatchResult enableStaff(StaffDto staffDto) {
        String msg = ResultCode.STAFF_DELETED_OR_ENABLED.message;
        BatchResult batchResult = new BatchResult();
        updateBatdh(staffDto, msg, batchResult);
        return batchResult;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/28 10:43
     * @Param [staffDto]
     * @return int
     * @Description 修改员工信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = Constant.RedisCache.STAFF ,key = "#staffDto.id")
    public BatchResult updateStaffInfo(StaffDto staffDto) {
        if (Constant.Is.NO.equals(staffDto.getIsEnterpriseAdmin())) {
            ShiroUtils.checkPerm(Constant.MenuDefine.STAFF_EDIT,staffDto.getCompanyId());
        }
        //校验编码唯一性
        staffDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        int row = staffMapper.findStaffByInfo(staffDto);
        if (row != 0) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
        //校验 手机号 是否唯一
        if(StringUtil.isNotEmpty(staffDto.getContactNumber())){
            checkContactNumber(staffDto);
        }
        //校验 银行号 是否唯一
        if(StringUtil.isNotEmpty(staffDto.getBankAccount())){
            checkBankAccount(staffDto);
        }
        //查看 数据是否已被他人修改  已被修改数据需页面更新后才能再次修改
        StaffVo staffVo = findStaffById(staffDto);
        BatchResult batchResult = new BatchResult();
        //数据未被他人修改
        boolean flag = null != staffVo && staffVo.getVersion().equals(staffDto.getVersion());
        if(flag){
            Staff staff = new StaffDto();
            FastUtils.copyProperties(staffDto,staff);
            setUpdateInfo(staff);
            List<Long> ids = new ArrayList<>();
            ids.add(staffDto.getId());
            staffDto.setIds(ids);
            FastUtils.updateBatch(staffMapper, staff, Constant.ColumnName.ID, staffDto.getIds(),null);
            //员工使用公司表同步新增数据
            StaffUseCompanyDto staffUserCompanyDto = setStaffUserCompanyDto(staffDto, staffDto.getId());
            staffUseCompanyService.insertStaffUseCompanyNotExist(staffUserCompanyDto);
            batchResult.setSuccessList(staffDto.getIds());
        }else {
            List<ReferenceDescription> failList = new ArrayList<>();
            ReferenceDescription description = new ReferenceDescription();
            description.setBusinessCode(Constant.Reference.STAFF);
            description.setBusinessId(staffDto.getId());
            description.setReferenceDescription(ResultCode.STAFF_INFO_CHANGED.message);
            failList.add(description);
            batchResult.setFailList(failList);
        }
        return batchResult;
    }

    /**
     * @Description //同步部门使用公司变更
     * @Author jds
     * @Date 2019/8/20 11:17
     * @Param [deptDto]
     * @return int
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStaffInfoFromDept(DeptDto deptDto){
        StaffDto staffDto=new StaffDto();
        staffDto.setUseCompanyId(deptDto.getUseCompanyId());
        staffDto.setBusinessUnitId(deptDto.getBusinessUnitId());
        //变更员工信息
        staffMapper.update(staffDto, new LambdaQueryWrapper<Staff>().in(Staff::getDeptId,deptDto.getId() ));
        List<StaffUseCompanyDto>StaffUseCompanyList=new ArrayList<>();
        List<Long>list=deptDto.getStaffIdList();
        for(Long id:list){
            StaffUseCompanyDto staffUseCompanyDto=new StaffUseCompanyDto();
            staffUseCompanyDto.setCreatorId(UserUtils.getUserVo().getUserId());
            staffUseCompanyDto.setCreatorName(UserUtils.getUserVo().getName());
            staffUseCompanyDto.setCreateTime(new Date());
            staffUseCompanyDto.setStaffId(id);
            staffUseCompanyDto.setDeptId(deptDto.getId());
            staffUseCompanyDto.setUseCompanyId(deptDto.getUseCompanyId());
            staffUseCompanyDto.setBusinessUnitId(deptDto.getBusinessUnitId());
            StaffUseCompanyList.add(staffUseCompanyDto);
        }
        //添加员工变更记录
        int result=staffUseCompanyService.insertStaffUseCompanyList(StaffUseCompanyList);
        //清除缓存
        RedisUtils.removeBatch(Constant.RedisCache.STAFF, list);
        return result;
    }


    /**
     * @Description //根据部门id查询关联员工
     * @Author jds
     * @Date 2019/8/20 13:59
     * @Param [deptDto]
     * @return java.util.List<java.lang.Long>
     **/
    @Override
    public List<Long> findStaffByDeptId(DeptDto deptDto){
        StaffDto staffDto=new StaffDto();
        staffDto.setDeptId(deptDto.getId());
        List<Long>idList=staffMapper.findStaffByDeptId(staffDto);
        return idList;
    }



    /**
     * @Author ZhuHC
     * @Date  2019/6/20 16:37
     * @Param [staffDto]
     * @return com.njwd.entity.basedata.vo.StaffVo
     * @Description 根据ID查询员工
     */
    @Override
    public StaffVo findStaffById(StaffDto staffDto) {
        StaffVo staffVo = staffService.findStaffByIdWithCache(staffDto);
        //校验数据是否被引用
        ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.STAFF, staffDto.getId());
        if(referenceResult.isReference() ){
            staffVo.setIsDraw(Constant.Number.ONE);
        }
        return staffVo;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/9 10:58
     * @Param [staffDto]
     * @return com.njwd.entity.basedata.vo.StaffVo
     * @Description 根据ID查询员工 带缓存
     */
    @Override
    @Cacheable(value = Constant.RedisCache.STAFF ,key = "#staffDto.id",unless = "#result==null")
    public StaffVo findStaffByIdWithCache(StaffDto staffDto){
        return staffMapper.findStaffById(staffDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 16:37
     * @Param [staffDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.StaffVo>
     * @Description 查询员工列表 分页
     */
    @Override
    public Page<StaffVo> findStaffPage(StaffDto staffDto) {
        Page<StaffVo> page = staffDto.getPage();
        staffDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Page<StaffVo> staffVoList = staffMapper.findPage(page,staffDto);
        return staffVoList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 15:37
     * @Param [staffDto]
     * @return java.lang.Integer
     * @Description 根据员工编码和企业ID查找员工是否存在
     */
    @Override
    public Integer findStaffByCode(StaffDto staffDto) {
        staffDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        return staffMapper.findStaffByCode(staffDto);
    }

    /**
    * @description: 根据员工编码和企业id查询员工id
    * @param staffDto
    * @return java.lang.Integer
    * @author LuoY
    * @date 2019/7/19 15:11
    */
    @Override
    public Integer findStaffInfoByCode(StaffDto staffDto){
        staffDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        return staffMapper.findStaffInfoByCode(staffDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:31
     * @Param staffDto
     * @return Integer
     * @Description
     */
    @Override
    public Integer findStaffInfoByNumberOrCard(StaffDto staffDto) {
        return staffMapper.findStaffInfoByNumberOrCard(staffDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 16:38
     * @Param [staffDto, response]
     * @return void
     * @Description 数据导出
     */
    @Override
    public void exportExcel(StaffDto staffDto, HttpServletResponse response) {
        Page<StaffVo> page = staffDto.getPage();
        staffDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        fileService.resetPage(page);
        Page<StaffVo> staffVoPage = staffMapper.findPage(page,staffDto);
        List<StaffVo> staffVoList = staffVoPage.getRecords();
        BatchResult batchResult;
        if (Constant.Is.NO.equals(staffDto.getIsEnterpriseAdmin())) {
            //验证权限
            batchResult=ShiroUtils.filterNotPermData(staffVoList, Constant.MenuDefine.STAFF_EXPORT, new ShiroUtils.CheckPermSupport<StaffVo>() {
                @Override
                public Long getBusinessId(StaffVo staffVo) {
                    return staffVo.getId();
                }
                @Override
                public Long getCompanyId(StaffVo staffVo) { return staffVo.getCompanyId(); }
            });
            //删选后的数据
            if(!FastUtils.checkNullOrEmpty(batchResult.getFailList())){
                List<StaffVo> failVoList = new LinkedList<>();
                for(ReferenceDescription res:batchResult.getFailList()){
                    for(StaffVo vo : staffVoList){
                        if(vo.getId().equals(res.getBusinessId())){
                            failVoList.add(vo);
                        }
                    }
                }
                staffVoList.removeAll(failVoList);
            }
        }
        fileService.exportExcel(response, staffVoList, MenuCodeConstant.STAFF,staffDto.getIsEnterpriseAdmin());
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/18 18:02
     * @Param [staffDto, menuDefine]
     * @return com.njwd.support.BatchResult
     * @Description
     */
    public BatchResult batchVerifyPermission(StaffDto staffDto,String menuDefine){
        List<StaffVo> staffVoList=staffMapper.findStaffListById(staffDto);
        return ShiroUtils.filterNotPermData(staffVoList, menuDefine, new ShiroUtils.CheckPermSupport<StaffVo>() {
            @Override
            public Long getBusinessId(StaffVo staffVo) {
                return staffVo.getId();
            }

            @Override
            public Long getCompanyId(StaffVo staffVo) { return staffVo.getCompanyId(); }
        });
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 14:56
     * @Param [staffDto]
     * @return void
     * @Description 校验员工编码
     */
    private void checkStaffUniqueness(StaffDto staffDto) {
        Integer row = findStaffByCode(staffDto);
        if (row != 0) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 16:25
     * @Param [staff]
     * @return void
     * @Description 设置更新人及更新时间等数据
     */
    private void setUpdateInfo(Staff staff)
    {
        SysUserVo userVo = UserUtils.getUserVo();
        staff.setUpdatorId(userVo.getUserId());
        staff.setUpdatorName(userVo.getName());
        staff.setUpdateTime(new Date());
    }
    /**
     * @Author ZhuHC
     * @Date  2019/7/4 15:26
     * @Param [staffDto, msg, batchResult]
     * @return void
     * @Description
     */
    private void updateBatdh(StaffDto staffDto, String msg, BatchResult batchResult) {
        //排除已删除/反禁用和已引用数据
        List<ReferenceDescription> failList = batchResult.getFailList();
        List<Long> enableIds = getEnableUseIds(staffDto, msg, failList);
        Staff staff = new StaffDto();
        if(!FastUtils.checkNullOrEmpty(enableIds)){
            FastUtils.copyProperties(staffDto, staff);
            //批量操作数据
            staff.setBatchIds(enableIds);
            baseCustomService.batchEnable(staff,staff.getIsEnable(),staffMapper,batchResult.getSuccessDetailsList());
        }
        batchResult.setSuccessList(enableIds);
        batchResult.setFailList(failList);
    }

    private List<Long> getEnableUseIds(StaffDto staffDto, String msg, List<ReferenceDescription> failList) {
        FastUtils.filterVersionIds(staffMapper,new QueryWrapper<>(), Constant.ColumnName.ID,staffDto.getIds(),staffDto.getVersions(),failList);
        List<Long> enableIds = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(staffDto.getIds())){
            List<StaffVo> deletedList = staffMapper.findDeletedIds(staffDto);
            //已删除/禁用数据的ID
            List<Long> deleteIds = new ArrayList<>();
            for (StaffVo staffVo : deletedList) {
                Long deleteId = staffVo.getId();
                deleteIds.add(deleteId);
                //设置已删除ID和描述
                ReferenceDescription referenceDescription = new ReferenceDescription();
                referenceDescription.setBusinessId(deleteId);
                referenceDescription.setReferenceDescription(msg);
                //添加到返回值之中
                failList.add(referenceDescription);
            }
            //可以修改的数据ID
            HashSet deleteHash = new HashSet(deleteIds);
            HashSet allHash = new HashSet(staffDto.getIds());
            allHash.removeAll(deleteHash);
            enableIds.addAll(allHash);
        }
        return enableIds;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/9 15:23
     * @Param [staffDto]
     * @return com.njwd.support.BatchResult
     * @Description 删除 通用方法
     */
    private BatchResult getBatchResult(StaffDto staffDto) {
        BatchResult batchResult = new BatchResult();
        if (Constant.Is.NO.equals(staffDto.getIsEnterpriseAdmin())) {
            //验证权限
            batchResult=batchVerifyPermission(staffDto,Constant.MenuDefine.STAFF_DELETE);
            //根据删选后的数据 获得可删除的员工ID和版本号
            structuredData(staffDto,batchResult);
        }
        setUpdateInfo(staffDto);
        String msg = ResultCode.STAFF_DELETED.message;
        //排除已删除和已引用数据 ; 排除 已修改（version 不匹配）的数据
        List<ReferenceDescription> failList = batchResult.getFailList();
        ReferenceContext referenceContext = getReferenceContext(staffDto, msg, failList);
        //未被引用的ID
        if(!referenceContext.getNotReferences().isEmpty()){
            //批量操作数据
            StaffDto dto = new StaffDto();
            dto.setIds(referenceContext.getNotReferences());
            dto.setUpdatorId(UserUtils.getUserVo().getUserId());
            dto.setUpdatorName(UserUtils.getUserVo().getName());
            dto.setUpdateTime(new Date());
            staffMapper.deleteStaffByIds(dto);
            batchResult.setSuccessList(referenceContext.getNotReferences());
        }
        //被引用的ID及说明
        if(!referenceContext.getReferences().isEmpty()){
            //返回不能删除数据及原因
            failList.addAll(referenceContext.getReferences());
        }
        batchResult.setFailList(failList);
        return batchResult;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/7/4 15:11
     * @Param [staffDto, msg, failList]
     * @return com.njwd.entity.basedata.ReferenceContext
     * @Description
     */
    private ReferenceContext getReferenceContext(StaffDto staffDto, String msg, List<ReferenceDescription> failList) {
        FastUtils.filterVersionIds(staffMapper,new QueryWrapper<>(), Constant.ColumnName.ID,staffDto.getIds(),staffDto.getVersions(),failList);
        if(!FastUtils.checkNullOrEmpty(staffDto.getIds())){
            List<StaffVo> deletedList = staffMapper.findDeletedIds(staffDto);
            //已删除/禁用数据的ID
            List<Long> deleteIds = new ArrayList<>();
            for(StaffVo staffVo : deletedList)
            {
                Long deleteId = staffVo.getId();
                deleteIds.add(deleteId);
                //设置已删除ID和描述
                ReferenceDescription referenceDescription = new ReferenceDescription();
                referenceDescription.setBusinessId(deleteId);
                referenceDescription.setReferenceDescription(msg);
                //添加到返回值之中
                failList.add(referenceDescription);
            }
            //可以修改的数据ID
            HashSet deleteHash = new HashSet(deleteIds);
            HashSet allHash = new HashSet(staffDto.getIds());
            allHash.removeAll(deleteHash);
            List<Long> enableIds = new ArrayList<>();
            enableIds.addAll(allHash);
            return referenceRelationService.isReference(Constant.Reference.STAFF,enableIds);
        }else {
            return new ReferenceContext();
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/21 14:20
     * @Param [staffDto]
     * @return void
     * @Description 校验 银行号 是否唯一
     */
    private void checkBankAccount(StaffDto staffDto) {
        StaffDto dto = new StaffDto();
        dto.setBankAccount(staffDto.getBankAccount());
        if(null != staffDto.getId() && !staffDto.getId().equals("")){
            dto.setId(staffDto.getId());
        }
        Integer num = findStaffInfoByNumberOrCard(dto);
        if (num != 0) {
            throw new ServiceException(ResultCode.EXIST_BANK_ACCOUNT);
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/21 14:20
     * @Param [staffDto]
     * @return void
     * @Description 校验 手机号  是否唯一
     */
    private void checkContactNumber(StaffDto staffDto) {
        StaffDto dto = new StaffDto();
        dto.setContactNumber(staffDto.getContactNumber());
        if(null != staffDto.getId() && !staffDto.getId().equals("")){
            dto.setId(staffDto.getId());
        }
        Integer num = findStaffInfoByNumberOrCard(dto);
        if (num != 0) {
            throw new ServiceException(ResultCode.EXIST_CONTACT_NUMBER);
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:33
     * @Param
     * @return StaffUseCompanyDto
     * @Description 设置员工变更表数据
     */
    private StaffUseCompanyDto setStaffUserCompanyDto(StaffDto staffDto, Long staffId) {
        StaffUseCompanyDto staffUserCompanyDto = new StaffUseCompanyDto();
        SysUserVo userVo = UserUtils.getUserVo();
        staffUserCompanyDto.setStaffId(staffId);
        staffUserCompanyDto.setDeptId(staffDto.getDeptId());
        staffUserCompanyDto.setUseCompanyId((staffDto.getUseCompanyId()));
        staffUserCompanyDto.setBusinessUnitId(staffDto.getBusinessUnitId());
        staffUserCompanyDto.setCreatorId(userVo.getUserId());
        staffUserCompanyDto.setCreatorName(userVo.getName());
        staffUserCompanyDto.setCreateTime(new Date());
        return staffUserCompanyDto;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 11:51
     * @Param [staffDto]
     * @return void
     * @Description 获得员工编码
     */
    private void getStaffCode(StaffDto staffDto) {
        //根据部门ID 获取 部门编码
        DeptDto deptDto = new DeptDto();
        deptDto.setId(staffDto.getDeptId());
        DeptVo deptVo = deptService.findDeptById(deptDto);
        staffDto.setCode(sequenceService.getCode(Constant.BaseCodeRule.LENGTH_THREE,staffDto.getCompanyId(),
                deptVo.getCode(),Constant.BaseCodeRule.COMPANY));
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 11:51
     * @Param [staffDto]
     * @return void
     * @Description 校验部门是否末级
     */
    private void checkDeptIsEnd(StaffDto staffDto) {
        Long enterpriseId = UserUtils.getUserVo().getRootEnterpriseId();
        DeptDto deptDto = new DeptDto();
        deptDto.setRootEnterpriseId(enterpriseId);
        deptDto.setId(staffDto.getDeptId());
        DeptVo deptVo = deptService.findDeptByIdForMapper(deptDto);
        if(null == deptVo && Constant.Is.NO.equals(deptVo.getIsEnd())) {
            throw new ServiceException(ResultCode.DEPT_IS_NOT_END);
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/18 17:40
     * @Param [staffDto, batchResult]
     * @return void
     * @Description 过滤未通过校验的员工ID对应的version
     */
    private void structuredData(StaffDto staffDto,BatchResult batchResult){
        if(batchResult.getFailList().size()>Constant.Number.ZERO){
            List<Long>arrayList=new ArrayList<>();
            arrayList.addAll(staffDto.getIds());
            List<Integer>verList=new ArrayList<>();
            verList.addAll(staffDto.getVersions());
            //有权限的ID集合
            List<Long>list=new ArrayList<>();
            list.addAll(staffDto.getIds());
            List<Long> fail = new ArrayList<>();
            for(ReferenceDescription res:batchResult.getFailList()){
                fail.add(res.getBusinessId());
            }
            list.removeAll(fail);
            //有权限的版本号集合
            List<Integer> versionList = new ArrayList<>();
            if(staffDto.getIds().size()>Constant.Number.ZERO){
                for(Long value:list){
                    versionList.add(verList.get(arrayList.indexOf(value)));
                }
            }
            staffDto.setIds(list);
            staffDto.setVersions(versionList);
        }
    }
}
