package edu.drexel.cs.db.db4pref.posterior.sequential1;

import java.util.Set;


public class StateData {

  double p;
  boolean done = false;
  Set<StateKey> children = null;
  
  
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
  
}
