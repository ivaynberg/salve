package salve.depend;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation added by salve to the generated dependency key fields. This
 * annotation preserves useful information about the original field and is used
 * to access this information once the original field has been removed by the
 * instrumentor.
 * 
 * @author ivaynberg
 * 
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface DependencyFieldInfo {
	/** field description */
	String desc();

	/** field name */
	String name();
}
