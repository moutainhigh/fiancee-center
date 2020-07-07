package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.SubjectSynergyApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 科目协同配置
 *
 * @author xyyxhcj@qq.com
 * @date 2019/10/28 17:26
 **/
@FeignClient(name = "platform", contextId = "SubjectSynergyFeignClient")
public interface SubjectSynergyFeignClient extends SubjectSynergyApi {

}
