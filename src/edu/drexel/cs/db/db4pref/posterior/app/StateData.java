package edu.drexel.cs.db.db4pref.posterior.app;

import java.util.HashMap;
import java.util.Map;


public class StateData {

  double p;
  boolean done = false;
  Map<StateKey, Double> children = null;
  
  
  public StateData() {
    this.p = 1d;
  }
  
  public StateData(double p) {
    this.p = p;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[").append(p);
    sb.append(", ").append((children == null) ? "null" : "not null");
    sb.append("]");
    return sb.toString();
  }
  
  
  public void addChild(StateKey state, double p) {
    if (children == null) children = new HashMap<StateKey, Double>();
    Double prev = children.get(state);
    if (prev != null) p += prev;
    children.put(state, p);
  }
  
}
