/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import scala.collection.{mutable, Factory}
import scala.collection.immutable.SortedSet

private [parsley] object XFactory {
    def SortedSetFactory[A: Ordering] = new Factory[A, SortedSet[A]] {
        def fromSpecific(it: Traversable[A]): SortedSet[A] = SortedSet(it.toSeq*)
        def newBuilder: mutable.Builder[A, SortedSet[A]] = SortedSet.newBuilder
    }
}
