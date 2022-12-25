package com.itheima.common;

/**
 * 基于Thlocal线程工具类获取用户当前的id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setThreadLocalid(Long id) {
        threadLocal.set(id);
    }

    public static Long getThreadLocal() {
        return threadLocal.get();
    }
}
