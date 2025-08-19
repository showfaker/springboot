package com.cie.nems.point.expression;

import java.util.List;

import com.cie.nems.common.service.CommonService;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressionDto {
	private Long ownPointId;
	/**
	 * 计算结果输出的目标测点
	 */
	private List<TargetPointDto> targetPoints;
	/**
	 * 用于指定计算公式，程序根据expression值查找对应的实现类去执行具体的计算逻辑</br>
	 * 例如：expType我01，则查找expressionServiceImpl01的实例，并执行其中的calc方法</br>
	 * 目前支持的公式有：</br>
	 * <ul>
	 * <li>expression01 (values1[0] + values1[1] + values1[2] + ... + values1[n]) * staticParamValues[0] + staticParamValues[1]</li>
	 * <li>expression02 ((values1[0] + values1[1] + values1[2] + ... + values1[n]) * staticParamValues[0]) / ((values2[0] + values2[1] + values2[2] + ... + values2[n]) * staticParamValues[1]) + staticParamValues[2]</li>
	 * <li>expression04 ((values1[0] + values1[1] + values1[2] + ... + values1[n]) * staticParamValues[0]) + ((values2[0] + values2[1] + values2[2] + ... + values2[n]) * staticParamValues[1]) + staticParamValues[2]</li>
	 * </ul>
	 */
	private String expression;
	/**
	 * values1、values2、dynamicParamValues中有非法入参时（为空，NaN，infinite等）的处理方法，</br>
	 * 定义在ExpressionService中：</br>
	 * 	ignore：遇到非法参数（null, NaN, inifinity）忽略该参数，其他参数继续运算</br>
	 *  return：遇到非法参数（null, NaN, inifinity）直接返回空</br>
	 */
	private String illegalParamStrategy;
	
	private List<Long> points1;
	private List<Double> values1;
	private List<Long> points2;
	private List<Double> values2;
	/**
	 * 计算公式中的静态变量值
	 */
	private List<Double> staticParamValues;
	/**
	 * 计算公式中的动态变量
	 */
	private List<DynamicParamDto> dynamicParams;
	/**
	 * 计算公式中的动态变量值
	 */
	private List<Double> dynamicParamValues;
	/**
	 * 上次计算时间
	 */
	private Long lastCalcTime;
	/**
	 * 为了减少计算频率，运行对计算公式设置计算间隔周期，单位毫秒
	 * 例如：calcInterval配置为300000（5分钟），上次计算时间为10:01:00,</br>
	 * 当该计算公式引用到的任意测点有新数据到达时，如果该数据的dt（数据时间）大于等于10:06:00，则可以触发计算</br>
	 * 否则不会触发计算
	 */
	private Long calcInterval;
	
	public Long getOwnPointId() {
		return ownPointId;
	}
	public void setOwnPointId(Long ownPointId) {
		this.ownPointId = ownPointId;
	}
	public List<TargetPointDto> getTargetPoints() {
		return targetPoints;
	}
	public void setTargetPoints(List<TargetPointDto> targetPoints) {
		this.targetPoints = targetPoints;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String getIllegalParamStrategy() {
		return illegalParamStrategy;
	}
	public void setIllegalParamStrategy(String illegalParamStrategy) {
		this.illegalParamStrategy = illegalParamStrategy;
	}
	public List<Long> getPoints1() {
		return points1;
	}
	public void setPoints1(List<Long> points1) {
		this.points1 = points1;
	}
	public List<Double> getValues1() {
		return values1;
	}
	public void setValues1(List<Double> values1) {
		this.values1 = values1;
	}
	public List<Long> getPoints2() {
		return points2;
	}
	public void setPoints2(List<Long> points2) {
		this.points2 = points2;
	}
	public List<Double> getValues2() {
		return values2;
	}
	public void setValues2(List<Double> values2) {
		this.values2 = values2;
	}
	public List<Double> getStaticParamValues() {
		return staticParamValues;
	}
	public void setStaticParamValues(List<Double> staticParamValues) {
		this.staticParamValues = staticParamValues;
	}
	public List<DynamicParamDto> getDynamicParams() {
		return dynamicParams;
	}
	public void setDynamicParams(List<DynamicParamDto> dynamicParams) {
		this.dynamicParams = dynamicParams;
	}
	public List<Double> getDynamicParamValues() {
		return dynamicParamValues;
	}
	public void setDynamicParamValues(List<Double> dynamicParamValues) {
		this.dynamicParamValues = dynamicParamValues;
	}
	public Long getLastCalcTime() {
		return lastCalcTime;
	}
	public void setLastCalcTime(Long lastCalcTime) {
		this.lastCalcTime = lastCalcTime;
	}
	public Long getCalcInterval() {
		return calcInterval;
	}
	public void setCalcInterval(Long calcInterval) {
		this.calcInterval = calcInterval;
	}
	
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
