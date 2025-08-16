package com.callcat.backend.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.Set;

public class BeanUpdateUtils {

    /**
     * Copies non-null properties from source to target.
     * This eliminates the need for repetitive if-null checks and setters.
     * 
     * @param source The source object to copy from
     * @param target The target object to copy to
     */
    public static void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * Gets the names of properties that are null in the source object.
     * These will be ignored during copying.
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        
        return emptyNames.toArray(new String[0]);
    }
}