package mg.itu.prom16.servlet.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME) 
public @interface Post {
    String value() default "";
}
    
