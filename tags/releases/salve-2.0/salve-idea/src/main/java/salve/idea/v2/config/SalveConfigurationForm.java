package salve.idea.v2.config;

import javax.swing.*;

/**
 * configuration panel for plugin configuration
 *
 * @author Peter Ertl
 */
public class SalveConfigurationForm
{
  private JPanel rootComponent;
  private JTextArea salve2IDEAIntegrationTextArea;

  public SalveConfigurationForm(final SalveConfiguration configuration)
  {
  }

  public boolean isModified()
  {
    return false;
  }

  public JPanel getRootComponent()
  {
    return rootComponent;
  }
}
