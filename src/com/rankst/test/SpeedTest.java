package com.rankst.test;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.filter.Filter;
import com.rankst.generator.MallowsUtils;
import com.rankst.incomplete.IncompleteGenerator;
import com.rankst.model.MallowsModel;
import com.rankst.triangle.Expands;
import com.rankst.util.Logger;
import java.io.File;


public class SpeedTest {

  public static void main(String[] args) throws Exception {
    int n = 20;
    ElementSet elements = new ElementSet(n);
    
    Ranking center = elements.getRandomRanking();
    double phi = 0.5;    
    MallowsModel model = new MallowsModel(center, phi);
    int sampleSize = 5000;
    Sample sample = MallowsUtils.sample(model, sampleSize);
    
    double missing = 0.8;
    Filter.remove(sample, missing);
    
    Expands.setThreshold(0.001);
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File arff = new File(folder, "incomplete.train.arff");
    IncompleteGenerator generator = new IncompleteGenerator(arff);
    
    long start = System.currentTimeMillis();
    generator.generateParallel(sample, 1);
    Logger.info("IncompleteGenerator done in %.1f sec", 1d * (System.currentTimeMillis() - start) / 1000);
    
  }
}
