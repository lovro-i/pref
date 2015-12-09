package com.rankst.generator;

import com.rankst.entity.*;
import com.rankst.triangle.Triangle;
import java.util.List;


public class RIMRSampler {

  protected Triangle triangle;
  
  public RIMRSampler(Triangle triangle) {
    this.triangle = triangle;
  }
  
  public Ranking generate() {
    Ranking r = new Ranking(triangle.getElements());
    List<Element> elements = triangle.getReference().getElements();
    
    r.add(elements.get(0));
    for (int i=1; i<elements.size(); i++) {
      Element e = elements.get(i);
      int pos = triangle.randomPosition(i);      
      r.addAt(pos, e);
    }
    return r;
  }
  
  public Sample generate(int count) {
    Sample sample = new Sample(triangle.getElements());
    for (int i=0; i<count; i++) {
      Ranking ranking = this.generate();
      sample.add(ranking);
    }
    return sample;
  }
}
