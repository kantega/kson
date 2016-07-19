package org.kantega.kson.example;

import fj.P;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;
import org.kantega.kson.parser.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

public class MicroBenchmark {

  public static void main(String[] args) {


    InputStream    in     = MicroBenchmark.class.getResourceAsStream("/rap.json");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    Stream<Character> characters = Stream.unfold(r -> {
      try {
        String line = r.readLine();
        return
            line != null ?
                Option.some(P.p(Stream.fromString(line), r)) :
                Option.none();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }, reader).bind(line -> line);

    String json = Stream.asString(characters);



    Instant now = Instant.now();

    List<String> results =
        Stream.range(0,200).map(i -> JsonParser.parse(json).validation(f -> f, s -> "Success")).toList();

    Instant end = Instant.now();

    System.out.print(results + "\nElapsed :" + (Duration.between(now, end).toMillis() / results.length()));

  }
}
