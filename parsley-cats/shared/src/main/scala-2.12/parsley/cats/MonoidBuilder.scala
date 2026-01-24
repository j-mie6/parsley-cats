/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import cats.Monoid
import scala.collection.mutable
import cats.syntax.all.*

class MonoidBuilder[A, M: Monoid](inj: A => M) extends mutable.Builder[A, M] {
    private var m = Monoid.empty[M]
    override def result(): M = m

    override def clear(): Unit = m = Monoid.empty[M]

    override def +=(elem: A): this.type = {
        m |+|= inj(elem)
        this
    }
}
