package com.cie.nems.common.redis;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

public interface RedisService {

	public static enum Data {
		DEFAULT,			//默认 
		DATA_TIME, 			//数据更新时间
		POINT_CURR_VALUE,	//测点当前值
		POINT_VALUES,		//测点历史数据曲线
		POINT_REMAIN_TIME,	//死数规则测点的相同值持续时间
		ALARM_REMAIN_TIME,	//测点符合告警规则持续时间
		ALARM_NOTICE		//告警通知消息
	};
	
	public RedisTemplate<String, String> getRedisTemplate(Data data, Integer channel);

	public String type(Data data, Integer channel, String key);
	public Boolean hasKey(Data data, Integer channel, String key);
	public Set<String> keys(Data data, Integer channel, String pattern);
	public void delete(Data data, Integer channel, String key);
	public Boolean expire(Data data, Integer channel, String key, long timeout, TimeUnit unit);

	/* string */
	public void set(Data data, Integer channel, String key, String value);
	public void set(Data data, Integer channel, String key, String value, long timeout, TimeUnit unit);
	public String get(Data data, Integer channel, String key);
	public String getAndSet(Data data, Integer channel, String key, String value);
	
	/* list right是底部，left是头部 */
	public String lpop(Data data, Integer channel, String key);
	public String rpop(Data data, Integer channel, String key);
	public Long lpush(Data data, Integer channel, String key, String value);
	public Long lpush(Data data, Integer channel, String key, List<String> values);
	public Long rpush(Data data, Integer channel, String key, String value);
	public Long rpush(Data data, Integer channel, String key, List<String> values);
	public Long lsize(Data data, Integer channel, String key);

	/* hash */
	public String hget(Data data, Integer channel, String key, String field);
	public List<String> hmget(Data data, Integer channel, String key, List<String> fields);
	public Long hsize(Data data, Integer channel, String key);
	public Cursor<Entry<String, String>> hscan(Data data, Integer channel, String key, ScanOptions options);
	public Map<String, String> hgetall(Data data, Integer channel, String key);
	public void hset(Data data, Integer channel, String key, String field, String value);
	public void hmset(Data data, Integer channel, String key, Map<String, String> values);
	public void hdel(Data data, Integer channel, String key, String field);
	public void hdel(Data data, Integer channel, String key, List<String> fields);

}
