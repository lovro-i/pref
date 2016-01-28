package edu.drexel.cs.db.rank.top;

import java.util.ArrayList;
import weka.core.Attribute;


public class TopIncompleteAttributes {
  
  public static final Attribute ATTRIBUTE_TRIANGLE_NO_ROW = new Attribute("triangle_no_row"); 
  public static final Attribute ATTRIBUTE_TRIANGLE_BY_ROW = new Attribute("triangle_by_row");
  public static final Attribute ATTRIBUTE_COMPLETER_MEAN = new Attribute("completer_mean");
  public static final Attribute ATTRIBUTE_COMPLETER_VAR = new Attribute("completer_var");
  public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
  

  static ArrayList<Attribute> getAttributes(boolean triangle, boolean triangleByRow, int boots) {
    ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    if (triangle) attributes.add(ATTRIBUTE_TRIANGLE_NO_ROW);
    if (triangleByRow) attributes.add(ATTRIBUTE_TRIANGLE_BY_ROW);
    if (boots > 0) {
      attributes.add(ATTRIBUTE_COMPLETER_MEAN);
      attributes.add(ATTRIBUTE_COMPLETER_VAR);
    }    
    attributes.add(ATTRIBUTE_REAL_PHI);    
    return attributes;
  }
  
  
  
}
