/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley

import _root_.cats.{Defer, FunctorFilter, Monad, MonoidK}

/** Contains instances for `cats` typeclasses.
  *
  * @since 0.1.0
  */
@deprecated("This object has been renamed to `parsley.cats.instances`, this will be removed in parsley-cats 2", "1.2.0")
object catsinstances {
    /** Instance for the core `cats` typeclasses used with parser combinators.
      *
      * @since 0.1.0
      */
    @deprecated("This has been renamed to `parsley.cats.instances.monadPlusForParsley`, this will be removed in parsley-cats 2", "1.2.0")
    implicit val monadPlusForParsley: Monad[Parsley] with MonoidK[Parsley] with FunctorFilter[Parsley] = parsley.cats.instances.monadPlusForParsley

    /** Instance for `cats` `Defer` typeclass, which allows for recursive parser generation.
      *
      * @since 0.2.0
      */
    @deprecated("This has been renamed to `parsley.cats.instances.deferForParsley`, this will be removed in parsley-cats 2", "1.2.0")
    implicit val deferForParsley: Defer[Parsley] = parsley.cats.instances.deferForParsley
}
