package ru.yandex.qatools.terra.usertest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
public class SpringFacade implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext != null) {
            throw new RuntimeException("applicationContext can be set just once!");
        }
        applicationContext = context;
    }

    public static ApplicationContext getContext() {
        return applicationContext;
    }
}
