package salve;

/**
 * Represents anything that is aware of a {@link CodeMarker}
 * 
 * @author igor.vaynberg
 * 
 */
public interface CodeMarkerAware {
	/**
	 * @return code marker, or null for none
	 */
	CodeMarker getCodeMarker();
}
