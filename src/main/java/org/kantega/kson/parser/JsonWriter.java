package org.kantega.kson.parser;

import fj.data.List;
import fj.data.Stream;
import org.kantega.kson.json.JsonValue;

import java.math.BigDecimal;

public class JsonWriter {

  public static String write(JsonValue json) {
    return json.fold(
        JsonValue.Folder.foldWith("")
            .onNull(() -> "")
            .onBool(Object::toString)
            .onNumber(BigDecimal::toString)
            .onString(s -> "\"" + s + "\"")
            .onArray(arr -> arr.isEmpty() ? "[]" : "[" + arr.tail().foldLeft((sum, val) -> sum + "," + write(val), write(arr.head())) + "]")
            .onObject(obj -> mkString(obj.toList().map(pairs -> "\"" + pairs._1() + "\":" + write(pairs._2())), "{", ",", "}"))
    );
  }

  public static String writePretty(JsonValue json) {
    return writePretty(json, 0);
  }

  private static String writePretty(JsonValue json, int indent) {
    return json.fold(
        JsonValue.Folder.foldWith("")
            .onNull(() -> "")
            .onBool(Object::toString)
            .onNumber(BigDecimal::toString)
            .onString(s -> "\"" + s + "\"")
            .onArray(arr -> arr.isEmpty() ? "[]" : "[\n" + arr.tail().foldLeft((sum, val) -> sum + line(",") + indent(indent+2)+writePretty(val, indent+2), indent(indent+2) + writePretty(arr.head(), indent+2)) + "\n"+indent(indent) +"]")
            .onObject(obj -> mkString(obj.toList().map(pairs -> indent(indent+2)+"\"" + pairs._1() + "\":" + writePretty(pairs._2(), indent+2)), line("{"), ",\n", "\n"+indent(indent) + "}"))
    );
  }

  private static String mkString(List<String> vals, String pre, String delim, String post) {
    return vals.isEmpty()
        ? pre + post
        : pre + vals.tail().foldLeft((sum, v) -> sum + delim + v, vals.head()) + post;
  }

  private static String indent(int depth) {
    return Stream.asString(Stream.repeat(' ').take(depth));
  }

  private static String line(String l) {
    return l + "\n";
  }

}
