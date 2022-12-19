/* SPDX-FileCopyrightText: © 2022 Parsley Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley

import cats.{Monad, MonoidK, FunctorFilter}

/** Contains instances for `cats` typeclasses.
  *
  * @since 0.1.0
  */
object catsinstances {
    /** Instance for the core `cats` typeclasses used with parser combinators.
      *
      * @since 0.1.0
      */
    implicit val monadPlusForParsley: Monad[Parsley] with MonoidK[Parsley] with FunctorFilter[Parsley] =
        // This must be kept in this ordering, with more generic further up
        new MonadForParsley with ApplicativeForParsley
                            with FunctorForParsley
                            with MonoidKForParsley
                            with FunctorFilterForParsley
}
