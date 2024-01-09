/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import parsley.Parsley

import cats.{Functor, FunctorFilter}

private [parsley] trait FunctorFilterForParsley extends FunctorFilter[Parsley] { self: Functor[Parsley] =>
    override def functor: Functor[Parsley] = this

    override def mapFilter[A, B](mx: Parsley[A])(f: A => Option[B]): Parsley[B] = mx.mapFilter(f)

    // FunctorFilter Overrides
    override def filter[A](mx: Parsley[A])(f: A => Boolean): Parsley[A] = mx.filter(f)
    override def filterNot[A](mx: Parsley[A])(f: A => Boolean): Parsley[A] = mx.filterNot(f)
    override def collect[A, B](mx: Parsley[A])(f: PartialFunction[A,B]): Parsley[B] = mx.collect(f)
}
