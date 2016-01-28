package edu.drexel.cs.db.rank.technion;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.UpTo;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


public class Expander {

  private MallowsModel model;
  private Map<Item, Integer> referenceIndex = new HashMap<Item, Integer>();
  private Ranking ranking;
  private MallowsExpands expands;
  
  
  public Expander(MallowsModel model, Ranking ranking) {
    this.model = model;
    this.ranking = ranking;
    buildReferenceIndexMap();
    expand();
  }
  
  private void buildReferenceIndexMap() {
    Ranking reference = model.getCenter();
    referenceIndex.clear();
    for (int i = 0; i < reference.size(); i++) {
      Item e = reference.get(i);
      referenceIndex.put(e, i);
    }    
  }
  
  private void expand() {
    expands = new MallowsExpands(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    
    for (int i = 0; i < reference.size(); i++) {
      Item e = reference.get(i);
      
      UpTo upto = new UpTo(ranking, i, referenceIndex); 
      int pos = upto.position;
      
      if (pos == -1) expands = expands.insertMissing(e);
      else expands = expands.insert(e, upto.previous);      
    }
    
    System.out.println(expands);
  }
  
  
  public double getProbability(Sequence seq) {    
    MallowsExpand ex = new MallowsExpand(seq);
    return expands.get(ex);
  } 

  public MallowsModel getModel() {
    return model;
  }
  
}
