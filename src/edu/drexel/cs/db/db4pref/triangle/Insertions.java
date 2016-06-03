package edu.drexel.cs.db.db4pref.triangle;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashSet;
import java.util.Set;


public class Insertions {

  public Integer[] ins;
  public int length;
  
  public Insertions(PreferenceSet pref, Ranking reference) {
    set(pref, reference);
  }
  
  public void set(PreferenceSet pref, Ranking reference) {
    if (ins == null || ins.length != reference.length()) ins = new Integer[reference.length()];
    
    Set<Item> items = new HashSet<Item>();
    int len = 0;
    for (int i = 0; i < reference.length(); i++) {
      Item e = reference.get(i);
      items.add(e);
      
      Ranking r = pref.toRanking(items);
      int pos = r.indexOf(e);
      if (pos == -1) break;
      len++;
      ins[i] = pos;
    }
    this.length = len;
  }
  
  public void set(Ranking pref, Ranking reference) {
    if (ins == null || ins.length != reference.length()) ins = new Integer[reference.length()];
    
    Set<Item> items = new HashSet<Item>();
    int len = 0;
    for (int i = 0; i < reference.length(); i++) {
      Item e = reference.get(i);
      items.add(e);
      
      Ranking r = pref.toRanking(items);
      int pos = r.indexOf(e);
      if (pos == -1) break;
      len++;
      ins[i] = pos;
    }
    this.length = len;
  }
  
}
