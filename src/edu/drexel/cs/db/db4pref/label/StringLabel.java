package edu.drexel.cs.db.db4pref.label;

import java.util.Objects;


public class StringLabel implements Label {
  
  public String value;
  
  public StringLabel(String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return value;
  }


  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.value);
    return hash;
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final StringLabel other = (StringLabel) obj;
    if (!Objects.equals(this.value, other.value)) return false;
    return true;
  }
  
}
