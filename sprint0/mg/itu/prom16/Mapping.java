package mg.itu.prom16;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class Mapping {
    Class<?> ControlleClass;    
    HashMap<String,Method> methods;
    public Mapping(Class<?> controlleClass, HashMap<String,Method> methods) {
        ControlleClass = controlleClass;
        this.methods = methods;
    }
    public Class<?> getControlleClass() {
        return ControlleClass;
    }
    public void setControlleClass(Class<?> controlleClass) {
        ControlleClass = controlleClass;
    }
    public HashMap<String,Method> getMethods() {
        return methods;
    }
    public void setMethods(HashMap<String,Method> methods) {
        this.methods = methods;
    }
    
    public String invokeStringMethod(HttpServletRequest req) throws Exception
    {
        String verb = req.getMethod();
        HashMap<String,Method> map = getMethods();
        if (!verb.equals(ClassScanner.getVerb(map.get(verb)))) {
            throw new Exception("Invalid verb for this link");
        }
        return (String)map.get(verb).invoke(this.getControlleClass().getDeclaredConstructor().newInstance());
    }

    public boolean isRestAPI(HttpServletRequest req) throws Exception
    {
        String verb = req.getMethod();
        HashMap<String,Method> map = getMethods();
        return map.get(verb).isAnnotationPresent(RestAPI.class);
    }

    public Object invoke(HttpServletRequest request) throws Exception
    {
        try {
            String verb = request.getMethod();
            HashMap<String,Method> map = getMethods();
            Method method =  map.get(verb);
            if (!verb.equals(ClassScanner.getVerb(method))) {
                throw new Exception("Invalid verb for this link");
            }
            Object ob = getControlleClass().getDeclaredConstructor().newInstance();
            Map<String,String> params = ServletUtil.extractParameters(request);
            Object[] args = ServletUtil.getMethodArguments(request , method, params);
            ServletUtil.processSession(ob, request);
            return method.invoke(ob, args);
        }catch (Exception e) {
            System.out.println("asndash");
            throw new ServletException(e);
        }
    }
}
