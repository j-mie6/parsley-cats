/* SPDX-FileCopyrightText: © 2022 Parsley Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import parsley.Parsley, Parsley.LazyParsley

import cats.Defer

private [parsley] class DeferForParsley extends Defer[Parsley] {
    def defer[A](p: =>parsley.Parsley[A]): parsley.Parsley[A] = ~p
}
