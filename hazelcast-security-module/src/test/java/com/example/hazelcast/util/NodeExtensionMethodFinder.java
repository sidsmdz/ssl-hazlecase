package com.example.hazelcast.util;

import com.hazelcast.instance.impl.DefaultNodeExtension;
import java.lang.reflect.Method;
import java.util.Arrays;

public class NodeExtensionMethodFinder {
    public static void main(String[] args) {
        // Get all methods from DefaultNodeExtension
        Method[] methods = DefaultNodeExtension.class.getDeclaredMethods();
        
        // Print methods related to SSL or context
        Arrays.stream(methods)
              .filter(method -> method.getName().toLowerCase().contains("ssl") || 
                               method.getName().toLowerCase().contains("context") ||
                               method.getName().toLowerCase().contains("socket"))
              .forEach(method -> {
                  System.out.println("Method: " + method.getName());
                  System.out.println("Return Type: " + method.getReturnType().getName());
                  System.out.println("Parameter Types: " + Arrays.toString(method.getParameterTypes()));
                  System.out.println();
              });
    }
}