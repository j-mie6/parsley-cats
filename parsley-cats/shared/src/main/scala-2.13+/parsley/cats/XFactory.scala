/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import scala.collection.immutable.SortedSet

private [parsley] object XFactory {
    def SortedSetFactory[A: Ordering] = SortedSet
}
