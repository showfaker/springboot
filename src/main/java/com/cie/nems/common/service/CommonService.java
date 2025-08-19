package com.cie.nems.common.service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.cie.nems.common.Constants;
import com.cie.nems.common.TreeData;
import com.cie.nems.common.exception.NemsException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 系统工具类
 */
@Component
public class CommonService {
	/**
	 * 预定义的日期字符串格式
	 */
	public static enum DateFormat {
		yy, yyyy, yyMM, yyyyMM, yyMMdd, yyyyMMdd, MMddHH, HHmmss, HHmm, HH
	};

	/**
	 * 时间类型
	 */
	public static enum TimeType {
		SECOND, MINUTE, HOUR, DAY, MONTH, QUARTER, YEAR
	};

	/**
	 * get formated string value from date
	 * 
	 * @param date
	 * @param fmt
	 *            日期格式，定义在枚举DateFormat中
	 * @return 格式化的日期字符串
	 */
	public static String getDateStringValue(Date date, DateFormat fmt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getDateStringValue(cal, fmt);
	}

	/**
	 * get formated string value from date
	 * 
	 * @param date
	 * @param fmt
	 *            日期格式，定义在枚举DateFormat中
	 * @return 格式化的日期字符串
	 */
	public static String getDateStringValue(Calendar cal, DateFormat fmt) {
		if (cal == null || fmt == null)
			return null;
		if (DateFormat.yy == fmt) {
			return String.valueOf(cal.get(Calendar.YEAR) % 100);
		} else if (DateFormat.yyyy == fmt) {
			return String.valueOf(cal.get(Calendar.YEAR));
		} else if (DateFormat.yyMM == fmt) {
			return String.valueOf(cal.get(Calendar.YEAR) % 100 * 100 + (cal.get(Calendar.MONTH) + 1));
		} else if (DateFormat.yyyyMM == fmt) {
			return String.valueOf(cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1));
		} else if (DateFormat.yyMMdd == fmt) {
			return String.valueOf(cal.get(Calendar.YEAR) % 100 * 10000 + (cal.get(Calendar.MONTH) + 1) * 100
					+ cal.get(Calendar.DAY_OF_MONTH));
		} else if (DateFormat.yyyyMMdd == fmt) {
			return String.valueOf(cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100
					+ cal.get(Calendar.DAY_OF_MONTH));
		} else if (DateFormat.MMddHH == fmt) {
			return StringUtils.leftPad(String.valueOf(cal.get(Calendar.MONTH) * 10000
					+ cal.get(Calendar.DAY_OF_MONTH) * 100 + cal.get(Calendar.HOUR_OF_DAY)), 6, '0');
		} else if (DateFormat.HHmmss == fmt) {
			return StringUtils.leftPad(String.valueOf(
					cal.get(Calendar.HOUR_OF_DAY) * 10000 + cal.get(Calendar.MINUTE) * 100 + cal.get(Calendar.SECOND)),
					6, '0');
		} else if (DateFormat.HHmm == fmt) {
			return StringUtils.leftPad(String.valueOf(
					cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE)), 4, '0');
		} else if (DateFormat.HH == fmt) {
			return StringUtils.leftPad(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)), 2, '0');
		} else {
			return null;
		}
	}

	/**
	 * get formated string value from date
	 * 
	 * @param date
	 * @param fmt
	 *            日期格式，定义在枚举DateFormat中
	 * @return 格式化的日期字符串
	 */
	public static long getDateLongValue(Date date, DateFormat fmt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getDateLongValue(cal, fmt);
	}

	/**
	 * get formated long value from date
	 * 
	 * @param date
	 * @param fmt
	 *            日期格式，定义在枚举DateFormat中
	 * @return 格式化的日期字符串
	 */
	public static long getDateLongValue(Calendar cal, DateFormat fmt) {
		if (cal == null || fmt == null)
			return 0L;
		if (DateFormat.yy == fmt) {
			return (long) (cal.get(Calendar.YEAR) % 100);
		} else if (DateFormat.yyyy == fmt) {
			return (long) cal.get(Calendar.YEAR);
		} else if (DateFormat.yyMM == fmt) {
			return (long) (cal.get(Calendar.YEAR) % 100 * 100 + (cal.get(Calendar.MONTH) + 1));
		} else if (DateFormat.yyyyMM == fmt) {
			return (long) (cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1));
		} else if (DateFormat.yyMMdd == fmt) {
			return (long) (cal.get(Calendar.YEAR) % 100 * 10000 + (cal.get(Calendar.MONTH) + 1) * 100
					+ cal.get(Calendar.DAY_OF_MONTH));
		} else if (DateFormat.yyyyMMdd == fmt) {
			return (long) (cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100
					+ cal.get(Calendar.DAY_OF_MONTH));
		} else if (DateFormat.MMddHH == fmt) {
			return (long) (cal.get(Calendar.MONTH) * 10000 + cal.get(Calendar.DAY_OF_MONTH) * 100
					+ cal.get(Calendar.HOUR_OF_DAY));
		} else if (DateFormat.HHmmss == fmt) {
			return (long) (cal.get(Calendar.HOUR_OF_DAY) * 10000 + cal.get(Calendar.MINUTE) * 100
					+ cal.get(Calendar.SECOND));
		} else if (DateFormat.HHmm == fmt) {
			return (long) (cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE));
		} else if (DateFormat.HH == fmt) {
			return (long) (cal.get(Calendar.HOUR_OF_DAY));
		} else {
			return 0L;
		}
	}

	public static Integer daysBetween(Date start, Date end) {
		if (start == null || end == null) return null;
		System.out.println("-----------------------------");
		System.out.println(end.getTime() - start.getTime());
		System.out.println("-----------------------------");
		return (int)((end.getTime() - start.getTime()) / 86400000);
	}

	public static Integer monthsBetween(Date start, Date end, boolean compareDate) {
		if (start == null || end == null) return null;
		
		Calendar cs = Calendar.getInstance();
		cs.setTime(start);
		cs = trunc(cs, TimeType.MONTH);
		
		Calendar ce = Calendar.getInstance();
		ce.setTime(end);
		ce = trunc(ce, TimeType.MONTH);
		
		int months = 0;
		if (cs.before(ce)) {
			while (cs.before(ce)) {
				cs.add(Calendar.MONTH, 1);
				++months;
			}
			if (compareDate) {
				Calendar csd = Calendar.getInstance();
				csd.setTime(start);
				int sd = csd.get(Calendar.DAY_OF_MONTH);
				Calendar ced = Calendar.getInstance();
				ced.setTime(start);
				int ed = ced.get(Calendar.DAY_OF_MONTH);
				if (sd > ced.getActualMaximum(Calendar.DAY_OF_MONTH)) {
					 sd = ced.getActualMaximum(Calendar.DAY_OF_MONTH);
				}
				if (ed < sd) {	//未满一个月不算
					--months;
				}
			}
		} else {
			while (cs.after(ce)) {
				cs.add(Calendar.MONTH, -1);
				--months;
			}
			if (compareDate) {
				Calendar csd = Calendar.getInstance();
				csd.setTime(start);
				int sd = csd.get(Calendar.DAY_OF_MONTH);
				Calendar ced = Calendar.getInstance();
				ced.setTime(start);
				int ed = ced.get(Calendar.DAY_OF_MONTH);
				if (sd < ed) {	//未满一个月不算
					++months;
				}
			}
		}
		return months;
	}

	/**
	 * 求两个日期之间相差的月份数
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static Integer monthsBetween(Date date1, Date date2) {
		if (date1 == null || date2 == null) return null;
		
		Calendar c1 = Calendar.getInstance();
		c1.setTime(date1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(date2);
		return monthsBetween(c1, c2);
	}

	/**
	 * 求两个日期之间相差的月份数
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static Integer monthsBetween(Calendar c1, Calendar c2) {
		if (c1 == null || c2 == null) return null;
		int y1 = c1.get(Calendar.YEAR);
		int y2 = c2.get(Calendar.YEAR);
		
		return (y2 * 12) + c2.get(Calendar.MONTH) - (y1 * 12) - c1.get(Calendar.MONTH);
	}

	/**
	 * 获取指定时间周期的开始时间
	 * 
	 * @param c
	 * @param type
	 *            MINUTE: 2017-06-07 19:04:12 -> 2017-06-07 19:04:00 HOUR:
	 *            2017-06-07 19:04:12 -> 2017-06-07 19:00:00 DAY: 2017-06-07
	 *            19:04:12 -> 2017-06-07 00:00:00 MONTH: 2017-06-07 19:04:12 ->
	 *            2017-06-01 00:00:00 YEAR: 2017-06-07 19:04:12 -> 2017-01-01
	 *            00:00:00
	 * @return
	 */
	public static Calendar trunc(Calendar c, TimeType type) {
		if (c == null)
			return null;

		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(c.getTimeInMillis());
		t.set(Calendar.MILLISECOND, 0);
		if (type == TimeType.SECOND) return t;
		
		t.set(Calendar.SECOND, 0);
		if (type == TimeType.MINUTE) return t;
		
		t.set(Calendar.MINUTE, 0);
		if (type == TimeType.HOUR) return t;
		
		t.set(Calendar.HOUR_OF_DAY, 0);
		if (type == TimeType.DAY) return t;
		
		t.set(Calendar.DATE, 1);
		if (type == TimeType.MONTH) return t;
		
		if (type == TimeType.QUARTER) {
			t.set(Calendar.MONTH, t.get(Calendar.MONTH) - (t.get(Calendar.MONTH) % 3));
		} else {
			t.set(Calendar.MONTH, Calendar.JANUARY);
		}
		return t;
	}

	/**
	 * 获取指定时间周期的开始时间
	 * 
	 * @param c
	 * @param type
	 *            MINUTE: 2017-06-07 19:04:12 -> 2017-06-07 19:04:00 HOUR:
	 *            2017-06-07 19:04:12 -> 2017-06-07 19:00:00 DAY: 2017-06-07
	 *            19:04:12 -> 2017-06-07 00:00:00 MONTH: 2017-06-07 19:04:12 ->
	 *            2017-06-01 00:00:00 YEAR: 2017-06-07 19:04:12 -> 2017-01-01
	 *            00:00:00
	 * @return
	 */
	public static Date trunc(Date time, TimeType type) {
		if (time == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(time);
		return trunc(c, type).getTime();
	}

	/**
	 * 获取指定时间周期的开始时间
	 * 
	 * @param c
	 * @param type
	 *            MINUTE: 2017-06-07 19:04:12 -> 2017-06-07 19:04:00 HOUR:
	 *            2017-06-07 19:04:12 -> 2017-06-07 19:00:00 DAY: 2017-06-07
	 *            19:04:12 -> 2017-06-07 00:00:00 MONTH: 2017-06-07 19:04:12 ->
	 *            2017-06-01 00:00:00 YEAR: 2017-06-07 19:04:12 -> 2017-01-01
	 *            00:00:00
	 * @return
	 */
	public static Timestamp trunc(Timestamp time, TimeType type) {
		if (time == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(time);
		c = trunc(c, type);
		return new Timestamp(c.getTimeInMillis());
	}

	/**
	 * 获取指定时间周期的最后一刻时间
	 * 
	 * @param c
	 * @param type
	 *            MINUTE: 2017-06-07 19:04:12 -> 2017-06-07 19:04:59 HOUR:
	 *            2017-06-07 19:04:12 -> 2017-06-07 19:59:59 DAY: 2017-06-07
	 *            19:04:12 -> 2017-06-07 23:59:59 MONTH: 2017-06-07 19:04:12 ->
	 *            2017-06-30 23:59:59 YEAR: 2017-06-07 19:04:12 -> 2017-12-31
	 *            23:59:59
	 * @return
	 */
	public static Calendar getEndTime(Calendar c, TimeType type) {
		if (c == null)
			return null;

		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(c.getTimeInMillis());
		t.set(Calendar.MILLISECOND, 999);
	
		if (type == TimeType.MINUTE) {
			t.set(Calendar.SECOND, 59);
		} else if (type == TimeType.HOUR) {
			t.set(Calendar.SECOND, 59);
			t.set(Calendar.MINUTE, 59);
		} else if (type == TimeType.DAY) {
			t.set(Calendar.SECOND, 59);
			t.set(Calendar.MINUTE, 59);
			t.set(Calendar.HOUR_OF_DAY, 23);
		} else if (type == TimeType.MONTH) {
			t.set(Calendar.SECOND, 59);
			t.set(Calendar.MINUTE, 59);
			t.set(Calendar.HOUR_OF_DAY, 23);
			t.set(Calendar.DAY_OF_MONTH, t.getActualMaximum(Calendar.DAY_OF_MONTH));
		} else if (type == TimeType.QUARTER) {
			t.set(Calendar.DAY_OF_MONTH, 1);
			t.set(Calendar.SECOND, 59);
			t.set(Calendar.MINUTE, 59);
			t.set(Calendar.HOUR_OF_DAY, 23);
			t.set(Calendar.MONTH, t.get(Calendar.MONTH) - (t.get(Calendar.MONTH) % 3) + 2);	
			t.set(Calendar.DAY_OF_MONTH, t.getActualMaximum(Calendar.DAY_OF_MONTH));
		} else if (type == TimeType.YEAR) {
			t.set(Calendar.SECOND, 59);
			t.set(Calendar.MINUTE, 59);
			t.set(Calendar.HOUR_OF_DAY, 23);
			t.set(Calendar.MONTH, t.getActualMaximum(Calendar.MONTH));
			t.set(Calendar.DAY_OF_MONTH, t.getActualMaximum(Calendar.DAY_OF_MONTH));
		}
		
		return t;
	}

	/**
	 * 获取指定时间周期的最后一刻时间
	 * 
	 * @param time
	 * @param type
	 *            MINUTE: 2017-06-07 19:04:12 -> 2017-06-07 19:04:59 HOUR:
	 *            2017-06-07 19:04:12 -> 2017-06-07 19:59:59 DAY: 2017-06-07
	 *            19:04:12 -> 2017-06-07 23:59:59 MONTH: 2017-06-07 19:04:12 ->
	 *            2017-06-30 23:59:59 YEAR: 2017-06-07 19:04:12 -> 2017-12-31
	 *            23:59:59
	 * @return
	 */
	public static Date getEndTime(Date time, TimeType type) {
		if (time == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(time);
		return getEndTime(c, type).getTime();
	}

	/**
	 * 获取指定时间周期的最后一刻时间
	 * 
	 * @param time
	 * @param type
	 *            MINUTE: 2017-06-07 19:04:12 -> 2017-06-07 19:04:59 HOUR:
	 *            2017-06-07 19:04:12 -> 2017-06-07 19:59:59 DAY: 2017-06-07
	 *            19:04:12 -> 2017-06-07 23:59:59 MONTH: 2017-06-07 19:04:12 ->
	 *            2017-06-30 23:59:59 YEAR: 2017-06-07 19:04:12 -> 2017-12-31
	 *            23:59:59
	 * @return
	 */
	public static Timestamp getEndTime(Timestamp time, TimeType type) {
		if (time == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(time);
		c = getEndTime(c, type);
		return new Timestamp(c.getTimeInMillis());
	}

	/**
	 * 消除字符串左侧指定的内容 ltrim("abcdefg", "ab") : "cdefg"
	 */
	public static String ltrim(String str, String chr) {
		if (str == null || str.length() == 0)
			return str;
		if (chr == null || chr.length() == 0)
			return str;
		if (str.length() < chr.length())
			return str;

		StringBuffer sb = new StringBuffer(str);
		int chrlen = chr.length();
		while (chr.equals(sb.substring(0, chr.length()))) {
			sb.delete(0, chr.length());
			if (sb.length() < chrlen)
				break;
		}
		return sb.toString();
	}

	/**
	 * 消除字符串右侧指定的内容 rtrim("abcdefg", "fg") : "abcde"
	 */
	public static String rtrim(String str, String chr) {
		if (str == null || str.length() == 0)
			return str;
		if (chr == null || chr.length() == 0)
			return str;
		if (str.length() < chr.length())
			return str;

		StringBuffer sb = new StringBuffer(str);
		int chrlen = chr.length();
		while (chr.equals(sb.substring(sb.length() - chrlen, sb.length()))) {
			sb.delete(sb.length() - chrlen, sb.length());
			if (sb.length() < chrlen)
				break;
		}
		return sb.toString();
	}

	/**
	 * 消除字符串两侧指定的内容 trim("abcdefgab", "ab") : "cdefg"
	 */
	public static String trim(String str, String chr) {
		return rtrim(ltrim(str, chr), chr);
	}

	/**
	 * 将set用字符separator连起来得到一个字符串
	 * 
	 * @param set
	 * @param separator
	 * @return
	 */
	public static <E> String join(Set<E> list, char separator) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null) sb.append(o);
		}
		return sb.substring(1).toString();
	}

	/**
	 * 将set用字符separator连起来得到一个字符串，并对每个对象添加引号
	 * 
	 * @param list
	 * @param separator
	 * @return
	 */
	public static <E> String join(Set<E> list, char separator, char quote) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null)
				sb.append(quote).append(o).append(quote);
		}
		return sb.substring(1).toString();
	}

	/**
	 * 将列表list用字符separator连起来得到一个字符串
	 * 
	 * @param list
	 * @param separator
	 * @return
	 */
	public static <E> String join(List<E> list, char separator) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null) sb.append(o);
		}
		return sb.substring(1).toString();
	}

	/**
	 * 将列表list用字符separator连起来得到一个字符串，并对每个对象添加引号
	 * 
	 * @param list
	 * @param separator
	 * @return
	 */
	public static <E> String join(List<E> list, char separator, char quote) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null)
				sb.append(quote).append(o).append(quote);
		}
		return sb.substring(1).toString();
	}

	/**
	 * 将数组array用字符separator连起来得到一个字符串
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static <E> String join(E[] array, char separator) {
		if (array == null)
			return null;
		if (array.length == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : array) {
			sb.append(separator);
			if (o != null) sb.append(o);
		}
		return sb.substring(1).toString();
	}

	/**
	 * 将数组array用字符separator连起来得到一个字符串，并对每个对象添加引号
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static <E> String join(E[] array, char separator, char quote) {
		if (array == null)
			return null;
		if (array.length == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : array) {
			sb.append(separator);
			if (o != null)
				sb.append(quote).append(o).append(quote);
		}
		return sb.substring(1).toString();
	}

	/**
	 * 将set用字符串separator连起来得到一个字符串
	 * 
	 * @param set
	 * @param separator
	 * @return
	 */
	public static <E> String join(Set<E> list, String separator) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null) sb.append(o);
		}
		if (separator != null)
			return sb.substring(separator.length()).toString();
		else
			return sb.toString();
	}

	/**
	 * 将set用字符串separator连起来得到一个字符串，并对每个对象添加引号
	 * 
	 * @param set
	 * @param separator
	 * @return
	 */
	public static <E> String join(Set<E> list, String separator, String quote) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null) {
				if (quote != null)
					sb.append(quote);
				sb.append(o);
				if (quote != null)
					sb.append(quote);
			}
		}
		if (separator != null)
			return sb.substring(separator.length()).toString();
		else
			return sb.toString();
	}

	/**
	 * 将列表list用字符串separator连起来得到一个字符串
	 * 
	 * @param list
	 * @param separator
	 * @return
	 */
	public static <E> String join(List<E> list, String separator) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null) sb.append(o);
		}
		if (separator != null)
			return sb.substring(separator.length()).toString();
		else
			return sb.toString();
	}

	/**
	 * 将列表list用字符串separator连起来得到一个字符串，并对每个对象添加引号
	 * 
	 * @param list
	 * @param separator
	 * @return
	 */
	public static <E> String join(List<E> list, String separator, String quote) {
		if (list == null)
			return null;
		if (list.size() == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator);
			if (o != null) {
				if (quote != null)
					sb.append(quote);
				sb.append(o);
				if (quote != null)
					sb.append(quote);
			}
		}
		if (separator != null)
			return sb.substring(separator.length()).toString();
		else
			return sb.toString();
	}

	/**
	 * 将数组array用字符串separator连起来得到一个字符串
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static <E> String join(E[] array, String separator) {
		if (array == null)
			return null;
		if (array.length == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : array) {
			sb.append(separator);
			if (o != null) sb.append(o);
		}
		if (separator != null)
			return sb.substring(separator.length()).toString();
		else
			return sb.toString();
	}

	/**
	 * 将数组array用字符串separator连起来得到一个字符串，并对每个对象添加引号
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static <E> String join(E[] array, String separator, String quote) {
		if (array == null)
			return null;
		if (array.length == 0)
			return "";

		StringBuffer sb = new StringBuffer();
		for (Object o : array) {
			sb.append(separator);
			if (o != null) {
				if (quote != null)
					sb.append(quote);
					sb.append(o);
				if (quote != null)
					sb.append(quote);
			}
		}
		if (separator != null)
			return sb.substring(separator.length()).toString();
		else
			return sb.toString();
	}

	/**
	 * 获取最接近的时间，例如： 当interval为5（分钟）时 12:03:12 -> 12:05:00 12:02:29 -> 12:00:00
	 * 
	 * @param time
	 * @param interval
	 *            单位：分钟
	 * @return
	 */
	public static Date getRecentTime(Date time, int interval) {
		if (time == null || interval <= 0) {
			return null;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTime(time);
			int minute = c.get(Calendar.MINUTE);
			minute = minute % interval;
			int second = c.get(Calendar.SECOND);
			second = minute * 60 + second;
			if (second > interval * 60 / 2) {
				c.add(Calendar.MINUTE, interval - minute);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
			} else {
				c.add(Calendar.MINUTE, -minute);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MILLISECOND, 0);
			}
			return c.getTime();
		}
	}

	/**
	 * 获取上一个时间，例如： 当interval为5（分钟）时 12:03:12 -> 12:00:00 12:02:29 -> 12:00:00
	 * 
	 * @param time
	 * @param interval
	 *            单位：分钟
	 * @return
	 */
	public static Date getPreTime(Date time, int interval) {
		if (time == null || interval <= 0) {
			return null;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTime(time);
			int minute = c.get(Calendar.MINUTE);
			minute = minute % interval;
			c.add(Calendar.MINUTE, -minute);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		}
	}

	/**
	 * 获取下一个时间，例如： 当interval为5（分钟）时 12:03:12 -> 12:05:00 12:02:29 -> 12:05:00
	 * 
	 * @param time
	 * @param interval
	 *            单位：分钟
	 * @return
	 */
	public static Date getNextTime(Date time, int interval) {
		if (time == null || interval <= 0) {
			return null;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTime(time);
			int minute = c.get(Calendar.MINUTE);
			minute = minute % interval;
			c.add(Calendar.MINUTE, interval - minute);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		}
	}

	/**
	 * 获取字符串s的MD5
	 * 
	 * @param s
	 * @return
	 */
	private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	public final static String md5(String s) {
		try {
			byte[] btInput = s.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str).toLowerCase();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isIp(String text) {
		if (StringUtils.isNotBlank(text)) {
			String regex = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";

			Pattern pattern = Pattern.compile(regex);
			return pattern.matcher(text).matches();
		}
		return false;
	}

	public static boolean isPort(Integer port) {
		if (port == null) return false;
		if (port < 0) return false;
		if (port > 65536) return false;
		return true;
	}

	/**
	 * 将compareStr字符串的内容和strings列表里的内容进行比较，找出字符串相似度>=similarity的字符串
	 * @param strings
	 * @param compareStr
	 * @param similarity
	 * @return
	 * @throws NemsException
	 */
	public static List<String> calcStringSimilarity(List<String> strings, String compareStr, double similarity) throws NemsException {
		if (strings == null || strings.isEmpty()) {
			return strings;
		}
		if (similarity <= 0.0) {
			similarity = 0.8;
		}
		List<String> result = new ArrayList<String>();
		for (String s : strings) {
			double sim = calcStringSimilarity(compareStr, s);
			if (sim >= similarity) {
				result.add(s);
			}
		}
		return result;
	}

	/**
	 * 计算两个字符串之间的相似度
	 * @param str1
	 * @param str2
	 * @return
	 * @throws NemsException 
	 */
	public static double calcStringSimilarity(String str1, String str2) throws NemsException {
		if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
			throw new NemsException("字符串为空!");
		}

		Map<Integer, int[]> map = new HashMap<Integer, int[]>();
		for (int i = 0; i < str1.length(); i++) {
			char d1 = str1.charAt(i);
			int charIndex = d1;
			int[] fq = (int[]) map.get(Integer.valueOf(charIndex));
			if ((fq != null) && (fq.length == 2)) {
				fq[0] += 1;
			} else {
				fq = new int[2];
				fq[0] = 1;
				fq[1] = 0;
				map.put(Integer.valueOf(charIndex), fq);
			}
		}
		for (int i = 0; i < str2.length(); i++) {
			char d2 = str2.charAt(i);
			int charIndex = d2;
			int[] fq = (int[]) map.get(Integer.valueOf(charIndex));
			if ((fq != null) && (fq.length == 2)) {
				fq[1] += 1;
			} else {
				fq = new int[2];
				fq[0] = 0;
				fq[1] = 1;
				map.put(Integer.valueOf(charIndex), fq);
			}
		}
		Iterator<Integer> iterator = map.keySet().iterator();
		double sqdoc1 = 0.0D;
		double sqdoc2 = 0.0D;
		double denominator = 0.0D;
		while (iterator.hasNext()) {
			int[] c = (int[]) map.get(iterator.next());
			denominator += c[0] * c[1];
			sqdoc1 += c[0] * c[0];
			sqdoc2 += c[1] * c[1];
		}
		return denominator / Math.sqrt(sqdoc1 * sqdoc2);
	}
	
	public static String formatDuration(Long duration, boolean msec) {
		if (duration == null || duration <= 0L) return "";
		long year = duration / 31536000000L;
		long day = duration % 31536000000L / 86400000;
		long hour = duration % 31536000000L % 86400000 / 3600000;
		long minute = duration % 31536000000L % 86400000 % 3600000 / 60000;
		long second = duration % 31536000000L % 86400000 % 3600000 % 60000 / 1000;
		long mSecond = duration % 31536000000L % 86400000 % 3600000 % 60000 % 1000;
		String time = "";
		if (year > 0L) time += year + "年";
		if (day > 0L) time += day + "天";
		if (hour > 0L) time += hour + "小时";
		if (minute > 0L) time += minute + "分";
		if (second > 0L) time += second + "秒";
		if (msec && mSecond > 0L) time += mSecond + "毫秒";
		return time;
	}
	
	public static String formatDate(String fmt, Date time) {
		if (fmt == null || time == null) return null;
		return new SimpleDateFormat(fmt).format(time);
	}

	public static Date parseDate(String fmt, String time) throws ParseException {
		if (fmt == null || StringUtils.isBlank(time)) return null;
		return new SimpleDateFormat(fmt).parse(time);
	}

	public static Date parseDate(String fmt, String time, Date defaultValue) throws ParseException {
		if (fmt == null || StringUtils.isBlank(time)) return defaultValue;
		return new SimpleDateFormat(fmt).parse(time);
	}
	
	public static Calendar getCalendar(Date date) {
		if (date == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public static Float round(Float d, int precision) {
		if (d == null || d.isInfinite() || d.isNaN()) {
			return d;
		}
		return new Float(Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision));
	}
	
	public static Double round(Double d, int precision) {
		if (d == null || d.isInfinite() || d.isNaN()) {
			return d;
		}
		return Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision);
	}
	
	public static Double formatEnergy(Double e, int precision) {
		if (e == null || e.isInfinite() || e.isNaN()) {
			return e;
		}
		if (e >= 100000000.0) {
			return round(e / 100000000.0, precision);
		} else if (e >= 10000.0) {
			return round(e / 10000.0, precision);
		} else {
			return round(e, precision);
		}
	}
	
	public static String getEnergyUnit(Double e) {
		if (e == null || e.isInfinite() || e.isNaN()) {
			return "kWh";
		}
		if (e >= 100000000.0) {
			return "亿kWh";
		} else if (e >= 10000.0) {
			return "万kWh";
		} else {
			return "kWh";
		}
	}
	
	public static Double formatPower(Double e, int precision) {
		if (e == null || e.isInfinite() || e.isNaN()) {
			return e;
		}
		if (e >= 100000000.0) {
			return round(e / 100000000.0, precision);
		} else if (e >= 10000.0) {
			return round(e / 10000.0, precision);
		} else {
			return round(e, precision);
		}
	}
	
	public static String getPowerUnit(Double e) {
		if (e == null || e.isInfinite() || e.isNaN()) {
			return "kW";
		}
		if (e >= 100000000.0) {
			return "亿kW";
		} else if (e >= 10000.0) {
			return "万kW";
		} else {
			return "kW";
		}
	}

	public static Double formatNumber(Double e, int precision) {
		if (e == null || e.isInfinite() || e.isNaN()) {
			return e;
		}
		if (e >= 100000000.0) {
			return round(e / 100000000.0, precision);
		} else if (e >= 10000.0) {
			return round(e / 10000.0, precision);
		} else {
			return round(e, precision);
		}
	}
	
	public static String getNumberUnit(Double e, String unit) {
		if (e == null || e.isInfinite() || e.isNaN()) {
			return unit;
		}
		if (e >= 100000000.0) {
			return "亿"+unit;
		} else if (e >= 10000.0) {
			return "万"+unit;
		} else {
			return unit;
		}
	}

	/** return FALSE is value is null or equals to 0, else return TRUE */
	public static boolean isNotEmpty(Integer value) {
		return (value == null || value == 0) ? false : true;
	}
	/** return TRUE is value is null or equals to 0, else return FALSE */
	public static boolean isEmpty(Integer value) {
		return (value == null || value == 0) ? true : false;
	}
	/** return FALSE is value is null or equals to 0.0, else return TRUE */
	public static boolean isNotEmpty(Double value) {
		return (value == null || value == 0.0) ? false : true;
	}
	/** return TRUE is value is null or equals to 0.0, else return FALSE */
	public static boolean isEmpty(Double value) {
		return (value == null || value == 0.0) ? true : false;
	}
	/** return FALSE is value is null or equals to 0L, else return TRUE */
	public static boolean isNotEmpty(Long value) {
		return (value == null || value == 0L) ? false : true;
	}
	/** return TRUE is value is null or equals to 0L, else return FALSE */
	public static boolean isEmpty(Long value) {
		return (value == null || value == 0L) ? true : false;
	}
	
	public static <E> boolean isNotEmpty(Page<E> list) {
		return (list == null || !list.hasContent() || list.getContent() == null || list.getContent().isEmpty()) ? false : true;
	}
	public static <E> boolean isEmpty(Page<E> list) {
		return (list == null || !list.hasContent() || list.getContent() == null || list.getContent().isEmpty()) ? true : false;
	}
	public static <E> boolean isNotEmpty(List<E> list) {
		return (list == null || list.isEmpty()) ? false : true;
	}
	public static <E> boolean isEmpty(List<E> list) {
		return (list == null || list.isEmpty()) ? true : false;
	}
	public static <E> boolean isNotEmpty(E[] array) {
		return (array == null || array.length == 0) ? false : true;
	}
	public static <E> boolean isEmpty(E[] array) {
		return (array == null || array.length == 0) ? true : false;
	}
	public static <E> boolean isNotEmpty(Set<E> list) {
		return (list == null || list.isEmpty()) ? false : true;
	}
	public static <E> boolean isEmpty(Set<E> list) {
		return (list == null || list.isEmpty()) ? true : false;
	}
	public static <E,T> boolean isNotEmpty(Map<E, T> map) {
		return (map == null || map.isEmpty()) ? false : true;
	}
	public static <E,T> boolean isEmpty(Map<E, T> map) {
		return (map == null || map.isEmpty()) ? true : false;
	}
	

	/**
	 * @return 当前JVM进程名称，格式为：pid@machineName
	 */
	public static final String getProcessName() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		return runtimeMXBean.getName();
	}

	/**
	 * @return 当前JVM进程名称，格式为：pid@machineName
	 */
	public static final Integer getProcessID() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
	}


	@Value("${cie.host-address:#{null}}")
	private String ip;

	/**
	 * @return 当前设备第一个合法的IP地址
	 * @throws Exception
	 */
	public InetAddress getLocalHostLANAddress() throws Exception {
		try {
			InetAddress candidateAddress = null;
			// 遍历所有的网络接口
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); 
					ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// 在所有的接口下再遍历IP
				for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
						if (inetAddr.isSiteLocalAddress()) {
							// 如果是site-local地址，就是它了
							if (ip == null) {
								return inetAddr;
							} else if (ip.equals(inetAddr.getHostAddress())) {
								return inetAddr;
							}
						} else if (candidateAddress == null) {
							// site-local类型的地址未被发现，先记录候选地址
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null) {
				return candidateAddress;
			}
			// 如果没有发现 non-loopback地址.只能用最次选的方案
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			return jdkSuppliedAddress;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return 当前设备所有合法的IP地址
	 * @throws Exception
	 */
	public List<InetAddress> getLocalHostLANAddresses () throws Exception {
		List<InetAddress> list = new ArrayList<InetAddress>(5);
		try {
			InetAddress candidateAddress = null;
			// 遍历所有的网络接口
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
					.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// 在所有的接口下再遍历IP
				for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
						if (inetAddr.isSiteLocalAddress()) {
							// 如果是site-local地址，就是它了
							list.add(inetAddr);
						} else if (candidateAddress == null) {
							// site-local类型的地址未被发现，先记录候选地址
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (list.isEmpty() && candidateAddress != null) {
				list.add(candidateAddress);
			}
			// 如果没有发现 non-loopback地址.只能用最次选的方案
			if (list.isEmpty()) {
				InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
				list.add(jdkSuppliedAddress);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static String getUUID32() {
		return UUID.randomUUID().toString().replace("-", "").toLowerCase();
	}

	public static int countResult(int[] result) {
		if (result == null || result.length == 0) return 0;
		int count = 0;
		for (int r : result) {
			count += r;
		}
		return count;
	}

	public static void setSqlParam(PreparedStatement ps, int i, Object value, int type) throws SQLException {
		if (value == null) {
			ps.setNull(i, Types.NULL);
		} else {
			ps.setObject(i, value, type);
		}
	}

	public static ObjectMapper om = new ObjectMapper();
	public static String toString(Object obj) {
		if (obj == null) return null;
		try {
			return om.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	public static <T> T readValue(String str) throws JsonParseException, JsonMappingException, IOException {
		return om.readValue(str, new TypeReference<T>() {});
	}

	public static Date getBeginTime(String timeType, String timeStr) throws ParseException, NemsException {
		if (StringUtils.isBlank(timeStr)) return null;
		if (Constants.QUERY_TIME_TYPE_DAY.equals(timeType)) {
			return getDayBegin(timeStr);
		} else if (Constants.QUERY_TIME_TYPE_MONTH.equals(timeType)) {
			return getMonthBegin(timeStr);
		} else if (Constants.QUERY_TIME_TYPE_YEAR.equals(timeType)) {
			return getYearBegin(timeStr);
		} else if (Constants.QUERY_TIME_TYPE_HOUR.equals(timeType)) {
			return getHourBegin(timeStr);
		} else {
			throw new NemsException("illegal time type: "+timeType);
		}
	}
	public static Date getEndTime(String timeType, String timeStr) throws ParseException, NemsException {
		if (StringUtils.isBlank(timeStr)) return null;
		if (Constants.QUERY_TIME_TYPE_DAY.equals(timeType)) {
			return getDayEnd(timeStr);
		} else if (Constants.QUERY_TIME_TYPE_MONTH.equals(timeType)) {
			return getMonthEnd(timeStr);
		} else if (Constants.QUERY_TIME_TYPE_YEAR.equals(timeType)) {
			return getYearEnd(timeStr);
		} else if (Constants.QUERY_TIME_TYPE_HOUR.equals(timeType)) {
			return getHourEnd(timeStr);
		} else {
			throw new NemsException("illegal time type: "+timeType);
		}
	}
	public static Date getHourBegin(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
		} catch (ParseException e) {
			try {
				time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
			} catch (ParseException ex) {
				time = new SimpleDateFormat(Constants.dateFormatHour).parse(timeStr);
			}
		}
		if (time != null) {
			time = trunc(time, TimeType.HOUR);
		}
		return time;
	}
	public static Date getHourEnd(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
		} catch (ParseException e) {
			time = new SimpleDateFormat(Constants.dateFormatHour).parse(timeStr);
		}
		if (time != null) {
			time = getEndTime(time, TimeType.HOUR);
		}
		return time;
	}
	public static Date getDayBegin(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
		} catch (ParseException e) {
			time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
		}
		if (time != null) {
			time = trunc(time, TimeType.DAY);
		}
		return time;
	}
	public static Date getDayBegin(String timeStr, Date defaultTime) throws ParseException {
		Date time = null;
		if (StringUtils.isEmpty(timeStr) && defaultTime != null) {
			time = defaultTime;
		} else {
			try {
				time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
			} catch (ParseException e) {
				time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
			}
		}
		if (time != null) {
			time = trunc(time, TimeType.DAY);
		}
		return time;
	}
	public static Date getDayEnd(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
		} catch (ParseException e) {
			time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
		}
		if (time != null) {
			time = getEndTime(time, TimeType.DAY);
		}
		return time;
	}
	public static Date getDayEnd(String timeStr, Date defaultTime) throws ParseException {
		Date time = null;
		if (StringUtils.isEmpty(timeStr) && defaultTime != null) {
			time = defaultTime;
		} else {
			try {
				time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
			} catch (ParseException e) {
				time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
			}
		}
		if (time != null) {
			time = getEndTime(time, TimeType.DAY);
		}
		return time;
	}
	public static Date getMonthBegin(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
		} catch (ParseException e) {
			try {
				time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
			} catch (ParseException ex) {
				time = new SimpleDateFormat(Constants.dateFormatMonth).parse(timeStr);
			}
		}
		if (time != null) {
			time = trunc(time, TimeType.MONTH);
		}
		return time;
	}
	public static Date getMonthEnd(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
		} catch (ParseException e) {
			try {
				time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
			} catch (ParseException ex) {
				time = new SimpleDateFormat(Constants.dateFormatMonth).parse(timeStr);
			}
		}
		if (time != null) {
			time = getEndTime(time, TimeType.MONTH);
		}
		return time;
	}
	public static Date getYearBegin(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
		} catch (ParseException e) {
			try {
				time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
			} catch (ParseException ex) {
				time = new SimpleDateFormat(Constants.dateFormatMonth).parse(timeStr);
			}
		}
		if (time != null) {
			time = trunc(time, TimeType.YEAR);
		}
		return time;
	}
	public static Date getYearEnd(String timeStr) throws ParseException {
		if (StringUtils.isEmpty(timeStr)) return null;
		Date time = null;
		try {
			time = new SimpleDateFormat(Constants.dateFormatDay).parse(timeStr);
		} catch (ParseException e) {
			try {
				time = new SimpleDateFormat(Constants.dateFormatSecond).parse(timeStr);
			} catch (ParseException ex) {
				time = new SimpleDateFormat(Constants.dateFormatMonth).parse(timeStr);
			}
		}
		if (time != null) {
			time = getEndTime(time, TimeType.YEAR);
		}
		return time;
	}

	public static int compare(Double a, Double b) {
		if (a == null && b == null) return 0;
		else if (a == null) return -1;
		else if (b == null) return 1;
		else return a.compareTo(b);
	}
	public static int compare(Float a, Float b) {
		if (a == null && b == null) return 0;
		else if (a == null) return -1;
		else if (b == null) return 1;
		else return a.compareTo(b);
	}
	public static int compare(Long a, Long b) {
		if (a == null && b == null) return 0;
		else if (a == null) return -1;
		else if (b == null) return 1;
		else return a.compareTo(b);
	}
	public static int compare(Integer a, Integer b) {
		if (a == null && b == null) return 0;
		else if (a == null) return -1;
		else if (b == null) return 1;
		else return a.compareTo(b);
	}
	public static int compare(Short a, Short b) {
		if (a == null && b == null) return 0;
		else if (a == null) return -1;
		else if (b == null) return 1;
		else return a.compareTo(b);
	}

	/**
	 * 用对象列表组装为树形结构，有且只能有一个根节点
	 * @param objs 需要组装未树形结构的对象列表
	 * @param getIdMethodName 对象中获取id的方法名
	 * @param getPidMethodName 对象中获取父id的方法名
	 * @return TreeData树形对象
	 * @throws Exception
	 */
	public static <E> TreeData<E> getTree(List<E> objs, String getIdMethodName, String getPidMethodName) throws Exception {
		if (isEmpty(objs)) return null;
		
		Method getIdMethod = objs.get(0).getClass().getMethod(getIdMethodName);
		Method getPidMethod = objs.get(0).getClass().getMethod(getPidMethodName);
		
		TreeData<E> root = null;
		Map<Object, TreeData<E>> treeDataMap = new HashMap<Object, TreeData<E>>();

		//存放各对象id和对象的map
		Map<Object, E> objMap = new HashMap<Object, E>();
		for (E obj : objs) {
			Object id = getIdMethod.invoke(obj);
			
			objMap.put(id, obj);
			
			TreeData<E> data = new TreeData<E>();
			data.setData(obj);
			treeDataMap.put(id, data);
		}
		
		//存放各对象id和其父对象的map
		Map<Object, E> parentMap = new HashMap<Object, E>();
		for (E obj : objs) {
			Object pid = getPidMethod.invoke(obj);
			if (pid == null) {
				parentMap.put(getIdMethod.invoke(obj), null);
			} else {
				parentMap.put(getIdMethod.invoke(obj), objMap.get(pid));
			}
		}
		
		//构造树形数据结构
		for (E obj : objs) {
			Object id = getIdMethod.invoke(obj);
			
			E parent = parentMap.get(id);
			TreeData<E> data = treeDataMap.get(id);
			
			if (parent != null) {
				//能找到福对象，则加入到父对象的TreeData对象的children中
				TreeData<E> pData = treeDataMap.get(getIdMethod.invoke(parent));
				if (pData != null) {
					if (pData.getChildren() == null) {
						pData.setChildren(new ArrayList<TreeData<E>>());
					}
					pData.getChildren().add(data);
				}
			} else {
				//找不到父对象，说明是根节点
				if (root == null) {
					root = data;
				} else {
					//本方法只允许有一个根节点
					throw new NemsException("multipule root: " + getIdMethod.invoke(root.getData())
						+ " and " + getIdMethod.invoke(data.getData()));
				}
			}
		}
		
		if (root == null) {
			throw new NemsException("no root object");
		}
		
		//设置是否leaf
		for (TreeData<E> data : treeDataMap.values()) {
			data.setLeaf(isEmpty(data.getChildren()));
		}
		
		return root;
	}
	
	/**
	 * 用对象列表组装为树形结构，允许有多个根节点
	 * @param objs 需要组装未树形结构的对象列表
	 * @param getIdMethodName 对象中获取id的方法名
	 * @param getPidMethodName 对象中获取父id的方法名
	 * @return TreeData列表
	 * @throws Exception
	 */
	public static <E> List<TreeData<E>> getTreeList(List<E> objs, String getIdMethodName, String getPidMethodName) throws Exception {
		if (isEmpty(objs)) return null;
		
		Method getIdMethod = objs.get(0).getClass().getMethod(getIdMethodName);
		Method getPidMethod = objs.get(0).getClass().getMethod(getPidMethodName);
		
		List<TreeData<E>> roots = new ArrayList<TreeData<E>>();
		Map<Object, TreeData<E>> treeDataMap = new HashMap<Object, TreeData<E>>();

		//存放各对象id和对象的map
		Map<Object, E> objMap = new HashMap<Object, E>();
		for (E obj : objs) {
			Object id = getIdMethod.invoke(obj);
			
			objMap.put(id, obj);
			
			TreeData<E> data = new TreeData<E>();
			data.setData(obj);
			treeDataMap.put(id, data);
		}
		
		//存放各对象id和其父对象的map
		Map<Object, E> parentMap = new HashMap<Object, E>();
		for (E obj : objs) {
			Object pid = getPidMethod.invoke(obj);
			if (pid == null) {
				parentMap.put(getIdMethod.invoke(obj), null);
			} else {
				parentMap.put(getIdMethod.invoke(obj), objMap.get(pid));
			}
		}
		
		//构造树形数据结构
		for (E obj : objs) {
			Object id = getIdMethod.invoke(obj);
			
			E parent = parentMap.get(id);
			TreeData<E> data = treeDataMap.get(id);
			
			if (parent != null) {
				//能找到福对象，则加入到父对象的TreeData对象的children中
				TreeData<E> pData = treeDataMap.get(getIdMethod.invoke(parent));
				if (pData != null) {
					if (pData.getChildren() == null) {
						pData.setChildren(new ArrayList<TreeData<E>>());
					}
					pData.getChildren().add(data);
				}
			} else {
				//找不到父对象，说明是根节点
				roots.add(data);
			}
		}
		
		//设置是否leaf
		for (TreeData<E> data : treeDataMap.values()) {
			data.setLeaf(isEmpty(data.getChildren()));
		}
		
		return roots;
	}

	public static boolean equals(String a, String b) {
		if (a == null && b != null) return false;
		if (a != null && b == null) return false;
		if (a == null && b == null) return true;
		return a.equals(b);
	}

	public static boolean equals(Integer a, Integer b) {
		if (a == null && b != null) return false;
		if (a != null && b == null) return false;
		if (a == null && b == null) return true;
		return a.equals(b);
	}

	public static boolean equals(Long a, Long b) {
		if (a == null && b != null) return false;
		if (a != null && b == null) return false;
		if (a == null && b == null) return true;
		return a.equals(b);
	}

	public static boolean equals(Float a, Float b) {
		if (a == null && b != null) return false;
		if (a != null && b == null) return false;
		if (a == null && b == null) return true;
		return a.equals(b);
	}

	public static boolean equals(Double a, Double b) {
		if (a == null && b != null) return false;
		if (a != null && b == null) return false;
		if (a == null && b == null) return true;
		return a.equals(b);
	}

	public static String getClassName(Object obj) {
		if (obj == null) return null;
		return getClassName(obj.getClass().getName());
	}
	public static String getClassName(String className) {
		if (className == null) return null;
		int idx = className.lastIndexOf('.');
		if (idx >= 0) {
			return className.substring(idx + 1);
		}
		return className;
	}

	public static String camelCaseToUnderline(String name) {
		if (StringUtils.isEmpty(name)) return name;
		
		StringBuffer result = new StringBuffer();
		for (int i=0; i<name.length(); ++i) {
			char c = name.charAt(i);
			if (65 <= c && c <= 90) {
				result.append('_').append((char)(c + 32));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	public static int getListInitCapacity(int size) {
		return (int) Math.ceil(size * 1.2);
	}

	public static String getString(JsonNode node) {
		if (node == null || node.isNull()) return null;
		return node.asText();
	}

	public static Integer getInt(JsonNode node) {
		if (node == null || node.isNull()) return null;
		return node.isInt() ? node.asInt() : null;
	}

	public static Long getLong(JsonNode node) {
		if (node == null || node.isNull()) return null;
		return node.isLong() || node.isInt() ? node.asLong() : null;
	}

	public static Double getDouble(JsonNode node) {
		if (node == null || node.isNull()) return null;
		return node.isNumber() ? node.asDouble() : null;
	}

}
