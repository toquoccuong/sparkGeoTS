package main.scala.overlapping.timeSeriesOld.secondOrder.multivariate.lossFunctions

import breeze.linalg.{diag, DenseVector, DenseMatrix}
import main.scala.overlapping.containers.TSInstant
import org.apache.spark.broadcast.Broadcast

/**
 * Created by Francois Belletti on 9/28/15.
 */
class DiagonalNoiseARLoss[IndexT](
   val sigmaEps: DenseVector[Double],
   val nSamples: Long,
   val mean: Broadcast[DenseVector[Double]])
  extends Serializable{

  val d = sigmaEps.size
  val precisionMatrix = DenseVector.ones[Double](d)
  precisionMatrix :/= sigmaEps

  val precisionMatrixAsDiag = diag(precisionMatrix)

  def apply(params: Array[DenseMatrix[Double]],
            data: Array[(TSInstant[IndexT], DenseVector[Double])]): Double = {

    val p = params.length
    val prevision = DenseVector.zeros[Double](d)

    val meanValue = mean.value

    for(h <- 1 to p){
      prevision += params(h - 1) * (data(p - h)._2 - meanValue)
    }

    val normError = (data(p)._2 - prevision) dot (precisionMatrixAsDiag * (data(p)._2 - meanValue - prevision))

    normError / nSamples.toDouble
  }

}
