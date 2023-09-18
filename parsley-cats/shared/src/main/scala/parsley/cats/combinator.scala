/*
 * Copyright 2022 Parsley Contributors <https://github.com/j-mie6/Parsley/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import cats.data.NonEmptyList

import parsley.Parsley, Parsley.notFollowedBy
import parsley.combinator.{many, manyUntil}
import parsley.lift.lift2

/** This module contains pre-made combinators that are very useful for a variety of purposes, specialised to `cats`.
  *
  * In particular, it contains functionality found normally in `parsley.combinator`, but returning the `cats` `NonEmptyList`
  * instead of a regular Scala `List`.
  *
  * @since 1.2.0
  */
object combinator {
    private [cats] def nonEmptyList[A](p: Parsley[A], ps: =>Parsley[List[A]]) = lift2[A, List[A], NonEmptyList[A]](NonEmptyList(_, _), p, ps)

    /** This combinator repeatedly parses a given parser '''one''' or more times, collecting the results into a list.
      *
      * Parses a given parser, `p`, repeatedly until it fails. If `p` failed having consumed input,
      * this combinator fails. Otherwise when `p` fails '''without consuming input''', this combinator
      * will return all of the results, `x,,1,,` through `x,,n,,` (with `n >= 1`), in a non-empty list: `NonEmptyList.of(x,,1,,, .., x,,n,,)`.
      * If `p` was not successful at least one time, this combinator fails.
      *
      * @example {{{
      * scala> import parsley.character.string
      * scala> import parsley.cats.combinator.some
      * scala> val p = some(string("ab"))
      * scala> p.parse("")
      * val res0 = Failure(..)
      * scala> p.parse("ab")
      * val res1 = Success(NonEmptyList.of("ab"))
      * scala> p.parse("abababab")
      * val res2 = Success(NonEmptyList.of("ab", "ab", "ab", "ab"))
      * scala> p.parse("aba")
      * val res3 = Failure(..)
      * }}}
      *
      * @param p the parser to execute multiple times.
      * @return a parser that parses `p` until it fails, returning the non-empty list of all the successful results.
      * @since 1.2.0
      */
    def some[A](p: Parsley[A]): Parsley[NonEmptyList[A]] = nonEmptyList(p, many(p))

    /** This combinator repeatedly parses a given parser '''one''' or more times, until the `end` parser succeeds, collecting the results into a list.
      *
      * First ensures that trying to parse `end` fails, then tries to parse `p`. If it succeed then it will repeatedly: try to parse `end`, if it fails
      * '''without consuming input''', then parses `p`, which must succeed. When `end` does succeed, this combinator will return all of the results
      * generated by `p`, `x,,1,,` through `x,,n,,` (with `n >= 1`), in a non-empty list: `NonEmptyList.of(x,,1,,, .., x,,n,,)`. The parser `p` must succeed
      * at least once before `end` succeeds.
      *
      * @example This can be useful for scanning comments: {{{
      * scala> import parsley.character.{string, item, endOfLine}
      * scala> import parsley.cats.combinator.someUntil
      * scala> val comment = string("//") *> someUntil(item, endOfLine)
      * scala> p.parse("//hello world")
      * val res0 = Failure(..)
      * scala> p.parse("//hello world\n")
      * val res1 = Success(NonEmptyList.of('h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd'))
      * scala> p.parse("//\n")
      * val res2 = Failure(..)
      * scala> p.parse("//a\n")
      * val res3 = Success(NonEmptyList.of('a'))
      * }}}
      *
      * @param p the parser to execute multiple times.
      * @param end the parser that stops the parsing of `p`.
      * @return a parser that parses `p` until `end` succeeds, returning the non-empty list of all the successful results.
      * @since 1.2.0
      */
    def someUntil[A](p: Parsley[A], end: Parsley[_]): Parsley[NonEmptyList[A]] = notFollowedBy(end) *> (nonEmptyList(p, manyUntil(p, end)))

