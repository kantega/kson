package org.kantega.kson.json;

import fj.Equal;
import fj.Ord;
import fj.P2;
import fj.data.List;
import fj.data.TreeMap;

public class JsonObject extends JsonValue {

  public final Equal<JsonObject> eq =
      Equal.treeMapEqual(Equal.stringEqual, JsonValue.eq()).contramap(obj -> obj.pairs);

  public final TreeMap<String, JsonValue> pairs;

  public JsonObject(TreeMap<String, JsonValue> pairs) {
    this.pairs = pairs;
  }

  public static JsonObject JsonObject(List<P2<String, JsonValue>> vals) {
    return new JsonObject(TreeMap.iterableTreeMap(Ord.stringOrd, vals));
  }

}
