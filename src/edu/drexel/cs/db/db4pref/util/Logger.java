package edu.drexel.cs.db.db4pref.util;

import java.io.IOException;
import java.util.Date;


public class Logger {

  public static void info(String msg, Object... args) {
    System.out.println(String.format("[%s] %s", new Date(), String.format(msg, args)));
  }
  
  public static void info(Object obj) {
    System.out.println(String.format("[%s] %s", new Date(), obj.toString()));
  }
  
  public static void warn(Throwable t) {
    System.err.println(t.getMessage());
  }
  
  public static void warn(String msg, Object... args) {
    System.err.println(String.format(msg, args));
  }
  
  public static void warn(Throwable t, String msg, Object... args) {
    System.err.println(t.getMessage() + " " + String.format(msg, args));
  }
  
  public static void error(Throwable t) {
    System.err.println(t.getMessage());
  }
  
  public static void error(String msg, Object... args) {
    System.err.println(String.format(msg, args));
  }
  
  public static void error(Throwable t, String msg, Object... args) {
    System.err.println(t.getMessage() + " " + String.format(msg, args));
  }
  
  public static void time(String msg, long start) {
    info("%s | time: %.1f sec", msg, 0.001d * (System.currentTimeMillis() - start));
  }
    
  public static void waitKey() {
    try { System.in.read(); }
    catch (IOException e) {}
  }
  
  public static void trace() {
    Thread.dumpStack();
  }
  
}
