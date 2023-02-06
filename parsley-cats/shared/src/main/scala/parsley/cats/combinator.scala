package parsley.cats

import parsley.Parsley, Parsley.notFollowedBy
import parsley.combinator.{many, manyUntil}
import parsley.lift.lift2

import cats.data.NonEmptyList

object combinator {
    private def nonEmptyList[A](p: Parsley[A], ps: =>Parsley[List[A]]) = lift2[A, List[A], NonEmptyList[A]](NonEmptyList(_, _), p, ps)

    def some[A](p: Parsley[A]): Parsley[NonEmptyList[A]] = nonEmptyList(p, many(p))

    def someUntil[A](p: Parsley[A], end: Parsley[_]): Parsley[NonEmptyList[A]] = notFollowedBy(end) *> (nonEmptyList(p, manyUntil(p, end)))

    def sepBy1[A](p: Parsley[A], sep: =>Parsley[_]): Parsley[NonEmptyList[A]] = nonEmptyList(p, many(sep *> p))

    def sepEndBy1[A](p: Parsley[A], sep: =>Parsley[_]): Parsley[NonEmptyList[A]] = parsley.combinator.sepEndBy1(p, sep).map(NonEmptyList.fromList(_).get)
    
    def endBy1[A](p: Parsley[A], sep: =>Parsley[_]): Parsley[NonEmptyList[A]] = some(p <* sep)
}
