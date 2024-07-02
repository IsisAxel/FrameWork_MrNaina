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

    public Object invoke(HttpServletRequest request) throws ServletException , IllegalArgumentException
    {
        try {
            Object ob = getControlleClass().getDeclaredConstructor().newInstance();
            Method method = getMethod();
            Map<String,String> params = ServletUtil.extractParameters(request);
            Object[] args = ServletUtil.getMethodArguments(method, params);
            ServletUtil.processSession(ob, request);
            return method.invoke(ob, args);
        }catch (Exception e) {
            System.out.println("asndash");
            throw new ServletException(e);
        }
    }
}
