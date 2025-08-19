package com.cie.nems.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/test")
@Api(tags="测试接口")
public class TestController {

	@Autowired
	private TestService testService;

	@ApiOperation(value="发送测试测点数据", notes="根据")
	@RequestMapping(value = "sendTestDatas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> kafkaSendToDistr(
			@RequestParam(required = true) List<Long> pointIds,
			@RequestParam(required = false) Integer offset,
			@RequestParam(required = false) Integer limit) throws Exception {
		return ResponseEntity.ok(testService.kafkaSendToDistr(pointIds, offset, limit));
	}

}
