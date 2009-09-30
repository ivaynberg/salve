package salve.ideaV2;

import com.intellij.openapi.options.Configurable;

/**
 * public interface to Salve Plugin component
 *
 * @author Peter Ertl
 */
public interface SalveComponent extends Configurable
{
  boolean isEnabled();

  void setEnabled(boolean enabled);
}
