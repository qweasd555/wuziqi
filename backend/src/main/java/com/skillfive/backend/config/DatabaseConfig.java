package com.skillfive.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库配置类
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    
    // 数据库配置可以在这里扩展
    // 例如配置数据源、JPA属性等
    
    // 如需自定义EntityManagerFactory或TransactionManager
    // 可以在这里添加对应的Bean定义
}