package net.jgp.books.spark.ch17.lab200_feed_delta;

import static org.apache.spark.sql.functions.*;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * JSON Lines ingestion in a dataframe.
 * 
 * For more details about the JSON Lines format, see: http://jsonlines.org/.
 * 
 * @author jgp
 */
public class FeedDeltaLakeApp {

  /**
   * main() is your entry point to the application.
   * 
   * @param args
   */
  public static void main(String[] args) {
    FeedDeltaLakeApp app =
        new FeedDeltaLakeApp();
    app.start();
  }

  /**
   * The processing code.
   */
  private void start() {
    // Creates a session on a local master
    SparkSession spark = SparkSession.builder()
        .appName("JSON Lines to Dataframe")
        .master("local[*]")
        .getOrCreate();

    // Creates the schema
    StructType schema = DataTypes.createStructType(new StructField[] {
        DataTypes.createStructField(
            "authorId",
            DataTypes.StringType,
            false),
        DataTypes.createStructField(
            "authorType",
            DataTypes.StringType,
            true),
        DataTypes.createStructField(
            "authorZipCode",
            DataTypes.StringType,
            true),
        DataTypes.createStructField(
            "body",
            DataTypes.StringType,
            true),
        DataTypes.createStructField(
            "createdAt",
            DataTypes.TimestampType,
            false),
        DataTypes.createStructField(
            "enabled",
            DataTypes.BooleanType,
            true),
        DataTypes.createStructField(
            "endAt",
            DataTypes.TimestampType,
            true),
        DataTypes.createStructField(
            "fullAddress",
            DataTypes.StringType,
            true),
        DataTypes.createStructField(
            "id",
            DataTypes.StringType,
            false),
        DataTypes.createStructField(
            "lat",
            DataTypes.DoubleType,
            true),
        DataTypes.createStructField(
            "link",
            DataTypes.StringType,
            true),
        DataTypes.createStructField(
            "lng",
            DataTypes.DoubleType,
            true),
        DataTypes.createStructField(
            "startAt",
            DataTypes.TimestampType,
            false),
        DataTypes.createStructField(
            "title",
            DataTypes.StringType,
            true),
        DataTypes.createStructField(
            "updatedAt",
            DataTypes.TimestampType,
            true),
        DataTypes.createStructField(
            "url",
            DataTypes.StringType,
            true) });

    // Reads a CSV file with header, called books.csv, stores it in a
    // dataframe
    Dataset<Row> df = spark.read().format("json")
        .schema(schema)
        .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
        .load("data/france_grand_debat/20190302 EVENTS.json");

    df =df.withColumn("authorZipCode", col("authorZipCode").cast(DataTypes.IntegerType));
    df.show(25);
    df.printSchema();
    
    df = df.groupBy(col("authorZipCode")).count().orderBy(col("authorZipCode").desc());

    // Shows at most 5 rows from the dataframe
    df.show(25);
    df.printSchema();
    System.out.println(df.count());

    df.write().format("delta")
        .mode("overwrite")
        .save("/tmp/delta-events");
  }
}
