package com.njwd.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.njwd.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("all")
@Component
public class CheckUrlSignUtil {
	private final static Logger LOGGER = LoggerFactory.getLogger(CheckUrlSignUtil.class);
	/**
	 * 请求头验参传递使用的key
	 */
	public final static String WD_AUTH = "wd_auth";

	/**
	 * 验参所需的key
	 */
	private final static String ROOT_ENTERPRISE_ID = Constant.ColumnName.ROOT_ENTERPRISE_ID;
	private final static String SYSTEM_CODE = "system_code";
	private final static String TIMESTAMP = "timestamp";
	private final static String SIGN = "sign";
	/**
	 * sign时效，单位分钟
	 */
	private final static int TIME = 1;

	/**
	 * 校验sign
	 *
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getSign(Map<String, Object> map, String key) {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != "") {
				list.add(entry.getKey() + "=" + entry.getValue() + "&");
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		// Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		String result = sb.toString();
		if (!StringUtils.isEmpty(key)) {
			result = "key=" + key + "&" + result;
		}
		result = result.substring(0, result.length() - 1);
		result = MD5Util.getMD5Code(result);
		return result;
	}

	/**
	 * 校验URL有效期
	 *
	 * @param timestamp 时间戳
	 * @param time      有效时长：分钟
	 * @return
	 */
	public static boolean checkUrlValidity(String timestamp, Integer time) {
		time = time == null ? 10 : time;
		long mins = (System.currentTimeMillis()-Long.valueOf(timestamp))/1000;
		// 验证时间有效性
		return mins < time * 60;
	}

	/**
	 * 获取放入请求头的验参字符串
	 * @param systemCode 子系统编码
	 * @return str
	 */
	public static String getAuthStr(String systemCode) {
		try {
			Map<String, Object> params = new LinkedHashMap<>();
			//params.put(ROOT_ENTERPRISE_ID, UserUtils.getUserVo().getRootEnterpriseId());  平台和基础资料 用户系统不统一 暂时注释
			params.put(SYSTEM_CODE, systemCode);
			params.put(TIMESTAMP, System.currentTimeMillis());
			params.put(SIGN, CheckUrlSignUtil.getSign(params, systemCode));
			return Base64.getEncoder().encodeToString(JsonUtils.object2Json(params).getBytes());
		} catch (Exception e) {
			LOGGER.error("feign请求添加验参错误", e);
		}
		return null;
	}

	/**
	 * 校验feign请求是否有效
	 * @return boolean
	 */
	public static boolean checkFeignValidity(HttpServletRequest request) {
		String header = request.getHeader(WD_AUTH);
		if (StringUtils.isEmpty(header)) {
			return false;
		}
		Map<String, Object> params = JsonUtils.json2MapPojo(new String(Base64.getDecoder().decode(header)), new TypeReference<LinkedHashMap<String, Object>>(){});
		if (params == null) {
			return false;
		}
		String sign = params.remove(SIGN).toString();
		if (sign != null && sign.equals(getSign(params, params.get(SYSTEM_CODE).toString()))) {
			return checkUrlValidity(params.get(TIMESTAMP).toString(), TIME);
		}
		return false;
	}

	/**
	 * 校验请求
	 */
	public static void checkFeignSign(HttpServletRequest request) {
		if (!checkFeignValidity(request)) {
			throw new HttpMessageConversionException("非法请求！");
		}
	}
}
