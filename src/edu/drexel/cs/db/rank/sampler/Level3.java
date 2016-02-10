package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Map from prefix to PreferenceSet */
class Level3 {

  private final Ranking reference;
  private final int index;
  private final PreferenceConstraint cons;
  private final Map<Ranking, Users> map = new HashMap<Ranking, Users>();
  
  
  Level3(Ranking reference, PreferenceConstraint cons, int index) {
    this.reference = reference;
    this.cons = cons;
    this.index = index;
  }

  public Ranking top(PreferenceSet pref) {
    Map<Item, Integer> items = new HashMap<Item, Integer>();
    for (int i = 0; i < index; i++) {
      items.put(reference.get(i), 0);
    }
    
    for (int i = 0; i < index-1; i++) {
      Item it1 = reference.get(i);
      for (int j = i+1; j < index; j++) {
        Item it2 = reference.get(j);
        Boolean b = pref.isHigher(it1, it2);
        if (b == null) return null;
        if (b) {
          int c = items.get(it2);
          items.put(it2, c+1);
        }
        else {
          int c = items.get(it1);
          items.put(it1, c+1);
        }
      }      
    }
    
    Map<Integer, Item> reverse = new HashMap<Integer, Item>();
    for (Item it: items.keySet()) reverse.put(items.get(it), it);
    
    Ranking top = new Ranking(reference.getItemSet());
    for (int i = 0; i < index; i++) {
      top.add(reverse.get(i));
    }    
    return top;
  }

  void add(PreferenceSet pref, double w, Ranking prefix) {
    Users users = map.get(prefix);
    if (users == null) {
      users = new Users(prefix, pref, cons, reference);
      map.put(prefix, users);
    }
    users.add(pref, w);
  }
  
  public Collection<Users> getUsers() {
    return map.values();
  }

}
