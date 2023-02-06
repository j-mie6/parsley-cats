/* SPDX-FileCopyrightText: © 2022 Parsley Cats Contributors <https://github.com/j-mie6/parsley-cats/graphs/contributors>
 * SPDX-License-Identifier: BSD-3-Clause
 */
package parsley.cats

import parsley.Parsley
import parsley.combinator
import parsley.registers.{RegisterMaker, RegisterMethods}

import cats.{Alternative, Monad}

private [parsley] trait MonadForParsley extends Monad[Parsley] {
    override def flatMap[A, B](mx: Parsley[A])(f: A => Parsley[B]): Parsley[B] = mx.flatMap(f)

    override def tailRecM[A, B](x: A)(f: A => Parsley[Either[A, B]]): Parsley[B] = f(x).flatMap {
        case Left(nextX) => tailRecM(nextX)(f)
        case Right(y) => Parsley.pure(y)
    }

    // Monad Overrides
    override def ifM[B](mx: Parsley[Boolean])(ifTrue: => Parsley[B], ifFalse: => Parsley[B]): Parsley[B] = combinator.ifP(mx, ifTrue, ifFalse)
    override def whileM_[A](p: Parsley[Boolean])(body: =>Parsley[A]): Parsley[Unit] = {
        combinator.when(p, combinator.whileP(body ~> p))
    }
    override def untilM_[A](body: Parsley[A])(p: => Parsley[Boolean]): Parsley[Unit] = combinator.whileP(body *> p.map(!_))

    override def whileM[G[_]: Alternative, A](p: Parsley[Boolean])(body: => Parsley[A]): Parsley[G[A]] = {
        val G = implicitly[Alternative[G]]
        G.empty[A].makeReg { acc =>
            whileM_(p) {
                acc.modify(body.map(x => (xs: G[A]) => G.appendK(xs, x)))
            } *> acc.get
        }
    }

    override def untilM[G[_]: Alternative, A](body: Parsley[A])(cond: => Parsley[Boolean]): Parsley[G[A]] = {
        val G = implicitly[Alternative[G]]
        map2(body, whileM(cond)(body))(G.prependK(_, _))
    }

    override def untilDefinedM[A](mox: Parsley[Option[A]]): Parsley[A] = {
        lazy val loop: Parsley[A] = combinator.decide(mox, loop)
        loop
    }

    override def iterateUntil[A](mx: Parsley[A])(p: A => Boolean): Parsley[A] = {
        lazy val loop: Parsley[A] = mx.persist { mx =>
            combinator.ifP(mx.map(p), mx, loop)
        }
        loop
    }
    override def iterateWhile[A](mx: Parsley[A])(p: A => Boolean): Parsley[A] = {
        lazy val loop: Parsley[A] = mx.persist { mx =>
            combinator.ifP(mx.map(p), loop, mx)
        }
        loop
    }
    override def ifElseM[A](branches: (Parsley[Boolean], Parsley[A])*)(els: Parsley[A]): Parsley[A] = {
        branches.foldRight(els) {
            case ((cond, t), e) => combinator.ifP(cond, t, e)
        }
    }
}
