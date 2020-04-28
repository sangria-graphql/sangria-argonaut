## v1.0.0 (2017-01-16)

* 1.0 Release
* Updated argonaut to v6.2-RC2 

## v0.3.2 (2016-11-19)

* Cross-compile for scala 2.11 and 2.12
* Updated dependencies

## v0.3.1 (2016-05-01)

* Updated to sangria-marshalling-api v0.2.1

## v0.3.0 (2016-03-24)

* Updated to sangria-marshalling-api v0.2.0

## v0.2.0 (2016-02-28)

* Added support for `EncodeJson`/`DecodeJson`. This provides `ToInput` and `FromInput` instances for arbitrary tuples, case classes, etc. as long
  as you have appropriate codec in scope
* Added support for input parsing by providing an instance of `InputParser` type class 
* Updated to latest version of marshalling API

## v0.1.0 (2016-01-23)

* Initial release
