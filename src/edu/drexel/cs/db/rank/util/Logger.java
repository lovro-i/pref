package edu.drexel.cs.db.rank.util;

import java.io.IOException;


public class Logger {

  public static void info(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }
  
  public static void info(Object obj) {
    System.out.println(obj.toString());
  }
  
  public static void time(String msg, long start) {
    info("%s | time: %.1f sec", msg, 0.001d * (System.currentTimeMillis() - start));
  }
    
  public static void waitKey() {
    try { System.in.read(); }
    catch (IOException e) {}
  }
  
  public static void waitKey(String msg, Object... args) {
    info(msg, args);
    waitKey();
  }
  
  public static void waitKey(Object obj) {
    info(obj);
    waitKey();
  }
  
}
