package parsley

import cats.{Monad, MonoidK, FunctorFilter}
import cats.Functor
import cats.Alternative

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

            // MonoidK
            override def combineK[A](p: Parsley[A], q: Parsley[A]): Parsley[A] = p <|> q

            override def empty[A]: Parsley[A] = Parsley.empty

            // FunctorFilter
            override def functor: Functor[Parsley] = this

            override def mapFilter[A, B](mx: Parsley[A])(f: A => Option[B]): Parsley[B] = mx.collect(f.unlift)

            // Functor Overrides
            override def map[A, B](mx: Parsley[A])(f: A => B): Parsley[B] = mx.map(f)
            override def as[A, B](mx: Parsley[A], y: B): Parsley[B] = mx #> y

            // Applicative Overrides
            override def productL[A, B](mx: Parsley[A])(my: Parsley[B]): Parsley[A] = mx <~ my
            override def productR[A, B](mx: Parsley[A])(my: Parsley[B]): Parsley[B] = mx ~> my
            override def product[A, B](mx: Parsley[A], my: Parsley[B]): Parsley[(A, B)] = mx <~> my
            override def ap[A, B](mf: Parsley[A => B])(mx: Parsley[A]): Parsley[B] = mf <*> mx
            // maps and tupleds

            // Monad Overrides
            override def ifM[B](mx: Parsley[Boolean])(ifTrue: => Parsley[B], ifFalse: => Parsley[B]): Parsley[B] = combinator.ifP(mx, ifTrue, ifFalse)
            override def whileM_[A](p: Parsley[Boolean])(body: =>Parsley[A]): Parsley[Unit] = {
                combinator.when(p, combinator.whileP(body ~> p))
            }
            override def untilM_[A](body: Parsley[A])(p: => Parsley[Boolean]): Parsley[Unit] = combinator.whileP(body *> p.map(!_))

            override def whileM[G[_]: Alternative, A](p: Parsley[Boolean])(body: => Parsley[A]): Parsley[G[A]] = {
                import registers._
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

            // MonoidK Overrides
            override def sum[A, B](mx: Parsley[A], my: Parsley[B])(implicit F: Functor[Parsley]): Parsley[Either[A,B]] = mx <+> my

            // FunctorFilter Overrides
            override def filter[A](mx: Parsley[A])(f: A => Boolean): Parsley[A] = mx.filter(f)
            override def filterNot[A](mx: Parsley[A])(f: A => Boolean): Parsley[A] = mx.filterNot(f)
            override def collect[A, B](mx: Parsley[A])(f: PartialFunction[A,B]): Parsley[B] = mx.collect(f)
        }
}
