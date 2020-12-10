import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
object CustomerCountCalculator {

  val getDateInstance: (String, String) => Calendar = (value, sourceFormat) => {
    val parsedData = new SimpleDateFormat(sourceFormat).parse(value)
    val cal = Calendar.getInstance
    cal.setTime(parsedData)
    cal
  }

  val getWeekOfMonth: UserDefinedFunction = udf(
    (value: String, sourceFormat: String) => {
      val cal: Calendar = getDateInstance(value, sourceFormat)
      cal.get(Calendar.WEEK_OF_MONTH) match {
        case 1 => cal.set(Calendar.DATE, 7)
        case 2 => cal.set(Calendar.DATE, 14)
        case 3 => cal.set(Calendar.DATE, 21)
        case 4 =>
          cal.set(Calendar.DATE, 28)
        case _ =>
          cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE))
      }

      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      val parsedData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      Timestamp.valueOf(parsedData.format(cal.getTime))

    }
  )
  def main(args: Array[String]): Unit = {

//    LogManager.getRootLogger.setLevel(Level.OFF)
    val spark: SparkSession = SparkSession
      .builder()
      .master("local")
      .appName("customer_count")
      .getOrCreate()

    spark.sparkContext.setLogLevel("OFF")

    val sourceDataFrame = spark.read
      .option("header", "true")
      .csv("C:\\Users\\gopasali\\Documents\\rt.csv").limit(1000).cache()


    val weekCount = sourceDataFrame
      .withColumn(
        "weekofmonth",
        getWeekOfMonth(col("ts"), lit("MM/dd/yyyy HH:mm"))
      )
      .groupBy("weekofmonth", "number")
      .count().orderBy("weekofmonth").cache()


    val dates: Array[Timestamp] =
      weekCount.select("weekofmonth").distinct().collect().map(_.getTimestamp(0))

    val startDat = dates.head

    println(startDat)

    dates.foldLeft(weekCount)((df, week) => {

      val updatedDf: DataFrame = if (startDat == week) {
       val updateddf =  df.where(s"weekofmonth='$week'")
          .groupBy("weekofmonth")
          .agg(sum("count").as(week.toString))
          .withColumn("week", lit(week.toString)).select("week","weekofmonth","count")

        updateddf.show(false)
        updateddf
      } else {

        val currentWeek: Dataset[Row] = weekCount.where(s"weekofmonth='$week'")
        val weeks: Array[Timestamp] = dates.filter(date => date.before(week) || date.equals(week) )

        val weekDf = weeks
          .map(week => {
            val count = weekCount
              .where(s"weekofmonth='$week'")
              .join(currentWeek, Seq("number"))
              .groupBy(weekCount("weekofmonth"))
              .agg(sum(weekCount("count").as("count")))
              .collect()
              .map(_.get(1).toString)
              .head

            (week, count)

          })
          .foldLeft(df)((df, tup) => {
            df.withColumn(tup._1.toString, lit(tup._2))
              .withColumn(
                "week",
                when(col("week").isNull, tup._1.toString).otherwise(col("week"))
              )

          })
        weekDf.show(false)
        weekDf

      }

      updatedDf
    })

  }

}
