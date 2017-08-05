import java.text.ParseException; 
import java.util.*;

// 混亂程式 是逐漸產生的
// 重構 => 漸進整理程式碼
// 經 TDD 測試及驗證 確保 重構後，程式能正確進行

public class Args {
  // 一堆變數, 沒有單一職責
  // 變數命名過長
  private String schema;
  private String[] args;
  private boolean valid = true;
  private Set<Character> unexpectedArguments = new TreeSet<Character>(); 
  // 沒有抽象化
  private Map<Character, ArgumentMarshaler> booleanArgs = new HashMap<Character, ArgumentMarshaler>();
  private Map<Character, ArgumentMarshaler> stringArgs = new HashMap<Character, ArgumentMarshaler>(); 
  private Map<Character, ArgumentMarshaler> intArgs = new HashMap<Character, ArgumentMarshaler>(); 
  
  private Set<Character> argsFound = new HashSet<Character>();
  private int currentArgument;
  private char errorArgumentId = '\0';
  
  // 看不懂 "TILT"
  private String errorParameter = "TILT";
  private ErrorCode errorCode = ErrorCode.OK;
  
  // 運用 try catch block
  // parse, setIntArg, setStringArg
  
  private enum ErrorCode {
    OK, MISSING_STRING, MISSING_INTEGER, INVALID_INTEGER, UNEXPECTED_ARGUMENT
  }
    
  public Args(String schema, String[] args) throws ParseException { 
    this.schema = schema;
    this.args = args;
    valid = parse();
  }
  
  private boolean parse() throws ParseException { 
    if (schema.length() == 0 && args.length == 0)
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
	// parseBooleanSchemaElement, parseStringSchemaElement, parseIntegerSchemaElement 應合併
    if (isBooleanSchemaElement(elementTail)) 
      parseBooleanSchemaElement(elementId);
    else if (isStringSchemaElement(elementTail)) 
      parseStringSchemaElement(elementId);
    else if (isIntegerSchemaElement(elementTail)) 
      parseIntegerSchemaElement(elementId);
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
  
  // 抽象化 可合併 parseBooleanSchemaElement, parseBooleanSchemaElement, parseStringSchemaElement
  private void parseBooleanSchemaElement(char elementId) { 
    booleanArgs.put(elementId, new BooleanArgumentMarshaler());
  }
  
  private void parseIntegerSchemaElement(char elementId) { 
    intArgs.put(elementId, new IntegerArgumentMarshaler());
  }
  
  // 合併
  private void parseStringSchemaElement(char elementId) { 
    stringArgs.put(elementId, new StringArgumentMarshaler());
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
    for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
      String arg = args[currentArgument];
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
    if (isBooleanArg(argChar))
      setBooleanArg(argChar); 
    else if (isStringArg(argChar))
      setStringArg(argChar); 
    else if (isIntArg(argChar))
      setIntArg(argChar); 
    else
      return false;
    
    return true; 
  }
  
  private boolean isIntArg(char argChar) {
    return intArgs.containsKey(argChar);
  }
  
  
  private void setIntArg(char argChar) throws ArgsException { 
    currentArgument++;
    String parameter = null;
    try {
      parameter = args[currentArgument];
      intArgs.get(argChar).set(parameter); 
    } catch (ArgsException e) {
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
  
  private void setStringArg(char argChar) throws ArgsException { 
    currentArgument++;
    try {
      stringArgs.get(argChar).set(args[currentArgument]); 
    } catch (ArrayIndexOutOfBoundsException e) {
      valid = false;
      errorArgumentId = argChar;
      errorCode = ErrorCode.MISSING_STRING; 
      throw new ArgsException();
    } 
  }
  
  private boolean isStringArg(char argChar) { 
    return stringArgs.containsKey(argChar);
  }
  
  private void setBooleanArg(char argChar) { 
    try {
      booleanArgs.get(argChar).set("true");
    } catch (ArgsException e) {
      
    }
  }
  
  private boolean isBooleanArg(char argChar) { 
    return booleanArgs.containsKey(argChar);
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
    Args.ArgumentMarshaler am = stringArgs.get(arg);
    return am == null ? "" : (String)am.get();
  }
  
  public int getInt(char arg) {
    Args.ArgumentMarshaler am = intArgs.get(arg);
    return am == null ? 0 : (Integer)am.get();
  }
  
  public boolean getBoolean(char arg) { 
    Args.ArgumentMarshaler am = booleanArgs.get(arg);
    return am != null && (Boolean)am.get();
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