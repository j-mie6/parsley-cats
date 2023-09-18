/*
 * Copyright 2022 Typelevel
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import Predef.{ArrowAssoc => _}

import parsley.{ParsleyTest, Success, Failure}
import parsley.implicits.character.{charLift, stringLift}
import parsley.cats.combinator._
import cats.data.NonEmptyList

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
}
