package org.hws;

import org.hws.application.ConfigurationApplicationContext;
import org.hws.config.AppConfig;
import org.hws.service.ITeacherService;
import org.hws.service.TeacherService;

/**
 * @Description TODO
 * @Author Gengby
 * @Date 2022/9/15
 */
public class ApplicationTest {
    public static void main(String[] args) {
        ConfigurationApplicationContext applicationContext = new ConfigurationApplicationContext(AppConfig.class);
        //testScope(applicationContext);
        //testAutowired(applicationContext);
        //testAware(applicationContext);
        //testProxy(applicationContext);
    }


    public static void testProxy(ConfigurationApplicationContext applicationContext) {
        ITeacherService teacherService = (ITeacherService) applicationContext.getBean("teacherService");
        teacherService.test();
    }


    /**
     * 测试aware回调接口
     *
     * @param applicationContext
     */
    public static void testAware(ConfigurationApplicationContext applicationContext) {
        TeacherService teacherService = (TeacherService) applicationContext.getBean("teacherService");
        teacherService.testBeanName();
    }

    /**
     * 测试bean作用域
     *
     * @param applicationContext
     */
    public static void testScope(ConfigurationApplicationContext applicationContext) {
        System.out.println(applicationContext.getBean("studentService"));
        System.out.println(applicationContext.getBean("studentService"));
        System.out.println(applicationContext.getBean("teacherService"));
        System.out.println(applicationContext.getBean("teacherService"));
    }

    /**
     * 测试bean依赖注入
     *
     * @param applicationContext
     */
    public static void testAutowired(ConfigurationApplicationContext applicationContext) {
        TeacherService teacherService = (TeacherService) applicationContext.getBean("teacherService");
        teacherService.test();
        teacherService = (TeacherService) applicationContext.getBean("teacherService");
        teacherService.test();
    }
}