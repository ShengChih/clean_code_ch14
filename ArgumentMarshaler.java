// 由於作者想支援新的型別參數
// 在粗稿 14-9 需大幅更動多個地方
// 想將管理參數型態的功能獨立為一個類別，以達到 "新增型別" 方便

// 重構

private abstract class ArgumentMarshaler { 
  
  private int integerValue;
  
  
  public boolean getBoolean() {
	  return booleanValue;
  } 
  
  public void setString(String s) { 
    stringValue = s;
  }
  
  public String getString() {
	  return stringValue == null ? "" : stringValue;
  }
  
  public void setInteger(int i) { 
    integerValue = i;
  }
  
  public int getInteger() {
	  return integerValue;
  }
  
  // setString, setInteger 需要 String s
  public abstract void set(String s);
  public abstract Object get();
}

private class BooleanArgumentMarshaler extends ArgumentMarshaler {
  private boolean booleanValue = false;
  
  public void set(String s) {
      booleanValue = true;
  }
  
  public Object get() {
    return booleanValue;
  }
}
private class StringArgumentMarshaler extends ArgumentMarshaler {
  private String stringValue;
  
  public void set(String s) {
      stringValue = true;
  }
  
  public Object get() {
    return stringValue;
  }
}
private class IntegerArgumentMarshaler extends ArgumentMarshaler {
  public void set(String s) {
      
  }
  
  public Object get() {
    return null;
  }
}