private class BooleanArgumentMarshaler extends ArgumentMarshaler {
  private boolean booleanValue = false;
  
  public void set(Iterator<String> currentArgument) throws ArgsException {
    booleanValue = true;
  }
  
  public Object get() {
    return booleanValue;
  }
}