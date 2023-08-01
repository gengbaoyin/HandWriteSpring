package org.hws.processor;

/**
 * @Description 自定义BeanPostProcessor接口
 * @Author Gengby
 * @Date 2022/9/16
 */
public interface BeanPostProcessor {

    /**
     * bean后置处理器前置方法
     *
     * @param beanName
     * @param bean
     */
    Object postProcessBeforeInitialization(String beanName, Object bean);


    /**
     * bean后置处理器后置方法
     *
     * @param beanName
     * @param bean
     */
    Object postProcessAfterInitialization(String beanName, Object bean);
}