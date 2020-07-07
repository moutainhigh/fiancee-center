package com.njwd.basedata.cloudclient;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "platform")
public interface TestFeignClient /*extends TestApi*/ {



}
