private interface class ArgumentMarshaler {
  public void set(Iterator<String> currentArgument)
                       throws ArgsException;
  public Object get();
}

private class BooleanArgumentMarshaler extends ArgumentMarshaler {
  private boolean booleanValue = false;
  
  public void set(Iterator<String> currentArgument) throws ArgsException {
    booleanValue = true;
  }
  
  public Object get() {
    return booleanValue;
  }
}
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

private class IntegerArgumentMarshaler extends ArgumentMarshaler {
  private int intValue = 0;

  
  public void set(Iterator<String> currentArgument) throws ArgsException {
    String parameter = null;
    try {
      parameter = currentArgument.next();
      intValue = Integer.parseInt(parameter);
    } catch (NoSuchElementException e) {
      errorCode = ErrorCode.MISSING_INTEGER;
      throw e;
    } catch (NumberFormatException e) {
      errorParameter = parameter;
      errorCode = ErrorCode.INVALID_INTEGER; 
      throw new ArgsException();
    }
  }
  
  public Object get() {
    return intValue;
  }
}

private class DoubleArgumentMarshaler extends ArgumentMarshaler {
  private int doubleValue = 0;
  
  public void set(Iterator<String> currentArgument) throws ArgsException {
    String parameter = null;
    try {
      parameter = currentArgument.next();
      doubleValue = Double.parseInt(parameter);
    } catch (NoSuchElementException e) {
      errorCode = ErrorCode.MISSING_INTEGER;
      throw e;
    } catch (NumberFormatException e) {
      errorParameter = parameter;
      errorCode = ErrorCode.INVALID_INTEGER; 
      throw new ArgsException();
    }
  }
  
  public Object get() {
    return doubleValue;
  }
}