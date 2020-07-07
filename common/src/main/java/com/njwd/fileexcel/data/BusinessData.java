package com.njwd.fileexcel.data;

import com.njwd.common.Constant;
import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.support.Result;
import com.njwd.utils.SpringUtils;
import com.njwd.utils.UserUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.njwd.common.ExcelDataConstant.BUSINESS_DATA_COMPANY;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/11 11:22
 */
public class BusinessData extends AbstractMappingData {

    private RestTemplate restTemplate;

    BusinessData(String dataType) {
        this(dataType,false);
    }

    public BusinessData(String dataType, boolean reverse) {
        super(reverse);
        this.dataType = dataType;
        restTemplate = SpringUtils.getBean("restTemplate0");
    }

    @Override
    public Map<Object, Object> findData() {
        Map<Object, Object> map = new HashMap<>();
        switch (dataType) {
            case BUSINESS_DATA_COMPANY:
                map = findCompanyData();
                break;
            default:
        }
        return map;
    }

    /**
     * @description: 获取公司数据
     * @param: []
     * @return: java.util.Map<java.lang.Object, java.lang.Object>
     * @author: xdy
     * @create: 2019-06-11 11-33
     */
    private Map<Object, Object> findCompanyData() {
        SysUserVo userVo = UserUtils.getUserVo();

        List<Company> companyList = findCompany(userVo.getRootEnterpriseId());
        companyList.addAll(findCompany(0L));
        Map<Object, Object> map = new HashMap<>();
        for (Company company : companyList) {
            map.put(company.getName(), company.getId());
        }
        return map;
    }

    private List<Company> findCompany(Long rootEnterpriseId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> requestMap= new LinkedMultiValueMap();
        requestMap.add("rootEnterpriseId", rootEnterpriseId);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity(requestMap,headers);
        ParameterizedTypeReference<Result<List<Company>>> responseBodyType = new ParameterizedTypeReference<Result<List<Company>>>(){};
        ResponseEntity<Result<List<Company>>> resp = restTemplate.exchange("http://"+ Constant.ServiceName.SERVICE_BASE_DATA +"/financeback/company/findCompanyByEnterprise", HttpMethod.POST,request,responseBodyType);
        Result<List<Company>> result = resp.getBody();
        if(result.getData()==null)
            return new ArrayList<>();
        return result.getData();
    }

}
