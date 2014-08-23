package me.lightspeed7.mongofs.spring;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public final class SpringContext {

    private static ApplicationContext ctx;

    private SpringContext() {

        // cannot construct
    }

    // factory method
    public static <T> T getBean(final Class<T> beanClass) {

        if (getCtx() == null) {
            throw new IllegalStateException("Spring application context is not initialized");
        }
        return getCtx().getBean(beanClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(final String beanName) {

        if (getCtx() == null) {
            throw new IllegalStateException("Spring application context is not initialized");
        }
        return (T) getCtx().getBean(beanName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBeanOptional(final String beanName) throws Exception {

        if (getCtx() == null) {
            throw new IllegalStateException("Spring application context is not initialized");
        }

        try {
            return (T) getCtx().getBean(beanName);
        } catch (Exception e) {
            if (e instanceof NoSuchBeanDefinitionException) {
                return null;
            }
            throw e;
        }
    }

    public static boolean isInitialized() {

        return getCtx() != null;
    }

    public static void dumpDefinedBeanNames() {

        Logger log = LoggerFactory.getLogger(SpringContext.class.getName());

        log.info("=====================================================");
        String[] beanDefinitionNames = getCtx().getBeanDefinitionNames();
        Arrays.sort(beanDefinitionNames, String.CASE_INSENSITIVE_ORDER);

        for (String name : beanDefinitionNames) {
            log.info(name);
        }
        log.info("=====================================================");
        log.info("Spring Beans, count = " + getCtx().getBeanDefinitionCount());
    }

    public static ApplicationContext getCtx() {
        return ctx;
    }

    public static void setCtx(final ApplicationContext ctx) {
        SpringContext.ctx = ctx;
    }
}
