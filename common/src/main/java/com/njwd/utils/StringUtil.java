package com.njwd.utils;

import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class StringUtil {

    /**
     * 正则表达式转义字符
     */
    private static final Pattern REG_CHAR = Pattern.compile("([\\{\\}\\[\\]\\(\\)\\^\\$\\.\\*\\?\\-\\+\\\\])");

    private StringUtil() {
    }

    /**
     * 判断空
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim()) || "null".equalsIgnoreCase(str)) {
            return true;
        }
        return false;
    }

    /**
     * 判断非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 用参数替换文本内容模板，返回替换后的文本内容。 用途:发送极光模板消息
     *
     * @param string 原字符串
     * @param params 参数键值对
     * @return 参数替换后的字符串
     */
    public static String formatTemple(String string, Map<String, Object> params) {
        if (string == null || params == null) {
            return string;
        }
        for (Map.Entry<String, Object> item : params.entrySet()) {
            String temp = item.getKey().trim();
            String key;
            if (temp.startsWith("{") && temp.endsWith("}")) {
                key = temp;
            } else {
                key = "{" + temp + "}";
            }
            // 替换正则表达式的转义字符, KEY不需要支持正则表达式
            // 如果不替换, user.id这个点就会成为通配符
            key = REG_CHAR.matcher(key).replaceAll("\\\\$1");

            Object object = item.getValue();
            String value = object == null ? "" : object.toString();
            string = string.replaceAll(key, value);
        }
        return string;
    }

    /**
     * 采用StringBuilder将指定的字符串连接起来
     *
     * @param sequences
     * @return
     */
    public static String bufferAppend(CharSequence... sequences) {
        if (null == sequences) {
            return null;
        }
        StringBuilder sb = new StringBuilder(sequences.length * 5);
        for (CharSequence cs : sequences) {
            sb.append(cs);
        }
        return sb.toString();
    }

    /**
     * @param str
     * @return
     */
    public static int utf8Length(String str) {
        try {
            return str.getBytes("utf-8").length;
        } catch (UnsupportedEncodingException e) {
            return str.length();
        }
    }

    /**
     * 方法描述：生成的随机数的长度<br/>
     * #since 1.0.0<br/>
     *
     * @param length
     * @return
     */
    public static String generateRandomPassword(int length) {
        String result = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            // 输出字母还是数字
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            // 字符串
            if ("char".equalsIgnoreCase(charOrNum)) {
                // 取得大写字母还是小写字母,65是 'A',97 是 'a'
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
                result += (char) (choice + random.nextInt(26));
            } else {
                // 数字
                result += String.valueOf(random.nextInt(10));
            }
        }
        return result;
    }

    /**
     * 方法描述：生成数字验证码 <br/>
     * <p>
     * #since 1.0.0<br/>
     *
     * @param length
     * @return
     */
    public static String generateVerifyCode(int length) {
        String result = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result += String.valueOf(random.nextInt(10));
        }
        return result;
    }

    /**
     * 方法描述：非空结果集汇总 <br/>
     * <p>
     * #since 1.0.0<br/>
     *
     * @param params
     * @return
     */
    public static String collectResult(String... params) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (null != params[i]) {
                builder.append(params[i]);
                builder.append(";");
            }
        }

        if (!isEmpty(builder.toString())) {
            String result = builder.toString();
            return result.substring(0, result.length() - 1);
        }
        return null;
    }

    /***
     *
     * 方法描述：Object判断<br/>
     *
     * #since 1.0.0<br/>
     *
     * @param obj
     * @return
     */
    public static final boolean isNull(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return isEmpty((String) obj);
        } else {
            return false;
        }
    }

    public static final boolean gt(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj2 == null) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj1.getClass().equals(obj2.getClass())) {
            try {
                int i = ((Comparable) obj1).compareTo(obj2);
                return i > 0;
            } catch (Exception e) {
                return false;
            }
        }
        try {
            return Double.parseDouble((new StringBuilder()).append("").append(obj1).toString())
                    - Double.parseDouble((new StringBuilder()).append("").append(obj2).toString()) > 0.0D;
        } catch (Exception e) {
            return (new StringBuilder()).append("").append(obj1).toString().compareTo(obj2.toString()) > 0;
        }
    }

    /**
     * 方法描述：替换空值<br/>
     *
     * @param str
     * @return
     */
    public static final String replaceNull(String str) {
        return isEmpty(str) ? "" : str;
    }

    /**
     * 方法描述：拼接字符串<br/>
     *
     * @param s1   拼接字符串
     * @param s2   拼接元素
     * @param mark 分隔符
     * @return
     */
    public static final String mergeStr(String s1, String s2, String mark) {
        if (StringUtil.isEmpty(s2)) {
            return s1;
        }

        if (StringUtil.isEmpty(s1)) {
            s1 = s2;
        } else {
            s1 = s1 + mark + s2;
        }
        return s1;
    }

    /**
     * 方法描述：截取字符串, 并添加后缀<br/>
     *
     * @param str        被截取的字符串
     * @param beginIndex 开始位置
     * @param endIndex   结束位置
     * @param suffixStr  后缀字符串
     * @return
     */
    public static final String cutStr(String str, int beginIndex, int endIndex, String suffixStr) {
        if (StringUtil.isEmpty(str)) {
            return "";
        }
        str = str.substring(beginIndex, endIndex);
        if (StringUtil.isEmpty(suffixStr)) {
            return str;
        }
        return str + suffixStr;
    }

    /**
     * 方法描述：校验字符串是否为数字.<br/>
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (StringUtil.isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[+-]?+[0-9]+(.[0-9]*)?$");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /***
     *
     * 方法描述：校验不同格式參數<br/>
     *
     * @param src
     *            校验内容串
     * @param reg
     *            校验格式
     * @param ignoreCase
     *            是否忽视
     * @return
     */
    public static boolean regMatch(String src, String reg, boolean ignoreCase) {
        if (StringUtils.isBlank(src)) {
            return false;
        }
        Pattern pattern = null;
        if (ignoreCase) {
            pattern = Pattern.compile(reg, 2);
        } else {
            pattern = Pattern.compile(reg);
        }
        Matcher matcher = pattern.matcher(src);
        return matcher.find();
    }

    /**
     * 方法描述：手机号中间4位变星号<br/>
     *
     * @param phoneNumber
     * @return
     */
    public static String phoneMumberHidden(String phoneNumber) {
        if (isEmpty(phoneNumber)) {
            return null;
        } else {
            return phoneNumber.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
    }

    /**
     * 方法描述：姓名姓之后的内容变星号 <br/>
     *
     * @param patientName
     * @return
     */
    public static String nameHidden(String patientName) {
        if (isEmpty(patientName)) {
            return "";
        } else {
            return patientName.replaceAll("(\\S{1})\\S*", "$1**");
        }
    }

    /**
     * 方法描述：将Set集合中的数据转换成SQL中可以In查询的字符串.<br/>
     *
     * @param set
     * @return
     */
    public static String hashSetToSqlInString(Set<String> set) {
        String resultString = new String();

        if (set == null || set.isEmpty()) {
            return resultString;
        }

        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (resultString.length() == 0) {
                resultString = "'" + str + "'";
            } else {
                resultString = resultString + "," + "'" + str + "'";
            }
        }
        return resultString;
    }

    /**
     * 方法描述：将List集合中的数据转换成SQL中可以In查询的字符串.<br/>
     *
     * @param list
     * @return
     */
    public static String listToSqlInString(List<String> list) {
        String resultString = new String();

        if (list == null || list.isEmpty()) {
            return resultString;
        }

        for (String str : list) {
            if (resultString.length() == 0) {
                resultString = "'" + str + "'";
            } else {
                resultString = resultString + "," + "'" + str + "'";
            }
        }

        return resultString;
    }

    /**
     * 方法描述：String转换成float.<br/>
     *
     * @param str
     * @return
     */
    public static float toFloat(String str) {
        if (str != null && str.length() > 0) {
            return Float.parseFloat(str);
        } else {
            return Float.parseFloat("0");
        }
    }

    public static boolean isBlank(Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof CharSequence) {
            CharSequence string = (CharSequence) object;
            int length;
            if ((length = string.length()) == 0) {
                return true;
            }
            for (int i = 0; i < length; i++) {
                if (Character.isWhitespace(string.charAt(i)) == false) {
                    return false;
                }
            }
            return true;
        } else if (object.getClass().isArray()) {
            return Array.getLength(object) == 0;
        } else if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        } else if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            return map.isEmpty();
        } else {
            return false;
        }
    }

    public static boolean isNotBlank(Object object) {
        return !isBlank(object);
    }

    /**
     * 只有一个为空就返回true
     *
     * @param objects
     * @return
     */
    public static boolean isAnyBlank(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }

        for (Object object : objects) {
            if (isBlank(object)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 指定分割符拼接字符串
     *
     * @param strs 字符数组
     * @param mask 分隔符
     * @return
     * @author
     */
    public static String concatStr(String[] strs, String mask) {
        String str = "";
        if (strs.length > 0) {
            str = strs[0];
            for (int i = 1; i < strs.length; i++) {
                str += mask + strs[i].trim();
            }
        }
        return str;
    }

    /**
     * 全都为空就返回true
     *
     * @param objects
     * @return
     */
    public static boolean isAllBlank(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }

        for (Object object : objects) {
            if (isNotBlank(object)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 全都不为空就返回true
     *
     * @param objects
     * @return
     */
    public static boolean isNoneBlank(Object... objects) {
        return !isAnyBlank(objects);
    }

    /**
     * 方法描述：1.第一个字符为字母时直接返回 2.第一个字符为非字母时补足为18位字符串 #author fanchunlei<br/>
     * #date 2017年12月21日 下午3:53:10<br/>
     * #since 1.0.0<br/>
     *
     * @param strInput
     * @return
     */
    public static String complementTo18Bit(String strInput) {
        // 如果字符串为空返回
        if (isEmpty(strInput)) {
            return "";
        }
        // 字符串长度大于18,返回原字符串
        if (strInput.length() >= 18) {
            return strInput;
        }
        // 取首字母
        String firstChar = String.valueOf(strInput.charAt(0));
        // 验证首字母是否为a-zA-Z
        Pattern pStr = Pattern.compile("[a-zA-Z]");
        if (pStr.matcher(firstChar).matches()) {
            return strInput;
        } else {
            // 其他情况补足长度为18的字符串
            StringBuilder sb = new StringBuilder();
            int len = 18 - strInput.length();
            int i = 0;
            while (i < len) {
                sb.append("0");
                i++;
            }
            return sb.toString().concat(strInput);
        }

    }

    /**
     * 方法描述：1.第一个字符为字母时直接返回 2.第一个字符为非字母时补足为指定位字符串
     *
     * @param strInput
     * @param maxLen   最大位数
     * @return
     */
    public static String complementZeroOnLen(String strInput, int maxLen) {
        // 如果字符串为空返回
        if (isEmpty(strInput)) {
            return "";
        }
        // 字符串长度大于maxLen,返回原字符串
        if (strInput.length() >= maxLen) {
            return strInput;
        }
        // 取首字母
        String firstChar = String.valueOf(strInput.charAt(0));
        // 验证首字母是否为a-zA-Z
        Pattern pStr = Pattern.compile("[a-zA-Z]");
        if (pStr.matcher(firstChar).matches()) {
            return strInput;
        } else {
            // 其他情况补足长度为maxLen的字符串
            StringBuilder sb = new StringBuilder();
            int len = maxLen - strInput.length();
            int i = 0;
            while (i < len) {
                sb.append("0");
                i++;
            }
            return sb.toString().concat(strInput);
        }

    }

    /**
     * 方法描述：去除字符串前面的0 <br/>
     * <p>
     * #author fanchunlei<br/>
     * #since 1.0.0<br/>
     *
     * @param strInput
     * @return
     */
    public static String removeHeadZero(String strInput) {
        if (isEmpty(strInput)) {
            return "";
        }
        return strInput.replaceFirst("^0*", "");
    }

    /**
     * 检验是否为空
     */
    public static void checkEmpty(Long id) {
        if (id == null) {
            throw new ServiceException(ResultCode.ID_IS_NULL);
        }
    }

}
