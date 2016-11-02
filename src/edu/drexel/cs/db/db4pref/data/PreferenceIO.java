package edu.drexel.cs.db.db4pref.data;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import java.util.StringTokenizer;

/** Used for converting string representation of a preference set into format suitable for CSV files (removes commas) */
public class PreferenceIO {

  /** Serialize preference set into string suitable for CVS files (contains no commas) */
  public static String toString(PreferenceSet pref) {
    return pref.getPreferences().toString().replace(" ", "").replace(',', ' ');
  }
  
  /** Deserialize a string representation of a preference set */
  public static MapPreferenceSet fromString(String s, ItemSet items) {
    s = s.replace('[', ' ').replace(']', ' ').trim();
    MapPreferenceSet pref = new MapPreferenceSet(items);
    StringTokenizer tokenizer = new StringTokenizer(s, " >");
    while (tokenizer.hasMoreTokens()) {
      String i1 = tokenizer.nextToken();
      Item it1 = items.getItemByTag(i1);
      
      String i2 = tokenizer.nextToken();
      Item it2 = items.getItemByTag(i2);
      
      pref.add(it1, it2);
    }
    return pref;
  }
  

}
