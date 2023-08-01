package org.hws.application;

import org.hws.annotation.*;
import org.hws.aware.BeanNameAware;
import org.hws.enums.ScopeEnum;
import org.hws.initializing.InitializingBean;
import org.hws.processor.BeanPostProcessor;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description Spring容器
 * @Author Gengby
 * @Date 2022/9/15
 */
public class ConfigurationApplicationContext {

    /**
     * 配置类信息
     * 根据配置类获取扫描路径
     */
    private Class configClazz;

    /**
     * 存放beanDefinition信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 存放单例bean
     */
    private Map<String, Object> singletonMaps = new ConcurrentHashMap<>();

    /**
     * 存放bean后置处理器
     */
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    private static final String SUFFIX = ".class";


    public ConfigurationApplicationContext(Class configClazz) {
        this.configClazz = configClazz;
        //判断传入等配置类是否带有扫描路径注解
        //扫描->生成beanDefinition->存入beanDefinitionMap
        if (configClazz.isAnnotationPresent(ComponentScan.class)) {
            //获取扫描注解信息路径等
            ComponentScan annotation = (ComponentScan) configClazz.getAnnotation(ComponentScan.class);
            String[] paths = annotation.value();
            for (String path : paths) {
                path = path.replace(".", "/");
                //获取容器的类加载器
                ClassLoader classLoader = ConfigurationApplicationContext.class.getClassLoader();
                //根据扫描路径去加载url资源
                URL resource = classLoader.getResource(path);
                //将url资源封装成文件信息
                File file = new File(resource.getFile());
                //判断是否是文件类型
                if (file.isDirectory()) {
                    //获取所有的文件信息
                    File[] files = file.listFiles();
                    //遍历所有的文件信息
                    Arrays.stream(files).forEach(f -> {
                        //获取每个文件的绝对路径
                        String fileName = f.getAbsolutePath();
                        //判断文件是否以.class结尾
                        if (fileName.endsWith(SUFFIX)) {
                            //如果是class文件就截取对应的相对路径
                            String className = fileName.substring(fileName.indexOf("org"), fileName.indexOf(".class"));
                            className = className.replace("/", ".");
                            try {
                                //通过类加载器去加载相应的类
                                Class<?> clazz = classLoader.loadClass(className);
                                //判断该class文件是否包含声明bean的注解@Compoent
                                if (clazz.isAnnotationPresent(Component.class)) {

                                    //将BeanPostProcessor添加到集合中，创建bean时遍历处理
                                    if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                        BeanPostProcessor instance = (BeanPostProcessor) clazz.newInstance();
                                        beanPostProcessorList.add(instance);
                                    }

                                    Component component = clazz.getAnnotation(Component.class);
                                    String beanName = component.value();
                                    //如果@Component属性值为空则默认首字母小写的类名
                                    if (beanName.equals("")) {
                                        beanName = Introspector.decapitalize(clazz.getSimpleName());
                                    }
                                    //包含就是bean
                                    BeanDefinition beanDefinition = new BeanDefinition();
                                    beanDefinition.setType(clazz);
                                    //不带scope注解 默认单例
                                    if (!clazz.isAnnotationPresent(Scope.class)) {
                                        beanDefinition.setScope(ScopeEnum.SINGLETON.name());
                                    } else {
                                        Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                        //带scope注解 值为空也默认单例
                                        if (scopeAnnotation.value() == "") {
                                            beanDefinition.setScope(ScopeEnum.SINGLETON.name());
                                        }
                                        //否则就按照注解值设置作用域
                                        beanDefinition.setScope(scopeAnnotation.value());
                                    }
                                    beanDefinitionMap.put(beanName, beanDefinition);
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }

            }
        }

        //遍历beanDefinitionMap实例化所有的单例bean->存入singleMaps中
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals(ScopeEnum.SINGLETON.name())) {
                Object bean = createBean(beanName, beanDefinition);
                singletonMaps.put(beanName, bean);
            }
        }
    }


    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        //根据beanDefinition获取类的信息
        Class clazz = beanDefinition.getType();
        try {
            //利用反射创建bean对象
            Object o = clazz.getConstructor().newInstance();
            //遍历bean所有属性 判断是否需要依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    //如果需要依赖注入就给bean赋值
                    field.setAccessible(true);
                    field.set(o, getBean(field.getName()));
                }
            }



            //aware回调 设置beanName 也可以通过实现其它aware接口 设置其它属性
            if (o instanceof BeanNameAware) {
                ((BeanNameAware) o).setName(beanName);
            }


            //bean初始化前执行后置处理器前置方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                o = beanPostProcessor.postProcessBeforeInitialization(beanName, o);
            }

            //bean初始化
            if (o instanceof InitializingBean) {
                ((InitializingBean) o).afterPropertiesSet();
            }

            //bean初始化后执行bean后置处理器后置方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                o = beanPostProcessor.postProcessAfterInitialization(beanName, o);
            }

            return o;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if (ScopeEnum.SINGLETON.name().equals(scope)) {
                Object bean = singletonMaps.get(beanName);
                if (bean == null) {
                    Object o = createBean(beanName, beanDefinition);
                    singletonMaps.put(beanName, o);
                }
                return bean;
            } else {
                return createBean(beanName, beanDefinition);
            }
        }
    }
}