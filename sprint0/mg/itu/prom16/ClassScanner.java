package mg.itu.prom16;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Method;

import mg.itu.prom16.Mapping;
import mg.itu.prom16.Get;
public class ClassScanner {

    public static Map<String,Mapping> getMapping(String packageName,Class<? extends Annotation> annotationClass) throws Exception{
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> resources = classLoader.getResources(packagePath);
    
        Map<String,Mapping> map=new HashMap<String,Mapping>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(resource.getPath());
                for (File file : directory.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> class1=Class.forName(className);
                        if (class1.isAnnotationPresent(annotationClass)) {
                            map.putAll(getMethods(class1));                            
                        }
                    }
                }
            }
        }
        return map;
    }    
    private static Map<String,Mapping> getMethods(Class<?> clazz) throws Exception{
        
        Map<String,Mapping> methods = new HashMap<String,Mapping>();
        for (Method method : clazz.getDeclaredMethods()) {
            Get an= method.getAnnotation(Get.class);
            if(an!=null&& !an.url().isEmpty()){
                if(methods.containsKey(an.url())){
                    throw new Exception("Duplicate url");
                }
                methods.put(an.url().trim(),new Mapping(clazz,method));
            }
        }
        return methods;
    }
    
}
