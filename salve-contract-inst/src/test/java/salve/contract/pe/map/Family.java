package salve.contract.pe.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Family {
	public Map<String, Member> members = new HashMap<String, Member>();
	public List<Member> ancestors = new ArrayList<Member>();
}
