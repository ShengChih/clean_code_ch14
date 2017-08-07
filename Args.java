import java.text.ParseException; 
import java.util.*;

// 混亂程式 是逐漸產生的
// 重構 => 漸進整理程式碼
// 經 TDD 測試及驗證 確保 重構後，程式能正確進行

public class Args {
  // 一堆變數, 沒有單一職責
  // 變數命名過長
  private String schema;
  private boolean valid = true;
  private Set<Character> unexpectedArguments = new TreeSet<Character>(); 

  private Map<Character, ArgumentMarshaler> marshaler = new HashMap<Character, ArgumentMarshaler>();
  
  private Set<Character> argsFound = new HashSet<Character>();
  private int currentArgument;
  private char errorArgumentId = '\0';
  
  // 看不懂 "TILT"
  private String errorParameter = "TILT";
  private ErrorCode errorCode = ErrorCode.OK;
  
  private List<String> argsList;
  
  // 運用 try catch block
  // parse, setIntArg, setStringArg
  
  private enum ErrorCode {
    OK, MISSING_STRING, MISSING_INTEGER, INVALID_INTEGER, UNEXPECTED_ARGUMENT
  }
    
  public Args(String schema, String[] args) throws ParseException { 
    this.schema = schema;
    argsList = Arrays.asList(args);
    valid = parse();
  }
  
  private boolean parse() throws ParseException { 
    if (schema.length() == 0 && argsList.size == 0)
      return true; 
    parseSchema(); 
    try {
      parseArguments();
    } catch (ArgsException e) {
    }
    return valid; // default true ?
  }
  
  private boolean parseSchema() throws ParseException { // 這段僅 多了 String trimmedElement
    for (String element : schema.split(",")) {
      if (element.length() > 0) {
        String trimmedElement = element.trim(); 
        parseSchemaElement(trimmedElement);
      } 
    }
    return true; 
  }
  
  private void parseSchemaElement(String element) throws ParseException { 
    char elementId = element.charAt(0);
    String elementTail = element.substring(1); 
    validateSchemaElementId(elementId);
	
	// isBooleanSchemaElement, isStringSchemaElement, isIntegerSchemaElement 應合併
  // 移除 parseXXXSchemaElement
    if (isBooleanSchemaElement(elementTail)) 
      marshalers.put(elementId, new BooleanArgumentMarshaler());
    else if (isStringSchemaElement(elementTail)) 
      marshalers.put(elementId, new StringArgumentMarshaler());
    else if (isIntegerSchemaElement(elementTail)) 
      marshalers.put(elementId, new IntegerArgumentMarshaler());
    else
	  // Exception 應單一職責 統一內容錯誤訊息
      throw new ParseException(String.format("Argument: %c has invalid format: %s.", 
        elementId, elementTail), 0);
    } 
  }
    
  private void validateSchemaElementId(char elementId) throws ParseException { 
    if (!Character.isLetter(elementId)) {
	  // Exception 應單一職責 統一內容錯誤訊息
      throw new ParseException("Bad character:" + elementId + "in Args format: " + schema, 0);
    }
  }
  
  private boolean isStringSchemaElement(String elementTail) { 
    return elementTail.equals("*");
  }
  
  private boolean isBooleanSchemaElement(String elementTail) { 
    return elementTail.length() == 0;
  }
  
  private boolean isIntegerSchemaElement(String elementTail) { 
    return elementTail.equals("#");
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
    try { // 
      if (m instanceof BooleanArgumentMarshaler)
        m.set(currentArgument);
      else if (m instanceof StringArgumentMarshaler)
        setStringArg(m);
      else if (m instanceof IntegerArgumentMarshaler))
        setIntArg(m);
    } catch (ArgsException e) {
      valid = false;
      errorArgumentId = argChar;
      throw e;
    }
    return true; 
  }
  
  /**
   *  省略 setXXXArg
   *
   */
  private void setIntArg(ArgumentMarshaler m) throws ArgsException { 
    String parameter = null;
    try {
      parameter = currentArgument.next();
      m.set(parameter);  // (2)
    } catch (NoSuchElementException e) {
      valid = false;
      errorArgumentId = argChar;
      errorCode = ErrorCode.MISSING_INTEGER;
      throw e;
    } catch (NumberFormatException e) {
      valid = false;
      errorArgumentId = argChar; 
      errorParameter = parameter;
      errorCode = ErrorCode.INVALID_INTEGER; 
      throw new ArgsException();
    } 
  }
  
  private void setStringArg(ArgumentMarshaler m) throws ArgsException { 
    currentArgument++;
    try {
      m.set(currentArgument.next()); 
    } catch (NoSuchElementException e) {
      valid = false;
      errorArgumentId = argChar;
      errorCode = ErrorCode.MISSING_STRING; 
      throw new ArgsException();
    } 
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
  
  public String errorMessage() throws Exception { 
    switch (errorCode) {
      case OK:
        throw new Exception("TILT: Should not get here.");
      case UNEXPECTED_ARGUMENT:
        return unexpectedArgumentMessage();
      case MISSING_STRING:
        return String.format("Could not find string parameter for -%c.", errorArgumentId);
      case INVALID_INTEGER:
        return String.format("Argument -%c expects an integer but was '%s'.", errorArgumentId, errorParameter);
      case MISSING_INTEGER:
        return String.format("Could not find integer parameter for -%c.", errorArgumentId);
    }
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