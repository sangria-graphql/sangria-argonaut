package sangria.marshalling

import _root_.argonaut._
import _root_.argonaut.Argonaut._

import scala.util.{Failure, Success}

object argonaut {
  implicit object ArgonautResultMarshaller extends ResultMarshaller {
    type Node = Json
    type MapBuilder = ArrayMapBuilder[Node]

    def emptyMapNode(keys: Seq[String]) = new ArrayMapBuilder[Node](keys)
    def addMapNodeElem(builder: MapBuilder, key: String, value: Node, optional: Boolean) =
      builder.add(key, value)

    def mapNode(builder: MapBuilder) = Json.obj(builder.toSeq: _*)
    def mapNode(keyValues: Seq[(String, Json)]) = Json.obj(keyValues: _*)

    def arrayNode(values: Vector[Json]) = Json.array(values: _*)
    def optionalArrayNodeValue(value: Option[Json]) = value match {
      case Some(v) => v
      case None => nullNode
    }

    def scalarNode(value: Any, typeName: String, info: Set[ScalarValueInfo]) = value match {
      case v: String => Json.jString(v)
      case v: Boolean => Json.jBool(v)
      case v: Int => Json.jNumber(v)
      case v: Long => Json.jNumber(v)
      case v: Float => Json.jNumber(v).get
      case v: Double => Json.jNumber(v).get
      case v: BigInt => Json.jNumber(BigDecimal(v))
      case v: BigDecimal => Json.jNumber(v)
      case v => throw new IllegalArgumentException("Unsupported scalar value: " + v)
    }

    def enumNode(value: String, typeName: String) = Json.jString(value)

    def nullNode = Json.jNull

    def renderCompact(node: Json) = node.nospaces
    def renderPretty(node: Json) = node.spaces2
  }

  implicit object ArgonautMarshallerForType extends ResultMarshallerForType[Json] {
    val marshaller = ArgonautResultMarshaller
  }

  implicit object ArgonautInputUnmarshaller extends InputUnmarshaller[Json] {
    def getRootMapValue(node: Json, key: String) = node.obj.get(key)

    def isMapNode(node: Json) = node.isObject
    def getMapValue(node: Json, key: String) = node.obj.get(key)
    def getMapKeys(node: Json) = node.obj.get.fields

    def isListNode(node: Json) = node.isArray
    def getListValue(node: Json) = node.array.get

    def isDefined(node: Json) = !node.isNull
    def getScalarValue(node: Json) =
      if (node.isBool)
        node.bool.get
      else if (node.isNumber) {
        val num = node.number.get.toBigDecimal

        num.toBigIntExact.getOrElse(num)
      } else if (node.isString)
        node.string.get
      else
        throw new IllegalStateException(s"$node is not a scalar value")

    def getScalaScalarValue(node: Json) = getScalarValue(node)

    def isEnumNode(node: Json) = node.isString

    def isScalarNode(node: Json) =
      node.isBool || node.isNumber || node.isString

    def isVariableNode(node: Json) = false
    def getVariableName(node: Json) = throw new IllegalArgumentException(
      "variables are not supported")

    def render(node: Json) = node.nospaces
  }

  implicit object argonautToInput extends ToInput[Json, Json] {
    def toInput(value: Json) = (value, ArgonautInputUnmarshaller)
  }

  implicit object argonautFromInput extends FromInput[Json] {
    val marshaller = ArgonautResultMarshaller
    def fromResult(node: marshaller.Node) = node
  }

  implicit def argonautEncodeJsonToInput[T: EncodeJson]: ToInput[T, Json] =
    new ToInput[T, Json] {
      def toInput(value: T) = implicitly[EncodeJson[T]].apply(value) -> ArgonautInputUnmarshaller
    }

  implicit def argonautDecoderFromInput[T: DecodeJson]: FromInput[T] =
    new FromInput[T] {
      val marshaller = ArgonautResultMarshaller
      def fromResult(node: marshaller.Node) =
        implicitly[DecodeJson[T]]
          .decodeJson(node)
          .fold((error, _) => throw InputParsingError(Vector(error)), identity)
    }

  implicit object ArgonautInputParser extends InputParser[Json] {
    def parse(str: String) =
      str.decodeEither[Json].fold(error => Failure(ArgonautParsingException(error)), Success(_))
  }

  case class ArgonautParsingException(message: String) extends Exception(message)
}
