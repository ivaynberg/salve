package salve.idea.config;

import salve.idea.SalveComponentImpl;

import javax.swing.*;

/**
 * configuration panel for plugin configuration
 *
 * @author Peter Ertl
 */
public class SalveConfigurationForm
{
  private JPanel rootComponent;
  private JTextArea salveIDEAIntegrationPluginTextArea;

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
