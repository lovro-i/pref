package edu.drexel.cs.db.db4pref.util;

import java.io.*;


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
        while ((line = reader.readLine()) != null) out.println(line);
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
      try { reader.close(); }
      catch (IOException ex) { }
    }

  }

}

