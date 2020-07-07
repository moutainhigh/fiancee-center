package com.njwd.fileexcel.data;

import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.support.Result;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.SpringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 辅助资料
 * @author: xdy
 * @create: 2019/6/10 16:46
 */
public class AuxData extends AbstractMappingData{


    private RestTemplate restTemplate;

    AuxData(String dataType) {
        this(dataType,false);
    }

    AuxData(String dataType,boolean reverse) {
        super(reverse);
        this.dataType = dataType;
        restTemplate = SpringUtils.getBean("restTemplate0");
    }




    /**
     * @description: 获取辅助资料数据
     * @param: []
     * @return: java.util.Map<java.lang.Object,java.lang.Object>
     * @author: xdy
     * @create: 2019-06-10 17-22
     */
    @Override
    public Map<Object, Object> findData() {
        SysAuxDataDto sysAuxDataDto = new SysAuxDataDto();
        sysAuxDataDto.setType(dataType);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> request = new HttpEntity(JsonUtils.object2Json(sysAuxDataDto),headers);
        ParameterizedTypeReference<Result<List<SysAuxDataVo>>> responseBodyType = new ParameterizedTypeReference<Result<List<SysAuxDataVo>>>(){};
        ResponseEntity<Result<List<SysAuxDataVo>>> resp = restTemplate.exchange("http://platform/platform/sysAuxData/findSysAuxDataListByType",HttpMethod.POST,request,responseBodyType);
        Result<List<SysAuxDataVo>> result = resp.getBody();
        List<SysAuxDataVo> auxDataList = result.getData();
        Map<Object,Object> map = new HashMap<>();
        if(auxDataList!=null){
            for(SysAuxDataVo auxData:auxDataList){
                map.put(auxData.getName(),auxData.getId());
            }
        }
        return map;
    }
}
