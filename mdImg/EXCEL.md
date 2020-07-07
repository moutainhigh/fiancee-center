#一、excel导入自定义扩展说明
excel分步骤导入，上传--校验--导入。校验不过的数据可以下载日志查看，其余数据可继续导入。导入异常的数据可以下载日志查看，其余数据正常入库。
上传、校验、导入使用统一的接口就行，请求参数中templateType来区分不同的业务模块。需要自定义校验、导入的可使用以下方法。
##1、校验excel
通用规则的校验在数据库中进行配置，特殊规则的校验可以通过代码实现。
业务模块service层添加注解@ExcelExtend(type = "test_excel")，并实现CheckExtend接口。
示例如下：
```
@Service
@ExcelExtend(type = "test_excel")//对应excel模板类型templateType
public class TestServiceImpl implements TestService, CheckExtend {
    
    @Override
    public void check(CheckContext checkContext) {
        checkContext.setDataType(TestDto.class);
        //通用规则校验通过后，会校验自定义的规则
        checkContext.addHandler("code",getCodeCheckHandler())
                .addHandler("name",getNameCheckHandler());
    }
    
    /**
     * 校验code列 规则
     */
    public CheckHandler<TestDto> getCodeCheckHandler(){
        return data -> {
            System.err.println(data.getCode());
            //校验成功
            return CheckResult.ok();
        };
    }
    
    /**
     * 校验name列 规则
     */
    public CheckHandler<TestDto> getNameCheckHandler(){
        return data -> {
            System.err.println(data.getName());
            //校验失败，并返回原因
            return CheckResult.error("名称不符合规范");
        };
    }
    
}
```
##2、导入excel
简单的单表导入在数据库中进行配置，多表等复杂导入可以通过代码实现。
业务模块service层添加注解@ExcelExtend(type = "test_excel")，并实现AddExtend<T>接口。
示例如下：
```
@Service
@ExcelExtend(type = "test_excel")//对应excel模板类型templateType
public class TestServiceImpl implements TestService, AddExtend<TestDto> {
    
    @Resource
    TestMapper testMapper;

    /**
     * excel数据批量入库
     */
    @Override
    public int addBatch(List<TestDto> datas) {
        //批量入库操作
        return 0;
    }
    
    /**
     * excel数据逐条入库，在批量入库方法(addBatch)异常后才会调用此方法
     */
    @Override
    public int add(TestDto data) {
        testMapper.insert(data);
        return 0;
    }
    
}
```
##3、excel行与业务实体映射
excel列位置与实体字段进行对应。在实现CheckExtend、AddExtend等多接口时，映射的业务实体的类型保持一致，此处都为TestDto。
```
public class TestDto {

    private Long id;
    
    @ExcelCell(index = 0)//index对应excel列位置
    private String name;
    
    @ExcelCell(index = 1)//index对应excel列位置
    private String code;
    
}
```
##4、excel数据转换
通用的数据转换可以在数据库进行配置，特殊的数据转换可以通过代码实现。
业务模块service层添加注解@ExcelExtend(type = "test_excel")，并实现ConvertExtend接口。
通用的数据转换发生在检验前，自定义的数据转换可以发生在导入前或校验前，
默认是在导入前进行转换，ConvertContext.CONVERT_BEFORE_CHECK指定在校验前进行数据转换。
如果数据转换是在导入前以及实现了自定义导入，数据转换也可以在AddExtend接口中的addBatch和add方法中进行转换。
示例如下：
```
@Service
@ExcelExtend(type = "test_excel")//对应excel模板类型templateType
public class TestServiceImpl implements TestService, ConvertExtend {

    @Override
    public void convert(ConvertContext convertContext) {
        convertContext.setDataType(TestDto.class);
        convertContext.addHandler("code",getCodeConvertHandler())//默认在导入数据前进行数据转换
                .addHandler("name",getNameConvertHandler(), ConvertContext.CONVERT_BEFORE_CHECK);//指定在校验前进行数据转换
    }

    /**
     * code列 数据转换
     */
    private ConvertHandler<TestDto> getCodeConvertHandler(){
        return data->{
            //返回转换后的数据
            return data.getCode()+"11";
        };
    }

    /**
     * name列 数据转换
     */
    private ConvertHandler<TestDto> getNameConvertHandler(){
        return data->{
            //返回转换后的数据
            return data.getName()+"22";
        };
    }

}
```

