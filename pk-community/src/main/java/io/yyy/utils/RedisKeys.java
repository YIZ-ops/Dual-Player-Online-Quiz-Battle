
package io.yyy.utils;

/**
 * Redis所有Keys
 *
 */
public class RedisKeys {

    public static String getUserKey(Integer userId){
        return "userId:" + userId;
    }
}
