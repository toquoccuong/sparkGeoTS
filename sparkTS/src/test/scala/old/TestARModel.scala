package test.scala.old

import breeze.linalg._
import breeze.stats.distributions.Gaussian
import main.scala.overlapping.containers._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{FlatSpec, Matchers}

class TestARModel extends FlatSpec with Matchers{

  "The AR model " should " retrieve AR parameters." in {

    implicit def signedDistMillis = (t1: TSInstant, t2: TSInstant) => (t2.timestamp.getMillis - t1.timestamp.getMillis).toDouble

    implicit def signedDistLong = (t1: Long, t2: Long) => (t2 - t1).toDouble

    val d             = 3
    val N             = 1000000L
    val paddingMillis = 100L
    val deltaTMillis  = 1L
    val nPartitions   = 8

    implicit val config = TSConfig(deltaTMillis, d, N, paddingMillis.toDouble)

    val conf = new SparkConf().setAppName("Counter").setMaster("local[*]")
    implicit val sc = new SparkContext(conf)

    val p = 3
    val ARcoeffs = Array.fill[DenseVector[Double]](d)(
      (DenseVector.rand[Double](p) * 0.05) +
      (DenseVector((p to 1 by -1).map(x => x.toDouble / p.toDouble).toArray) * 0.30))

    val noiseMagnitudes = DenseVector.ones[Double](d)

    val rawTS = Surrogate.generateAR(
      ARcoeffs,
      d,
      N.toInt,
      deltaTMillis,
      Gaussian(0.0, 1.0),
      noiseMagnitudes,
      sc)

    val (overlappingRDD: RDD[(Int, SingleAxisBlock[TSInstant, DenseVector[Double]])], _) =
      SingleAxisBlockRDD((paddingMillis, paddingMillis), nPartitions, rawTS)

    val estimARCoeffs = ARModel(overlappingRDD, p)

    for(i <- 0 until d) {
        for(j <- 0 until p) {

          estimARCoeffs(i).covariation(j) should be (ARcoeffs(i)(j) +- 0.05)

        }

      estimARCoeffs(i).variation should be (noiseMagnitudes(i) +- 0.05)
    }

  }

}