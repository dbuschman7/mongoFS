package me.lightspeed7.mongofs.spring;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class SpringContext {

    public static ApplicationContext ctx;

    private SpringContext() {

        // cannot construct
    }

    // factory method
    public static <T> T getBean(Class<T> beanClass) {

        if (ctx == null) {
            throw new IllegalStateException("Spring application context is not initialized");
        }
        return ctx.getBean(beanClass);
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T getBean(String beanName) {

        if (ctx == null) {
            throw new IllegalStateException("Spring application context is not initialized");
        }
        return (T) ctx.getBean(beanName);
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T getBeanOptional(String beanName)
            throws Exception {

        if (ctx == null) {
            throw new IllegalStateException("Spring application context is not initialized");
        }

        try {
            return (T) ctx.getBean(beanName);
        } catch (Exception e) {
            if (e instanceof NoSuchBeanDefinitionException) {
                return null;
            }
            throw e;
        }
    }

    public static boolean isInitialized() {

        return ctx != null;
    }

    public static void dumpDefinedBeanNames() {

        Logger log = LoggerFactory.getLogger(SpringContext.class.getName());

        log.info("=====================================================");
        String[] beanDefinitionNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanDefinitionNames, String.CASE_INSENSITIVE_ORDER);

        for (String name : beanDefinitionNames) {
            log.info(name);
        }
        log.info("=====================================================");
        log.info("Spring Beans, count = " + ctx.getBeanDefinitionCount());
    }
}
