import java.io.File

import USABaseLineScala.getClass
import org.apache.spark.sql.catalyst.encoders.{ExpressionEncoder, RowEncoder}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}


object USABaseLineSpark {

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession.builder().master("local").appName("BaseLine").getOrCreate()
    val products = Array("corn", "barley", "cotton", "rice", "pork", "poultry", "beef")

    products.foreach(product => {

      println(new File(s"src/main/resources/${product}_usa.csv").getAbsolutePath)
      val usaDataDf: DataFrame = cleanDf(spark.read.option("header", "true").csv(s"src/main/resources/${product}_usa.csv"))
      usaDataDf.show()
      val worldDataDf: DataFrame = cleanDf(spark.read.option("header", "true").csv(s"/${product}_world.csv"))
      val secondColumn = worldDataDf.columns.filterNot(_.trim.toLowerCase != "year").head
     val finalDf =  usaDataDf.join(worldDataDf, Seq("year")).withColumn("usa_contribution_%",
       usaDataDf(secondColumn) / worldDataDf(secondColumn) * 100).drop(usaDataDf(secondColumn))

      finalDf.show(false)
    })

    def cleanDf(df: DataFrame): DataFrame = {


      /*val encode: ExpressionEncoder[Row] = RowEncoder(df.schema)
      val dataset: Dataset[Row] = df.map(row => {
       val newVal =  row.getInt(0)+1
        val updatedRow :Row = new GenericRowWithSchema(row.toSeq.patch(0,Seq(newVal),1).toArray,df.schema)
        updatedRow
      })(encode)*/

      val trimmedDf = df.columns.foldLeft(df)((df: DataFrame, column: String) => {
        df.withColumn(column, trim(col(column)))
      }
      )
      val secondcolumn = df.columns.filterNot(_.trim.toLowerCase != "year").head
      trimmedDf.withColumn(secondcolumn, col(secondcolumn).cast(DoubleType))
    }

  }

}
