# Parsley Cats ![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/j-mie6/parsley-cats/ci.yml?branch=master) ![GitHub release](https://img.shields.io/github/v/release/j-mie6/parsley-cats?include_prereleases&sort=semver) [![GitHub license](https://img.shields.io/github/license/j-mie6/parsley-cats.svg)](https://github.com/j-mie6/parsley-cats/blob/master/LICENSE) ![GitHub commits since latest release (by SemVer)](https://img.shields.io/github/commits-since/j-mie6/parsley-cats/latest) [![Badge-Scaladoc]][Link-Scaladoc]


## What is `parsley-cats`?
The `parsley-cats` library exposes `cats` instances for `MonoidK[Parsley]`, `Monad[Parsley]`, and `FunctorFilter[Parsley]`.
Care should still be taken to not define truly recursive parsers using the `cats` API (although monadic parser with `flatMap`
may be generally recursive, just slow).

## How do I use it? [![parsley Scala version support](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats) [![parsley Scala version support](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats/latest-by-scala-version.svg?platform=sjs1)](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats) [![parsley Scala version support](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats/latest-by-scala-version.svg?platform=native0.4)](https://index.scala-lang.org/j-mie6/parsley-cats/parsley-cats)

Parsley is distributed on Maven Central, and can be added to your project via:

```scala
libraryDependencies += "com.github.j-mie6" %% "parsley-cats" % "0.1.0"
```

it requires `parsley` and `cats-core` to also be dependencies of your project. The current version
matrix for `parsley-cats`:

* `0.1.0` supports `4 <= parsley < 5` , `2.8 <= cats-core < 3`

Documentation can be found [**here**][Link-Scaladoc]

<!-- Badges and Links -->


[Link-Scaladoc]: https://javadoc.io/doc/com.github.j-mie6/parsley-cats_2.13/latest/index.html

[Badge-Scaladoc]: https://img.shields.io/badge/documentation-available-green
