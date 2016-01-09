package edu.drexel.cs.db.rank.entity;


public class Element {

  private int id;
  private Object tag;
    
  Element(int id) {
    this(id, String.valueOf(id));
  }
  
  Element(int id, Object tag) {
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
  
  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    Element e = (Element) o;
    return e.id == this.id;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + this.id;
    return hash;
  }
  
}
