package ru.yandex.qatools.terra.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.terra.api.PluginInterop;
import ru.yandex.qatools.terra.api.PluginsInterop;
import ru.yandex.qatools.terra.api.annotations.Plugin;
import ru.yandex.qatools.terra.api.annotations.Plugins;
import ru.yandex.qatools.terra.core.ProcessingEngine;
import ru.yandex.qatools.terra.util.AnnotatedFieldListener;

import java.lang.reflect.Field;

import static ru.yandex.qatools.clay.utils.ReflectUtil.getAnnotationValue;
import static ru.yandex.qatools.terra.util.ServiceUtil.injectAnnotatedField;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
public class PluginsContextIntoBeansInjector implements BeanPostProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ProcessingEngine processingEngine;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        final PluginsInterop interop = processingEngine.getInterop();
        try {
            injectAnnotatedField(bean.getClass(), bean, Plugins.class, new AnnotatedFieldListener<PluginsInterop>() {
                @Override
                public PluginsInterop found(Field field, Object annotation) throws Exception {
                    return interop;
                }
            });
        } catch (Exception e) {
            logger.error("Could not inject field annotated with @Plugins into bean " + bean, e);
        }
        try {
            injectAnnotatedField(bean.getClass(), bean, Plugin.class, new AnnotatedFieldListener<PluginInterop>() {
                @Override
                public PluginInterop found(Field field, Object annotation) throws Exception {
                    Class pluginClass = (Class) getAnnotationValue(annotation, "value");
                    if (!pluginClass.getName().equals(Plugin.class.getName())) {
                        return interop.forPlugin(pluginClass);
                    }
                    return interop.forPlugin((String) getAnnotationValue(annotation, "id"));
                }
            });
        } catch (Exception e) {
            logger.error("Could not inject fields annotated with @Plugin into bean " + bean, e);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
