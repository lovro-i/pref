package edu.drexel.cs.db.db4pref.core;

import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

/** Set of items (items, alternatives), with id going from 0 to n-1 */
public class ItemSet implements List<Item>, Serializable {
  
  private static final long serialVersionUID = -621735148019149586L;

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
  
  public void save(String filename) throws IOException {
    save(new File(filename));
  }
    
  public void save(File file) throws IOException {
    PrintWriter out = FileUtils.write(file);
    save(out);
    out.close();
  }
  
  public void save(PrintWriter out) {
    for (Item item: this) {
      out.println(String.format("%d: %s", item.id, item));
    }
  }
  
  public static ItemSet load(String filename) throws IOException {
    return load(new File(filename));
  }
  
  public static ItemSet load(File file) throws IOException {
    return load(new FileReader(file));
  }
  
  public static ItemSet load(Reader reader) throws IOException {
    BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    String line = br.readLine();
    Map<Integer, String> map = new HashMap<Integer, String>();
    int max = -1;
    while (line != null) {
      StringTokenizer tokenizer = new StringTokenizer(line, ":");
      Integer id = Integer.valueOf(tokenizer.nextToken());
      String s = tokenizer.nextToken();
      if (s != null) s = s.trim();
      map.put(id, s);
      max = Math.max(max, id);
      line = br.readLine();
    }
    
    Object[] objects = new Object[max+1];
    for (Integer i: map.keySet()) {
      objects[i] = map.get(i);
    }
    return new ItemSet(objects);
  }

  public static void main(String[] args) throws IOException {
//    ItemSet items = new ItemSet(10);
//    items.tagLetters();
//    items.save("c:/temp/items.txt");

    ItemSet items = ItemSet.load("c:/temp/items.txt");
    System.out.println(items);
  }
}
