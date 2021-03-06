package org.kantega.kson.json;

import fj.Equal;
import fj.F;
import fj.Ord;
import fj.P2;
import fj.data.List;
import fj.data.Option;
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

    public JsonObject empty() {
        return empty;
    }

    public <T> Option<T> onObject(F<TreeMap<String, JsonValue>, T> f) {
        return Option.some(f.f(pairs));
    }

    public JsonObject update(F<TreeMap<String, JsonValue>, TreeMap<String, JsonValue>> f) {
        return new JsonObject(f.f(pairs));
    }

    public JsonObject withField(String name, JsonValue value) {
        return new JsonObject(pairs.set(name, value));
    }

    public Option<JsonValue> get(String fieldName) {
        return pairs.get(fieldName);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("JsonObject{");
        sb.append(pairs);
        sb.append('}');
        return sb.toString();
    }
}
