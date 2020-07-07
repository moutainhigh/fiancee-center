package com.njwd.financeback.controller;

import com.njwd.basedata.cloudclient.TestFeignClient;
import com.njwd.basedata.service.DeptService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.CustomerSupplierCompany;
import com.njwd.entity.basedata.SysMenu;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.dto.SysMenuDto;
import com.njwd.entity.basedata.vo.CustomerSupplierVo;
import com.njwd.entity.basedata.vo.SysMenuVo;
import com.njwd.financeback.mapper.SysMenuMapper;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 测试Controller
 * @author: fancl
 * @create: 2019-05-21
 */
@RestController
@RequestMapping("test1")
@SuppressWarnings("all")
public class TestController extends BaseController {
    @Autowired
    private TestFeignClient testFeignClient;

    @Autowired
    private SenderService senderService;
    @Resource
    private SysMenuMapper sysMenuMapper;

    @Autowired
    DeptService deptService;

    //@Autowired
    //private SysTabColumnFeignClient sysTabColumnFeignClient;

    //测试缓存
    @RequestMapping("addCache")

    public Result addCache(@RequestBody DeptDto deptDto){
        //RedisUtils.remove("dept");
        deptService.findDeptById(deptDto);
        return ok("");
    }

    //单个删除缓存
    @RequestMapping("deleteCacheOne")
    public Result deleteCacheOne(@RequestBody DeptDto deptDto){
        RedisUtils.remove("dept",deptDto.getId());

        return ok("");
    }

    //批量删除缓存
    @RequestMapping("deleteCacheBatch")
    public Result deleteCacheBatch(@RequestBody DeptDto deptDto ){
        //清除指定多个缓存
        RedisUtils.removeBatch("dept",deptDto.getIdList());
        return ok("");

    }

    @RequestMapping("generateLog")
    public Result generateLog() {

        senderService.sendLog(UserUtils.getUserLogInfo(HttpUtils.getIpAddr(getRequest()),
                "财务后台", "基础模块", "新增", "add"));
        return ok("");


    }

    @GetMapping("hello")
    public Result test() {
        return null;//testFeignClient.test();
    }

    //@PostMapping("findSysTabColumnList")
    //public Result findSysTabColumnList(){
    //    return sysTabColumnFeignClient.findSysTabColumnList();
    //}

    /**
     * 测试添加目录权限
     */
    @PostMapping("insertMenu")
    @Transactional(rollbackFor = Exception.class)
    public Result insertMenu(@RequestBody SysMenuDto sysMenuDto) {
        SysMenu catalog = new SysMenu();
        FastUtils.copyProperties(sysMenuDto, catalog);
        catalog.setCreatorId(0L);
        catalog.setIsChild(Constant.Is.NO);
        catalog.setType(Constant.MenuType.CATALOG);
        sysMenuMapper.insert(catalog);
        for (SysMenuVo secondVo : sysMenuDto.getSysMenuList()) {
            SysMenu second = new SysMenu();
            FastUtils.copyProperties(secondVo, second);
            second.setParentId(catalog.getMenuId());
            second.setCreatorId(0L);
            second.setIsChild(Constant.Is.NO);
            second.setType(Constant.MenuType.MODULE);
            sysMenuMapper.insert(second);
            for (SysMenuVo menuVo : secondVo.getSysMenuList()) {
                SysMenu menu = new SysMenu();
                FastUtils.copyProperties(menuVo, menu);
                menu.setParentId(second.getMenuId());
                menu.setCreatorId(0L);
                menu.setIsChild(Constant.Is.NO);
                menu.setType(Constant.MenuType.MENU);
                menu.setTypeName(catalog.getName());
                sysMenuMapper.insert(menu);
                // 通用操作
                SysMenu common = new SysMenu();
                common.setParentId(menu.getMenuId());
                common.setCreatorId(0L);
                common.setName("通用操作");
                common.setIsChild(Constant.Is.NO);
                common.setType(Constant.MenuType.GROUP);
                sysMenuMapper.insert(common);
                if (menuVo.getSysMenuList() != null) {
                    for (SysMenuVo buttonVo : menuVo.getSysMenuList()) {
                        SysMenu button = new SysMenu();
                        FastUtils.copyProperties(buttonVo, button);
                        button.setParentId(common.getMenuId());
                        button.setCreatorId(0L);
                        button.setType(Constant.MenuType.BUTTON);
                        sysMenuMapper.insert(button);
                    }
                }
            }
        }
        return ok(true);
    }

