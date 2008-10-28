package salve.idea;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import salve.idea.config.SalveConfigurationForm;
import salve.idea.config.SalveMutableConfiguration;

import javax.swing.*;

/**
 * Salve IDEA plugin component
 *
 * @author Peter Ertl
 */

// find the documentation for this stuff
@State(name = "SalveComponent", storages = {@Storage(id = "other", file = "$WORKSPACE_FILE$")})
public final class SalveComponentImpl implements SalveComponent,
                                                 ProjectComponent,
                                                 PersistentStateComponent<SalveMutableConfiguration>
{
// ------------------------------ FIELDS ------------------------------

  private static final Logger log = Logger.getInstance(SalveComponentImpl.class.getName());

  @NonNls
  private static final String COMPONENT_NAME = "salve.integration.idea";

  @NonNls
  private static final String DISPLAY_NAME = "Salve Integration";

  // configuration form
  private SalveConfigurationForm configurationForm;

  // related project
  private final Project project;

  // configuration state
  private SalveMutableConfiguration configuration;

  // salve bytecode transformer
  private final SalveInstrumentingCompiler instrumentor;

// --------------------------- CONSTRUCTORS ---------------------------

  public SalveComponentImpl(Project project)
  {
    this.project = project;
    configuration = new SalveMutableConfiguration();
    instrumentor = new SalveInstrumentingCompiler(configuration);
  }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface BaseComponent ---------------------

  @NotNull
  public String getComponentName()
  {
    return COMPONENT_NAME;
  }

  public void initComponent()
  {
    CompilerManager.getInstance(project).addCompiler(instrumentor);
  }

  public void disposeComponent()
  {
    CompilerManager.getInstance(project).removeCompiler(instrumentor);
  }

// --------------------- Interface Configurable ---------------------

  @Nls
  public String getDisplayName()
  {
    return DISPLAY_NAME;
  }

  public Icon getIcon()
  {
    // TODO get another icon
    return Messages.getInformationIcon();
  }

  public String getHelpTopic()
  {
    // no help so far... who needs help anyway?! *g*
    return null;
  }

// --------------------- Interface PersistentStateComponent ---------------------

  public SalveMutableConfiguration getState()
  {
    return configuration;
  }

  public void loadState(final SalveMutableConfiguration state)
  {
    this.configuration = state;
  }

// --------------------- Interface ProjectComponent ---------------------

  public void projectOpened()
  {
    // log.debug("project opened");
  }

  public void projectClosed()
  {
    // log.debug("project closed");
  }

// --------------------- Interface SalveComponent ---------------------

  public boolean isEnabled()
  {
    return configuration.isEnabled();
  }

  public void setEnabled(final boolean enabled)
  {
    configuration.setEnabled(enabled);
  }

// --------------------- Interface UnnamedConfigurable ---------------------

  public JComponent createComponent()
  {
    return (configurationForm = new SalveConfigurationForm(configuration)).getRootComponent();
  }

  public boolean isModified()
  {
    return configurationForm != null && configurationForm.isModified();
  }

  public void apply() throws ConfigurationException
  {
    // move stuff: form -> state
    // log.debug("apply settings");
  }

  public void reset()
  {
    // move stuff: state -> form
    // log.debug("reset settings");
  }

  public void disposeUIResources()
  {
    configurationForm = null;
  }
}
