package parsley

import cats.{MonoidK, Functor}

private [parsley] trait MonoidKForParsley extends MonoidK[Parsley] {
    // MonoidK
    override def combineK[A](p: Parsley[A], q: Parsley[A]): Parsley[A] = p <|> q
    override def empty[A]: Parsley[A] = Parsley.empty

    // MonoidK Overrides
    override def sum[A, B](mx: Parsley[A], my: Parsley[B])(implicit F: Functor[Parsley]): Parsley[Either[A,B]] = mx <+> my
    override def combineAllK[A](ps: TraversableOnce[Parsley[A]]): Parsley[A] = combinator.choice(ps.toIterator.toSeq: _*)
    override def combineAllOptionK[A](ps: TraversableOnce[Parsley[A]]): Option[Parsley[A]] = ps.toIterator.reduceRightOption(_<|>_)
}