package sangria.marshalling

import _root_.argonaut._

object argonaut {
  implicit object ArgonautResultMarshaller extends ResultMarshaller {
    type Node = Json

    def emptyMapNode = Json.obj()
    def mapNode(keyValues: Seq[(String, Json)]) = Json.obj(keyValues: _*)
    def addMapNodeElem(node: Json, key: String, value: Json, optional: Boolean) = node.withObject(_ + (key, value))

    def arrayNode(values: Vector[Json]) = Json.array(values: _*)
    def optionalArrayNodeValue(value: Option[Json]) = value match {
      case Some(v) ⇒ v
      case None ⇒ nullNode
    }

    def booleanNode(value: Boolean) = Json.jBool(value)
    def floatNode(value: Double) = Json.jNumber(value).get
    def stringNode(value: String) = Json.jString(value)
    def intNode(value: Int) = Json.jNumber(value)
    def bigIntNode(value: BigInt) = Json.jNumber(BigDecimal(value))
    def bigDecimalNode(value: BigDecimal) = Json.jNumber(value)

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

        num.toBigIntExact getOrElse num
      } else if (node.isString)
        node.string.get
      else
        throw new IllegalStateException(s"$node is not a scalar value")

    def getScalaScalarValue(node: Json) = getScalarValue(node)

    def isEnumNode(node: Json) = node.isString

    def isScalarNode(node: Json) =
      node.isBool || node.isNumber || node.isString

    def isVariableNode(node: Json) = false
    def getVariableName(node: Json) = throw new IllegalArgumentException("variables are not supported")

    def render(node: Json) = node.nospaces
  }

  implicit object argonautToInput extends ToInput[Json, Json] {
    def toInput(value: Json) = (value, ArgonautInputUnmarshaller)
  }

  implicit object argonautFromInput extends FromInput[Json] {
    val marshaller = ArgonautResultMarshaller
    def fromResult(node: marshaller.Node) = node
  }
}