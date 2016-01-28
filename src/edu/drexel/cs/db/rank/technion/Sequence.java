package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;

/** Defines a sequence where some items are at specific places (set using Sequence.set method)
 * Items at other positions are undefined
 */
public class Sequence {

  private ItemSet itemSet;
  private Item[] sequence;
  
  public Sequence(ItemSet itemSet) {
    this.itemSet = itemSet;
    this.sequence = new Item[itemSet.size()];
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sequence.length; i++) {
      if (sequence[i] != null) sb.append("sequence[").append(i).append("]: ").append(sequence[i]).append('\n');
    }    
    return sb.toString();
  }
  
  public boolean contains(Item e) {
    for (int i = 0; i < sequence.length; i++) {
      if (e.equals(sequence[i])) return true;
    }
    return false;
  }
  
  public Item[] getItems() {
    return sequence;
  }
  
  public Ranking getRanking() {
    Ranking r = new Ranking(itemSet);
    for (Item item: sequence) {
      if (item != null) r.add(item);
    }
    return r;
  }
  
  
  public int size() {
    int size = 0;
    for (int i = 0; i < sequence.length; i++) {
      if (sequence[i] != null) size++;
    }
    return size;
  }
  
  
  /** Set the index position of the sequence to be the Item e. 
   * @throws IllegalArgumentException if the item is already at some other place in the sequence
   */ 
  public void set(int index, Item e) {
    if (this.contains(e) && !e.equals(sequence[index])) {
      throw new IllegalArgumentException("Item already in the sequence");
    }    
    sequence[index] = e;
  }
  
  /** An alias for set(pos, e). Put item e at position pos */
  public void put(Item e, int pos) {
    set(pos, e);
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Sequence seq = new Sequence(items);    
    seq.set(1, items.get(3));
    seq.set(5, items.get(7));
    System.out.println(seq);
  }
  
}
