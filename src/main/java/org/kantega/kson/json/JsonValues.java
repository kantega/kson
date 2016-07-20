package org.kantega.kson.json;

import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;

import java.math.BigDecimal;

import static fj.data.Option.*;

public class JsonValues {

  private static final JsonNull jNull = new JsonNull();

  public static JsonValue jArray(List<JsonValue> vals){
    return new JsonArray(vals);
  }

  public static JsonValue jArray(JsonValue... vals){
    return jArray(List.arrayList(vals));
  }

  public static JsonValue jObj(List<P2<String,JsonValue>> fields){
    return JsonObject.JsonObject(fields);
  }

  public static JsonValue jObj(TreeMap<String, JsonValue> pairs){
    return new JsonObject(pairs);
  }

  public static JsonValue jObj(P2<String,JsonValue> ... fields){
    return jObj(List.arrayList(fields));
  }

  public static P2<String,JsonValue> field(String name, JsonValue value){
    return P.p(name,value);
  }

  public static JsonValue jNull(){
    return jNull;
  }

  public static JsonValue jString(String str){
    return new JsonString(str);
  }

  public static JsonValue jBool(boolean val){
    return new JsonBool(val);
  }

  public static JsonValue jNum(BigDecimal n){
    return new JsonNumber(n);
  }

  public static JsonValue jNum(long n){
    return new JsonNumber(BigDecimal.valueOf(n));
  }

  public static JsonValue jNum(double n){
    return new JsonNumber(BigDecimal.valueOf(n));
  }


}
