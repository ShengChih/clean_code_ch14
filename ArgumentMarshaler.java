private interface class ArgumentMarshaler {
  public void set(Iterator<String> currentArgument)
                       throws ArgsException;
  public Object get();
}