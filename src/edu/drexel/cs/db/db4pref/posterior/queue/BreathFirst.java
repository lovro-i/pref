package edu.drexel.cs.db.db4pref.posterior.queue;

import java.util.Comparator;


public class BreathFirst implements Comparator<State> {

  private static BreathFirst instance = new BreathFirst();
  
  public static BreathFirst getInstance() {
    return instance;
  }
  
  @Override
  public int compare(State state1, State state2) {
    if (state1.equals(state2)) return 0;
    
    int len1 = state1.length();
    int len2 = state2.length();
    if (len1 < len2) return -1;
    else if (len1 > len2) return 1;
    
    int diff = state1.miss.length - state2.miss.length;
    if (diff != 0) return diff;
    
    for (int i = 0; i < state1.miss.length; i++) {
      diff = state1.miss[i] - state2.miss[i];
      if (diff != 0) return diff;      
    }
    
    for (int i = 0; i < state1.items.length; i++) {
      diff = state1.items[i].compareTo(state2.items[i]);
      if (diff != 0) return diff;
    }
    return 0;
  }

}
