package salve.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import salve.aop.MethodAdvice;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@MethodAdvice(instrumentorClass = TracedAdvice.class, instrumentorMethod = "advise")
public @interface Traced {

}
