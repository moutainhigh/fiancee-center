package com.njwd.platform.service.impl;

import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.dto.MessageDto;
import com.njwd.platform.service.MessageService;
import com.njwd.support.Result;
import com.njwd.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 10:19
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${constant.message.baseDataIps}")
    private String baseDataIps;
    @Value("${constant.message.baseDataPort}")
    private Integer baseDataPort;

    private Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Override
    public void sendMessage(Byte messageType,MessageDto messageDto) {
        try {
            if(messageDto==null){
                logger.debug("Message-messageDto为空");
                return;
            }
            if(baseDataIps==null){
                logger.debug("Message-baseDataIp未配置");
                return;
            }
            String[] baseDataIpArr = baseDataIps.split(",");
            if(baseDataIpArr.length==0){
                logger.debug("Message-baseDataIp未配置");
                return;
            }
            if(baseDataPort==null)
                baseDataPort = 80;
            if(messageType==null)
                messageType = PlatformConstant.MessageType.SYSTEM_NOTICE;
            messageDto.setType(messageType);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String param = JsonUtils.object2Json(messageDto);
            logger.debug("Message-param:{}",param);
            HttpEntity<String> request = new HttpEntity(param,headers);
            ParameterizedTypeReference<Result> responseBodyType = new ParameterizedTypeReference<Result>(){};
            for(String baseDataIp:baseDataIpArr){
                String serviceUrl = String.format("http://%s:%d/financeback/message/addMessage",baseDataIp,baseDataPort);
                ResponseEntity<Result> resp = restTemplate.exchange(serviceUrl, HttpMethod.POST,request,responseBodyType);
                Result result = resp.getBody();
                if(result.getCode()==Result.SUCCESS){
                    logger.debug("Message-baseDataIp:{}发送成功",baseDataIp);
                }else {
                    logger.debug("Message-baseDataIp:{}发送失败",baseDataIp);
                }
            }
        }catch (Exception e){
            logger.debug("Message发送异常");
            e.printStackTrace();
        }

    }
}
