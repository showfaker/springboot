package com.cie.nems.topology.cache.point;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cie.nems.point.PointInfoDto;
import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/point")
@Api(tags="测点缓存")
public class PointCacheController {

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@ApiOperation(value="查询测点信息缓存长度", notes="返回一个map，展示测点缓存长度，对象指标缓存长度，测点更新时间缓存长度")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Integer>> getPointCacheSize() {
		return ResponseEntity.ok(pointCacheService.getPointCacheSize());
	}

	@ApiOperation(value="根据pointId查询测点缓存信息", notes="返回一个map，key为pointId，value为测点缓存对象")
	@RequestMapping(value = "points", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<Long, PointInfoDto>> getPointCache(@RequestParam(required = true) List<Long> pointIds) {
		return ResponseEntity.ok(pointCacheService.getPointsByPointIds(pointIds));
	}

	@ApiOperation(value="根据psrId和指标查询测点缓存信息", notes="查找指定psr对象的测点，cateId或sysCateId任意一个匹配都可以返回。")
	@RequestMapping(value = "objPoints", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PointInfoDto>> getObjPoints(@RequestParam(required = true) String psrId,
			@RequestParam(required = false) Set<String> cateIds,
			@RequestParam(required = false) Set<String> sysCateIds) {
		return ResponseEntity.ok(pointCacheService.getPoints(psrId, cateIds, sysCateIds));
	}

	@ApiOperation(value="根据pointId查询测点最新时间和值", notes="返回一个map，key为pointId，value最近关系时间和值")
	@RequestMapping(value = "pointLastValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<Long, String>> getPointLastValue(@RequestParam(required = true) List<Long> pointIds) {
		return ResponseEntity.ok(pointValueCacheService.getPointUpdateTime(pointIds));
	}

	@ApiOperation(value="根据结果测点查找其计算公式", notes="根据结果测点查找其计算公式")
	@RequestMapping(value = "expression", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ExpressionDto> getExpression(@RequestParam(required = true) Long pointId) {
		return ResponseEntity.ok(pointCacheService.getPointExpressions(pointId));
	}

	@ApiOperation(value="根据被引用测点查找计算公式", notes="根据被引用测点查找计算公式")
	@RequestMapping(value = "refExpressions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ExpressionDto>> getRefExpressions(@RequestParam(required = true) Long pointId) {
		return ResponseEntity.ok(pointCacheService.getRefPointExpressions(pointId));
	}

}
