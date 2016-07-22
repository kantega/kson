package org.kantega.kson.util;

import fj.*;

import static fj.P.*;

/**
 * Helper functions to convert tuples of hig arity to tuples of tuple2
 */
public class Products {

  public static <A, B, C> P2<A, P2<B, C>> expand(P3<A, B, C> p) {
    return p(p._1(), p(p._2(), p._3()));
  }

  public static <A, B, C> P3<A, B, C> flatten3(P2<A, P2<B, C>> n) {
    return p(n._1(), n._2()._1(), n._2()._2());
  }

  public static <A, B, C, D> P2<A, P2<B, P2<C, D>>> expand(P4<A, B, C, D> p) {
    return p(p._1(), expand(p(p._2(), p._3(), p._4())));
  }

  public static <A, B, C, D> P4<A, B, C, D> flatten4(P2<A, P2<B, P2<C, D>>> n) {
    P3<B, C, D> t = flatten3(n._2());
    return p(n._1(), t._1(), t._2(), t._3());
  }

  public static <A, B, C, D, E> P2<A, P2<B, P2<C, P2<D, E>>>> expand(P5<A, B, C, D, E> p) {
    return p(p._1(), expand(p(p._2(), p._3(), p._4(), p._5())));
  }

  public static <A, B, C, D, E> P5<A, B, C, D, E> flatten5(P2<A, P2<B, P2<C, P2<D, E>>>> n) {
    P4<B, C, D, E> t = flatten4(n._2());
    return p(n._1(), t._1(), t._2(), t._3(), t._4());
  }

  public static <A, B, C, D, E, FF> P2<A, P2<B, P2<C, P2<D, P2<E, FF>>>>> expand(P6<A, B, C, D, E, FF> p) {
    return p(p._1(), expand(p(p._2(), p._3(), p._4(), p._5(), p._6())));
  }

  public static <A, B, C, D, E, FF> P6<A, B, C, D, E, FF> flatten6(P2<A, P2<B, P2<C, P2<D, P2<E, FF>>>>> n) {
    P5<B, C, D, E, FF> t = flatten5(n._2());
    return p(n._1(), t._1(), t._2(), t._3(), t._4(), t._5());
  }

  public static <A, B, C, D, E, FF, G> P2<A, P2<B, P2<C, P2<D, P2<E, P2<FF, G>>>>>> expand(P7<A, B, C, D, E, FF, G> p) {
    return p(p._1(), expand(p(p._2(), p._3(), p._4(), p._5(), p._6(), p._7())));
  }

  public static <A, B, C, D, E, FF, G> P7<A, B, C, D, E, FF, G> flatten7(P2<A, P2<B, P2<C, P2<D, P2<E, P2<FF, G>>>>>> n) {
    P6<B, C, D, E, FF, G> t = flatten6(n._2());
    return p(n._1(), t._1(), t._2(), t._3(), t._4(), t._5(), t._6());
  }

  public static <A, B, C, D, E, FF, G, H> P2<A, P2<B, P2<C, P2<D, P2<E, P2<FF, P2<G, H>>>>>>> expand(P8<A, B, C, D, E, FF, G, H> p) {
    return p(p._1(), expand(p(p._2(), p._3(), p._4(), p._5(), p._6(), p._7(), p._8())));
  }

  public static <A, B, C, D, E, FF, G, H> P8<A, B, C, D, E, FF, G, H> flatten8(P2<A, P2<B, P2<C, P2<D, P2<E, P2<FF, P2<G, H>>>>>>> n) {
    P7<B, C, D, E, FF, G, H> t = flatten7(n._2());
    return p(n._1(), t._1(), t._2(), t._3(), t._4(), t._5(), t._6(), t._7());
  }

}
