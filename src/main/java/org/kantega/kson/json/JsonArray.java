package org.kantega.kson.json;


import fj.Equal;
import fj.F;
import fj.data.List;
import fj.data.Option;

public class JsonArray extends JsonValue {

  public static final Equal<JsonArray> eq =
      Equal.listEqual(JsonValue.eq()).contramap(arr -> arr.values);

  public final List<JsonValue> values;

  public JsonArray(List<JsonValue> values) {
    this.values = values;
  }

  public <T> Option<T> onArray(F<List<JsonValue>, T> f) {
    return Option.some(f.f(values));
  }

  public JsonArray update(F<List<JsonValue>,List<JsonValue>> f){
    return new JsonArray(f.f(values));
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonArray{");
    sb.append(values);
    sb.append('}');
    return sb.toString();
  }
}
