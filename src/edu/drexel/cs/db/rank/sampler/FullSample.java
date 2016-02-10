package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import java.util.ArrayList;
import java.util.List;

/** Sample containing all rankings of length n */
public class FullSample extends Sample {

  public FullSample(ItemSet items) {
    super(items);
    Ranking r = items.getReferenceRanking();
    permute(r, 0);
  }
  
  public FullSample(ItemSet allItems, List<Item> items) {
    super(allItems);
    Ranking r = new Ranking(allItems);
    for (Item e: items) r.add(e);
    permute(r, 0);
  }
  
  public FullSample(Ranking reference) {
    super(reference.getItemSet());
    permute(reference, 0);
  }
  
  private void permute(Ranking r, int k) {
    for (int i = k; i < r.size(); i++) {
      java.util.Collections.swap(r.getItems(), i, k);
      this.permute(r, k + 1);
      java.util.Collections.swap(r.getItems(), k, i);
    }
    if (k == r.size() - 1) {
      Ranking a = new Ranking(r);
      this.add(a);
    }
  }

  public static void main(String[] args) {
    int n = 6;
    ItemSet items = new ItemSet(n);
    
    Ranking reference = items.getReferenceRanking();
    Sample sample = new FullSample(reference);
    System.out.println(sample);
    
    List<Item> es = new ArrayList<Item>();
    es.add(items.get(0));
    Sample s = new FullSample(items, es);
    System.out.println(s);
    
  }
  
}
