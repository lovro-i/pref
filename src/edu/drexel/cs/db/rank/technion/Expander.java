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

  private final MallowsModel model;
  private final Map<Item, Integer> referenceIndex;
  private final Ranking ranking;
  private MallowsExpands expands;
  
  
  public Expander(MallowsModel model, Ranking ranking) {
    this.model = model;
    this.ranking = ranking;
    this.referenceIndex = model.getCenter().getIndexMap();
    expand();
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
  }
  
  
  public double getProbability(Sequence seq) {    
    MallowsExpand ex = new MallowsExpand(seq);
    Double p = expands.get(ex);
    if (p == null) return 0;
    return p;
  } 

  public MallowsModel getModel() {
    return model;
  }
  
}
