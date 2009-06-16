package salve.aop;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marker used to mark methods as instrumented by the aop instrumentor. Primarily there to prevent
 * double instrumentation.
 * 
 * @author igor.vaynberg
 * 
 */
@Retention(RetentionPolicy.CLASS)
public @interface Instrumented {

}
