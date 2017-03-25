package org.kantega.kson.parser;

public class ParseFailure extends RuntimeException {


  public final int offset;
  public final int line;
  public final int i;

  public ParseFailure(String message, int offset, int line, int i) {
    super(message);
    this.offset = offset;
    this.line = line;
    this.i = i;
  }



}
