/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import parsley.Parsley

import cats.{Functor, MonoidK}

private [parsley] trait MonoidKForParsley extends MonoidK[Parsley] {
    // MonoidK
    override def combineK[A](p: Parsley[A], q: Parsley[A]): Parsley[A] = p <|> q
    override def empty[A]: Parsley[A] = Parsley.empty

    // MonoidK Overrides
    override def sum[A, B](mx: Parsley[A], my: Parsley[B])(implicit F: Functor[Parsley]): Parsley[Either[A,B]] = mx <+> my
    override def combineAllK[A](ps: IterableOnce[Parsley[A]]): Parsley[A] = parsley.combinator.choice(ps.iterator.toSeq: _*)
    override def combineAllOptionK[A](ps: IterableOnce[Parsley[A]]): Option[Parsley[A]] = ps.iterator.reduceRightOption(_<|>_)
}
