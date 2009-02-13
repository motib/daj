//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//
//  Moti Ben-Ari
//  LogFile for logging algorithm steps and
//    automatically running from the log file.
package daj;
import java.io.*;
class LogFile {
    
  LogFile(int n) {
      numNodes = n;
  }

  // Open, close and rename files.
  void OpenReader() {
    if (DEBUG) System.out.println("Open reader");
    try {
      reader    = new BufferedReader(new FileReader("login.txt"));
      stream    = new StreamTokenizer(reader);
      stream.slashSlashComments(true);
      stream.parseNumbers();
      getStreamTokens(); // Lookahead.
    }
    catch (IOException e) {
      System.out.println("Cannot open log input file");
    }
  }

  void CloseReader() {
    if (DEBUG) System.out.println("Close reader");
    if (reader != null)
      try {
        reader.close();
      }
      catch (IOException e) {
        System.out.println("Cannot close log input file");
      }
    nvalue = -1;
  }

  void OpenWriter() {
    if (DEBUG) System.out.println("Open writer");
    try {
      writer = new BufferedWriter(new FileWriter("logout.txt"));
    }
    catch (IOException e) {
      System.out.println("Cannot open log output file");
    }
  }

  void CloseWriter() {
    if (DEBUG) System.out.println("Close writer");
    if (writer != null)
      try {
        writer.close();
      }
      catch (IOException e) {
        System.out.println("Cannot close log output file");
      }
  }

  void RenameFiles() {
    if (DEBUG) System.out.println("Rename files");
    File loginputfile = new File("login.txt");
    if (!loginputfile.delete())
      System.out.println("Cannot delete log input file");
    File logoutputfile  = new File("logout.txt");
    if (!logoutputfile.renameTo(loginputfile))
      System.out.println("Cannot rename log output file");
  }

  // Get tokens from stream: node number and command.
  //  Commands must be a single word.
  private boolean getStreamTokens() {
    int token = getFromStream();
    if (token != StreamTokenizer.TT_NUMBER)
      return false;
    nvalue = (int) stream.nval;
    token = getFromStream();  //Get string token
    if (token != StreamTokenizer.TT_WORD)
      return false;
    svalue = stream.sval;
    return true;
  }

  // Get next token from stream.
  // Treat errors like end of file.
  private int getFromStream() {
    try {
      return stream.nextToken();
    }
    catch (IOException e) {
      System.out.println("Error or EOF in log input file");
      return StreamTokenizer.TT_EOF;
    }
  }

  // Accessors for token values.
  int getNumToken() { return nvalue; }
  String getStringToken() { return svalue; }

  //  Execute a step, then lookahead for next values.
  boolean step(DistAlg[] da) {
    if (DEBUG) System.out.println(nvalue + " " + svalue);
    if ((nvalue < 0) || (nvalue >= numNodes)) {
        System.out.println("Log file: wrong number of nodes");
        return false;
    }
    da[nvalue].doAction(svalue, ' ');
    return getStreamTokens();
  }

  // Write command to log file.
  void write(String s) {
    try {
      writer.write(s);
    }
    catch (IOException e) {
      System.out.println("Cannot write on log file");
    }
  }

  private BufferedWriter writer;
  private BufferedReader reader;
  private StreamTokenizer stream;

  //private boolean oktowrite = false;  // File opened for write?
  //private boolean oktoread  = false;  // File opened for read?
  private int     nvalue    = -1;     // Values returned by tokenizer
  private String  svalue;
  private int     numNodes;           // Number of nodes

  private static final boolean DEBUG = false;
}
