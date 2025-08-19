package com.cie.nems.topology.cache.pre;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cie.nems.pre.PointPreprocessRule;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/pre")
@Api(tags="预处理规则缓存接口")
public class PreCacheController {

	@Autowired
	private PreRuleCacheService preRuleCacheService;

	@ApiOperation(value="查询预处理规则缓存长度", notes="返回一个map，展示测点规则缓存长度，规则缓存长度")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Integer>> getPreRuleCacheSize() {
		return ResponseEntity.ok(preRuleCacheService.getPreRuleCacheSize());
	}

	@ApiOperation(value="根据ruleId查询预处理缓存信息", notes="返回一个map，key为ruleId，value为预处理规则缓存对象")
	@RequestMapping(value = "rules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<Long, PointPreprocessRule>> getPreRuleCache(@RequestParam(required = true) List<Long> ruleIds) {
		return ResponseEntity.ok(preRuleCacheService.getPreprocessRules(ruleIds));
	}

	@ApiOperation(value="根据pointId查询该测点适配的预处理缓存信息", notes="返回适用于该测点的预处理规则列表")
	@RequestMapping(value = "pointRules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PointPreprocessRule>> getObjPreRules(@RequestParam(required = true) Long pointId) {
		return ResponseEntity.ok(preRuleCacheService.getPreprocessRules(pointId));
	}

}
