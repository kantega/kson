package org.kantega.kson.parser;

import java.util.function.Supplier;

public class ParseFailure {

  final Supplier<String> message;

  public ParseFailure(Supplier<String> message) {
    this.message = message;
  }


  public String getMessage() {
    return message.get();
  }
}
