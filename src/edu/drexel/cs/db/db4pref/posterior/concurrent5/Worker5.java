package edu.drexel.cs.db.db4pref.posterior.concurrent5;

import edu.drexel.cs.db.db4pref.core.Item;


public class Worker5 implements java.util.function.BiConsumer<State5, Double> {

  private final Item item;
  private final boolean missing;
  private final Expands5 dstExpands;
  

  
  Worker5(Expands5 exs, Item item, boolean missing) {
    this.dstExpands = exs;
    this.item = item;
    this.missing = missing;
  }
  

  @Override
  public void accept(State5 state, Double p) {
    state.insert(dstExpands, item, missing, p);
  }
  

}
