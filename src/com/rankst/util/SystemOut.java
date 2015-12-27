package com.rankst.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Stack;


public class SystemOut {

  private static final PrintStream mute = new PrintStream(new OutputStream() {
     @Override
     public void write(int arg0) throws IOException { }
  });
  
  private static final Stack<PrintStream> stack = new Stack<PrintStream>();
  
  public static final PrintStream out = System.out;
  
  public static synchronized void mute() {
    stack.push(System.out);
    System.setOut(mute);
  }
  
  public static synchronized void unmute() {
    if (stack.isEmpty()) return;
    PrintStream out = stack.pop();
    System.setOut(out);
  }
  
  public static synchronized void reset() {
    stack.clear();
    System.setOut(out);
  }

  public static void println(String format, Object... args) {
    out.println(String.format(format, args));
  }
  
}
