package salve.ideaV2.config;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Salve IDEA plugin configuration
 *
 * @author Peter Ertl
 */
public final class SalveMutableConfiguration implements SalveConfiguration
{
  private int version = 1;         // update this if state is altered, allows migration of configs
  private boolean enabled = true;  // initially salve instrumentation is enabled

  public void writeExternal(final ObjectOutput out) throws IOException
  {
    out.writeInt(version);
    out.writeBoolean(enabled);
  }

  public void readExternal(final ObjectInput in) throws IOException
  {
    version = in.readInt();
    enabled = in.readBoolean();
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(final boolean enabled)
  {
    this.enabled = enabled;
  }
}