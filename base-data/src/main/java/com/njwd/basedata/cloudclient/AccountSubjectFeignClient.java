package com.njwd.basedata.cloudclient;



import com.njwd.platform.api.MoreSubjectTemplateApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 会计科目
 *
 * @author 周鹏
 * @since 2019/6/19
 */
@FeignClient(name = "platform", contextId = "AccountSubjectFeignClient")
public interface AccountSubjectFeignClient extends MoreSubjectTemplateApi {


}
