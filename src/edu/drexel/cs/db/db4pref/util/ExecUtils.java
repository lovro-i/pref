package edu.drexel.cs.db.db4pref.util;

import java.io.*;
import java.util.concurrent.TimeUnit;


public class ExecUtils {

  
  public static int execute(String exec) {
    try {
      Process p = Runtime.getRuntime().exec(exec);
      StreamSucker input = new StreamSucker(p.getInputStream(), System.out);
      StreamSucker error = new StreamSucker(p.getErrorStream(), System.err);

      int exitCode = p.waitFor();
      input.join();
      error.join();
      return exitCode; // p.exitValue();
    }
    catch (Exception err) {
      err.printStackTrace();
      return -1;
    }
  }

  public static int execute(ProcessBuilder builder) {
    try {
      Process p = builder.start();
      StreamSucker input = new StreamSucker(p.getInputStream(), System.out);
      StreamSucker error = new StreamSucker(p.getErrorStream(), System.err);

      int exitCode = p.waitFor();
      input.join();
      error.join();
      return exitCode; // p.exitValue();
    }
    catch (Exception err) {
      err.printStackTrace();
      return -1;
    }
  }
  
  public static int execute(ProcessBuilder builder, long timeout) {
    return execute(builder, timeout, System.out, System.err);
  }
  
  public static int execute(ProcessBuilder builder, PrintStream out, PrintStream err) {
    try {
      Process p = builder.start();
      StreamSucker input = new StreamSucker(p.getInputStream(), out);
      StreamSucker error = new StreamSucker(p.getErrorStream(), err);

      int exitValue = p.waitFor();
      input.join();
      error.join();
      return exitValue;
    }
    catch (Exception ex) {
      ex.printStackTrace();
      return -1;
    }
  }
  
  public static int execute(ProcessBuilder builder, long timeout, PrintStream out, PrintStream err) {
    try {
      Process p = builder.start();
      StreamSucker input = new StreamSucker(p.getInputStream(), out);
      StreamSucker error = new StreamSucker(p.getErrorStream(), err);

      boolean ok = p.waitFor(timeout, TimeUnit.MILLISECONDS);
      if (ok) {
        input.join();
        error.join();
        return p.exitValue();
      }
      else {
        Logger.info("Process timeout after %d ms", timeout);
        return -1;
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      return -1;
    }
  }
  
  public static int execute(String exec, File folder) {
    try {
      Process p = Runtime.getRuntime().exec(exec, null, folder);
      StreamSucker input = new StreamSucker(p.getInputStream(), System.out);
      StreamSucker error = new StreamSucker(p.getErrorStream(), System.err);

      int exitCode = p.waitFor();
      input.join();
      error.join();
      return exitCode; // p.exitValue();
    }
    catch (Exception err) {
      err.printStackTrace();
      return -1;
    }
  }
  
  
  private static class StreamSucker extends Thread {

    private BufferedReader reader;
    private PrintStream out;

    public StreamSucker(InputStream stream) {
      this(stream, System.out);
    }

    public StreamSucker(InputStream stream, PrintStream out) {
      this.reader = new BufferedReader(new InputStreamReader(stream));
      this.out = out;
      this.start();
    }

    @Override
    public void run() {
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          if (out != null) out.println(line);
        }
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
      try { reader.close(); }
      catch (IOException ex) { }
    }

  }

}

