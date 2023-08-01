package org.hws.aware;

/**
 * @Description aware回调接口
 * @Author Gengby
 * @Date 2022/9/15
 */
public interface BeanNameAware {

    /**
     * 设置beanName
     *
     * @param beanName
     */
    void setName(String beanName);
}