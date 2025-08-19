package com.cie.nems.topology.distribute.sgcc;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface SgccDistributeService {

	/**
	 * 通讯协议命令字<br/>
	 * 所有通讯协议
	 */
	public static enum ProtocolCmd {
		/** 登陆: 所有交易在登录成功后才可以正常执行，否则Server端会立即中断该连接 */
		LOGIN("00"),
		/** 注册厂站: 客户端登陆成功后执行注册厂站，告诉Server端本连接可以发送多少个厂站的数据 */
		REGIST("01"),
		/** 上送厂站测点配置信息: Server下发招厂站配置信息命令客户端收到后将厂站测点配置信息发送至Server */
		UPLOAD_POINT("02"),
		/** 主动上送测点值（不同时标）: 客户端主动上送测点实时数据信息，每条测点值带自己的时标 */
		DATTA_SAME("04"),
		/** 主动上送测点值（同一时标）: 客户端主动上送测点实时数据信息，所有测点值的时标相同 */
		DATTA_DIFF("05"),
		/** 采集器上报告警 */
		COLLECTER_ALARM("06"),
		/** 召测测点值: Server端向客户端发送测点信息，主动获取需要的测点值信息 */
		QUERY("07"),
		/** 更改厂站测点配置信息: 客户端将测点配置信息的更改及时通知到Server端 */
		UPDATE("03"),
		/** 链路测试（心跳）:  客户端周期性（每空闲15~30分钟）向Server端发送心跳报文，Server端会即时响应 */
		HEART("99"),
		/** 网关下线 */
		OFFLINE("FF");
		
		private String value;
		private ProtocolCmd(String value) {this.value = value;}
		public String toString() {return value;}
	}

	public void execute(ConsumerRecord<Integer, String> msg) throws Exception;

}
