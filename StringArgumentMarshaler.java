private class StringArgumentMarshaler extends ArgumentMarshaler {
  private String stringValue;
  
  public void set(Iterator<String> currentArgument) throws ArgsException {
    try {
      stringValue = currentArgument.next(); 
    } catch (NoSuchElementException e) {
      errorArgumentId = argChar;
      errorCode = ErrorCode.MISSING_STRING; 
      throw new ArgsException();
    } 
  }
  
  public Object get() {
    return stringValue;
  }
}