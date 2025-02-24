package mg.itu.prom16;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import mg.itu.prom16.validation.annotation.Max;
import mg.itu.prom16.validation.annotation.Min;
import mg.itu.prom16.validation.annotation.NotEmpty;
import mg.itu.prom16.validation.annotation.Validate;
import mg.itu.prom16.validation.BindingResult;
import mg.itu.prom16.validation.FieldError;
import mg.itu.prom16.validation.annotation.Email;
import mg.itu.prom16.validation.annotation.ErrorUrl;
import mg.itu.prom16.validation.exception.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    public static Object[] getMethodArguments( HttpServletRequest request, Method method, Map<String, String> params) throws IllegalArgumentException ,Exception {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];
        List<FieldError> errors = new ArrayList<>();
        boolean isValidAnnotationPresent = false;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ReqParam reqParam = parameter.getAnnotation(ReqParam.class);
            ReqBody reqBody = parameter.getAnnotation(ReqBody.class);
            ReqFile reqFile = parameter.getAnnotation(ReqFile.class);

            if(reqBody != null){
                try {                
                    Constructor<?> constructor = parameter.getType().getDeclaredConstructor();
                    Object obj = constructor.newInstance();
                    
                    for (Field field : obj.getClass().getDeclaredFields()) {
                        String fieldName = field.getName();
                        String paramValue = params.get(fieldName);
                        if (paramValue != null) {
                            field.setAccessible(true);
                            Object val = TypeConverter.convert(paramValue, field.getType());
                            field.set(obj, val);
                            if (parameter.isAnnotationPresent(Validate.class)) {
                                isValidAnnotationPresent = true;
                                checkValidation(val, field , errors);
                            }
                        }
                    }
                    arguments[i] = obj;
                } catch (Exception e) {
                    throw e;
                }
            } else if(reqFile != null){
                setMultipartFile(parameter, request, arguments , i);
            } else {
                if(parameter.getType().equals(BindingResult.class))
                {
                    BindingResult bindingResult = new BindingResult();
                    bindingResult.setFieldErrors(errors);
                    arguments[i] = bindingResult;
                }
                else
                {
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
        }
        return arguments;
    }

    public static void checkValidation(Object value , Field field , List<FieldError> errors) throws Exception
    {
        if (field.isAnnotationPresent(NotEmpty.class)) {
            if (value instanceof String) {
                String stringValue = (String) value;
                NotEmpty notEmpty = field.getAnnotation(NotEmpty.class);
                if (stringValue.trim().isEmpty()) {
                    String message = notEmpty.message();
                    String mess = "Le champ " + field.getName() + " ne doit pas etre vide";
                    FieldError error = new FieldError(field.getName(), message, value);
                    errors.add(error);
                    if (message.trim().isEmpty()) {
                        error.setMessage(mess);
                    }
                }
            } else {
                String mess = "Le champ " + field.getName() + " doit etre une chaine de caractere" ;
                FieldError error = new FieldError(field.getName(), mess, value);
                errors.add(error);
            }
        }
        if (field.isAnnotationPresent(Min.class)) {
            Min min = field.getAnnotation(Min.class);
            String message = min.message();
            if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                if (d < min.value()) {
                    String mess = "Le champ " + field.getName() + " doit etre superieur a "+min.value();
                    FieldError error = new FieldError(field.getName(), message, value);
                    errors.add(error);
                    if (message.trim().isEmpty()) {
                        error.setMessage(mess);
                    }
                }
            } else {
                String mess = "Le champ " + field.getName() + " doit etre un nombre ";
                FieldError error = new FieldError(field.getName(), mess, value);
                errors.add(error);
            }
        }
        if (field.isAnnotationPresent(Max.class)) {
            Max max = field.getAnnotation(Max.class);
            String message = max.message();
            if (value instanceof Number) {
                double d = ((Number) value).doubleValue();
                if (d > max.value()) {
                    String mess = "Le champ " + field.getName() + " doit etre inferieur a "+max.value();
                    FieldError error = new FieldError(field.getName(), message, value);
                    errors.add(error);
                    if (message.trim().isEmpty()) {
                        error.setMessage(mess);
                    }
                }
            } else {
                String mess ="Le champ " + field.getName() + " doit etre un nombre ";
                FieldError error = new FieldError(field.getName(), mess, value);
                errors.add(error);
            }
        } 
        if (field.isAnnotationPresent(Email.class)) {
            Email email = field.getAnnotation(Email.class);
            String message = email.message();
            if (value instanceof String) {
                boolean emailValid = isValidEmail(value.toString());
                if (!emailValid) {
                    String mess = "Le champ " + field.getName() + " doit etre un email valide";
                    FieldError error = new FieldError(field.getName(), message, value);
                    errors.add(error);
                    if (message.trim().isEmpty()) {
                        error.setMessage(mess);
                    }
                }
            } else {
                String mess = "Le champ " + field.getName() + " doit etre une chaine de caractere";
                FieldError error = new FieldError(field.getName(), mess, value);
                errors.add(error);
            }
        }
    }

    public static boolean isValidEmail(String email) {
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email);
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

    private static void setMultipartFile(Parameter argParameter, HttpServletRequest request, Object[] values , int index) throws Exception {
        ReqFile reqFile = (ReqFile)argParameter.getAnnotation(ReqFile.class);
        String nameFile = "";
        if (reqFile != null && !reqFile.value().isEmpty()) {
            nameFile = reqFile.value();
        } else {
            nameFile = argParameter.getName();
        }
        int i = 0;
        Part part = request.getPart(nameFile);
        if (part == null) {
            values[i] = null;
        } else if (argParameter.getType().isAssignableFrom(MultiPartFile.class)) {
            Class class1 = argParameter.getType();
            Constructor constructor = class1.getDeclaredConstructor();
            Object o = constructor.newInstance();
            MultiPartFile mlprt = (MultiPartFile)o;
            mlprt.buildInstance(part, "1859");
            values[i] = mlprt;
        } else {
            throw new Exception("Parameter not valid Exception for File!");
        }
    }

    public  static void validationErrorRedirect(HttpServletRequest request , Method method , BindingResult br , Map<String,Mapping> controllerList) throws Exception
    {
        if (method.isAnnotationPresent(ErrorUrl.class)) {
            ErrorUrl errorHandlerUrl = method.getAnnotation(ErrorUrl.class);
            String handlerUrlPage = errorHandlerUrl.url();

            request = new HttpServletRequestWrapper(request) {
                @Override
                public String getMethod() {
                    return "GET";
                }
            };

            Mapping mapping =  controllerList.get(handlerUrlPage);
            Object val = mapping.invoke(request , controllerList);

            if (val instanceof ModelView) {
                br.setPreviousPage((ModelView) val);  
            } else {
                throw new Exception("La page a retourner doit retourne une valeur de type ModelView");
            }
        } else {
            throw new Exception("Annotation ErrorUrl not found on the method :" + method.getName());
        }
    }
}
