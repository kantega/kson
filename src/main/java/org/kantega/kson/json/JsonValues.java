package org.kantega.kson.json;

import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Option;

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

  public static P2<String,JsonValue> field(String name, JsonValue value){
    return P.p(name,value);
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

  public static Option<String> asText(JsonValue value){
    return value.fold(JsonValue.Fold.foldWith(Option.<String>none()).onString(Option::some));
  }

  public static Option<BigDecimal> asNumber(JsonValue value){
    return value.fold(JsonValue.Fold.foldWith(Option.<BigDecimal>none()).onNumber(Option::some));
  }

  public static Option<JsonValue> getField(JsonValue value,String field){
    return value.fold(JsonValue.Fold.foldWith(Option.<JsonValue>none()).onObject(m->m.get(field)));
  }

  public static Option<String> getFieldAsText(JsonValue value,String field){
    return getField(value,field).bind(JsonValues::asText);
  }

  public static Option<BigDecimal> getFieldAsNumber(JsonValue value,String field){
    return getField(value,field).bind(JsonValues::asNumber);
  }

}
