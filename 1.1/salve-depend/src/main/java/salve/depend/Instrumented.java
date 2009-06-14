package salve.depend;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * this class is dynamically created by salve so in theory it's never needed. however, when using
 * retrotranslator (and probably other similar tools) it has to be visible for bytecode verification.
 * this annotation has no impact on runtime as it's visible is 'source-only'.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Instrumented
{
}
