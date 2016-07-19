package org.kantega.kson.json;

import fj.P2;
import fj.data.List;

import java.math.BigDecimal;

public class JsonValues {

  public static JsonArray jArray(List<JsonValue> vals){
    return new JsonArray(vals);
  }

  public static JsonArray jArray(JsonValue... vals){
    return jArray(List.arrayList(vals));
  }

  public static JsonObject jObj(List<P2<String,JsonValue>> fields){
    return JsonObject.JsonObject(fields);
  }

  public static JsonObject jObj(P2<String,JsonValue> ... fields){
    return jObj(List.arrayList(fields));
  }

  public static JsonNull jNull(){
    return new JsonNull();
  }

  public static JsonString jString(String str){
    return new JsonString(str);
  }

  public static JsonBool jBool(boolean val){
    return new JsonBool(val);
  }

  public static JsonNumber jNum(BigDecimal n){
    return new JsonNumber(n);
  }

  public static JsonNumber jNum(long n){
    return new JsonNumber(BigDecimal.valueOf(n));
  }

  public static JsonNumber jNum(double n){
    return new JsonNumber(BigDecimal.valueOf(n));
  }
}
