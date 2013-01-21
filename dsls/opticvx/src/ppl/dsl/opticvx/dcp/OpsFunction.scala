package ppl.dsl.opticvx.dcp

import ppl.dsl.opticvx.common._
import ppl.dsl.opticvx.model._
import scala.collection.immutable.Seq
import scala.collection.immutable.Set


trait DCPOpsFunction extends DCPOpsGlobal {
  
  /*
    val square = {
      val x = cvxexpr()
      val t = cvxexpr()
      cvxfun(
        params(),
        args(scalar -> x),
        sign(positive),
        tonicity(x.sign),
        vexity(positive),
        over(scalar -> t),
        let(),
        where(
          in_secondorder_cone(cat(2*x, t-1), t+1)
        ),
        value(t)
      )
    }
  */

  /*
    cvxfun0(() => args()

    val square = {
      val n = cvxfunparam()
      val x = cvxfunexpr()
      val t = cvxfunexpr()
      cvxfun(
        params(),
        args(scalar -> x),
        sign(positive),
        tonicity(x.sign),
        vexity(positive),
        over(scalar -> t),
        let(),
        where(
          in_secondorder_cone(cat(2*x, t-1), t+1)
        ),
        value(t)
      )
    }
  */

  case class CvxFunExpr(val fx: Function)
  case class CvxFunConstraint(val fx: Function) {
    if(!(fx.isIndicator)) throw new IRValidationException()
  }

  class CvxFunParamSymbol {
    protected[DCPOpsFunction] var boundparam: IRPoly = null
    protected[DCPOpsFunction] def bind(x: IRPoly) {
      if(boundparam != null) throw new IRValidationException()
      boundparam = x
    }
    protected[DCPOpsFunction] def release() {
      if(boundparam == null) throw new IRValidationException()
      boundparam = null
    }
  }
  class CvxFunExprSymbol {
    protected[DCPOpsFunction] var boundexpr: CvxFunExpr = null
    protected[DCPOpsFunction] var boundsign: SignumPoly = null
    protected[DCPOpsFunction] def bindexpr(x: CvxFunExpr) {
      if(boundexpr != null) throw new IRValidationException()
      boundexpr = x
    }
    protected[DCPOpsFunction] def releaseexpr() {
      if(boundexpr == null) throw new IRValidationException()
      boundexpr = null
    }
    protected[DCPOpsFunction] def bindsign(x: SignumPoly) {
      if(boundsign != null) throw new IRValidationException()
      boundsign = x
    }
    protected[DCPOpsFunction] def releasesign() {
      if(boundsign == null) throw new IRValidationException()
      boundsign = null
    }
    def sign: SignumPoly = {
      if(boundsign == null) throw new IRValidationException()
      boundsign
    }
  }

  implicit def cvxfunparamssym2val(sym: CvxFunParamSymbol): IRPoly = {
    if(sym.boundparam == null) throw new IRValidationException()
    sym.boundparam
  }
  implicit def cvxfunexprsym2val(sym: CvxFunExprSymbol): CvxFunExpr = {
    if(sym.boundexpr == null) throw new IRValidationException()
    sym.boundexpr
  }

  def cvxfunparam(): CvxFunParamSymbol = new CvxFunParamSymbol
  def cvxfunexpr(): CvxFunExprSymbol = new CvxFunExprSymbol

  class CvxFunParams(val params: Seq[CvxFunParamSymbol])

  class CvxFunArgs(val args: Seq[CvxFunArgBinding])
  class CvxFunArgBinding(val size: IRPoly, val symbol: CvxFunExprSymbol)

  class CvxFunSign(val sign: SignumPoly)

  class CvxFunTonicity(val tonicity: Seq[SignumPoly])

  class CvxFunVexity(val vexity: SignumPoly)

  class CvxFunOver(val vars: Seq[CvxFunOverBinding])
  class CvxFunOverBinding(val size: IRPoly, val symbol: CvxFunExprSymbol)

  class CvxFunLet(val exprs: Seq[CvxFunLetBinding])
  class CvxFunLetBinding(val expr: CvxFunExpr, val symbol: CvxFunExprSymbol)

  class CvxFunWhere(val constraints: Seq[CvxFunConstraint])

  trait CvxFunValue
  class CvxFunMinimize(val expr: CvxFunExpr) extends CvxFunValue
  class CvxFunMaximize(val expr: CvxFunExpr) extends CvxFunValue

  def params(ps: CvxFunParamSymbol*): CvxFunParams = new CvxFunParams(Seq(ps:_*))

  def args(as: CvxFunArgBinding*): CvxFunArgs = new CvxFunArgs(Seq(as:_*))
  implicit def argbindingimpl(tpl: Tuple2[IRPoly, CvxFunExprSymbol]): CvxFunArgBinding =
    new CvxFunArgBinding(tpl._1, tpl._2)