#二、excel导入数据库配置说明
##1、表wd_excel_template
配置excel模板信息，列说明如下：
* type:模板类型(唯一)
* name:模板业务模块名称
* template_path:模板存放的绝对路径，供下载使用
* business_table:简单的单表导入(系统负责导入)时对应的表名，自定义扩展的导入时忽略。
##2、表wd_excel_rule
配置excel数据的通用校验规则及通用数据转换，列说明如下：
* type:模板类型(唯一)，同wd_excel_template的type列
* name:模板业务模块名称
* col_type:excel列数据类型，string(字符串)，date(时间)
* data_range:数据范围，digital(数字),letter(字母),digital_letter(数字字母),text(全字符),aux_data_资料类型,business_data_业务类型。资料类型：对应wd_sys_aux_data中的type值，数据范围为该类型的辅助资料数据。比	如：形态的数据范围写成aux_data_form，form为资料类型；公司类型的数据范围写成aux_data_company_type，company_type为资料类型。业务类型：数据范围为当前用户的业务模块类型的数据。暂时只支持business_data_company，用户的公司范围，business_data_dept，用户的部门范围。
* data_exclude:数据中不能出现的字符
* length_type:数据长度校验类型， 0不限制1小于等于2指定值(一个或多个)
* data_length:当length_type为0时忽略，为1时设置最大长度，为2时指定长度，多个长度使用逗号(,)隔开。
* is_empty:能否不填写数据，0必填，1可不填。
* is_unique:数据是否唯一 0不唯一1唯一。
* business_column:简单的单表导入时，excel的列对应的数据库表列名，自定义扩展导入时忽略。
* redundancy_column:简单的单表导入以及存在值转换时，excel的列对应的数据库表列名，数据对应转换前的原始数据。自定义扩展导入时忽略。
* convert_type:通用的数据转换类型。aux_data_资料类型,business_data_业务类型。资料类型、业务类型的说明同data_range。
* seri:排序对应excel列的顺序。

#三、excel导出说明
excel导出接口类似分页接口。controller层方法多个形参HttpServletResponse,且无需返回值，如下：
```
//分页接口
@RequestMapping("findCompanyPage")
public String findCompanyPage(@RequestBody CompanyDto companyDto){}
//导出接口
@RequestMapping("exportExcel")
public void exportExcel(@RequestBody CompanyDto companyDto, HttpServletResponse response){}
```
Service层使用fileService.resetPage(Page page)方法重置分页对象，最后使用fileService.exportExcel(HttpServletResponse response,List<T> datas,ExcelColumn... excelColumns)方法导出数据，如下：
```
//分页接口
public Page<CompanyVo> findPage(CompanyDto companyDto) {
    companyDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
    Page<CompanyVo> page = companyDto.getPage();
    List<CompanyVo> companyVoList = companyMapper.findPage(companyDto, page);
    return page.setRecords(companyVoList);
}
//导出接口
public void exportExcel(CompanyDto companyDto, HttpServletResponse response) {
    companyDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
    Page<CompanyVo> page = companyDto.getPage();
    fileService.resetPage(page);
    List<CompanyVo> companyVoList = companyMapper.findPage(companyDto, page);
    fileService.exportExcel(response,companyVoList
            ,new ExcelColumn("code","编码")
            ,new ExcelColumn("name","名称")
            ,new ExcelColumn("simpleName","简称")
            ,new ExcelColumn("companyFormName","形态")
            ,new ExcelColumn("companyTypeName","公司类型")
            ,new ExcelColumn("isAccountingName","建账状态"));
}
```