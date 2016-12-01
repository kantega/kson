package org.kantega.kson;

import fj.F;
import fj.F0;
import fj.data.List;
import fj.data.Validation;

import java.util.function.Supplier;

/**
 * A thin wrapper around Validation&lparen;String,A>
 *
 * @param <A>
 */
public class JsonResult<A> {

    final Validation<String, A> validation;

    private JsonResult(Validation<String, A> validation) {
        this.validation = validation;
    }

    public static <A> JsonResult<A> fail(String msg) {
        return new JsonResult<>(Validation.fail(msg));
    }

    public static <A> JsonResult<A> success(A a) {
        return new JsonResult<>(Validation.success(a));
    }

    public static <A> JsonResult<A> fromValidation(Validation<String, A> validation) {
        return new JsonResult<>(validation);
    }

    public Validation<String, A> toValidation() {
        return validation;
    }

    public static <A> JsonResult<List<A>> sequence(List<JsonResult<A>> results) {
        return results.foldLeft(
          (memo, result) -> result.bind(a -> memo.map(list -> list.cons(a))),
          JsonResult.success(List.<A>nil())).map(List::reverse);
    }

    public <B> JsonResult<B> mod(F<Validation<String, A>, Validation<String, B>> f) {
        return new JsonResult<>(f.f(validation));
    }

    public <T> T fold(F<String, T> onError, F<A, T> onSuccess) {
        return validation.validation(onError, onSuccess);
    }

    public JsonResult<A> mapFail(F<String, String> f) {
        return mod(validation -> validation.f().map(f));
    }

    public <B> JsonResult<B> map(F<A, B> f) {
        return mod(v -> v.map(f));
    }

    public <B> JsonResult<B> bind(F<A, JsonResult<B>> f) {
        return mod(v -> v.map(f).bind(v2 -> v2.validation));
    }

    public A orElse(F0<A> a) {
        return validation.validation(f -> a.f(), aa -> aa);
    }

    public JsonResult<A> orResult(F0<JsonResult<A>> other) {
        return bind(u -> other.f());
    }

    public A orThrow(){
        return orThrow(RuntimeException::new);
    }

    public A orThrow(F<String, ? extends RuntimeException> supplier) {
        if (validation.isFail())
            throw supplier.f(validation.fail());
        else return validation.success();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("JsonResult{");
        sb.append(validation.validation(f -> f, Object::toString));
        sb.append('}');
        return sb.toString();
    }
}