    /**
     * 测试添加目录权限2
     */
    @PostMapping("insertMenu2")
    @Transactional(rollbackFor = Exception.class)
    public Result insertMenu2(@RequestBody SysMenuDto sysMenuDto) {
        SysMenu catalog = new SysMenu();
        if (sysMenuDto.getMenuId() == null) {
            FastUtils.copyProperties(sysMenuDto, catalog);
            catalog.setCreatorId(0L);
            catalog.setIsChild(Constant.Is.NO);
            catalog.setType(Constant.MenuType.CATALOG);
            sysMenuMapper.insert(catalog);
        } else {
            catalog.setMenuId(sysMenuDto.getMenuId());
            catalog.setName(sysMenuDto.getName());
        }
        for (SysMenuVo secondVo : sysMenuDto.getSysMenuList()) {
            SysMenu second = new SysMenu();
            if (secondVo.getMenuId() == null) {
                FastUtils.copyProperties(secondVo, second);
                second.setParentId(catalog.getMenuId());
                second.setCreatorId(0L);
                second.setIsChild(Constant.Is.NO);
                second.setType(Constant.MenuType.MODULE);
                sysMenuMapper.insert(second);
            } else {
                second.setMenuId(secondVo.getMenuId());
                second.setName(secondVo.getName());
            }
            for (SysMenuVo menuVo : secondVo.getSysMenuList()) {
                SysMenu menu = new SysMenu();
                SysMenu common = new SysMenu();
                if (menuVo.getMenuId() == null) {
                    FastUtils.copyProperties(menuVo, menu);
                    menu.setParentId(second.getMenuId());
                    menu.setCreatorId(0L);
                    menu.setIsChild(Constant.Is.NO);
                    menu.setType(Constant.MenuType.MENU);
                    menu.setTypeName(catalog.getName());
                    sysMenuMapper.insert(menu);
                    // 通用操作
                    common.setParentId(menu.getMenuId());
                    common.setCreatorId(0L);
                    common.setName("通用操作");
                    common.setIsChild(Constant.Is.NO);
                    common.setType(Constant.MenuType.GROUP);
                    sysMenuMapper.insert(common);
                } else {
                    common.setMenuId(menuVo.getMenuId());
                }
                if (menuVo.getSysMenuList() != null) {
                    for (SysMenuVo buttonVo : menuVo.getSysMenuList()) {
                        SysMenu button = new SysMenu();
                        FastUtils.copyProperties(buttonVo, button);
                        button.setParentId(common.getMenuId());
                        button.setCreatorId(0L);
                        button.setType(Constant.MenuType.BUTTON);
                        sysMenuMapper.insert(button);
                    }
                }
            }
        }
        return ok(true);
    }

    public static void main(String[] args) {
        List<CustomerSupplierVo> csList = new ArrayList<>();
        List<CustomerSupplierCompany> cscList = new ArrayList<>();
        CustomerSupplierVo cs = new CustomerSupplierVo();
        CustomerSupplierCompany csc = new CustomerSupplierCompany();
        for (int i=0;i<10; i++) {
            cs = new CustomerSupplierVo();
            cs.setId(Long.valueOf(i+1));
            cs.setCompanyId(Long.valueOf(i+1));
            cs.setName("你好吗"+i);
            csList.add(cs);
        }
        for (int i=0;i<200; i++) {
            csc = new CustomerSupplierCompany();
            csc.setCompanyId(Long.valueOf(i+2));
            //csc.setLinkman("杨叶"+i);
            cscList.add(csc);
        }
        long start = System.currentTimeMillis();
        /*MergeUtil.merge(csList,cscList,
                (cs1, csc1) -> cs1.getId().equals(csc1.getCustomerSupplierId()),
                (cs1, csc1) -> {
                    cs1.setUseCompanyId(csc1.getCompanyId());
                    cs1.setLinkman(csc1.getLinkman());
                });*/
        MergeUtil.mergeList(csList,cscList,
                (cs1, csc1)->{return true;},
                (cs1, cscList1) -> {
                    cs1.getCustomerSupplierCompanyList().addAll(cscList1);
                });
        long end = System.currentTimeMillis();
        csList.forEach(System.out::println);
    }

    private static Boolean conditionc(CustomerSupplierVo s1, CustomerSupplierCompany s2) {
        return false;
    }
}

