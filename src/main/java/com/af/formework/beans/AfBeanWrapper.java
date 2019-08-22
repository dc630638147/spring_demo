package com.af.formework.beans;

import lombok.Data;

/**
 * AF
 */
@Data
public class AfBeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrappedClass;

    public AfBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }
}
