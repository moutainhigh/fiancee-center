package com.njwd.basedata.cloudclient;


import com.njwd.platform.api.SubjectApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 科目表
 *
 * @author 周鹏
 * @since 2019/6/19
 */
@FeignClient(name = "platform", contextId = "SubjectFeignClient")
public interface SubjectFeignClient extends SubjectApi {


}
