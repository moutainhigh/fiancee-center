package com.njwd.ledger.cloudclient;

import com.njwd.basedata.api.SubjectApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 科目表
 *
 * @author 郑勇浩
 * @since 2019/6/19
 */
@FeignClient(name = "base-data", contextId = "SubjectFeignClient")
public interface SubjectFeignClient extends SubjectApi {
}
