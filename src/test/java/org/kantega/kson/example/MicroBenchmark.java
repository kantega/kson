package org.kantega.kson.example;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import fj.P;
import fj.data.List;
import fj.data.Option;
import fj.data.Stream;
import org.kantega.kson.JsonResult;
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





    int iters = 10;
    Instant now2 = Instant.now();

    System.out.println("Start Simplejson at"+now2);

    List<JsonValue> results2 =
      Stream.range(0,iters).map(i->Json.parse(json)).toList();

    Instant end2 = Instant.now();

    System.out.println("Simplejson end  at"+end2);

    System.out.println("\nSimplejson elapsed :" + (Duration.between(now2, end2).toMillis() / results2.length()));

    Instant now = Instant.now();
    System.out.println("Kson start at"+now);
    List<JsonResult<org.kantega.kson.json.JsonValue>> results =
        Stream.range(0,iters).map(i -> JsonParser.parse(json)).toList();

    Instant end = Instant.now();

    System.out.println("End at"+end);

    System.out.println("\nElapsed :" + (Duration.between(now, end).toMillis() / results.length()));




  }
}
