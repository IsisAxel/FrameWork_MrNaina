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

    public static Object[] getMethodArguments(Method method, Map<String, String> params) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ReqParam reqParam = parameter.getAnnotation(ReqParam.class);
            String paramName = "";
            if (reqParam == null || reqParam.value().isEmpty()) {
                paramName = parameter.getName();
            } else {
                paramName = reqParam.value();
            }

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
        return arguments;
    }

    private static boolean isBooleanType(Parameter parameter) {
        return parameter.getType().equals(boolean.class) || parameter.getType().equals(Boolean.class);
    }
}
