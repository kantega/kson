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

  @SafeVarargs
  public static JsonValue jObj(P2<String,JsonValue> ... fields){
    return jObj(List.arrayList(fields));
  }

  public static P2<String,JsonValue> field(String name, JsonValue value){
    return P.p(name,value);
  }

  public static P2<String,JsonValue> field(String name, String value){
    return field(name,jString(value));
  }

  public static P2<String,JsonValue> field(String name, long value){
    return field(name,jNum(value));
  }

  public static P2<String,JsonValue> field(String name, double value){
    return field(name,jNum(value));
  }

  public static P2<String,JsonValue> field(String name, BigDecimal value){
    return field(name,jNum(value));
  }

  public static P2<String,JsonValue> field(String name, boolean value){
    return field(name,jBool(value));
  }

  public static JsonValue jNull(){
    return jNull;
  }

  public static JsonValue jString(String str){
    return new JsonString(str);
  }

  final static JsonValue jTrue = jBool(true);
  final static JsonValue jFalse = jBool(false);

  public static JsonValue jBool(boolean val){
    return val ? jTrue : jFalse;
  }

  public static JsonValue jNum(BigDecimal n){
    return new JsonNumber(n.toString());
  }

  public static JsonValue jNum(long n){
    return new JsonNumber(String.valueOf(n));
  }

  public static JsonValue jNum(double n){
    return new JsonNumber(String.valueOf(n));
  }


}
