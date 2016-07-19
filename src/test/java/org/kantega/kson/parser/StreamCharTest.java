package org.kantega.kson.parser;

import fj.data.Stream;

public class StreamCharTest {


  public static void main(String[] args) {
    Stream<Character> chars = Stream.arrayStream(args).map(string->string.charAt(0));
    //Stream<Character> chars = Stream.arrayStream('s','\\','n','l');//Stream.fromString("Dette er \t linjeskift");


    chars.foreachDoEffect(character -> System.out.println("Char "+character.toString()));
  }
}
