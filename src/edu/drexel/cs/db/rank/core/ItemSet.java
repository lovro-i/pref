package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** Set of items (items, alternatives), with id going from 0 to n-1 */
public class ItemSet implements List<Item>, Serializable {

  private final List<Item> items = new ArrayList<Item>();
  
  public ItemSet(int n) {
    for (int i=0; i<n; i++) items.add(new Item(i));
  }
  
  public ItemSet(Collection objects) {
    this(objects.toArray());
  }
  
  public ItemSet(Object[] objects) {
    for (int i = 0; i < objects.length; i++) {
      Item e = new Item(i, objects[i]);
      items.add(e);
    }
  }

 
  public Item getItemById(int id) {
    return items.get(id);
  }
  
  public Item getItemByTag(Object tag) {
    for (Item e: items) {
      if (tag.equals(e.getTag())) return e;
    }
    return null;
  }
  
  @Override
  public int size() {
    return items.size();
  }
  
  /** Convert item names (tags) of the items to letters */
  public void tagLetters() {
    for (Item e: items) {
      e.setTag(String.valueOf(Character.toChars(e.getId()+'A')));
    }
  }
  
  /** Convert item names (tags) to sigma_i (one based) */
  public void tagSigmas() {
    int i = 0;
    for (Item e: items) {
      i++;
      
      String s = "";
      int a = i;
      while (a > 0) {
        s = String.valueOf(Character.toChars('\u2080' + (a % 10))) + s;
        a = a / 10;
      }      
      s = "\u03c3" + s;
      e.setTag(s);
    }
  }
  
  /** Convert item names (tags) to one based Integers */
  public void tagOneBased() {
    for (Item e: items) {
      e.setTag(new Integer(e.getId()+1));
    }
  }
  
  public void tagZeroBased() {
    for (Item e: items) {
      e.setTag(new Integer(e.getId()));
    }
  }
  
  /** @return random ranking of length len */
  public Ranking getRandomRanking(int len) {
    if (len > items.size()) throw new IllegalArgumentException("Ranking length cannot be greater that the number of items");
    Ranking ranking = new Ranking(this);
    List<Item> elems = new ArrayList<Item>(items);
    while (ranking.length() < len) {
      int id = MathUtils.RANDOM.nextInt(elems.size());
      Item item = elems.get(id);
      ranking.add(item);
      elems.remove(id);
    }
    return ranking;
  }
  
  /** @return random ranking containing all items */
  public Ranking getRandomRanking() {
    Ranking ranking = new Ranking(this.getReferenceRanking());
    for (int i = 0; i < ranking.length() - 1; i++) {
      int j = i + MathUtils.RANDOM.nextInt(ranking.length() - i);
      ranking.swap(i, j);
    }
    return ranking;
  }
  
  public Ranking getReferenceRanking() {
    Ranking ranking = new Ranking(this);
    for (Item e: items) ranking.add(e);
    return ranking;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof ItemSet)) return false;
    ItemSet items = (ItemSet) obj;
    if (items.size() != this.size()) return false;
    
    for (int i = 0; i < this.items.size(); i++) {
      if (!this.items.get(i).equals(items.items.get(i))) return false;
    }
    return true;
  }

  @Override
  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return items.contains(o);
  }

  @Override
  public Iterator<Item> iterator() {
    return items.iterator();
  }

  @Override
  public Object[] toArray() {
    return items.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return items.toArray(a);
  }

  @Override
  public boolean add(Item e) {    
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return items.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends Item> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean addAll(int index, Collection<? extends Item> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public Item get(int index) {
    return items.get(index);
  }

  @Override
  public Item set(int index, Item item) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public void add(int index, Item item) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public Item remove(int index) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public int indexOf(Object o) {
    return items.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return items.lastIndexOf(o);
  }

  @Override
  public ListIterator<Item> listIterator() {
    return items.listIterator();
  }

  @Override
  public ListIterator<Item> listIterator(int index) {
    return items.listIterator(index);
  }

  @Override
  public List<Item> subList(int fromIndex, int toIndex) {
    return items.subList(fromIndex, toIndex);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.size()).append(" items: { ");
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) sb.append(", ");
      sb.append(items.get(i));
    }
    sb.append(" }");
    return sb.toString();
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(15);
    items.tagSigmas();
    System.out.println(items);
  }

}
