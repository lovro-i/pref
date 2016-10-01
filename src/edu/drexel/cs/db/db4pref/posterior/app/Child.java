package edu.drexel.cs.db.db4pref.posterior.app;


public class Child {

  final StateKey state;
  final double p;
  
  public Child(StateKey state, double p) {
    this.state = state;
    this.p = p;
  }
  
  @Override
  public String toString() {
    return state.toString() + " (" + p + ")";
  }
  
}
