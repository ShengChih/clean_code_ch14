package com.objectmentor.utilities.args;

import static com.objectmentor.utilities.args.ArgsException.ErrorCode.*; 
import java.util.*;

public class Args {
  /**
   *  @params Map<Character, ArgumentMarshaler> marshalers 放置類別實例
   *	ArgumentMarshaler (interface)
   *	ArgumentMarshaler bool_args = new BooleanArgumentMarshaler();
   * 	ArgumentMarshaler int_args = new IntegerArgumentMarshaler();
   *	...
   *    marshalers.put("l", bool_args);
   *	marshalers.put("p", int_args);
   *	...
   *  @params Set<Character> argsFound 符合 schema 定義規則的參數
   *
   *
   */
  private Map<Character, ArgumentMarshaler> marshalers;
  private Set<Character> argsFound;
  private ListIterator<String> currentArgument;
  
  /**
   *  @params String schema "l,p#,d*" => { -l: boolean, -p: integer, -d: string}
   *  @params String[] args (java main.java -l true -p 10 -d string_args) => e.g. args[] = ["-l", "true", "-p", "10", "-d", "string_args"]
   *
   */
  public Args(String schema, String[] args) throws ArgsException {
	// 初始化
    marshalers = new HashMap<Character, ArgumentMarshaler>(); 
    argsFound = new HashSet<Character>();
    
	// 定義規則初始化
    parseSchema(schema);
	// 
    parseArgumentStrings(Arrays.asList(args)); // Arrays static function 元素轉 List (array args => list)
  }
  
  /**
   *  以","分割字串，根據 schema 定義型態初始化 marshalers
   *  @params String schema "l,p#,d*"
   */
  private void parseSchema(String schema) throws ArgsException { 
    for (String element : schema.split(",")) {
      if (element.length() > 0) { 
        parseSchemaElement(element.trim()); // ["l", "p#", "d*"]
	    }
    }
  }
  
  /**
   *  根據 schema 決定 new 什麼 instance 至 marshalers (類似工廠模式)
   *  @params String element => ["l", "p#", "d*"]
   */
  private void parseSchemaElement(String element) throws ArgsException { 
    char elementId = element.charAt(0); // ["l", "p#", "d*"] => [l, p, d]
    String elementTail = element.substring(1); //["l", "p#", "d*"] => ["", "#", "*"]
	
	validateSchemaElementId(elementId);
	
    if (elementTail.length() == 0)
      marshalers.put(elementId, new BooleanArgumentMarshaler());
    else if (elementTail.equals("*")) 
      marshalers.put(elementId, new StringArgumentMarshaler());
    else if (elementTail.equals("#"))
      marshalers.put(elementId, new IntegerArgumentMarshaler());
    else if (elementTail.equals("##")) 
      marshalers.put(elementId, new DoubleArgumentMarshaler());
    else if (elementTail.equals("[*]"))
      marshalers.put(elementId, new StringArrayArgumentMarshaler());
    else
      throw new ArgsException(INVALID_ARGUMENT_FORMAT, elementId, elementTail);
  }
  
  /**
   *  判斷是否為字母
   *  @params char elementId => e.g. [l, p, d]
   */
  private void validateSchemaElementId(char elementId) throws ArgsException { 
    if (!Character.isLetter(elementId))
      throw new ArgsException(INVALID_ARGUMENT_NAME, elementId, null); 
  }
  
  /**
   *  處理 args[] 變數
   *  @params List<String> argsList => e.g. "-l" -> "true" -> "-p" -> "10" -> "-d" -> "string_args"
   *
   */
  private void parseArgumentStrings(List<String> argsList) throws ArgsException {
    for (currentArgument = argsList.listIterator(); currentArgument.hasNext();) {
      String argString = currentArgument.next(); 
      if (argString.startsWith("-")) { // 是否 "-" 作為起始字串
        parseArgumentCharacters(argString.substring(1)); // "l" -> "p" -> "d"
      } else {
        currentArgument.previous(); // 非 - 表示 參數錯誤, 不處理之後的參數
        break; 
      }
    } 
  }
  
  /**
   *
   *  @params String argChars e.g. "l" -> '"p" -> "d"
   *
   */
  private void parseArgumentCharacters(String argChars) throws ArgsException { 
    for (int i = 0; i < argChars.length(); i++)
      parseArgumentCharacter(argChars.charAt(i)); 
  }
  
  /**
   *
   *  @params argChar "l" -> "p" -> "d"
   *
   */
  private void parseArgumentCharacter(char argChar) throws ArgsException { 
    ArgumentMarshaler m = marshalers.get(argChar); // 取出對應的實例 bool, int, string
    if (m == null) {
      throw new ArgsException(UNEXPECTED_ARGUMENT, argChar, null); 
    } else {
      argsFound.add(argChar); // match 放入 <Set> argsFound
      try {
        m.set(currentArgument); // 讀取後頭的參數
		    // 實作 interface ArgumentMarshaler.set; 傳物件 currentArgument 進去做操作 => 相依性注入(Dependency Injection) 去耦合
        // 減少多個參數
      } catch (ArgsException e) {
        e.setErrorArgumentId(argChar);
        throw e; 
      }
    } 
  }
  
  /**
   *  檢查 Set 是否有用到參數 arg
   *  @params arg
   *  @return boolean
   */
  public boolean has(char arg) { 
    return argsFound.contains(arg);
  }
  
  /**
   *  下一位
   */
  public int nextArgument() {
    return currentArgument.nextIndex();
  }
  
  /**
   *  轉型用 => 由於  marshalers.get(arg) 得到的物件都會是 ArgumentMarshaler
   *  BooleanArgumentMarshaler.getValue 會強制將marshalers.get(arg) ArgumentMarshaler 轉型為 BooleanArgumentMarshaler, 並操作
   *  @parms Char arg => e.g. "l", "d", "p"
   *  @return Boolean
   */
  public boolean getBoolean(char arg) {
    return BooleanArgumentMarshaler.getValue(marshalers.get(arg));
  }
  
  public String getString(char arg) {
    return StringArgumentMarshaler.getValue(marshalers.get(arg));
  }
  
  public int getInt(char arg) {
    return IntegerArgumentMarshaler.getValue(marshalers.get(arg));
  }
  
  public double getDouble(char arg) {
    return DoubleArgumentMarshaler.getValue(marshalers.get(arg));
  }
  
  public String[] getStringArray(char arg) {
    return StringArrayArgumentMarshaler.getValue(marshalers.get(arg));
  } 
}
