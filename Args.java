import java.util.*;

// 混亂程式 是逐漸產生的
// 重構 => 漸進整理程式碼
// 經 TDD 測試及驗證 確保 重構後，程式能正確進行

public class Args {
  private String schema;
  private boolean valid = true;
  private Set<Character> unexpectedArguments = new TreeSet<Character>(); 

  private Map<Character, ArgumentMarshaler> marshaler = new HashMap<Character, ArgumentMarshaler>();
  
  private Set<Character> argsFound = new HashSet<Character>();
  private int currentArgument;
  private char errorArgumentId = '\0';
  
  private String errorParameter = "TILT";
  private ArgsException.ErrorCode errorCode = ArgsException.ErrorCode.OK;
  
  private List<String> argsList;
  
    
  public Args(String schema, String[] args) throws ArgsException { 
    this.schema = schema;
    argsList = Arrays.asList(args);
    valid = parse();
  }
  
  private boolean parse() throws ArgsException { 
    if (schema.length() == 0 && argsList.size == 0)
      return true; 
    parseSchema(); 
    try {
      parseArguments();
    } catch (ArgsException e) {
    }
    return valid; // default true ?
  }
  
  private boolean parseSchema() throws ArgsException {
    for (String element : schema.split(",")) {
      if (element.length() > 0) {
        String trimmedElement = element.trim(); 
        parseSchemaElement(trimmedElement);
      } 
    }
    return true; 
  }
  
  private void parseSchemaElement(String element) throws ArgsException { 
    char elementId = element.charAt(0);
    String elementTail = element.substring(1); 
    validateSchemaElementId(elementId);
    
    if (elementTail.length() == 0) 
      marshalers.put(elementId, new BooleanArgumentMarshaler());
    else if (elementTail.equals("*")) 
      marshalers.put(elementId, new StringArgumentMarshaler());
    else if (elementTail.equals("#")) 
      marshalers.put(elementId, new IntegerArgumentMarshaler());
    else if (elementTail.equals("##"))
      marshalers.put(elementId, new DoubleArgument());
    else
      throw new ArgsException(String.format("Argument: %c has invalid format: %s.", 
        elementId, elementTail), 0);
    } 
  }
    
  private void validateSchemaElementId(char elementId) throws ArgsException { 
    if (!Character.isLetter(elementId)) {
      throw new ArgsException("Bad character:" + elementId + "in Args format: " + schema, 0);
    }
  }
  
  private boolean parseArguments() throws ArgsException {
    for (currentArgument = argsList.iterator(); currentArgument.hasNext()) {
      String arg = currentArgument.next();
      parseArgument(arg); 
    }
    return true; 
  }
  
  private void parseArgument(String arg) throws ArgsException { 
    if (arg.startsWith("-"))
      parseElements(arg); 
  }
  
  private void parseElements(String arg) throws ArgsException { 
    for (int i = 1; i < arg.length(); i++)
      parseElement(arg.charAt(i)); 
  }
  
  private void parseElement(char argChar) throws ArgsException { 
    if (setArgument(argChar))
      argsFound.add(argChar); 
    else 
      unexpectedArguments.add(argChar); 
      errorCode = ErrorCode.UNEXPECTED_ARGUMENT; 
      valid = false;
  }
  
  private boolean setArgument(char argChar) throws ArgsException { 
    ArgumentMarshaler m = marshaler.get(argChar);
    if (m == null) {
      return false;
    }
    try {
      m.set(currentArgument);
    } catch (ArgsException e) {
      valid = false;
      errorArgumentId = argChar;
      throw e;
    }
    return true; 
  }
  
  public int cardinality() { 
    return argsFound.size();
  }
  
  public String usage() { 
    if (schema.length() > 0)
      return "-[" + schema + "]"; 
    else
      return ""; 
  }
  
  private String unexpectedArgumentMessage() {
    StringBuffer message = new StringBuffer("Argument(s) -"); 
    for (char c : unexpectedArguments) {
      message.append(c); 
    }
    message.append(" unexpected.");
    
    return message.toString(); 
  }
  
  private boolean falseIfNull(Boolean b) { 
    return b != null && b;
  }
  
  private int zeroIfNull(Integer i) { 
    return i == null ? 0 : i;
  }
  
  private String blankIfNull(String s) { 
    return s == null ? "" : s;
  }
  
  public String getString(char arg) { 
    Args.ArgumentMarshaler am = marsharlers.get(arg);
    try {
      return am == null ? "" : (String)am.get();
    } catch (ClassCastException e) {
      return "";
    }
  }
  
  public int getInt(char arg) {
    Args.ArgumentMarshaler am = marsharlers.get(arg);
    try {
      return am == null ? 0 : (Integer)am.get();
    } catch (Exception e) {
      return 0;
    }
  }
  
  public double getDouble(char arg) {
    Args.ArgumentMarshaler am = marsharlers.get(arg);
    try {
      return am == null ? 0 : (Double)am.get();
    } catch (Exception e) {
      return 0.0;
    }
  }
  
  public boolean getBoolean(char arg) { 
    Args.ArgumentMarshaler am = marsharlers.get(arg);
    boolean b = false;
    // 若 get 的參數不為物件 會 exception
    try {
      b = am != null && (Boolean)am.get();
    } catch (ClassCastException e) {
      b = false;
    }
    return b;
  }
  
  public boolean has(char arg) { 
    return argsFound.contains(arg);
  }
  
  public boolean isValid() { 
    return valid;
  }
  
  private class ArgsException extends Exception {
  } 
}