package org.kantega.kson.lens;

import fj.F;
import fj.F2;
import fj.data.Validation;

public class JsonLens<S, A> {

  final F<S, Validation<String, A>>     get;
  final F2<A, S, Validation<String, S>> set;


  public JsonLens(F<S, Validation<String, A>> get, F2<A, S, Validation<String, S>> set) {
    this.get = get;
    this.set = set;
  }

  public Validation<String, A> get(S value) {
    return get.f(value);
  }

  public Validation<String, S> set(S origin, A value) {
    return set.f(value, origin);
  }

  public Validation<String, S> mod(S s, F<A, Validation<String,A>> f) {
    Validation<String, A> aV = get(s).bind(f);
    return aV.bind(a -> set(s, a));
  }

  public <B> JsonLens<S, B> compose(JsonLens<A, B> other) {
    return new JsonLens<>(
        s -> get(s).bind(a -> other.get(a)),
        (b, s) -> mod(s,a->other.set(a,b))
    );
  }
}
