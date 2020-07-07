package com.njwd.basedata.cloudclient;

import com.njwd.platform.api.SubjectCategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 科目类别
 *
 * @author 周鹏
 * @since 2019/8/22
 */
@FeignClient(name = "platform", contextId = "SubjectCategoryFeignClient")
public interface SubjectCategoryFeignClient extends SubjectCategoryApi {


}
