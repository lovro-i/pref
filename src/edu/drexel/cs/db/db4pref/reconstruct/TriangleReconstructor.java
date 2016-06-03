package edu.drexel.cs.db.db4pref.reconstruct;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.sampler.MallowsUtils;
import edu.drexel.cs.db.db4pref.math.Polynomial;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.triangle.MallowsTriangle;
import edu.drexel.cs.db.db4pref.triangle.SampleTriangle;
import edu.drexel.cs.db.db4pref.triangle.Triangle;
import edu.drexel.cs.db.db4pref.triangle.TriangleRow;
import edu.drexel.cs.db.db4pref.util.Logger;

/** One that should reconstruct phi directly from the triangle, without sampling
 * Not working fine, still experimental
 * ToDo 
 */
public class TriangleReconstructor {


  public static double reconstructPhi(Triangle triangle) {
    double sumPhi = 0;
    int countPhi = 0;
    Ranking reference = triangle.getReference();
    for (int item = 1; item < reference.length(); item++) {
      TriangleRow row = triangle.getRow(item);
      for (int pos = 0; pos < row.size(); pos++) {
        double p = row.getProbability(pos);
        if (p == 0) continue;
        
        double[] a = new double[item + 2];
        a[0] = p;
        a[item + 1] = -p;
        
        double[] b = new double[item - pos + 2];
        b[item - pos + 1] = -1;
        b[item - pos] = 1;
        
        Polynomial left = new Polynomial(a);
        Polynomial right = new Polynomial(b);
        Polynomial solve = left.sub(right);
        double phi = solve.root(0, 1, 0.00001);
        if (!Double.isNaN(phi)) {
          sumPhi += phi;
          countPhi++;
          
        }
        Logger.info("%f %f %d %d", p, phi, item, pos);
      }
    }
    return sumPhi / countPhi;
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking reference = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(reference, 0.5);
    MallowsTriangle triangle = new MallowsTriangle(model);
    //System.out.println(triangle);
    //System.out.println(reconstructPhi(triangle));
    
    
    
    RankingSample sample = MallowsUtils.sample(model, 10000);
    SampleTriangle tr = new SampleTriangle(reference, sample);
    System.out.println(reconstructPhi(tr));
  }
}
