package overlapping

import org.apache.spark.rdd.RDD
import overlapping.dataShaping.block.OverlappingBlock
import overlapping.dataShaping.graph.OverlappingGraph

/**
 * Created by Francois Belletti on 8/6/15.
 */
trait GraphBlock[LocationT, DataT] {

  def partitions: RDD[(Array[LocationT], OverlappingBlock[LocationT, OverlappingGraph[DataT]])]
  def transpose: BlockGraph[LocationT, DataT]

}