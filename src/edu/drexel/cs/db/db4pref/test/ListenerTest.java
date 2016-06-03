package edu.drexel.cs.db.db4pref.test;

import java.util.ArrayList;
import java.util.List;


public class ListenerTest {

  private List<OnIterationListener> listeners = new ArrayList<OnIterationListener>();
   
  public void performIterations() {
    for (int i = 1; i < 1005; i++) {
      for (OnIterationListener listener: listeners) {
        listener.doThis(i);
      }
    }
  }
    
    
  public void addListener(OnIterationListener listener) {
    this.listeners.add(listener);
  }
    
  
  public static void main(String[] args) {
    // the real implementation
    OnIterationListener listener1 = new MyListener();
  
    // or, we can create an anonymous class...
    OnIterationListener listener2 = new OnIterationListener() {
      @Override
      public void doThis(int iteration) {
        System.out.println("iteration " + iteration);
      }
      
    };
    
    
    ListenerTest tester = new ListenerTest();
    tester.addListener(listener1);
    tester.addListener(listener2);
    tester.performIterations();
    
    System.out.println("Done.");
    
  }
  
  
}
  

interface OnIterationListener {
    
  public void doThis(int iteration);
}


class MyListener implements OnIterationListener {

  @Override
  public void doThis(int iteration) {
    if (iteration % 100 == 0) System.out.println("hundred " + iteration);
  }
  
}