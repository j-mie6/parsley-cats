/*
 * Copyright 2022 Parsley-Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import Predef.{ArrowAssoc => _, _}

import parsley.{ParsleyTest, Success, Failure}
import parsley.character.digit
import parsley.syntax.character.{charLift, stringLift}
import parsley.cats.combinator._
import cats.data.{NonEmptyList, NonEmptySet}

class CombinatorTests extends ParsleyTest {
    "sepBy1" must "not allow sep at the end of chain" in cases(sepBy1('a', 'b')) (
        "ab" -> None,
    )
    it should "be able to parse 2 or more p" in cases(sepBy1('a', 'b')) (
        "aba" -> Some(NonEmptyList.of('a', 'a')),
        "ababa" -> Some(NonEmptyList.of('a', 'a', 'a')),
        "abababa" -> Some(NonEmptyList.of('a', 'a', 'a', 'a')),
    )

    it must "require a p" in cases(sepBy1('a', 'b')) (
        "a" -> Some(NonEmptyList.of('a')),
        "" -> None,
    )

    "sepEndBy1" should "not require sep at the end of chain" in cases(sepEndBy1('a', 'b')) (
        "a" -> Some(NonEmptyList.of('a'))
    )
    it should "be able to parse 2 or more p" in cases(sepEndBy1('a', 'b'))(
        "aba" -> Some(NonEmptyList.of('a', 'a')),
        "ababa" -> Some(NonEmptyList.of('a', 'a', 'a')),
    )
    it should "be able to parse a final sep" in cases(sepEndBy1('a', 'b'))(
        "ab" -> Some(NonEmptyList.of('a')),
        "abab" -> Some(NonEmptyList.of('a', 'a')),
        "ababab" -> Some(NonEmptyList.of('a', 'a', 'a')),
    )
    it should "fail if p fails after consuming input" in cases(sepEndBy1("aa", 'b')) (
        "ab" -> None,
    )
    it should "fail if sep fails after consuming input" in cases(sepEndBy1('a', "bb")) (
        "ab" -> None,
    )
    it must "require a p" in {
        sepEndBy1('a', 'b').parse("a") should not be a [Failure[_]]
        sepEndBy1('a', 'b').parse(input = "") shouldBe a [Failure[_]]
    }

    "endBy1" must "require sep at end of chain" in {
        endBy1('a', 'b').parse("a") shouldBe a [Failure[_]]
        endBy1('a', 'b').parse("ab") should be (Success(NonEmptyList.of('a')))
    }
    it should "be able to parse 2 or more p" in {
        endBy1('a', 'b').parse("abab") should be (Success(NonEmptyList.of('a', 'a')))
        endBy1('a', 'b').parse("ababab") should be (Success(NonEmptyList.of('a', 'a', 'a')))
    }
    it must "require a p" in {
        endBy1('a', 'b').parse("ab") should not be a [Failure[_]]
        endBy1('a', 'b').parse(input = "") shouldBe a [Failure[_]]
    }

    "manyMap" should "collapse zero or more things" in {
        val p = digit.manyMap(Set(_))

        p.parse("") should be (Success(Set.empty))
        p.parse("1231") should be (Success(Set('1', '2', '3')))
    }
    it should "work for option lifted semigroups" in {
        val p = digit.manyMap[Option[NonEmptySet[Char]]](c => Some(NonEmptySet.one(c)))
        p.parse("") should be (Success(None))
        p.parse("1231") should be (Success(Some(NonEmptySet.of('1', '2', '3'))))
    }

    "someMap" should "collapse one or more things" in {
        val p = digit.someMap(NonEmptySet.one(_))

        p.parse("") shouldBe a [Failure[_]]
        p.parse("1231") should be (Success(NonEmptySet.of('1', '2', '3')))
    }
}
