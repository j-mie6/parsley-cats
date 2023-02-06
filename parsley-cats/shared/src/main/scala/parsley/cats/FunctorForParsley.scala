/* SPDX-FileCopyrightText: © 2022 Parsley Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import parsley.Parsley

import cats.Functor

private [parsley] trait FunctorForParsley extends Functor[Parsley] {
    override def map[A, B](mx: Parsley[A])(f: A => B): Parsley[B] = mx.map(f)
    override def as[A, B](mx: Parsley[A], y: B): Parsley[B] = mx #> y
    override def void[A](mx: Parsley[A]): Parsley[Unit] = mx.void
}
