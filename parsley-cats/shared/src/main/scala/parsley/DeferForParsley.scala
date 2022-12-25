package parsley

import cats.Defer

import Parsley.LazyParsley

private [parsley] class DeferForParsley extends Defer[Parsley] {
    def defer[A](p: =>parsley.Parsley[A]): parsley.Parsley[A] = ~p
}
