package salve.contract;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Once a field marked with this annotation has its value set to a non-null
 * value all further attempts to change its value will be met with an
 * IllegalStateException.
 * 
 * @author igor.vaynberg
 * 
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
@Documented
public @interface InitOnce {

}
