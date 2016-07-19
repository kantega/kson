package org.kantega.kson.json;


import fj.Equal;
import fj.data.List;

public class JsonArray extends JsonValue {

  public static final Equal<JsonArray> eq =
      Equal.listEqual(JsonValue.eq()).contramap(arr -> arr.values);

  public final List<JsonValue> values;

  public JsonArray(List<JsonValue> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonArray{");
    sb.append(values);
    sb.append('}');
    return sb.toString();
  }
}
