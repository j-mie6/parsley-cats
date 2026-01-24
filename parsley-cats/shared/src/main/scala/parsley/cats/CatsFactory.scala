/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import cats.kernel.Order
import cats.data.{Chain, NonEmptyList, NonEmptyVector, NonEmptyChain, NonEmptySet}
import scala.collection.{mutable, Factory}
import scala.collection.immutable.SortedSet
import org.typelevel.scalaccompat.annotation.unused

// This is for standard cats structures (not non-empty ones, which are handled separately)
// We need some way of constructing them that then allows us to implicitly convert into a Factory for parsley's
// regular iterative combinators
private [parsley] class ChainFactory[A] extends Factory[A, Chain[A]] {
    override def newBuilder: mutable.Builder[A, Chain[A]] = new MonoidBuilder[A, Chain[A]](Chain.one)

    override def fromSpecific(it: IterableOnce[A]): Chain[A] = Chain.fromIterableOnce(it)
}
object ChainFactory {
    implicit def chainFactory[A](@unused chain: Chain.type): Factory[A, Chain[A]] = new ChainFactory[A]
}

// We also need a way of constructing non-empty-Cs from an element and their underlying C (and we need access to C's factory too)
abstract class NEFactory[A, +NEC] {
    type C
    def nec(x: A, c: C): NEC
    def unsafeUncons(c: C): (A, C)
    def factory: Factory[A, C]
    private [parsley] def unsafeNec(c: C): NEC = {
        val (x, xs) = unsafeUncons(c)
        nec(x, xs)
    }
}
object NEFactory {
    implicit def nelFactory[A](@unused nel: NonEmptyList.type): NEFactory[A, NonEmptyList[A]] = new NEFactory[A, NonEmptyList[A]] {
        type C = List[A]
        def nec(x: A, c: List[A]): NonEmptyList[A] = NonEmptyList(x, c)
        def unsafeUncons(c: List[A]): (A, List[A]) = (c.head, c.tail)
        def factory: Factory[A, List[A]] = List
    }
    implicit def necFactory[A](@unused nel: NonEmptyChain.type): NEFactory[A, NonEmptyChain[A]] = new NEFactory[A, NonEmptyChain[A]] {
        type C = Chain[A]
        override def nec(x: A, c: Chain[A]): NonEmptyChain[A] = NonEmptyChain.fromChainPrepend(x, c)
        override def unsafeUncons(c: Chain[A]): (A, Chain[A]) = c.uncons.get
        override def factory: Factory[A, Chain[A]] = new ChainFactory[A]
    }
    implicit def nevFactory[A](@unused nev: NonEmptyVector.type): NEFactory[A, NonEmptyVector[A]] = new NEFactory[A, NonEmptyVector[A]] {
        type C = Vector[A]
        override def nec(x: A, c: Vector[A]): NonEmptyVector[A] = NonEmptyVector(x, c)
        override def unsafeUncons(c: Vector[A]): (A, Vector[A]) = (c.head, c.tail)
        override def factory: Factory[A, Vector[A]] = Vector
    }
    implicit def nesFactory[A: Order](@unused nes: NonEmptySet.type): NEFactory[A, NonEmptySet[A]] = new NEFactory[A, NonEmptySet[A]] {
        private implicit val ordering: Ordering[A] = Order[A].toOrdering
        type C = SortedSet[A]
        override def nec(x: A, c: SortedSet[A]): NonEmptySet[A] = NonEmptySet(x, c)
        override def unsafeUncons(c: SortedSet[A]): (A, SortedSet[A]) = (c.head, c.tail)
        override def factory: Factory[A, SortedSet[A]] = XFactory.SortedSetFactory
    }
}
