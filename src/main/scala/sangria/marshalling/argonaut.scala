package sangria.marshalling

import _root_.argonaut._
import _root_.argonaut.Argonaut._

import scala.util.{Failure, Success, Try}

object argonaut {
  implicit object ArgonautResultMarshaller extends ResultMarshaller {
    type Node = Json
    type MapBuilder = ArrayMapBuilder[Node]

    def emptyMapNode(keys: Seq[String]) = new ArrayMapBuilder[Node](keys)
    def addMapNodeElem(
        builder: MapBuilder,
        key: String,
        value: Node,
        optional: Boolean): ArrayMapBuilder[Node] =
      builder.add(key, value)

    def mapNode(builder: MapBuilder): Node = Json.obj(builder.toSeq: _*)
    def mapNode(keyValues: Seq[(String, Json)]): Node = Json.obj(keyValues: _*)

    def arrayNode(values: Vector[Json]): Node = Json.array(values: _*)
    def optionalArrayNodeValue(value: Option[Json]): Node = value match {
      case Some(v) => v
      case None => nullNode
    }

    def scalarNode(value: Any, typeName: String, info: Set[ScalarValueInfo]): Node = value match {
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

    def enumNode(value: String, typeName: String): Node = Json.jString(value)

    def nullNode: Node = Json.jNull

    def renderCompact(node: Json): String = node.nospaces
    def renderPretty(node: Json): String = node.spaces2
  }

  implicit object ArgonautMarshallerForType extends ResultMarshallerForType[Json] {
    val marshaller: ArgonautResultMarshaller.type = ArgonautResultMarshaller
  }

  implicit object ArgonautInputUnmarshaller extends InputUnmarshaller[Json] {
    def getRootMapValue(node: Json, key: String): Option[Json] = node.obj.get(key)

    def isMapNode(node: Json): JsonBoolean = node.isObject
    def getMapValue(node: Json, key: String): Option[Json] = node.obj.get(key)
    def getMapKeys(node: Json): List[Json.JsonField] = node.obj.get.fields

    def isListNode(node: Json): JsonBoolean = node.isArray
    def getListValue(node: Json): Json.JsonArray = node.array.get

    def isDefined(node: Json): JsonBoolean = !node.isNull
    def getScalarValue(node: Json): Any =
      if (node.isBool)
        node.bool.get
      else if (node.isNumber) {
        val num = node.number.get.toBigDecimal

        num.toBigIntExact.getOrElse(num)
      } else if (node.isString)
        node.string.get
      else
        throw new IllegalStateException(s"$node is not a scalar value")

    def getScalaScalarValue(node: Json): Any = getScalarValue(node)

    def isEnumNode(node: Json): JsonBoolean = node.isString

    def isScalarNode(node: Json): JsonBoolean =
      node.isBool || node.isNumber || node.isString

    def isVariableNode(node: Json): JsonBoolean = false
    def getVariableName(node: Json) = throw new IllegalArgumentException(
      "variables are not supported")

    def render(node: Json): String = node.nospaces
  }

  implicit object argonautToInput extends ToInput[Json, Json] {
    def toInput(value: Json): (Json, InputUnmarshaller[Json]) = (value, ArgonautInputUnmarshaller)
  }

  implicit object argonautFromInput extends FromInput[Json] {
    val marshaller: ArgonautResultMarshaller.type = ArgonautResultMarshaller
    def fromResult(node: marshaller.Node): Json = node
  }

  implicit def argonautEncodeJsonToInput[T: EncodeJson]: ToInput[T, Json] =
    new ToInput[T, Json] {
      def toInput(value: T): (Json, ArgonautInputUnmarshaller.type) =
        implicitly[EncodeJson[T]].apply(value) -> ArgonautInputUnmarshaller
    }

  implicit def argonautDecoderFromInput[T: DecodeJson]: FromInput[T] =
    new FromInput[T] {
      val marshaller: ArgonautResultMarshaller.type = ArgonautResultMarshaller
      def fromResult(node: marshaller.Node): T =
        implicitly[DecodeJson[T]]
          .decodeJson(node)
          .fold((error, _) => throw InputParsingError(Vector(error)), identity)
    }

  implicit object ArgonautInputParser extends InputParser[Json] {
    def parse(str: String): Try[Json] =
      str.decodeEither[Json].fold(error => Failure(ArgonautParsingException(error)), Success(_))
  }

  case class ArgonautParsingException(message: String) extends Exception(message)
}
