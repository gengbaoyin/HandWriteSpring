package org.hws.initializing;

/**
 * @Description Bean初始化是
 * @Author Gengby
 * @Date 2022/9/15
 */
public interface InitializingBean {

    /**
     * Bean初始化方法
     */
    void afterPropertiesSet();
}