    /** This combinator parses '''one''' or more occurrences of `p`, separated by `sep`.
      *
      * First parses a `p`. Then parses `sep` followed by `p` until there are no more `sep`s.
      * The results of the `p`'s, `x,,1,,` through `x,,n,,`, are returned as `NonEmptyList.of(x,,1,,, .., x,,n,,)`.
      * If `p` or `sep` fails having consumed input, the whole parser fails. Requires at least
      * one `p` to have been parsed.
      *
      * @example {{{
      * scala> ...
      * scala> val args = sepBy1(int, string(", "))
      * scala> args.parse("7, 3, 2")
      * val res0 = Success(NonEmptyList.of(7, 3, 2))
      * scala> args.parse("")
      * val res1 = Failure(..)
      * scala> args.parse("1")
      * val res2 = Success(NonEmptyList.of(1))
      * scala> args.parse("1, 2, ")
      * val res3 = Failure(..) // no trailing comma allowed
      * }}}
      *
      * @param p the parser whose results are collected into a list.
      * @param sep the delimiter that must be parsed between every `p`.
      * @return a parser that parses `p` delimited by `sep`, returning the non-empty list of `p`'s results.
      * @since 1.2.0
      */
    def sepBy1[A](p: Parsley[A], sep: =>Parsley[_]): Parsley[NonEmptyList[A]] = nonEmptyList(p, many(sep *> p))

    /** This combinator parses '''one''' or more occurrences of `p`, separated and optionally ended by `sep`.
      *
      * First parses a `p`. Then parses `sep` followed by `p` until there are no more: if a final `sep` exists, this is parsed.
      * The results of the `p`'s, `x,,1,,` through `x,,n,,`, are returned as `NonEmptyList.of(x,,1,,, .., x,,n,,)`.
      * If `p` or `sep` fails having consumed input, the whole parser fails. Requires at least
      * one `p` to have been parsed.
      *
      * @example {{{
      * scala> ...
      * scala> val args = sepEndBy1(int, string(";\n"))
      * scala> args.parse("7;\n3;\n2")
      * val res0 = Success(NonEmptyList.of(7, 3, 2))
      * scala> args.parse("")
      * val res1 = Failure(..)
      * scala> args.parse("1")
      * val res2 = Success(NonEmptyList.of(1))
      * scala> args.parse("1;\n2;\n")
      * val res3 = Success(NonEmptyList.of(1, 2))
      * }}}
      *
      * @param p the parser whose results are collected into a list.
      * @param sep the delimiter that must be parsed between every `p`.
      * @return a parser that parses `p` delimited by `sep`, returning the non-empty list of `p`'s results.
      * @since 1.2.0
      */
    def sepEndBy1[A](p: Parsley[A], sep: =>Parsley[_]): Parsley[NonEmptyList[A]] = parsley.combinator.sepEndBy1(p, sep).map { xxs =>
        val (x::xs) = xxs
        NonEmptyList(x, xs)
    }

    /** This combinator parses '''one''' or more occurrences of `p`, separated and ended by `sep`.
      *
      * Parses `p` followed by `sep` one or more times.
      * The results of the `p`'s, `x,,1,,` through `x,,n,,`, are returned as `NonEmptyList.of(x,,1,,, .., x,,n,,)`.
      * If `p` or `sep` fails having consumed input, the whole parser fails.
      *
      * @example {{{
      * scala> ...
      * scala> val args = endBy1(int, string(";\n"))
      * scala> args.parse("7;\n3;\n2")
      * val res0 = Failure(..)
      * scala> args.parse("")
      * val res1 = Failure(..)
      * scala> args.parse("1;\n")
      * val res2 = Success(NonEmptyList.of(1))
      * scala> args.parse("1;\n2;\n")
      * val res3 = Success(NonEmptyList.of(1, 2))
      * }}}
      *
      * @param p the parser whose results are collected into a list.
      * @param sep the delimiter that must be parsed between every `p`.
      * @return a parser that parses `p` delimited by `sep`, returning the non-empty list of `p`'s results.
      * @since 1.2.0
      */
    def endBy1[A](p: Parsley[A], sep: =>Parsley[_]): Parsley[NonEmptyList[A]] = some(p <* sep)
}
