package edu.drexel.cs.db.db4pref.posterior.concurrent6;

import edu.drexel.cs.db.db4pref.core.Item;


public class Worker6 implements java.util.function.BiConsumer<State6, Double> {

  private final Item item;
  private final boolean missing;
  private final Expands6 dstExpands;
  

  
  Worker6(Expands6 exs, Item item, boolean missing) {
    this.dstExpands = exs;
    this.item = item;
    this.missing = missing;
  }
  

  @Override
  public void accept(State6 state, Double p) {
    state.insert(dstExpands, item, missing, p);
  }
  

}
