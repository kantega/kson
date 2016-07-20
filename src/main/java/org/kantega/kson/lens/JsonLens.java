package org.kantega.kson.lens;

import fj.F;
import fj.F2;

public class JsonLens<S, A> {

  final F<S, LensResult<A>>     get;
  final F2<A, S, LensResult<S>> set;


  public JsonLens(F<S, LensResult<A>> get, F2<A, S, LensResult<S>> set) {
    this.get = get;
    this.set = set;
  }

  public LensResult<A> get(S value) {
    return get.f(value);
  }

  public F<S, LensResult<A>> get() {
    return this::get;
  }

  public LensResult<S> set(S origin, A value) {
    return set.f(value, origin);
  }

  public F<S, LensResult<S>> set(A value) {
    return origin -> set(origin, value);
  }

  public S setF(S origin, A value) {
    return set(origin, value).orElse(origin);
  }

  public F<S, S> setF(A value) {
    return origin -> setF(origin, value);
  }

  public LensResult<S> mod(S s, F<A, LensResult<A>> f) {
    return get(s).bind(f).bind(a -> set(s, a));
  }

  public F<S, LensResult<S>> mod(F<A, LensResult<A>> f) {
    return s -> mod(s,f);
  }

  public S modF(S origin, F<A, A> f) {
    return mod(origin, a -> LensResult.success(f.f(a))).orElse(origin);
  }

  public F<S, S> modF(F<A, A> f) {
    return origin -> modF(origin,f);
  }


  /**
   * Change the type of the "inner" value
   *
   * @param f
   * @param g
   * @param <B>
   * @return
   */
  public <B> JsonLens<S, B> xmap(F<A, LensResult<B>> f, F<B, LensResult<A>> g) {
    return new JsonLens<>(
        s -> get(s).bind(f),
        (b, s) -> g.f(b).bind(a -> set(s, a))
    );
  }

}