  def sign(s: SignumPoly): CvxFunSign = new CvxFunSign(s)

  def tonicity(ts: SignumPoly*): CvxFunTonicity = new CvxFunTonicity(Seq(ts:_*))

  def vexity(v: SignumPoly): CvxFunVexity = new CvxFunVexity(v)

  def over(vs: CvxFunOverBinding*): CvxFunOver = new CvxFunOver(Seq(vs:_*))
  implicit def overbindingimpl(tpl: Tuple2[IRPoly, CvxFunExprSymbol]): CvxFunOverBinding = 
    new CvxFunOverBinding(tpl._1, tpl._2)

  def let(xs: CvxFunLetBinding*): CvxFunLet = new CvxFunLet(Seq(xs:_*))
  implicit def letbindingimpl(tpl: Tuple2[CvxFunExpr, CvxFunExprSymbol]): CvxFunLetBinding =
    new CvxFunLetBinding(tpl._1, tpl._2)

  def where(xs: CvxFunConstraint*): CvxFunWhere = new CvxFunWhere(Seq(xs:_*))

  def minimize(x: CvxFunExpr): CvxFunValue = new CvxFunMinimize(x)
  def maximize(x: CvxFunExpr): CvxFunValue = new CvxFunMaximize(x)

  def positive: SignumPoly = SignumPoly.const(Signum.Positive, globalSignumArity)

  def cvxfun(
    ts_params: =>CvxFunParams,
    ts_args: =>CvxFunArgs,
    ts_sign: =>CvxFunSign,
    ts_tonicity: =>CvxFunTonicity,
    ts_vexity: =>CvxFunVexity,
    ts_over: =>CvxFunOver,
    ts_let: =>CvxFunLet,
    ts_where: =>CvxFunWhere,
    ts_value: =>CvxFunValue
    ): Function =
  {
    // bind the parameters
    val s_params: Seq[CvxFunParamSymbol] = ts_params.params
    for(i <- 0 until s_params.length) {
      s_params(i).bind(IRPoly.param(i, s_params.length))
    }
    globalArity = s_params.length
    // bind the arguments
    val s_args: Seq[CvxFunArgBinding] = ts_args.args
    for(i <- 0 until s_args.length) {
      s_args(i).symbol.bindsign(SignumPoly.param(i, s_args.length))
    }
    // gather the dcp information for the function
    globalSignumArity = s_args.length
    val s_sign: SignumPoly = ts_sign.sign
    val s_tonicity: Seq[SignumPoly] = ts_tonicity.tonicity
    val s_vexity: SignumPoly = ts_vexity.vexity
    globalSignumArity = -1
    // get the variables
    val s_over = ts_over.vars
    val s_argsize: Seq[IRPoly] = (s_args map (x => x.size)) ++ (s_over map (x => x.size)) 
    // rebind the arguments
    for(i <- 0 until s_args.length) {
      s_args(i).symbol.releasesign()
      s_args(i).symbol.bindexpr(CvxFunExpr(Function.param(i, s_argsize)))
    }
    // bind the variables
    for(i <- 0 until s_over.length) {
      s_over(i).symbol.bindexpr(CvxFunExpr(Function.param(i + s_args.length, s_argsize)))
    }
    // bind the let-expressions
    val s_let = ts_let.exprs
    for(b <- s_let) {
      b.symbol.bindexpr(b.expr)
    }
    // get the constraints and value
    val s_where = ts_where.constraints
    val s_value = ts_value
    globalArity = -1
    // make the return value
    val tmpfxn = s_value match {
      case x: CvxFunMinimize => s_where.foldLeft(x.expr.fx)((a,b) => a + b.fx)
      case x: CvxFunMaximize => s_where.foldLeft(x.expr.fx)((a,b) => a - b.fx)
      case _ => throw new IRValidationException()
    }
    val minfxn = s_over.foldLeft(tmpfxn)((a,b) => a.minimize_over_lastarg)
    minfxn.chdcp(s_sign, s_tonicity, s_vexity)
  }

  class FunctionHack(fx: Function) {
    def apply(exprs: CvxFunExpr*) = CvxFunExpr(fx.compose(Seq(exprs:_*) map (x => x.fx)))
  }

  implicit def functionhackimpl(fx: Function) = new FunctionHack(fx)

  /*
  val square = {
    val x = cvxfunexpr()
    val t = cvxfunexpr()
    cvxfun(
      params(),
      args(scalar -> x),
      sign(positive),
      tonicity(x.sign),
      vexity(positive),
      over(scalar -> t),
      let(),
      where(
        //in_secondorder_cone(cat(2*x, t-1), t+1)
      ),
      value(t)
    )
  }
  */
}