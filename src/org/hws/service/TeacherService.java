package org.hws.service;

import org.hws.annotation.Autowired;
import org.hws.annotation.Component;
import org.hws.annotation.PostConstruct;
import org.hws.aware.BeanNameAware;
import org.hws.initializing.InitializingBean;

/**
 * @Description TODO
 * @Author Gengby
 * @Date 2022/9/15
 */
@Component
public class TeacherService implements BeanNameAware, InitializingBean, ITeacherService {
    @Autowired
    private StudentService studentService;

    private String beanName;

    @Override
    public void test() {
        System.out.println(studentService);
    }

    public void testBeanName() {
        System.out.println(beanName);
    }

    @Override
    public void setName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("bean初始化。。。");
    }

    @PostConstruct
    public void testPostConstruct() {
        System.out.println("执行postConstruct........");
    }
}