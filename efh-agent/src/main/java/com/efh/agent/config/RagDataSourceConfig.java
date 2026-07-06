package com.efh.agent.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class RagDataSourceConfig {

    @Bean(name = "knowledgeDataSource")
    @ConfigurationProperties(prefix = "efh.agent.datasource.knowledge")
    public DataSource knowledgeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "communityDataSource")
    @ConfigurationProperties(prefix = "efh.agent.datasource.community")
    public DataSource communityDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate knowledgeJdbcTemplate(@Qualifier("knowledgeDataSource") DataSource knowledgeDataSource) {
        return new JdbcTemplate(knowledgeDataSource);
    }

    @Bean
    public JdbcTemplate communityJdbcTemplate(@Qualifier("communityDataSource") DataSource communityDataSource) {
        return new JdbcTemplate(communityDataSource);
    }

    @Bean(name = "agentDataSource")
    @ConfigurationProperties(prefix = "efh.agent.datasource.agent")
    public DataSource agentDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate agentJdbcTemplate(@Qualifier("agentDataSource") DataSource agentDataSource) {
        return new JdbcTemplate(agentDataSource);
    }
}
