package edu.drexel.cs.db.rank.entity;


public class Rating {

  public final Element element;
  public final double rating;
  
  public Rating(Element element, double rating) {
    this.element = element;
    this.rating = rating;
  }
  
  @Override
  public String toString() {
    return element.toString() + ": " + rating;
  }
}
