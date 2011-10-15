package ppl.dsl.optiml.vector

import ppl.dsl.optiml.{Vector, DenseVector, RangeVector, IndexVector, IndexVectorDense}
import ppl.dsl.optiml.{OptiMLExp, OptiML}
import ppl.delite.framework.{DeliteApplication, DSLType}
import scala.virtualization.lms.common.{EffectExp, BaseExp, Base, ScalaGenBase}
import scala.virtualization.lms.util.OverloadHack
import java.io.PrintWriter

trait IndexVectorDenseOps extends DSLType with Base with OverloadHack { this: OptiML =>

  implicit def repToIndexVecDenseOps(x: Rep[IndexVectorDense]) = new IndexVecDenseOpsCls(x)
  implicit def varToIndexVecDenseOps(x: Var[IndexVectorDense]) = new IndexVecDenseOpsCls(readVar(x))
  implicit def indexVecDenseToInterface(lhs: Rep[IndexVectorDense]) = new IVInterface(new IndexVecDenseOpsCls(lhs))
  
  def indexVecDenseBuilder = new VectorBuilder[Int,IndexVectorDense] {
    def alloc(length: Rep[Int], isRow: Rep[Boolean]) = IndexVector(length)
    def toIntf(x: Rep[IndexVectorDense]): Interface[IndexVector] = indexVecDenseToInterface(x)
  }  
  
  // would like to have multiple inheritance here and inherit dense vector ops, but a
  // Rep[IndexVectorDense] and Rep[DenseVector] have no relation
  class IndexVecDenseOpsCls(val elem: Rep[IndexVectorDense]) extends IndexVecOpsCls {
    type VA = IndexVectorDense
    def toOps(x: Rep[IndexVectorDense]) = repToIndexVecDenseOps(x)
    def toIntf(x: Rep[IndexVectorDense]) = indexVecDenseToInterface(x)
    def builder: VectorBuilder[Int,IndexVectorDense] = indexVecDenseBuilder
    def mVA = manifest[IndexVectorDense]
          
    // VectorOps
    def length = throw new UnsupportedOperationException("tbd") 
    def isRow = throw new UnsupportedOperationException("tbd") 
    def apply(n: Rep[Int]) = throw new UnsupportedOperationException("tbd") 
    def sort(implicit o: Ordering[Int]) = throw new UnsupportedOperationException("tbd")     
    def t = throw new UnsupportedOperationException("tbd") 
    def mt() = throw new UnsupportedOperationException("tbd")    
    def update(n: Rep[Int], y: Rep[Int]): Rep[Unit] = throw new UnsupportedOperationException("tbd")
    def +=(y: Rep[Int]) = throw new UnsupportedOperationException("tbd")
    def copyFrom(pos: Rep[Int], y: Rep[IndexVectorDense]) = throw new UnsupportedOperationException("tbd")
    def insert(pos: Rep[Int], y: Rep[Int]) = throw new UnsupportedOperationException("tbd")
    def insertAll(pos: Rep[Int], y: Rep[IndexVectorDense]) = throw new UnsupportedOperationException("tbd")
    def removeAll(pos: Rep[Int], len: Rep[Int]) = throw new UnsupportedOperationException("tbd")
    def trim() = throw new UnsupportedOperationException("tbd")
    def clear() = throw new UnsupportedOperationException("tbd")        
  }   
}

trait IndexVectorDenseOpsExp extends IndexVectorDenseOps { this: OptiMLExp => 
}
  
