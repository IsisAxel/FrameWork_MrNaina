package mg.itu.prom16;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class Mapping {
    Class<?> ControlleClass;    
    Method method;
    public Mapping(Class<?> controlleClass, Method method) {
        ControlleClass = controlleClass;
        this.method = method;
    }
    public Class<?> getControlleClass() {
        return ControlleClass;
    }
    public void setControlleClass(Class<?> controlleClass) {
        ControlleClass = controlleClass;
    }
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }
    
    public String invokeStringMethod() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException
    {
        return (String)this.getMethod().invoke(this.getControlleClass().getDeclaredConstructor().newInstance());
    }
}
