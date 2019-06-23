package sangria.marshalling

import _root_.argonaut._
import _root_.argonaut.Argonaut._
import org.scalatest.{Matchers, WordSpec}

import sangria.marshalling.argonaut._
import sangria.marshalling.testkit._

class ArgonautSupportSpec extends WordSpec with Matchers with MarshallingBehaviour with InputHandlingBehaviour with ParsingBehaviour {
  "ArgonautJson integration" should {
    implicit def CommentCodecJson: CodecJson[Comment] =
      casecodec2(Comment.apply, Comment.unapply)("author", "text")

    implicit def ArticleCodecJson: CodecJson[Article] =
      casecodec4(Article.apply, Article.unapply)("title", "text", "tags", "comments")

    behave like `value (un)marshaller` (ArgonautResultMarshaller)

    behave like `AST-based input unmarshaller` (argonautFromInput)
    behave like `AST-based input marshaller` (ArgonautResultMarshaller)

    behave like `case class input unmarshaller`
    behave like `case class input marshaller` (ArgonautResultMarshaller)

    behave like `input parser` (ParseTestSubjects(
      complex = """{"a": [null, 123, [{"foo": "bar"}]], "b": {"c": true, "d": null}}""",
      simpleString = "\"bar\"",
      simpleInt = "12345",
      simpleNull = "null",
      list = "[\"bar\", 1, null, true, [1, 2, 3]]",
      syntaxError = List("[123, \"FOO\" \"BAR\"")
    ))
  }

  val toRender = Json.obj(
    "a" -> Json.array(Json.jNull, Json.jNumber(123), Json.array(Json.obj("foo" -> Json.jString("bar")))),
    "b" -> Json.obj(
      "c" -> Json.jBool(true),
      "d" -> Json.jNull))

  "InputUnmarshaller" should {
    "throw an exception on invalid scalar values" in {
      an [IllegalStateException] should be thrownBy
          ArgonautInputUnmarshaller.getScalarValue(Json.obj())
    }

    "throw an exception on variable names" in {
      an [IllegalArgumentException] should be thrownBy
          ArgonautInputUnmarshaller.getVariableName(Json.jString("$foo"))
    }

    "render JSON values" in {
      val rendered = ArgonautInputUnmarshaller.render(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }

  "ResultMarshaller" should {
    "render pretty JSON values" in {
      val rendered = ArgonautResultMarshaller.renderPretty(toRender)

      rendered.replaceAll("\r", "") should be (
        """{
          |  "a" : [
          |    null,
          |    123,
          |    [
          |      {
          |        "foo" : "bar"
          |      }
          |    ]
          |  ],
          |  "b" : {
          |    "c" : true,
          |    "d" : null
          |  }
          |}""".stripMargin.replaceAll("\r", ""))
    }

    "render compact JSON values" in {
      val rendered = ArgonautResultMarshaller.renderCompact(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }
}
