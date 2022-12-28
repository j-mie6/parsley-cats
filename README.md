# Parsley Cats ![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/j-mie6/parsley-cats/ci.yml?branch=master) [![parsley-cats Scala version support](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats/latest.svg)](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats) [![GitHub license](https://img.shields.io/github/license/j-mie6/parsley-cats.svg)](https://github.com/j-mie6/parsley-cats/blob/master/LICENSE) ![GitHub commits since latest release (by SemVer)](https://img.shields.io/github/commits-since/j-mie6/parsley-cats/latest) ![Code of Conduct](https://img.shields.io/badge/Code%20of%20Conduct-Scala-blue.svg) [![Badge-Scaladoc]][Link-Scaladoc] <a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="60px" align="right" alt="Cats friendly" /></a>

## What is `parsley-cats`?
The `parsley-cats` library exposes `cats` instances for `MonoidK[Parsley]`, `Monad[Parsley]`, and `FunctorFilter[Parsley]` as well as `Defer[Parsley]`.
Care should still be taken to not define truly recursive parsers using the `cats` API (although monadic parser with `flatMap`
may be generally recursive, just slow). In particular, make use of `Defer[Parsley].fix`
to handle recursion, or plain `lazy val` based construction (as in regular `parsley` use).

## How do I use it? [![parsley-cats Scala version support](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats) [![parsley-cats Scala version support](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats/latest-by-scala-version.svg?platform=sjs1)](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats) [![parsley-cats Scala version support](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats/latest-by-scala-version.svg?platform=native0.4)](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats)

Parsley cats is distributed on Maven Central, and can be added to your project via:

```scala
libraryDependencies += "com.github.j-mie6" %% "parsley-cats" % "0.2.0"
```

it requires `parsley` and `cats-core` to also be dependencies of your project. The current version
matrix for `parsley-cats`:

| `parsley-cats` version | `parsley` version | `cats-core` version |
| :--------------------: | :---------------: | :-----------------: |
| `0.1.x`                | `>= 4 && < 5`     | `>= 2.8 && < 3`     |
| `0.2.x`                | `>= 4 && < 5`     | `>= 2.8 && < 3`     |

Documentation can be found [**here**][Link-Scaladoc]

## What is `parsley`?

[Parsley](https://github.com/j-mie6/parsley) is a fast, modern, parser combinator library based loosely on Haskell's `parsec` and
`megaparsec`. For examples, see its repo and wiki!

## Known Incompatiblities
The following are known conflicts between the syntactic extensions of `cats` and the base combinators on `parsley`. This only needs to be considered when writing _concrete_ values of type `Parsley[A]`: combinators that rely on generic instances over a type `F` will use the `cats` version of the conflicting combinators.

* The `SemigroupK` syntax for `combine` of `<+>` is incompatible with `parsley`, which defines
  `<+>` to be a combine combinator returning `Parsley[Either[A, B]]`: the `cats` combinator `<+>` is known in `parsley` as `<|>`, `orElse`, or `|`.


<!-- examples should go here, but <+> conflicts between parsley and cats,
     which makes examples difficult... -->

<!-- Badges and Links -->


[Link-Scaladoc]: https://javadoc.io/doc/com.github.j-mie6/parsley-cats_2.13/latest/index.html

[Badge-Scaladoc]: https://img.shields.io/badge/documentation-available-green
