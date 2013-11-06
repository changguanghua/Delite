package ppl.tests.scalatest.delite

import ppl.tests.scalatest._
import ppl.delite.framework.datastructures._
import scala.virtualization.lms.common.Record

/* Tests the generated code functionality for Delite ops, using core Delite data structures.
*/

trait DeliteTestBase extends DeliteTestModule with DeliteTestDSLApplication {
  def Complex(re: Rep[Double], im: Rep[Double]) = new Record { val real = re; val imag = im }

  def collectArray[A:Manifest](buf: Rep[DeliteArrayBuffer[A]], expectedLength: Rep[Int], expectedValues: Rep[Int] => Rep[A]) {
    collect(buf.length == expectedLength)
    for (i <- 0 until buf.length) {
      collect(buf(i) == expectedValues(i))
    }
  }
}

object DeliteMapRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteMap
trait DeliteMap extends DeliteTestBase {
  def main() = {

    val v = DeliteArrayBuffer.fromFunction(1000){ i => 0.0 }
    val v2 = v map { e => 10 }
    collectArray(v2, 1000, i => 10)

    val vs = DeliteArrayBuffer.fromFunction(500){ i => Complex(0.0, 0.0) }
    val vs2 = vs map { e => Complex(e.real + 5.0, e.imag - 5.0) }
    collectArray(vs2, 500, i => Complex(5, -5))

    val ve = DeliteArrayBuffer.fromFunction(0){ i => 0 }
    val ve2 = ve map { e => 1 }
    collectArray(ve2, 0, i => 1)
    
    mkReport
  }
}

object DeliteFlatMapRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteFlatMap
trait DeliteFlatMap extends DeliteTestBase {
  def main() = {

    val v = DeliteArrayBuffer.fromFunction(1000){ i => i }
    val v2 = v flatMap { i => DeliteArrayBuffer.fromFunction(10){ j => i }}
    collectArray(v2, 10000, i => i/10)

    val ve = DeliteArrayBuffer.fromFunction(0){ i => 0 }
    val ve2 = ve flatMap { i => DeliteArrayBuffer.fromFunction(0) { j => i }}
    collectArray(ve2, 0, i => i)

    mkReport
  }
}

object DeliteZipRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteZip
trait DeliteZip extends DeliteTestBase {
  def main() = {

    val v1 = DeliteArrayBuffer.fromFunction(1000){ i => 1.0 }
    val v2 = DeliteArrayBuffer.fromFunction(1000){ i => 0.0 } map { e => 2.0 }
    val v3 = v1.zip(v2){ _ + _ }
    collectArray(v3, 1000, i => 3.0)

    val ve1 = DeliteArrayBuffer.fromFunction(0){ i => 0.0 }
    val ve2 = DeliteArrayBuffer.fromFunction(0){ i => 0.0 }
    val ve3 = ve1.zip(ve2){ _ - _ }
    collectArray(ve3, 0, i => 0.0)
    
    // TODO: what is expected behavior when collections aren't the same size? (should be an exception)
    
    mkReport
  }
}

object DeliteReduceRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteReduce
trait DeliteReduce extends DeliteTestBase {
  def main() = {

    val v = DeliteArrayBuffer(DeliteArray[Int](1000), 1000)
    collect(v.reduce( _ + _ )(0) == 0)

    val ve = DeliteArrayBuffer[Int]()
    collect(ve.reduce( _ + _ )(0) == 0)

    mkReport
  }
}

object DeliteMapReduceRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteMapReduce
trait DeliteMapReduce extends DeliteTestBase {
  def main() = {

    val v = DeliteArrayBuffer.fromFunction(1000){ i => i } 
    collect(v.reduce( _ + _ )(0) == 499500)

    val v2 = DeliteArrayBuffer.fromFunction(500){ i => Complex(i, 0-i) }    
    val x2 = v2.reduce{ (a,b) => Complex(a.real + b.real, a.imag + b.imag) }(Complex(0,0))
    collect(x2 == Complex(124750, -124750))

    mkReport
  }
}

object DeliteFilterRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteFilter
trait DeliteFilter extends DeliteTestBase {
  def main() = {

    val v1 = DeliteArrayBuffer.fromFunction(100){ i => i }
    val v2 = v1.filter(_ % 2 == 1)
    collectArray(v2, 50, i => v1(1+i*2))

    val ve = DeliteArrayBuffer.fromFunction(0){ i => 0 }
    val ve2 = ve.filter(_ % 2 == 1)
    collectArray(ve2, 0, i => 0)
    
    mkReport
  }
}

object DeliteForeachRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteForeach
trait DeliteForeach extends DeliteTestBase {
  def main() = {

    val v = DeliteArrayBuffer.fromFunction(10){ i => i }
    for (e <- v) {
      if ((e > 0) && (e < v.length-1)) {
        collect(v(e+1) - v(e-1) == 2)
      }
    }

    val ve = DeliteArrayBuffer.fromFunction(0){ i => 0 }
    for (e <- ve) {
      collect(false) //shouldn't be executed
    }
    
    mkReport
  }
}

object DeliteZipWithReduceTupleRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteZipWithReduceTuple
trait DeliteZipWithReduceTuple extends DeliteTestBase {
  def main() = {

    val v = DeliteArrayBuffer.fromFunction(10){ i => i+5 }
    val i = DeliteArrayBuffer.fromFunction(10){ i => i+1 }

    val s = v.zip(i){ (a,b) => (a,b) }.reduce{ (a,b) => (b._1, b._2) }(make_tuple2((0,0)))
    collect(s._1 == 14)
    collect(s._2 == 10)

    //val maxWithIndex = v.zip(i){ (a,b) => (a,b) }.reduce{ (a,b) => if (a._1 < b._1) a else b }(unit(null)) //rFunc isn't separable

    mkReport
  }
}

object DeliteGroupByRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteGroupBy
trait DeliteGroupBy extends DeliteTestBase {
  def main() = {
    /*val res = DeliteArrayBuffer.fromFunction(1000){ i => i } groupBy { i => i % 2 == 0 }
    collect(res.size == 2)
    collectArray(res(true), 500, i => 2*i)
    collectArray(res(false), 500, i => 2*i+1)*/

    /*val res2 = DeliteArrayBuffer.fromFunction(1000*1000){ i => i/1000 } groupBy { i => i }
    collect(res2.size == 1000)
    for (i <- 0 until res2.size) {
      collectArray(res2(i), 1000, j => i)
    }*/

    val res2 = DeliteArrayBuffer.fromFunction(1000*1000){ i => new Record{ val a = infix_/(i,1000); val b = i }} groupBy { _.a }
    collect(res2.size == 1000)
    for (i <- 0 until res2.size) {
      collectArray(res2(i).map(_.a), 1000, j => i)
    }

    /*val res3 = DeliteArrayBuffer.fromFunction(0){ i => i } groupBy { i => i }
    collect(res3.size == 0)*/
  }
}

object DeliteNestedMapRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteNestedMap
trait DeliteNestedMap extends DeliteTestBase {
  def main() = {

    val res = DeliteArrayBuffer.fromFunction(1){ i => i } map { e => 
      DeliteArrayBuffer.fromFunction(1000){ j => 0 } map { f => 10 + e }
    }
    collectArray(res(0), 1000, i => 10)

    val res2 = DeliteArrayBuffer.fromFunction(1){ i => i } map { e => 
      DeliteArrayBuffer.fromFunction(0){ j => 0 } map { f => 10 + e }
    }
    collectArray(res2(0), 0, i => 10)
    
    mkReport
  }
}

object DeliteNestedZipRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteNestedZip
trait DeliteNestedZip extends DeliteTestBase {
  def main() = {

    val res = DeliteArrayBuffer.fromFunction(1){ i => i } map { e => 
      val v1 = DeliteArrayBuffer.fromFunction(1000){ i => 1.0 + e }
      val v2 = DeliteArrayBuffer.fromFunction(1000){ i => i } map { f => 2 + e }
      v1.zip(v2){ _ + _ }
    }
    collectArray(res(0), 1000, i => 3.0)

    val res2 = DeliteArrayBuffer.fromFunction(1){ i => i } map { e => 
      val ve1 = DeliteArrayBuffer.fromFunction(0){ i => 1.0 + e }
      val ve2 = DeliteArrayBuffer.fromFunction(0){ i => i } map { f => 2 + e }
      ve1.zip(ve2){ _ + _ }
    }
    collectArray(res2(0), 0, i => 3.0)
    
    mkReport
  }
}

object DeliteNestedReduceRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteNestedReduce
trait DeliteNestedReduce extends DeliteTestBase {
  def main() = {
    
    val res = DeliteArrayBuffer.fromFunction(1){ i => i } map { e => 
      val ve = DeliteArrayBuffer[Int](e)
      ve.reduce( _ + _ )(0)
    }
    collect(res(0) == 0)
    
    mkReport
  }
}

object DeliteNestedMapReduceRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteNestedMapReduce
trait DeliteNestedMapReduce extends DeliteTestBase {
  def main() = {

    val res = DeliteArrayBuffer.fromFunction(1){ i => i } map { e => 
      val v = DeliteArrayBuffer.fromFunction(1000){ i => i + e }
      v.reduce( _ + _ )(0)
    }
    collect(res(0) == 499500)

    mkReport
  }
}

object DeliteNestedForeachRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteNestedForeach
trait DeliteNestedForeach extends DeliteTestBase {
  def main() = {

    DeliteArrayBuffer.fromFunction(1){ i => i } foreach { i => 
      val v = DeliteArrayBuffer.fromFunction(10){ i => i }
      for (e <- v) {
        if ((e > 0) && (e < v.length-1)) {
          collect(v(e+1) - v(e-1) == 2)
        }
      }
    }

    DeliteArrayBuffer.fromFunction(1){ i => i } foreach { i =>
      val ve = DeliteArrayBuffer.fromFunction(0){ i => i }
      for (e <- ve) {
        collect(false) //shouldn't be executed
      }
    }
    
    mkReport
  }
}

object DeliteIfThenElseRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteIfThenElse
trait DeliteIfThenElse extends DeliteTestBase {
  def main() = {

    val p = unit(1.0).AsInstanceOf[Int] //trickery to prevent constant propagation
    if (p == 1) {
      var x1 = 99
      collect(x1 == 99)    
    }
    else {
      collect(false)
    }

    mkReport
  }
}

object DeliteHorizontalElemsRunner extends DeliteTestRunner with DeliteTestDSLApplicationRunner with DeliteHorizontalElems
trait DeliteHorizontalElems extends DeliteTestBase {
  def main() = {
    var i = 0
    while (i < 10) { //small collection sizes, test with multiple threads!
      val size = i
      //each line should fuse vertically (along with v), and all the lines should fuse horizontally
      val v = DeliteArrayBuffer.fromFunction(size){ j => 1 }
      collect(v.reduce( _ + _ )(0) == size)
      collect(v.filter(_ > 1).map(e => 1).reduce(_ + _)(0) == 0)
      collect(DeliteArrayBuffer.fromFunction(size)(j => j).filter(_ % 2 == 0).length == (size+1)/2)
      collect(v.filter(_ > 1).length == 0)
      collect(v.map(e => e + 1).length == size)
      i += 1
    }
    mkReport
  }
}

class DeliteOpSuite extends DeliteSuite {
  def testDeliteMap() { compileAndTest(DeliteMapRunner, CHECK_MULTILOOP) }
  def testDeliteFlatMap() { compileAndTest(DeliteFlatMapRunner) }
  def testDeliteZip() { compileAndTest(DeliteZipRunner, CHECK_MULTILOOP) }
  def testDeliteReduce() { compileAndTest(DeliteReduceRunner, CHECK_MULTILOOP) }
  def testDeliteMapReduce() { compileAndTest(DeliteMapReduceRunner, CHECK_MULTILOOP) }
  def testDeliteFilter() { compileAndTest(DeliteFilterRunner) }
  def testDeliteForeach() { compileAndTest(DeliteForeachRunner) }
  def testDeliteZipWithReduceTuple() { compileAndTest(DeliteZipWithReduceTupleRunner, CHECK_MULTILOOP) }
  def testDeliteGroupBy() { compileAndTest(DeliteGroupByRunner) }
  def testDeliteNestedMap() { compileAndTest(DeliteNestedMapRunner) }
  def testDeliteHorizontalElems() { compileAndTest(DeliteHorizontalElemsRunner, CHECK_MULTILOOP) }
  def testDeliteNestedZip() { compileAndTest(DeliteNestedZipRunner) }
  def testDeliteNestedReduce() { compileAndTest(DeliteNestedReduceRunner, CHECK_MULTILOOP) }
  def testDeliteNestedMapReduce() { compileAndTest(DeliteNestedMapReduceRunner, CHECK_MULTILOOP) }
  def testDeliteNestedForeach() { compileAndTest(DeliteNestedForeachRunner) }
  def testDeliteIfThenElse() { compileAndTest(DeliteIfThenElseRunner) }
}
