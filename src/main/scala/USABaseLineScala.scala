import scala.collection.mutable
import scala.util.Try


object USABaseLineScala {


  def main(args: Array[String]): Unit = {


    val measuresMap: mutable.Map[String, Map[String, (Double, Double)]] = mutable.Map.empty
    val secondNames: mutable.Map[String, String] = mutable.Map.empty
    val products = Array("corn", "barley","cotton","rice","pork","poultry","beef")


    products.foreach(prod => {

      val usaData: Seq[String] = scala.io.Source.fromInputStream(getClass.getResourceAsStream(s"/${prod}_usa.csv")).mkString
        .split("\n").toSeq

      val secondName: String = usaData.head.split(",")(1).trim
      secondNames.put(prod,secondName)

      val usaMeasures: Map[String, Double] = usaData.tail.filterNot(_.trim.isEmpty)
        .map((line: String) => line.split(","))
        .map(value =>
          (value(0).trim, Try(value(1).trim.toDouble).getOrElse(0.0))).toMap
      val worldData: Seq[String] = scala.io.Source.fromInputStream(getClass.getResourceAsStream(s"/${prod}_world.csv")).mkString
        .split("\n").toSeq
      val worldMeasures: Map[String, Double] = worldData.tail.map((line: String) => line.split(",")).map(value =>
        (value(0).trim, Try(value(1).trim.toDouble).getOrElse(0.0))).toMap

      val outMeasures: Map[String, (Double, Double)] = worldMeasures.map(data => {
        val usaValue: Double = usaMeasures.getOrElse(data._1, 0.0)
        val out: Double = (usaValue / data._2) * 100
        (data._1, (data._2, out))
      })
      measuresMap.put(prod, outMeasures)
    })


   val keys: Set[String] =  measuresMap.values.flatMap(_.keys).toSet

    val finalOut = keys.map(year => {
      val values = products.map(prod => {
        val measure: (Double, Double) = measuresMap.getOrElse(prod, null).getOrElse(year, (0.0,0.0))
        s"${measure._1}|${measure._2}"
      }).mkString("|")
      s"$year|$values"
    }).mkString("\n")


    val header = secondNames.map(prod => s"world_${prod._1}_${prod._2}|usa_${prod._1}_contribution_%").mkString("|")
    println(s"year|$header\n$finalOut")
  }


}

case class Measures(year: String, count: Int)