package org.kantega.kson;

import fj.F;
import fj.data.Validation;

/**
 * A thin wrapper around Validation&lparen;String,A>
 * @param <A>
 */
public class JsonResult<A> {

  final Validation<String,A> validation;

  private JsonResult(Validation<String, A> validation) {
    this.validation = validation;
  }
  public static <A> JsonResult<A> fail(String msg){
    return new JsonResult<>(Validation.fail(msg));
  }

  public static <A> JsonResult<A> success(A a){
    return new JsonResult<>(Validation.success(a));
  }

  public <B> JsonResult<B> mod(F<Validation<String,A>,Validation<String,B>> f){
    return new JsonResult<>(f.f(validation));
  }

  public <B> JsonResult<B> map(F<A,B> f){
    return mod(v->v.map(f));
  }

  public <B> JsonResult<B> bind(F<A,JsonResult<B>> f){
    return mod(v->v.map(f).bind(v2->v2.validation));
  }

  public A orElse(A a){
    return validation.validation(f->a,aa->aa);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonResult{");
    sb.append(validation.validation(f->f, Object::toString));
    sb.append('}');
    return sb.toString();
  }
}
