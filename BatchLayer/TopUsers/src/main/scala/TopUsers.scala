import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import com.datastax.spark.connector._

object TopUsers {

	def main(args: Array[String]){

		/** Set up configuration and context **/
		val conf = new SparkConf(true)
					.set("spark.cassandra.connection.host", "54.67.124.220")
    				.setAppName("TopUsers")

    	val sc = new SparkContext(conf)

    	// To Do: encapsulate data better:
    	//case class User(id: Int, name: String)
		//case class UserGender(userId: Int, gender: String)

    	/** Read in the files that need to be processed 
    	 ** Player Data: date,time,player name, points
    	 ** User Points: userid, points, player name, date
    	 **/
		val userPoints = sc.textFile("hdfs://ip-172-31-13-111.us-west-1.compute.internal:8020/user/solivero/fantasyfootball/userpoints/userPoints.csv")
		val playerPoints = sc.textFile("hdfs://hdfs://ip-172-31-13-111.us-west-1.compute.internal:8020/user/solivero/fantasyfootball/playerpoints/PlayerData.csv")

		/** Calculate Top 10 Users for all time **/
		val topUsers = file.map({line => val pieces = line.split(",") 
			(pieces(0), pieces(1).toDouble)}).reduceByKey(_+_)

		// Swap the keys and values for sorting!
		val vk = topUsers.map(_.swap).sortByKey(false) //false=descending
		val topTen = sc.parallelize(vk.take(10))

		topTen.saveToCassandra("fantasyfootball", "topusers", SomeColumns("points", "userid"))

		/** Calculate User's points by different granularity: Month, Day, Hour **/

		/*
		val scoresMonth = file.map({line => val pieces = line.split(",")
			val datePieces = pieces(3).split("/")
			(pieces(0) + "|" + datePieces(0), pieces(1).toDouble)}).reduceByKey(_+_)
		*/

		val scoresmonth = file.map({line => val pieces = line.split(",")
			val datePieces = pieces(3).split("/")
			((pieces(0),datePieces(0)),pieces(1).toDouble)}).reduceByKey(_+_)

		scoresmonth.saveToCassandra("fantasyfootball",)

		// Save as: userid, month, points


		val scoresday = file.map({line => val pieces = line.split(",")
			val datePieces = pieces(3).split("/")
			((pieces(0),datePieces(1)),pieces(1).toDouble)}).reduceByKey(_+_)

		// Save as: userid, day, points



		/** Calculate Player's points by different granularity: Month, Day, Hour **/


		/** Calculate Player's avg points **/
		// This is a naive way... Play with mlib library later
		val input = sc.parallelize(List(("coffee", 1) , ("coffee", 2) , ("panda", 4)))
		val result = input.combineByKey(
		  (v) => (v, 1),
		  (acc: (Int, Int), v) => (acc._1 + v, acc._2 + 1),
		  (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
		  ).map{ case (key, value) => (key, value._1 / value._2.toFloat) }
		//result.collectAsMap().map(println(_))


	}
}