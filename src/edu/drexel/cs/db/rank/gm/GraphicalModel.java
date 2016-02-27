package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.List;


public class GraphicalModel {

  private final MallowsModel model;
  private final PreferenceSet pref;

  private final List<Variable> variables = new ArrayList<Variable>();
  
  public GraphicalModel(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.pref = pref;
  }
  
  public MallowsModel getModel() {
    return model;
  }
  
  public void build() {
    Ranking reference = model.getCenter();
    for (int i = 0; i < reference.size(); i++) {      
      Item item = reference.get(i);
      if (!pref.contains(item)) continue;
      InsertVariable insert = new InsertVariable(this, item);
      variables.add(insert);
    }
  }
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Variable v: variables) sb.append(v).append("\n\n");
    return sb.toString();
  }
  
  
  
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    items.letters();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    // BD
    Ranking r = new Ranking(items);
    r.add(items.get(1));
    r.add(items.get(3));
    
    GraphicalModel gm = new GraphicalModel(model, r);
    gm.build();
    System.out.println(gm);
    
  }
}
