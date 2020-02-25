/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.config;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

import javax.jms.Queue;


/**
 * @author : Shital Raval
 * Date : 19/2/20
 * Time : 11:02 AM
 * Class Name : JMSConfig
 * Project Name : server
 */
@Configuration
@EnableJms
public class JMSConfig {

    @Value("${activemq.queuename}")
    String queueName;

    @Bean
    public Queue queue(){
        return new ActiveMQQueue(queueName);
    }
}
