package edu.drexel.cs.db.rank.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;

/** Counts instances of T */
public class Histogram<T> {

  Map<T, Double> map = new HashMap<T, Double>();
  
  public Histogram() {    
  }
  
  public Histogram(Collection<T> collection) {
    this.add(collection);
  }
  
  public Histogram(List<T> collection, List<Double> weights) {
    this.add(collection, weights);
  }
  
  public int size() {
    return map.size();
  }
  
  public boolean isEmpty() {
    return map.isEmpty();
  }
  
  public void add(Collection<T> collection) {
    for (T ranking: collection) {
      this.add(ranking, 1d);
    }
  }
  
  public void add(List<T> items, List<Double> weights) {
    for (int i = 0; i < items.size(); i++) {
      this.add(items.get(i), weights.get(i));      
    }
  }
  
  public void add(T[] items, double[] weights) {
    for (int i = 0; i < items.length; i++) {
      this.add(items[i], weights[i]);
    }
  }
  
  public Double get(T key) {
    return map.get(key);
  }
  
  public Map<T, Double> getMap() {
    return map;
  }
  
  public void add(T key, double weight) {
    double d = count(key);
    map.put(key, d + weight);    
  }
  
  public double count(T key) {
    Double i = map.get(key);
    if (i == null) i = 0d;
    return i;
  }
  
  public T getMostFrequent() {
    double max = 0;
    T key = null;
    for (T r: map.keySet()) {
      double c = map.get(r);
      if (c > max) {
        max = c;
        key = r;
      }
    }
    return key;
  }
  
  public double getMostFrequentCount() {
    return map.get(getMostFrequent());
  }
  
  public void output(OutputStream os) throws IOException {
    // FileOutputStream fos = new FileOutputStream("hashmap.ser");
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(map);
    oos.close();
  }
  
  public void input(InputStream is) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(is);
    try { map = (HashMap) ois.readObject(); } 
    catch (ClassNotFoundException ex) { }
    ois.close();
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (T t: map.keySet()) {
      sb.append(t).append(": ").append(map.get(t)).append("\n");
    }
    return sb.toString();
  }
}
