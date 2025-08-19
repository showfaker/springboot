package com.cie.nems.common.redis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;

@Service
public class RedisServiceImpl implements RedisService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource(name = "defaultRedisTemplate")
	private RedisTemplate<String, String> defaultRedisTemp;

	@Override
	public RedisTemplate<String, String> getRedisTemplate(Data data, Integer channel) {
		//TODO
		return defaultRedisTemp;
	}

	@Override
	public String type(Data data, Integer channel, String key) {
		return getRedisTemplate(data, channel).type(key).code();
	}

	@Override
	public Boolean hasKey(Data data, Integer channel, String key) {
		return getRedisTemplate(data, channel).hasKey(key);
	}

	@Override
	public Set<String> keys(Data data, Integer channel, String pattern) {
		//此种写法会锁服务器 return getRedisTemplate(data, channel).keys(pattern);
		Set<String> keys = new HashSet<String>();
		scan(data, channel, pattern, item -> {
			String key = new String(item, StandardCharsets.UTF_8);
			keys.add(key);
		});
		return keys;
	}

	private void scan(Data data, Integer channel, String pattern, Consumer<byte[]> consumer) {
		getRedisTemplate(data, channel).execute((RedisConnection connection) -> {
			try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().count(1000).match(pattern).build())) {
				cursor.forEachRemaining(consumer);
				return null;
			} catch (IOException e) {
				logger.error("execute scan failed! {}", e.getMessage());
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void delete(Data data, Integer channel, String key) {
		getRedisTemplate(data, channel).delete(key);
		logger.info("delete key channel: {}, key: {}", channel, key);
	}
	
	@Override
	public Boolean expire(Data data, Integer channel, String key, long timeout, TimeUnit unit) {
		return getRedisTemplate(data, channel).expire(key, timeout, unit);
	}

	/********************************
	 *             string           *
	 ********************************/

	@Override
	public void set(Data data, Integer channel, String key, String value) {
		getRedisTemplate(data, channel).boundValueOps(key).set(value);
	}

	@Override
	public void set(Data data, Integer channel, String key, String value, long timeout, TimeUnit unit) {
		getRedisTemplate(data, channel).boundValueOps(key).set(value, timeout, unit);
	}

	@Override
	public String get(Data data, Integer channel, String key) {
		return getRedisTemplate(data, channel).boundValueOps(key).get();
	}

	@Override
	public String getAndSet(Data data, Integer channel, String key, String value) {
		return getRedisTemplate(data, channel).boundValueOps(key).getAndSet(value);
	}

	/********************************
	 *             list             *
	 ********************************/

	@Override
	public String lpop(Data data, Integer channel, String key) {
		BoundListOperations<String, String> ops = getRedisTemplate(data, channel).boundListOps(key);
		return ops.leftPop();
	}

	@Override
	public String rpop(Data data, Integer channel, String key) {
		BoundListOperations<String, String> ops = getRedisTemplate(data, channel).boundListOps(key);
		return ops.rightPop();
	}

	@Override
	public Long lpush(Data data, Integer channel, String key, String value) {
		BoundListOperations<String, String> ops = getRedisTemplate(data, channel).boundListOps(key);
		return ops.leftPush(value);
	}

	@Override
	public Long lpush(Data data, Integer channel, String key, List<String> values) {
		BoundListOperations<String, String> ops = getRedisTemplate(data, channel).boundListOps(key);
		return ops.leftPushAll(values.toArray(new String[0]));
	}

	@Override
	public Long rpush(Data data, Integer channel, String key, String value) {
		BoundListOperations<String, String> ops = getRedisTemplate(data, channel).boundListOps(key);
		return ops.rightPush(value);
	}

	@Override
	public Long rpush(Data data, Integer channel, String key, List<String> values) {
		BoundListOperations<String, String> ops = getRedisTemplate(data, channel).boundListOps(key);
		return ops.rightPushAll(values.toArray(new String[0]));
	}

	@Override
	public Long lsize(Data data, Integer channel, String key) {
		BoundListOperations<String, String> ops = getRedisTemplate(data, channel).boundListOps(key);
		return ops.size();
	}

	/********************************
	 *             hash             *
	 ********************************/

	@Override
	public String hget(Data data, Integer channel, String key, String field) {
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		return ops.get(field);
	}

	@Override
	public List<String> hmget(Data data, Integer channel, String key, List<String> fields) {
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		return ops.multiGet(fields);
	}

	@Override
	public Long hsize(Data data, Integer channel, String key) {
		return getRedisTemplate(data, channel).boundHashOps(key).size();
	}

	@Override
	public Cursor<Entry<String, String>> hscan(Data data, Integer channel, String key, ScanOptions options) {
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		return ops.scan(options);
	}

	@Override
	public Map<String, String> hgetall(Data data, Integer channel, String key) {
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		return ops.entries();
	}

	@Override
	public void hset(Data data, Integer channel, String key, String field, String value) {
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		ops.put(field, value);
	}
	
	@Override
	public void hmset(Data data, Integer channel, String key, Map<String, String> values) {
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		ops.putAll(values);
	}

	@Override
	public void hdel(Data data, Integer channel, String key, String field) {
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		ops.delete(field);
	}

	@Override
	public void hdel(Data data, Integer channel, String key, List<String> fields) {
		if (CommonService.isEmpty(fields)) return;
		BoundHashOperations<String, String, String> ops = getRedisTemplate(data, channel).boundHashOps(key);
		ops.delete(fields.toArray());
	}

}
