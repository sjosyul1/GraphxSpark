
package main.scala.apps
import java.util.Date
import org.apache.log4j.{Level, Logger}
import scala.io.Source
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.Encoder
import org.apache.spark.sql.types._
import org.apache.spark.sql.SQLImplicits
import org.apache.spark.sql.{Dataset, DataFrame}
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions.udf
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD


object graphs {
Logger.getLogger("org").setLevel(Level.OFF)
    val spark = SparkSession.builder
             .master("local[*]")
             .appName("IFT443SparkModules")
             .getOrCreate()                       //> Using Spark's default log4j profile: org/apache/spark/log4j-defaults.proper
                                                  //| ties
                                                  //| spark  : org.apache.spark.sql.SparkSession = org.apache.spark.sql.SparkSess
                                                  //| ion@74d7184a
  import spark.implicits._
  println(spark.version)                          //> 2.1.0
  //create  vertices DataFrame
  val v = spark.sqlContext.createDataFrame(List(
  ("a", "Alice", 54),
  ("b", "Bob", 36),
  ("c", "Charlie", 30),
  ("d", "Darla", 67),
  ("e", "Elise", 72)
)).toDF("id", "name", "age")                      //> v  : org.apache.spark.sql.DataFrame = [id: string, name: string ... 1 more 
                                                  //| field]
v.printSchema()                                   //> root
                                                  //|  |-- id: string (nullable = true)
                                                  //|  |-- name: string (nullable = true)
                                                  //|  |-- age: integer (nullable = false)
                                                  //| 
// Create an Edge DataFrame with "src" and "dst" columns
val e = spark.sqlContext.createDataFrame(List(
  ("a", "b", "friend"),
  ("b", "c", "follow"),
  ("c", "b", "follow"),
  ("d", "c", "follow"),
  ("e", "c", "follow")
)).toDF("src", "dst", "relationship")             //> e  : org.apache.spark.sql.DataFrame = [src: string, dst: string ... 1 more 
                                                  //| field]
e.printSchema()                                   //> root
                                                  //|  |-- src: string (nullable = true)
                                                  //|  |-- dst: string (nullable = true)
                                                  //|  |-- relationship: string (nullable = true)
                                                  //| 
//create a GraphFrame
import org.graphframes._
import org.graphframes.GraphFrame

val f = GraphFrame(v, e)                          //> f  : org.graphframes.GraphFrame = GraphFrame(v:[id: string, name: string ..
                                                  //| . 1 more field], e:[src: string, dst: string ... 1 more field])
//val f:GraphFrame = GraphFrame(v:DataFrame, e:DataFrame)

// Query: Get in-degree of each vertex.
f.inDegrees.show()                                //> 
[Stage 0:>                                                          (0 + 0
                                                  //| ) / 5]
                                                                    
                                                  //|             
+---+--------+
                                                  //| | id|inDegree|
                                                  //| +---+--------+
                                                  //| |  c|       3|
                                                  //| |  b|       2|
                                                  //| +---+--------+
                                                  //| 

 //Query: Count the number of "follow" connections in the graph.
f.edges.filter("relationship = 'follow'").count() //> res0: Long = 4

// Run PageRank algorithm, and show results.
val results = f.pageRank.resetProbability(0.001).maxIter(10).run()
                                                  //> 
[Stage 19:=>                                                      (5 + 0) 
                                                  //| / 200]
[Stage 17:============> (181 + 8) / 200][Stage 19:>               (5
                                                  //|  + 0) / 200]
[Stage 17:=============>(198 + 2) / 200][Stage 19:===>        
                                                  //|    (43 + 8) / 200]
[Stage 17:=============>(199 + 1) / 200][Stage 19:======
                                                  //| =>      (105 + 7) / 200]
[Stage 19:========================================
                                                  //| ======>       (174 + 8) / 200]
[Stage 20:>                                 
                                                  //|                         (0 + 5) / 5]
[Stage 20:>                 (0 + 5) / 
                                                  //| 5][Stage 21:>                 (0 + 3) / 5]
[Stage 20:>                 (0 +
                                                  //|  5) / 5][Stage 21:==========>       (3 + 2) / 5]
[Stage 20:=======>        
                                                  //|   (2 + 3) / 5][Stage 21:==========>       (3 + 2) / 5]
[Stage 21:==========
                                                  //| ========================>                       (3 + 2) / 5]
[Stage 22:====
                                                  //| ===================================>              (146 + 8) / 200]
[Stage 2
                                                  //| 3:>                    
                                                  //| Output exceeds cutoff limit.
results.vertices.select("id", "pagerank").show()  //> +---+--------------------+
                                                  //| | id|            pagerank|
                                                  //| +---+--------------------+
                                                  //| |  a|               0.001|
                                                  //| |  b|0.025860424141218783|
                                                  //| |  d|               0.001|
                                                  //| |  c|0.025865399211088942|
                                                  //| |  e|               0.001|
                                                  //| +---+--------------------+
                                                  //| 
-

}
