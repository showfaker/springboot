package com.cie.nems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.util.SpringContextUtil;
import com.cie.nems.topology.cache.CacheService;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	@Value("${spring.profiles}")
	private String cfgProfiles;

	@Value("${server.port}")
	private int serverPort;
	
	@Value("${cie.app.id}")
	private String appId;
	
	@Value("${spring.datasource.url:#{null}}")
	private String cfgDatasourceUrl;
	@Value("${spring.datasource.username:#{null}}")
	private String cfgDatasourceUsername;
	
	@Value("${spring.data.mongodb.uri:#{null}}")
	private String cfgDataMongodbUri;
	
	@Value("${spring.redis.host:#{null}}")
	private String cfgRedisHost;
	@Value("${spring.redis.port:#{null}}")
	private Integer cfgRedisPort;
	@Value("${spring.redis.sentinel.master:#{null}}")
	private String cfgRedisSentinelMaster;
	@Value("${spring.redis.sentinel.nodes:#{null}}")
	private String cfgRedisSentinelNodes;
	@Value("${spring.redis.database:#{0}}")
	private Integer cfgRedisDatabase;

	@Value("${spring.kafka.bootstrap-servers:#{null}}")
	private String cfgKafkaBootstrapServers;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private KafkaService kafkaService;
	
	@Autowired
	private CommonService commonService;

	public static void main(String[] args) {
		System.setProperty("jasypt.encryptor.password", "upqK28TCkPwq");
		
		ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
		SpringContextUtil.setApplicationContext(applicationContext);
	}

	@Override
	public void run(String... args) throws Exception {
		printConfig();
		
		//加载缓存
		try {
			cacheService.initLocalCache();
		} catch (Exception e) {
			logger.error("local cache init failed!", e);
			System.exit(-1);
		}

		//启动消息监听器
		try {
			kafkaService.startKafkaListeners();
		} catch (Exception e) {
			logger.error("listeners start failed!", e);
			System.exit(-1);
		}
	}

	private void printConfig() {
		int maxLen = getMaxLength() + 16;
		if (maxLen < 50) maxLen = 50;

		StringBuffer cfg = new StringBuffer();
		appendCfgLine(cfg, maxLen, "*", false);
		appendCfgLine(cfg, maxLen, "NEMS-Topology", true);
		appendCfgLine(cfg, maxLen, getAppVersion(), true);
		appendCfgLine(cfg, maxLen, "*", false);
		appendCfgLine(cfg, maxLen, " localhsot:", false);
		try {
			List<InetAddress> addresses = commonService.getLocalHostLANAddresses();
			if (CommonService.isNotEmpty(addresses)) {
				InetAddress addr = null;
				for (int i=0; i<addresses.size(); ++i) {
					addr = addresses.get(i);
					appendCfgLine(cfg, maxLen, "     "+addr.getHostAddress()+" | "+addr.getHostName(), false);
				}
			}
		} catch (Exception e) {
			logger.error("get local host address failed!", e);
		}
		appendCfgLine(cfg, maxLen, "*", false);
		appendCfgLine(cfg, maxLen, " app.id: " + appId, false);
		appendCfgLine(cfg, maxLen, " profiles: " + cfgProfiles, false);
		appendCfgLine(cfg, maxLen, " server.port: " + serverPort, false);
		appendCfgLine(cfg, maxLen, " jdbc:", false);
		appendCfgLine(cfg, maxLen, "     url: " + cfgDatasourceUrl, false);
		appendCfgLine(cfg, maxLen, "     username: " + cfgDatasourceUsername, false);
		appendCfgLine(cfg, maxLen, " mongodb: " + cfgDataMongodbUri, false);
		appendCfgLine(cfg, maxLen, " redis:", false);
		appendCfgLine(cfg, maxLen, "     host: " + cfgRedisHost, false);
		appendCfgLine(cfg, maxLen, "     port: " + cfgRedisPort, false);
		appendCfgLine(cfg, maxLen, "     master: " + cfgRedisSentinelMaster, false);
		appendCfgLine(cfg, maxLen, "     nodes: " + cfgRedisSentinelNodes, false);
		appendCfgLine(cfg, maxLen, "     database: " + cfgRedisDatabase, false);
		appendCfgLine(cfg, maxLen, " kafka: " + cfgKafkaBootstrapServers, false);
		appendCfgLine(cfg, maxLen, "*", false);
		
		logger.info(cfg.toString());
	}

	private void appendCfgLine(StringBuffer cfg, int maxLen, String info, boolean isTitile) {
		cfg.append('\n');
		if ("*".equals(info) || "-".equals(info)) {
			cfg.append('*');
			for (int i=0; i<maxLen - 2; ++i) cfg.append(info);
			cfg.append('*');
		} else if (isTitile) {
			double spaceLen = (maxLen - 2 - info.length()) / 2.0;
			cfg.append('*');
			for (int i=0; i<Math.floor(spaceLen); ++i) cfg.append(' ');
			cfg.append(info);
			for (int i=0; i<Math.ceil(spaceLen); ++i) cfg.append(' ');
			cfg.append('*');
		} else {
			cfg.append('*');
			cfg.append(info);
			for (int i=0; i<(maxLen - 2 - info.length()); ++i) cfg.append(' ');
			cfg.append('*');
		}
	}

	private int getMaxLength() {
		int length = 0;
		if (cfgProfiles != null && cfgProfiles.length() > length) length = cfgProfiles.length();
		if (cfgDatasourceUrl != null && cfgDatasourceUrl.length() > length) length = cfgDatasourceUrl.length();
		if (cfgDatasourceUsername != null && cfgDatasourceUsername.length() > length) length = cfgDatasourceUsername.length();
		if (cfgDataMongodbUri != null && cfgDataMongodbUri.length() > length) length = cfgDataMongodbUri.length();
		if (cfgRedisHost != null && cfgRedisHost.length() > length) length = cfgRedisHost.length();
		if (cfgRedisSentinelMaster != null && cfgRedisSentinelMaster.length() > length) length = cfgRedisSentinelMaster.length();
		if (cfgRedisSentinelNodes != null && cfgRedisSentinelNodes.length() > length) length = cfgRedisSentinelNodes.length();
		if (cfgKafkaBootstrapServers != null && cfgKafkaBootstrapServers.length() > length) length = cfgKafkaBootstrapServers.length();
		return length;
	}

	public static String version;
	public static String getAppVersion() {
		if (version != null) return version;
		
		InputStream is = Application.class.getResourceAsStream("/doc/changelog.txt");
		if (is == null) return null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("[version]")) {
					version = "v" + StringUtils.replace(line, "[version] ", "");
					break;
				}
			}
		} catch (Exception e) {
			logger.error("read changelog.txt failed!", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("close BufferedReader failed!");
				}
			}
		}
		
		return version;
	}

}
