package mg.itu.prom16;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public abstract class ServletUtil {
    public static Map<String, String> extractParameters(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                parameters.put(key, values[0]);
            }
        });
        return parameters;
    }

    public static Object[] getMethodArguments(Method method, Map<String, String> params) throws IllegalArgumentException ,Exception {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ReqParam reqParam = parameter.getAnnotation(ReqParam.class);
            ReqBody reqBody = parameter.getAnnotation(ReqBody.class);

            if(reqBody == null && reqParam == null){
                System.out.println(reqParam);
                System.out.println(reqBody);
                throw new IllegalArgumentException("ETU002442 : Veuillez annoter touts les arguments de votre fonction");
            }
            else if(reqBody != null){
                try {                
                    Constructor<?> constructor = parameter.getType().getDeclaredConstructor();
                    Object obj = constructor.newInstance();
                    
                    for (Field field : obj.getClass().getDeclaredFields()) {
                        String fieldName = field.getName();
                        String paramValue = params.get(fieldName);
                        if (paramValue != null) {
                            field.setAccessible(true);
                            field.set(obj, TypeConverter.convert(paramValue, field.getType()));
                        }
                    }
                    arguments[i] = obj;
                } catch (Exception e) {
                    throw e;
                }
            } else {
                String paramName = "";
                if (reqParam.value().isEmpty()) {
                    paramName = parameter.getName();
                } else {
                    paramName = reqParam.value();
                }

                System.out.println("Name = " + paramName);
                String paramValue = params.get(paramName);
              
                if (paramValue != null) {
                    arguments[i] = TypeConverter.convert(paramValue, parameter.getType());
                } else {
                    arguments[i] = null;
                    if (isBooleanType(parameter)) {
                        arguments[i] = false;
                    }
                }
            }
        }
        return arguments;
    }

    public static void processSession(Object obj, HttpServletRequest request) throws Exception {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(MySession.class)) {
                field.setAccessible(true);
                Object sessionInstance = field.get(obj);
                if (sessionInstance == null) {
                    sessionInstance = MySession.class.getDeclaredConstructor().newInstance();
                    field.set(obj, sessionInstance);
                }

                MySession session = (MySession) sessionInstance;
                session.setSession(request.getSession());
                break;
            }
        }
    }

    private static boolean isBooleanType(Parameter parameter) {
        return parameter.getType().equals(boolean.class) || parameter.getType().equals(Boolean.class);
    }
}
