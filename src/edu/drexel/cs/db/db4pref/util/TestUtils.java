package edu.drexel.cs.db.db4pref.util;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class TestUtils {

  
  public static MapPreferenceSet generate(int m, double pVertex, double pEdge) {
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    Item lastItem = items.get(items.size()-1);
    
    MapPreferenceSet pref = new MapPreferenceSet(items);
    
    Random random = new Random();
    List<Item> its = new ArrayList<Item>();
    for (int i = 0; i < items.size()-1; i++) {
      double flip = random.nextDouble();
      if (flip < pVertex) its.add(items.get(i));
    }
    its.add(lastItem);
    if (its.size() == 1) its.add(items.get(random.nextInt(items.size()-1)));
    Collections.shuffle(its);
    
    for (int i = 0; i < its.size() - 1; i++) {
      Item higher = its.get(i);
      for (int j = i+1; j < its.size(); j++) {
        double flip = random.nextDouble();
        if (flip < pEdge) {
          Item lower = its.get(j);
          pref.add(higher, lower);
        }
      }
    }
    
    while (!pref.contains(lastItem)) {
      int i = its.indexOf(lastItem);
      int j = (i + 1) % its.size();
      if (i < j) pref.add(lastItem, its.get(j));
      else pref.add(its.get(j), lastItem);
    }
    
    return pref;
  }
  
  public static void main(String[] args) {
    MapPreferenceSet pref = generate(10, 0.2, 0.3);
    System.out.println(pref);
  }
  
  
  public static MapPreferenceSet generate(int m, int pairs) {
    return generate(m, 4, pairs);
  }
  
  /** Generate random PreferenceSet */
  public static MapPreferenceSet generate(int m, int type, int pairs) {
    int box = Math.min(10, m/2);
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(items);
 
    if (type == 0) { // top
      while (pref.size() < pairs) {
        Item item1 = items.get(MathUtils.RANDOM.nextInt(box));
        Item item2 = items.get(MathUtils.RANDOM.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 1) { // bottom
      while (pref.size() < pairs) {
        Item item1 = items.get(m - box + MathUtils.RANDOM.nextInt(box));
        Item item2 = items.get(m - box + MathUtils.RANDOM.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 2) {
      while (pref.size() < pairs) {
        Item item1 = items.get(MathUtils.RANDOM.nextInt(box));
        Item item2 = items.get(m - box + MathUtils.RANDOM.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 3) {
      int left = 0;
      while (pref.size() < pairs) {
        Item item1 = items.get(left + MathUtils.RANDOM.nextInt(box));
        Item item2 = items.get((left + (m - box) / 2 + MathUtils.RANDOM.nextInt(box)) % m);
        
        try { 
          pref.add(item1, item2);
          left = (left + (m-box)/2) % m;
        }
        catch (IllegalStateException e) {}
        
      }
    }
    else if (type == 4) {
      while (pref.size() < pairs) {
        Item item1 = items.get(MathUtils.RANDOM.nextInt(m));
        Item item2 = items.get(MathUtils.RANDOM.nextInt(m));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 5) {
      while (pref.size() < pairs) {
        Item item1 = items.get(m - box + MathUtils.RANDOM.nextInt(box));
        Item item2 = items.get(MathUtils.RANDOM.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 6) {
      while (pref.size() < pairs) {
        Item item1 = items.get(m - box + MathUtils.RANDOM.nextInt(box));
        Item item2 = items.get(MathUtils.RANDOM.nextInt(box));
        
        if (MathUtils.RANDOM.nextInt(2) == 0) {
          Item temp = item1;
          item1 = item2;
          item2 = temp;
        }
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    
    return pref;
  }
  
  public static MapPreferenceSet pref1() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(24, 19);
    pref.addByTag(26, 11);
    pref.addByTag(25, 14);
    pref.addByTag(25, 15);
    pref.addByTag(22, 13);
    return pref;
  }
  
  
  public static MapPreferenceSet pref2() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(18, 11);
    pref.addByTag(4, 30);
    pref.addByTag(25, 22);
    pref.addByTag(16, 30);
    pref.addByTag(1, 18);
    return pref;
  }
  
  /** Around 11 seconds */
  public static MapPreferenceSet pref3() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(7, 16);
    pref.addByTag(24, 3);
    pref.addByTag(20, 4);
    pref.addByTag(6, 22);
    pref.addByTag(23, 14);
    return pref;
  }
  
  /** Around 3 seconds; -4.583774 */
  public static MapPreferenceSet pref4() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(27, 24);
    pref.addByTag(16, 2);
    pref.addByTag(11, 30);
    pref.addByTag(9, 29);
    pref.addByTag(27, 17);
    return pref;
  }
  
  /** Around 15 seconds */
  public static MapPreferenceSet pref5() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(30, 1);
    pref.addByTag(3, 23);
    pref.addByTag(21, 9);
    pref.addByTag(4, 25);
    pref.addByTag(14, 20);
    return pref;
  }

  /** Around 70 seconds */
  public static MapPreferenceSet pref6() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(34, 30);
    pref.addByTag(35, 1);
    pref.addByTag(31, 32);
    pref.addByTag(16, 35);
    pref.addByTag(36, 26);
    return pref;
  }
  
  
  
  public static PrintWriter getWriter(String[] args) throws IOException {
    PrintWriter out;
    if (args.length > 0) {
      File file = new File(args[0]);
      Logger.info("Writing to %s", file);
      out = FileUtils.append(file);
    }
    else {
      Logger.warn("No file specified, writing to console");
      out = new PrintWriter(System.out);
    }
    return out;
  }
    
}
