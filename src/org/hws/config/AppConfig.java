package org.hws.config;

import org.hws.annotation.ComponentScan;

/**
 * @Description 配置类定义扫描路径
 * @Author Gengby
 * @Date 2022/9/15
 */
@ComponentScan({"org.hws.service","org.hws.processor"})
public class AppConfig {
}