package ppl.delite.runtime.codegen

import ppl.delite.runtime.graph.ops._
import ppl.delite.runtime.scheduler.{OpList, PartialSchedule}
import ppl.delite.runtime.Config
import ppl.delite.runtime.graph.targets.{OS, Targets}
import collection.mutable.ArrayBuffer
import sync._

trait ScalaExecutableGenerator extends ExecutableGenerator {

  protected def addSource(source: String) {
    ScalaCompile.addSource(source, executableName)
  }

  protected def writeJNIInitializer(locations: Set[Int]) {
    for (i <- locations) {
      out.append("import ")
      out.append("Sync_" + executableName(i))
      out.append("._\n")
    }
  }

  protected def writeHeader() {
    out.append("import ppl.delite.runtime.codegen.DeliteExecutable\n") //base trait
    out.append("import ppl.delite.runtime.profiler.PerformanceTimer\n")
    ScalaExecutableGenerator.writePath(kernelPath, out) //package of scala kernels
    val locations = Range.inclusive(0,location).toSet
    if (!this.isInstanceOf[SyncObjectGenerator]) writeJNIInitializer(locations)
    out.append("object ")
    out.append(executableName)
    out.append(" extends DeliteExecutable {\n")
  }

  protected def writeMethodHeader() {
    out.append("def run() {\n")
  }

  protected def writeMethodFooter() {
    out.append("}\n")
  }

  protected def writeFooter() {
    addAccessor() //provides a reference to the object instance
    out.append("}\n") //end object
  }

  //TODO: can/should this be factored out? need some kind of factory for each target
  protected def makeNestedFunction(op: DeliteOP) = op match {
    case c: OP_Condition => new ScalaConditionGenerator(c, location, kernelPath).makeExecutable()
    case w: OP_While => new ScalaWhileGenerator(w, location, kernelPath).makeExecutable()
    case v: OP_Variant => new ScalaVariantGenerator(v, location, kernelPath).makeExecutable()
    case err => sys.error("Unrecognized OP type: " + err.getClass.getSimpleName)
  }

  protected def writeFunctionCall(op: DeliteOP) {
    def returnsResult = op.outputType(op.getOutputs.head) == op.outputType
    def resultName = if (returnsResult) getSym(op, op.getOutputs.head) else getOpSym(op)

    if (op.task == null) return //dummy op
    if (Config.profile && !op.isInstanceOf[OP_MultiLoop])
      out.append("PerformanceTimer.start(\""+op.id+"\", Thread.currentThread.getName(), false)\n")
    out.append("val ")
    out.append(resultName)
    out.append(" : ")
    out.append(op.outputType)
    out.append(" = ")
    out.append(op.task)
    out.append('(')
    var first = true
    for ((input, name) <- op.getInputs) {
      if (!first) out.append(',') //no comma before first argument
      first = false
      out.append(getSym(input, name))
    }
    out.append(")\n")
    if (Config.profile && !op.isInstanceOf[OP_MultiLoop])
      out.append("PerformanceTimer.stop(\""+op.id+"\", false)\n")

    if (!returnsResult) {
      for (name <- op.getOutputs) {
        out.append("val ")
        out.append(getSym(op, name))
        out.append(" : ")
        out.append(op.outputType(name))
        out.append(" = ")
        out.append(resultName)
        out.append('.')
        out.append(name)
        out.append('\n')
      }
    }
  }

  protected def addAccessor() {
    out.append("def self = this\n")
  }

}

class ScalaMainExecutableGenerator(val location: Int, val kernelPath: String)
  extends ScalaExecutableGenerator with ScalaSyncGenerator {

  def executableName(location: Int) = "Executable" + location

  protected def syncObjectGenerator(syncs: ArrayBuffer[Send], target: Targets.Value) = {
    target match {
      case Targets.Scala => new ScalaMainExecutableGenerator(location, kernelPath) with ScalaSyncObjectGenerator {
        protected val sync = syncs
        override def executableName(location: Int) = executableNamePrefix + super.executableName(location)
      }
      case Targets.Cpp => new CppMainExecutableGenerator(location, kernelPath) with CppSyncObjectGenerator {
        protected val sync = syncs
        override def executableName(location: Int) = executableNamePrefix + super.executableName(location)
      }
      case _ => throw new RuntimeException("Unknown Host type " + target.toString)
    }
  }
}

object ScalaExecutableGenerator {

  def makeExecutables(schedule: PartialSchedule, kernelPath: String) {
    for (sch <- schedule if (sch.size > 0)) {
      val location = sch.peek.scheduledResource
      new ScalaMainExecutableGenerator(location, kernelPath).makeExecutable(sch)
    }
  }

  private[codegen] def writePath(kernelPath: String, out: StringBuilder) {
    if (kernelPath == "") return
    out.append("import generated.scala._\n")
    /*
    var begin = 0
    var end = kernelPath.length
    if (kernelPath.startsWith("/")) begin += 1
    if (kernelPath.endsWith("/")) end -= 1
    val packageName = kernelPath.replace('/','.').substring(begin,end)
    out.append(packageName)
    out.append(".scala._\n")
    */
  }
}
