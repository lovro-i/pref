package com.rankst.entity;


public class Element {

  private int id;
    
  public Element(int id) {
    this.id = id;
  }
  
  public int getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return String.valueOf(Character.toChars(id+'A'));
    // return String.valueOf(id);
  }
  
  @Override
  public boolean equals(Object o) {
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
