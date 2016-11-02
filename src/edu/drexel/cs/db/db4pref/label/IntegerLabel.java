package edu.drexel.cs.db.db4pref.label;


public class IntegerLabel implements Label {

  public int value;
  
  public IntegerLabel(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return value;
  }
  
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 37 * hash + this.value;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final IntegerLabel other = (IntegerLabel) obj;
    if (this.value != other.value) return false;
    return true;
  }
  
  
}
