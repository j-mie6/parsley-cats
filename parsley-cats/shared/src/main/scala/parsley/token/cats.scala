/*
 * Copyright 2022 Parsley Contributors <https://github.com/j-mie6/Parsley/graphs/contributors>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.token

import parsley.Parsley
import parsley.cats.combinator.sepBy1

import _root_.cats.data.NonEmptyList

object cats {
    implicit class LexerNonEmpty(val lexer: Lexer) {
        object cats {
            object lexeme {
                object separators {
                    /**  This combinator parses '''one''' or more occurrences of `p`, separated by semi-colons.
                     *
                     * First parses a `p`. Then parses a semi-colon followed by `p` until there are no more  semi-colons.
                     * The results of the `p`'s, `x,,1,,` through `x,,n,,`, are returned as `NonEmptyList(x,,1,,, .., x,,n,,)`.
                     * If `p` fails having consumed input, the whole parser fails. Requires at least
                     * one `p` to have been parsed.
                     *
                     * @example {{{
                     * scala> ...
                     * scala> val stmts = lexer.lexeme.separators.semiSep1(int)
                     * scala> stmts.parse("7; 3;2")
                     * val res0 = Success(NonEmptyList(7; 3; 2))
                     * scala> stmts.parse("")
                     * val res1 = Failure(..)
                     * scala> stmts.parse("1")
                     * val res2 = Success(NonEmptyList(1))
                     * scala> stmts.parse("1; 2; ")
                     * val res3 = Failure(..) // no trailing semi-colon allowed
                     * }}}
                     *
                     * @param p the parser whose results are collected into a list.
                     * @return a parser that parses `p` delimited by semi-colons, returning the list of `p`'s results.
                     * @since 1.3.0
                     */
                    def semiSep1[A](p: Parsley[A]): Parsley[NonEmptyList[A]] = sepBy1(p, lexer.lexeme.symbol.semi)
                    /**  This combinator parses '''one''' or more occurrences of `p`, separated by commas.
                     *
                     * First parses a `p`. Then parses a comma followed by `p` until there are no more  commas.
                     * The results of the `p`'s, `x,,1,,` through `x,,n,,`, are returned as `NonEmptyList(x,,1,,, .., x,,n,,)`.
                     * If `p` fails having consumed input, the whole parser fails. Requires at least
                     * one `p` to have been parsed.
                     *
                     * @example {{{
                     * scala> ...
                     * scala> val stmts = lexer.lexeme.separators.commaSep1(int)
                     * scala> stmts.parse("7, 3,2")
                     * val res0 = Success(NonEmptyList(7, 3, 2))
                     * scala> stmts.parse("")
                     * val res1 = Failure(..)
                     * scala> stmts.parse("1")
                     * val res2 = Success(NonEmptyList(1))
                     * scala> stmts.parse("1, 2, ")
                     * val res3 = Failure(..) // no trailing comma allowed
                     * }}}
                     *
                     * @param p the parser whose results are collected into a list.
                     * @return a parser that parses `p` delimited by commas, returning the list of `p`'s results.
                     * @since 1.3.0
                     */
                    def commaSep1[A](p: Parsley[A]): Parsley[NonEmptyList[A]] = sepBy1(p, lexer.lexeme.symbol.comma)
                }
            }
        }
    }
}
