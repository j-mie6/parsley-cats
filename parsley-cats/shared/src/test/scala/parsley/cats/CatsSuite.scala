/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalactic.source.Position

import parsley.Parsley, Parsley.pure
import parsley.Success
import parsley.character.{item, digit, char}
import parsley.cats.instances._

import cats.laws.{MonadLaws, MonoidKLaws, FunctorFilterLaws}
import cats.kernel.laws.IsEq

// TODO: More laws!
class CatsSuite extends AnyFlatSpec {
    val monadLaws = MonadLaws[Parsley]
    val monoidKLaws = MonoidKLaws[Parsley]
    val filterFunctorLaws = FunctorFilterLaws[Parsley]

    def applyLaw[A](law: IsEq[Parsley[A]]*)(inputs: String*)(implicit pos: Position): Unit = {
        for (IsEq(p1, p2) <- law; input <- inputs) {
            p1.parse(input) shouldBe p2.parse(input)
        }
    }

    "Functor" should "adhere to laws" in {
        applyLaw(monadLaws.covariantIdentity(item))("", "a", "b")
        applyLaw(monadLaws.covariantComposition[Char, Int, Int](digit, _.asDigit, _ + 1))("", "0", "4", "a")
    }

    "Applicative" should "adhere to laws" in {
        applyLaw(monadLaws.applicativeIdentity(item))("", "a", "b")
        applyLaw(monadLaws.applicativeHomomorphism[Int, Int](5, _ + 1))("", "a")
        applyLaw(monadLaws.applicativeInterchange[Int, String](7, item #> (_.toString)))("", "a", "b")
        applyLaw(monadLaws.applicativeMap[Char, Int](digit, _.asDigit))("", "0", "4")
    }

    "FilterFunctor" should "adhere to laws" in {
        applyLaw(filterFunctorLaws.mapFilterComposition[Char, Int, Int](
            item,
            c => if (c.isDigit) Some(c.asDigit) else None,
            x => if (x % 2 == 0) Some(x/2) else None))("", "a", "0", "8")
        applyLaw(filterFunctorLaws.filterConsistentWithMapFilter[Char](
            item,
            _.isDigit))("", "a", "0", "8")
    }

    "MonoidK" should "adhere to laws" in {
        applyLaw(monoidKLaws.monoidKLeftIdentity(item), monoidKLaws.monoidKRightIdentity(item))("", "a", "b")
        applyLaw(monoidKLaws.combineAllK(Vector(char('a'), char('b'), char('c'))))("", "a", "b", "c")
    }

    "Defer" should "allow for the construction of recursive parsers" in {
        val manya = deferForParsley.fix[List[Char]] { self =>
            char('a') <::> self <|> pure(Nil)
        }
        manya.parse("aaaaaaaaaaaab") shouldBe Success(List.fill(12)('a'))
    }
}
