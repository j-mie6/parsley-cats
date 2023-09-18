/*
 * Copyright 2022 Parsley Contributors <https://github.com/j-mie6/Parsley/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import parsley.Parsley

import cats.{Defer, FunctorFilter, Monad, MonoidK}

/** Contains instances for `cats` typeclasses.
  *
  * @since 1.2.0
  */
object instances {
    /** Instance for the core `cats` typeclasses used with parser combinators.
      *
      * @since 1.2.0
      */
    implicit val monadPlusForParsley: Monad[Parsley] with MonoidK[Parsley] with FunctorFilter[Parsley] =
        // This must be kept in this ordering, with more generic further up
        new MonadForParsley with ApplicativeForParsley
                            with FunctorForParsley
                            with MonoidKForParsley
                            with FunctorFilterForParsley

    /** Instance for `cats` `Defer` typeclass, which allows for recursive parser generation.
      *
      * @since 1.2.0
      */
    implicit val deferForParsley: Defer[Parsley] = new DeferForParsley
}
