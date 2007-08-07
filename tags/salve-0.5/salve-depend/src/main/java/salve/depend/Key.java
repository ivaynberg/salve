package salve.depend;

import java.io.Serializable;
import java.lang.annotation.Annotation;

public interface Key extends Serializable {
	Annotation[] getAnnotations();

	Class<?> getType();
}
