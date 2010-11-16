package ppl.delite.walktime.graph

/**
 * Author: Kevin J. Brown
 * Date: Nov 9, 2010
 * Time: 3:17:02 AM
 * 
 * Pervasive Parallelism Laboratory (PPL)
 * Stanford University
 */

object TestKernel1a {
  def apply() = println("op1a")
}

object TestKernel1b {
  def apply() = println("op1b")
}

object TestKernel1c {
  def apply() = println("op1c")
}

object TestKernel1d {
  def apply() = println("op1d")
}

object TestKernel2a {
  def apply() = println("op2a")
}

object TestKernel2b {
  def apply() = println("op2b")
}

object TestKernel2c {
  def apply() = println("op2c")
}

object TestKernel2d {
  def apply() = println("op2d")
}

object TestKernel3 {
  def apply() = println("op3")
}

object TestKernelBegin {
  def apply() = Array[Int](0,1,2,3,4,5,6,7,8)
}

object TestKernelMap {
  def apply(e: Int) = e + 1
}

object TestKernelEnd {
  def apply(out: Array[Int]) = {
    for (e <- out) print(e)
    print('\n')
  }
}
