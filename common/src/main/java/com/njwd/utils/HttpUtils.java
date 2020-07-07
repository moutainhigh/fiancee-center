package com.njwd.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口调用
 *
 * @author xyyxhcj@qq.com
 * @since 2018-08-17
 */
@SuppressWarnings("all")
@Component
public class HttpUtils {
    private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static RestTemplate CLIENT;
    @Resource
    private RestTemplate restTemplate;
    private static String[] IEBrowserSignals = {"MSIE", "Trident", "Edge"};
    /**
     * RestTemplate 表单提交
     */
    public static <T, T2> T restPost(String url, Map<String, String> headers, Map<String, T2> body, Class<T> cls) {
        HttpHeaders header = new HttpHeaders();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header.add(entry.getKey(), entry.getValue());
            }
        }
        // 提交方式:表单提交
        header.set("Accept", "application/x-www-form-urlencoded;charset=UTF-8");
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (body != null && !body.isEmpty()) {
            for (Map.Entry<String, T2> entry : body.entrySet()) {
                if (entry.getValue() != null) {
                    params.add(entry.getKey(), entry.getValue() instanceof String ? (String) entry.getValue() : String.valueOf(entry.getValue()));
                }
            }
        }
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, header);
        logger.info("发送httpPost请求：{}，参数：{}", url, JsonUtils.object2Str(JsonUtils.NON_NULL_MAPPER, requestEntity.getBody()));
        // 执行HTTP请求
        ResponseEntity<T> response = CLIENT.exchange(url, HttpMethod.POST, requestEntity, cls);
        // 输出结果
        logger.info(JsonUtils.object2Str(JsonUtils.NON_NULL_MAPPER, response.getBody()));
        return response.getBody();
    }

    /**
     * RestTemplate 表单提交 直接传递String请求体
     */
    public static <T, T2> T restPostWithBody(String url, Map<String, String> headers, String body, Class<T> cls) {
        HttpHeaders header = new HttpHeaders();
        header.set("Accept", "application/x-www-form-urlencoded;charset=UTF-8");
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header.add(entry.getKey(), entry.getValue());
            }
        }
        // 提交方式:表单提交
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpEntity<String> requestEntity = new HttpEntity<String>(body, header);
        logger.info("发送httpPost请求：{}，参数：{}", url, JsonUtils.object2Str(JsonUtils.NON_NULL_MAPPER, requestEntity.getBody()));
        // 执行HTTP请求
        ResponseEntity<T> response = CLIENT.exchange(url, HttpMethod.POST, requestEntity, cls);
        // 输出结果
        logger.info(JsonUtils.object2Str(JsonUtils.NON_NULL_MAPPER, response.getBody()));
        return response.getBody();
    }

    public static String restPostJson(String url, String jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/x-www-form-urlencoded;charset=UTF-8");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<String>(jsonObject, headers);
        logger.info("发送httpPost请求：{}，Json参数：{}", url, entity.getBody());
        String jsonResult = CLIENT.postForObject(url, entity, String.class);
        logger.info(jsonResult);
        return jsonResult;
    }

    public static String restPostJsonStr(String url, String jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json;charset=UTF-8");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<String>(jsonObject, headers);
        logger.info("发送httpPost请求：{}，Json参数：{}", url, entity.getBody());
        String jsonResult = CLIENT.postForObject(url, entity, String.class);
        logger.info(jsonResult);
        return jsonResult;
    }

    public static <T> T restGet(String url, Class<T> cls, Map<String, String> params) {
        StringBuilder concatUrl = new StringBuilder(url);
        if (params != null && params.size() != 0) {
            if (!url.contains("?")) {
                concatUrl.append("?");
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                concatUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/x-www-form-urlencoded;charset=UTF-8");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<T> entity = new HttpEntity<>(null, headers);
        url = concatUrl.toString();
        logger.info("发送httpGet请求：{}", url);
        // 执行HTTP请求
        ResponseEntity<T> response = CLIENT.exchange(url, HttpMethod.GET, entity, cls);
        // 输出结果
        logger.info(JsonUtils.object2Str(JsonUtils.NON_NULL_MAPPER, response.getBody()));
        return response.getBody();
    }

    public static <T> T restGetWithJson(String url, Class<T> cls, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/x-www-form-urlencoded;charset=UTF-8");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<T> entity = new HttpEntity<>(null, headers);
        logger.info("发送httpGet请求：{}：json：{}", url, json);
        // 执行HTTP请求
        ResponseEntity<T> response = CLIENT.exchange(url, HttpMethod.GET, entity, cls, json);
        // 输出结果
        logger.info(JsonUtils.object2Str(JsonUtils.NON_NULL_MAPPER, response.getBody()));
        return response.getBody();
    }

    @PostConstruct
    private void init() {
        CLIENT = restTemplate;
    }

    /**
     * 获取实际ip
     *
     * @param request request
     * @return ip
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet != null ? inet.getHostAddress() : null;
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            // 一个ip长度:"***.***.***.***".length()==15
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 获取原header
     *
     * @param request request
     * @return headers
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headers.put(key, value);
        }
        return headers;
    }

    public static boolean isMSBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        for (String signal : IEBrowserSignals) {
            if (userAgent.contains(signal)) {
                return true;
            }
        }
        return false;
    }


}
