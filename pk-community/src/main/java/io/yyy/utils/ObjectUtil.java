package io.yyy.utils;

/**
 * 对象工具类
 */
public class ObjectUtil {

    /**
     * @param obj
     * @return
     * @return Integer
     * @Title: isEmpty
     * @Description: 判断对象是否为空
     */
    public static boolean isEmpty(Object obj) {
        return null == obj || "".equals(obj);
    }

}
