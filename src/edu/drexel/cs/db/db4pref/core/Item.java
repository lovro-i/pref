package edu.drexel.cs.db.db4pref.core;

import edu.drexel.cs.db.db4pref.label.Label;
import edu.drexel.cs.db.db4pref.label.LabelSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** One item, alternative, element... */
public class Item implements Serializable, Comparable<Item> {

  public final int id;
  private Object tag;
  private Map<String, Label> labels;
    
  Item(int id) {
    this(id, String.valueOf(id));
  }
  
  Item(int id, Object tag) {
    this.id = id;
    this.tag = tag;
  }
  
  public Object getTag() {
    return tag;
  }
  
  public void setTag(Object tag) {
    this.tag = tag;
  }
  
  public int getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return tag.toString();
  }
  
  public Label getLabel(String name) {
    return labels.get(name);
  }
  
  public void setLabel(String name, Label label) {
    if (labels == null) labels = new HashMap<String, Label>();
    labels.put(name, label);
  }
  
  public void addLabel(String name, Label label) {
    if (labels == null) labels = new HashMap<String, Label>();
    Label lab = labels.get(name);
    if (lab instanceof LabelSet) {
      ((LabelSet) lab).add(label);
    }
    else {
      LabelSet set = new LabelSet();
      set.add(lab);
      set.add(label);
      labels.put(name, set);
    }
  }
  
  public boolean containsLabel(String name, Label label) {
    if (labels == null) return false;
    Label lab = labels.get(name);
    if (lab == null) return false;
    if (lab.equals(label)) return true;
    if (lab instanceof LabelSet) return ((LabelSet) lab).contains(label);
    return false;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    Item e = (Item) o;
    return e.id == this.id;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + this.id;
    return hash;
  }

  @Override
  public int compareTo(Item o) {
    return this.id - o.id;
  }
  
}
