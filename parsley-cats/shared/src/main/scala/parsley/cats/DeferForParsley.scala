/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import cats.Defer

import parsley.Parsley, Parsley.LazyParsley

private [parsley] class DeferForParsley extends Defer[Parsley] {
    def defer[A](p: =>parsley.Parsley[A]): parsley.Parsley[A] = ~p
}
