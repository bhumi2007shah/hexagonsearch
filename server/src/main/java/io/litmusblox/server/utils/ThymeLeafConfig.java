package io.litmusblox.server.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Component
public class ThymeLeafConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public ThymeLeafConfig(){
        super();
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SpringTemplateEngine templateEngineHTML() {
        SpringTemplateEngine templateEngineHTML = new SpringTemplateEngine();
        templateEngineHTML.addTemplateResolver(templateResolver());
        return templateEngineHTML;
    }

    /**
     * For conversion of email template
     * @return
     */
    private ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        //TODO: Populate path from MasterData
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        return templateResolver;
    }
}
