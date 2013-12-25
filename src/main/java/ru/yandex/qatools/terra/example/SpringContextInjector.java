package ru.yandex.qatools.terra.example;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.yandex.qatools.terra.config.PluginContext;
import ru.yandex.qatools.terra.core.PluginContextInjectorImpl;

import static ru.yandex.qatools.clay.utils.ContextUtils.autowireFields;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SpringContextInjector extends PluginContextInjectorImpl
        implements CamelContextAware, ApplicationContextAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;
    private CamelContext camelContext;

    @Override
    public void inject(Object pluginObj, PluginContext pluginConfig, Exchange exchange) {
        super.inject(pluginObj, pluginConfig, exchange);
        try {
            autowireFields(pluginObj, applicationContext, camelContext);
        } catch (Exception e) {
            logger.error("Could not autowire the fields for the object " + pluginObj, e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }
}
