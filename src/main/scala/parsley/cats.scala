package parsley

import cats.{Monad, MonoidK, FunctorFilter, Functor, Alternative}
import registers._
import lift._

object catsinstances {
    implicit val monadPlusForParsley: Monad[Parsley] with MonoidK[Parsley] with FunctorFilter[Parsley] =
        new Monad[Parsley] with MonoidK[Parsley] with FunctorFilter[Parsley] {
            // Monad methods
            override def flatMap[A, B](mx: Parsley[A])(f: A => Parsley[B]): Parsley[B] = mx.flatMap(f)

            override def tailRecM[A, B](x: A)(f: A => Parsley[Either[A, B]]): Parsley[B] = f(x).flatMap {
                case Left(nextX) => tailRecM(nextX)(f)
                case Right(y) => Parsley.pure(y)
            }

            // Applicative
            override def pure[A](x: A): Parsley[A] = Parsley.pure(x)
            override def unit: Parsley[Unit] = Parsley.unit
            override def point[A](x: A): Parsley[A] = Parsley.pure(x)

            // MonoidK
            override def combineK[A](p: Parsley[A], q: Parsley[A]): Parsley[A] = p <|> q

            override def empty[A]: Parsley[A] = Parsley.empty

            // Functor Overrides
            override def map[A, B](mx: Parsley[A])(f: A => B): Parsley[B] = mx.map(f)
            override def as[A, B](mx: Parsley[A], y: B): Parsley[B] = mx #> y
            override def void[A](mx: Parsley[A]): Parsley[Unit] = mx.void

            // FunctorFilter
            override def functor: Functor[Parsley] = this

            override def mapFilter[A, B](mx: Parsley[A])(f: A => Option[B]): Parsley[B] = mx.mapFilter(f)

            // Applicative Overrides
            override def productL[A, B](mx: Parsley[A])(my: Parsley[B]): Parsley[A] = mx <~ my
            override def productR[A, B](mx: Parsley[A])(my: Parsley[B]): Parsley[B] = mx ~> my
            override def product[A, B](mx: Parsley[A], my: Parsley[B]): Parsley[(A, B)] = mx <~> my
            override def ap[A, B](mf: Parsley[A => B])(mx: Parsley[A]): Parsley[B] = mf <*> mx

            override def replicateA[A](n: Int, mx: Parsley[A]): Parsley[List[A]] = combinator.exactly(n, mx)
            override def replicateA_[A](n: Int, mx: Parsley[A]): Parsley[Unit] = combinator.skip((0 until n).map(_ => mx): _*)

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
                    case (cond -> t, e) => combinator.ifP(cond, t, e)
                }
            }

            // MonoidK Overrides
            override def sum[A, B](mx: Parsley[A], my: Parsley[B])(implicit F: Functor[Parsley]): Parsley[Either[A,B]] = mx <+> my
            override def combineAllK[A](ps: IterableOnce[Parsley[A]]): Parsley[A] = combinator.choice(ps.iterator.toSeq: _*)
            override def combineAllOptionK[A](ps: IterableOnce[Parsley[A]]): Option[Parsley[A]] = ps.iterator.reduceRightOption(_<|>_)

            // FunctorFilter Overrides
            override def filter[A](mx: Parsley[A])(f: A => Boolean): Parsley[A] = mx.filter(f)
            override def filterNot[A](mx: Parsley[A])(f: A => Boolean): Parsley[A] = mx.filterNot(f)
            override def collect[A, B](mx: Parsley[A])(f: PartialFunction[A,B]): Parsley[B] = mx.collect(f)

            // Maps and Tuples
            override def map2[A, B, Z](mx: Parsley[A], my: Parsley[B])(f: (A, B) => Z): Parsley[Z] = lift2(f, mx, my)
            override def map3[A0, A1, A2, Z](f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2])(f: (A0, A1, A2) => Z): Parsley[Z] = lift3(f, f0, f1, f2)
            override def map4[A0, A1, A2, A3, Z](f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3])(f: (A0, A1, A2, A3) => Z): Parsley[Z] = {
                lift4(f, f0, f1, f2, f3)
            }
            override def map5[A0, A1, A2, A3, A4, Z](f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4])
                                                    (f: (A0, A1, A2, A3, A4) => Z): Parsley[Z] = lift5(f, f0, f1, f2, f3, f4)
            override def map6[A0, A1, A2, A3, A4, A5, Z](f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5])
                                                        (f: (A0, A1, A2, A3, A4, A5) => Z): Parsley[Z] = lift6(f, f0, f1, f2, f3, f4, f5)
            override def map7[A0, A1, A2, A3, A4, A5, A6, Z](f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3],
                                                             f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6])
                                                             (f: (A0, A1, A2, A3, A4, A5, A6) => Z): Parsley[Z] = lift7(f, f0, f1, f2, f3, f4, f5, f6)
            override def map8[A0, A1, A2, A3, A4, A5, A6, A7, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7) => Z): Parsley[Z] = lift8(f, f0, f1, f2, f3, f4, f5, f6, f7)
            override def map9[A0, A1, A2, A3, A4, A5, A6, A7, A8, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8])(f: (A0, A1, A2, A3, A4, A5, A6, A7, A8) => Z): Parsley[Z] = lift9(f, f0, f1, f2, f3, f4, f5, f6, f7, f8)
            override def map10[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9) => Z): Parsley[Z] = lift10(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9)
            override def map11[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => Z): Parsley[Z] = lift11(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
            override def map12[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => Z): Parsley[Z] = lift12(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11)
            override def map13[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => Z): Parsley[Z] = lift13(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12)
            override def map14[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => Z): Parsley[Z] =
                    lift14(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13)
            override def map15[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => Z): Parsley[Z] =
                    lift15(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14)
            override def map16[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => Z): Parsley[Z] =
                    lift16(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15)
            override def map17[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => Z): Parsley[Z] =
                    lift17(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16)
            override def map18[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => Z): Parsley[Z] =
                    lift18(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17)
            override def map19[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17], f18: Parsley[A18])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => Z): Parsley[Z] =
                    lift19(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18)
            override def map20[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17], f18: Parsley[A18], f19: Parsley[A19])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => Z): Parsley[Z] =
                    lift20(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19)
            override def map21[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17], f18: Parsley[A18], f19: Parsley[A19], f20: Parsley[A20])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => Z): Parsley[Z] =
                    lift21(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20)
            override def map22[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, Z]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17], f18: Parsley[A18], f19: Parsley[A19], f20: Parsley[A20], f21: Parsley[A21])
                (f: (A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => Z): Parsley[Z] =
                    lift22(f, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21)

            override def tuple2[A, B](f1: Parsley[A], f2: Parsley[B]): Parsley[(A, B)] = map2(f1, f2)((_, _))
            override def tuple3[A0, A1, A2](f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2]): Parsley[(A0, A1, A2)] = map3(f0, f1, f2)((_, _, _))
            override def tuple4[A0, A1, A2, A3]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3]): Parsley[(A0, A1, A2, A3)] = map4(f0, f1, f2, f3)((_, _, _, _))
            override def tuple5[A0, A1, A2, A3, A4]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4]): Parsley[(A0, A1, A2, A3, A4)] =
                    map5(f0, f1, f2, f3, f4)((_, _, _, _, _))
            override def tuple6[A0, A1, A2, A3, A4, A5]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5]): Parsley[(A0, A1, A2, A3, A4, A5)] =
                    map6(f0, f1, f2, f3, f4, f5)((_, _, _, _, _, _))
            override def tuple7[A0, A1, A2, A3, A4, A5, A6]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5],
                 f6: Parsley[A6]): Parsley[(A0, A1, A2, A3, A4, A5, A6)] = map7(f0, f1, f2, f3, f4, f5, f6)((_, _, _, _, _, _, _))
            override def tuple8[A0, A1, A2, A3, A4, A5, A6, A7]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6],
                 f7: Parsley[A7]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7)] = map8(f0, f1, f2, f3, f4, f5, f6, f7)((_, _, _, _, _, _, _, _))
            override def tuple9[A0, A1, A2, A3, A4, A5, A6, A7, A8]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8)] = map9(f0, f1, f2, f3, f4, f5, f6, f7, f8)((_, _, _, _, _, _, _, _, _))
            override def tuple10[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9)] =
                    map10(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9)((_, _, _, _, _, _, _, _, _, _))
            override def tuple11[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)] =
                    map11(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)((_, _, _, _, _, _, _, _, _, _, _))
            override def tuple12[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)] =
                    map12(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11)((_, _, _, _, _, _, _, _, _, _, _, _))
            override def tuple13[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11],
                 f12: Parsley[A12]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)] =
                    map13(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12)((_, _, _, _, _, _, _, _, _, _, _, _, _))
            override def tuple14[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12],
                 f13: Parsley[A13]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)] =
                    map14(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13)((_, _, _, _, _, _, _, _, _, _, _, _, _, _))
            override def tuple15[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13],
                 f14: Parsley[A14]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)] =
                    map15(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _))
            override def tuple16[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)] =
                    map16(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))
            override def tuple17[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)] =
                    map17(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))
            override def tuple18[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16],
                 f17: Parsley[A17]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)] =
                    map18(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17) {
                        (_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _)
                    }
            override def tuple19[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17],
                 f18: Parsley[A18]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)] =
                    map19(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18) {
                        (_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _)
                    }
            override def tuple20[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17], f18: Parsley[A18],
                 f19: Parsley[A19]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)] =
                    map20(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19) {
                        (_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _)
                    }
            override def tuple21[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17], f18: Parsley[A18], f19: Parsley[A19],
                 f20: Parsley[A20]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)] =
                    map21(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20) {
                        (_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _)
                    }
            override def tuple22[A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21]
                (f0: Parsley[A0], f1: Parsley[A1], f2: Parsley[A2], f3: Parsley[A3], f4: Parsley[A4], f5: Parsley[A5], f6: Parsley[A6], f7: Parsley[A7],
                 f8: Parsley[A8], f9: Parsley[A9], f10: Parsley[A10], f11: Parsley[A11], f12: Parsley[A12], f13: Parsley[A13], f14: Parsley[A14],
                 f15: Parsley[A15], f16: Parsley[A16], f17: Parsley[A17], f18: Parsley[A18], f19: Parsley[A19], f20: Parsley[A20],
                 f21: Parsley[A21]): Parsley[(A0, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)] =
                    map22(f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, f17, f18, f19, f20, f21) {
                        (_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _)
                    }
        }
}
