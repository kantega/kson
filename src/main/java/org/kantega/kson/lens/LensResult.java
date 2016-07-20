package org.kantega.kson.lens;

import fj.F;
import fj.data.Validation;

/**
 * A thin wrapper around Validation&lparen;String,A>
 * @param <A>
 */
public class LensResult<A> {

  final Validation<String,A> validation;

  private LensResult(Validation<String, A> validation) {
    this.validation = validation;
  }
  public static <A> LensResult<A> fail(String msg){
    return new LensResult<>(Validation.fail(msg));
  }

  public static <A> LensResult<A> success(A a){
    return new LensResult<>(Validation.success(a));
  }

  public <B> LensResult<B> mod(F<Validation<String,A>,Validation<String,B>> f){
    return new LensResult<>(f.f(validation));
  }

  public <B> LensResult<B> map(F<A,B> f){
    return mod(v->v.map(f));
  }

  public <B> LensResult<B> bind(F<A,LensResult<B>> f){
    return mod(v->v.map(f).bind(v2->v2.validation));
  }

  public A orElse(A a){
    return validation.validation(f->a,aa->aa);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("LensResult{");
    sb.append(validation.validation(f->f, Object::toString));
    sb.append('}');
    return sb.toString();
  }
}
