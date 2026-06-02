package com.ruoyi.common.utils;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

import java.util.regex.Pattern;

/**
 * 安全校验工具类
 * 用于检测SQL注入、XSS攻击、控制字符等安全威胁
 *
 * @author Davis
 */
public class SecurityValidationUtil {

    /** SQL注入关键字正则 */
    private static final Pattern SQL_PATTERN = Pattern.compile(
        ".*(;|--|/\\*|\\*/|xp_|sp_|exec|execute|drop|create|alter|insert|update|delete|truncate|declare|cast|convert).*",
        Pattern.CASE_INSENSITIVE
    );

    /** XSS脚本标签正则 */
    private static final Pattern XSS_PATTERN = Pattern.compile(
        ".*(<script|</script|<iframe|</iframe|javascript:|onerror=|onload=|onclick=|eval\\(|expression\\().*",
        Pattern.CASE_INSENSITIVE
    );

    /** 控制字符正则（排除常规换行符） */
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    /**
     * 检测字符串是否包含SQL注入字符
     *
     * @param input 待检测字符串
     * @return true-包含SQL注入字符, false-不包含
     */
    public static boolean containsSqlInjection(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return SQL_PATTERN.matcher(input).matches();
    }

    /**
     * 检测字符串是否包含XSS脚本
     *
     * @param input 待检测字符串
     * @return true-包含XSS脚本, false-不包含
     */
    public static boolean containsXss(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return XSS_PATTERN.matcher(input).matches();
    }

    /**
     * 检测字符串是否包含控制字符
     *
     * @param input 待检测字符串
     * @return true-包含控制字符, false-不包含
     */
    public static boolean containsControlCharacters(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }
        return CONTROL_CHAR_PATTERN.matcher(input).find();
    }

    /**
     * 校验单个字段的安全性
     *
     * @param fieldName 字段名称
     * @param fieldValue 字段值
     * @throws ServiceException 如果字段包含非法字符
     */
    public static void validateField(String fieldName, String fieldValue) {
        if (StringUtils.isEmpty(fieldValue)) {
            return;
        }

        if (containsSqlInjection(fieldValue)) {
            throw new ServiceException(fieldName + "包含非法SQL字符");
        }

        if (containsXss(fieldValue)) {
            throw new ServiceException(fieldName + "包含XSS脚本");
        }

        if (containsControlCharacters(fieldValue)) {
            throw new ServiceException(fieldName + "包含非法控制字符");
        }
    }

    /**
     * 校验字符串的安全性（通用方法）
     *
     * @param input 待校验字符串
     * @param fieldName 字段名称（用于错误提示）
     * @throws ServiceException 如果字符串包含非法字符
     */
    public static void validate(String input, String fieldName) {
        validateField(fieldName, input);
    }
}
