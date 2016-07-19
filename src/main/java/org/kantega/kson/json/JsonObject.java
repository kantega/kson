package org.kantega.kson.json;

import fj.Equal;
import fj.Ord;
import fj.P2;
import fj.data.List;
import fj.data.TreeMap;

public class JsonObject extends JsonValue {

  public static final JsonObject empty =
      JsonObject(List.nil());

  public static final Equal<JsonObject> eq =
      Equal.treeMapEqual(Equal.stringEqual, JsonValue.eq()).contramap(obj -> obj.pairs);

  public final TreeMap<String, JsonValue> pairs;

  public JsonObject(TreeMap<String, JsonValue> pairs) {
    this.pairs = pairs;
  }

  public static JsonObject JsonObject(List<P2<String, JsonValue>> vals) {
    return new JsonObject(TreeMap.iterableTreeMap(Ord.stringOrd, vals));
  }


  public JsonObject withField(String name, JsonValue value) {
    return new JsonObject(pairs.set(name, value));
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonObject{");
    sb.append(pairs);
    sb.append('}');
    return sb.toString();
  }
}
