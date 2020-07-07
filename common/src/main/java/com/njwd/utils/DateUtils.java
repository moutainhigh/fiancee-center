
package com.njwd.utils;


import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 日期工具类
 *
 * @author CJ
 */
@SuppressWarnings("all")
public class DateUtils {
    public static final String PATTERN_YEAR = "yyyy";
    public static final String PATTERN_MONTH = "yyyy-MM";
    public static final String PATTERN_DAY = "yyyy-MM-dd";
    public static final String PATTERN_HOUR = "yyyy-MM-dd HH";
    public static final String PATTERN_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String PATTERN_SECOND = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_HOUR_MINUTE = "HH:mm";
    public static final String CONCAT_START = " 00:00:00";
    public static final String CONCAT_END = " 23:59:59";
    public static final String CONCAT_MONTH_START = "-01 00:00:00";
    public static final long MILLIS_PER_SECOND = 1000;
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
    public static final int SECOND_PER_MINUTE = 60;

    /**
     * 月份长度
     */
    private static final int[] MONTH_LENGTH = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /**
     * 字符串是否是指定格式的时间.
     *
     * @param time    time
     * @param pattern pattern
     * @return boolean
     */
    public static boolean isDate(final String time, final String pattern) {
        boolean flag = true;
        if (time == null || pattern == null) {
            throw new NullPointerException();
        }
        String concatTime = concatTime(time, pattern);
        try {
            String reg = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
            Pattern compilePattern = Pattern.compile(reg);
            Matcher matcher = compilePattern.matcher(concatTime);
            if (matcher.matches()) {
                Timestamp.valueOf(concatTime);
            } else {
                flag = false;
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    private static String concatTime(String time, String pattern) {
        String concatTime = null;
        switch (pattern) {
            case PATTERN_YEAR:
                concatTime = time.concat("-01-01 00:00:00");
                break;
            case PATTERN_MONTH:
                concatTime = time.concat(CONCAT_MONTH_START);
                break;
            case PATTERN_DAY:
                concatTime = time.concat(CONCAT_START);
                break;
            case PATTERN_HOUR:
                concatTime = time.concat(":00:00");
                break;
            case PATTERN_MINUTE:
                concatTime = time.concat(":00");
                break;
            default:
        }
        return concatTime;
    }

    //////// 日期比较 ///////////

    /**
     * 是否同一天.
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 是否同一天同一时刻.
     */
    public static boolean isSameTime(final Date date1, final Date date2) {
        // date.getMillisOf() 比date.getTime()快
        return date1.compareTo(date2) == 0;
    }

    /**
     * 是否同一时分秒,不校验日期.
     */
    public static boolean isSameTimeIgnoreDate(final Date date1, final Date date2) {
        return compareTime(date1, date2) == 0;
    }

    /**
     * 判断日期是否在范围内, 包含相等的日期
     */
    public static boolean isBetween(final Date date, final Date start, final Date end) {
        if (date == null || start == null || end == null || start.after(end)) {
            throw new IllegalArgumentException("some date parameters is null or dateBegin after dateEnd");
        }
        return !date.before(start) && !date.after(end);
    }

    /**
     * 加一月
     */
    public static Date addMonths(final Date date, int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    /**
     * 减一月
     */
    public static Date subMonths(final Date date, int amount) {
        return add(date, Calendar.MONTH, -amount);
    }

    private static Date add(Date date, int field, int i) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(field, i);
        return c.getTime();
    }

    /**
     * 加一周
     */
    public static Date addWeeks(final Date date, int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    /**
     * 减一周
     */
    public static Date subWeeks(final Date date, int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, -amount);
    }

    /**
     * 加day天
     */
    public static Date addDays(final Date date, final int day) {
        //Calendar.DAY_OF_MONTH
        return add(date, Calendar.DAY_OF_MONTH, day);
    }

    /**
     * 减一天
     */
    public static Date subDays(final Date date, int day) {
        return add(date, Calendar.DAY_OF_MONTH, -day);
    }

    /**
     * 加一小时
     */
    public static Date addHours(final Date date, int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 加一小时
     */
    public static Date addHours(final String dateStr, int amount) {
        Date date = parseDate(dateStr, PATTERN_SECOND);
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 减一小时
     */
    public static Date subHours(final Date date, int amount) {
        return add(date, Calendar.HOUR_OF_DAY, -amount);
    }

    /**
     * 加一分钟
     */
    public static Date addMinutes(final Date date, int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    /**
     * 加一分钟
     */
    public static String addMinutes(final String dateStr, int amount) {
        Date date = parseDate(dateStr, PATTERN_SECOND);
        return format(add(date, Calendar.MINUTE, amount), PATTERN_SECOND);
    }

    /**
     * 减一分钟
     */
    public static Date subMinutes(final Date date, int amount) {
        return add(date, Calendar.MINUTE, -amount);
    }

    /**
     * 加一秒.
     */
    public static Date addSeconds(final Date date, int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    /**
     * 减一秒.
     */
    public static Date subSeconds(final Date date, int amount) {
        return add(date, Calendar.SECOND, -amount);
    }

    /**
     * 设置年份, 公元纪年.
     */
    public static Date setYears(final Date date, int amount) {
        return set(date, Calendar.YEAR, amount);
    }

    private static Date set(Date date, int field, int amount) {
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(date);
        c.set(field, amount);
        return c.getTime();
    }

    /**
     * 设置月份, 0-11.
     */
    public static Date setMonths(final Date date, int amount) {
        return set(date, Calendar.MONTH, amount);
    }

    /**
     * 设置日期, 1-31.
     */
    public static Date setDays(final Date date, int amount) {
        return set(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 设置小时, 0-23.
     */
    public static Date setHours(final Date date, int amount) {
        return set(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 设置分钟, 0-59.
     */
    public static Date setMinutes(final Date date, int amount) {
        return set(date, Calendar.MINUTE, amount);
    }

    /**
     * 设置秒, 0-59.
     */
    public static Date setSeconds(final Date date, int amount) {
        return set(date, Calendar.SECOND, amount);
    }

    /**
     * 设置毫秒.
     */
    public static Date setMilliseconds(final Date date, int amount) {
        return set(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 获得日期是一周的第几天. 已改为中国习惯, 1 是Monday, 而不是Sundays.
     */
    public static int getDayOfWeek(final Date date) {
        int result = get(date, Calendar.DAY_OF_WEEK);
        return result == 1 ? 7 : result - 1;
    }

    /**
     * 获得日期是一年的第几天, 返回值从1开始
     */
    public static int getDayOfYear(final Date date) {
        return get(date, Calendar.DAY_OF_YEAR);
    }

    /**
     * 获得日期是一月的第几周, 返回值从1开始.
     * <p>
     * 开始的一周, 只要有一天在那个月里都算. 已改为中国习惯, 1 是Monday, 而不是Sunday
     */
    public static int getWeekOfMonth(final Date date) {
        return get(date, Calendar.WEEK_OF_MONTH);
    }

    /**
     * 获得日期是一年的第几周, 返回值从1开始.
     * <p>
     * 开始的一周, 只要有一天在那一年里都算.已改为中国习惯, 1 是Monday, 而不是Sunday
     */
    public static int getWeekOfYear(final Date date) {
        return get(date, Calendar.WEEK_OF_YEAR);
    }

    private static int get(@NotNull(message = "The date must not be null") final Date date, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(date);
        return cal.get(field);
    }

    /**
     * 2016-11-10 07:33:23, 则返回2016-1-1 00:00:00
     */
    public static Date beginOfYear(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2016-11-10 07:33:23, 则返回2016-12-31 23:59:59.999
     */
    public static Date endOfYear(final Date date) {
        return new Date(nextYear(date).getTime() - 1);
    }

    /**
     * 2016-11-10 07:33:23, 则返回2017-1-1 00:00:00
     */
    public static Date nextYear(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, 1);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2016-11-10 07:33:23, 则返回2016-11-1 00:00:00
     */
    public static Date beginOfMonth(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2016-11-10 07:33:23, 则返回2016-11-30 23:59:59.999
     */
    public static Date endOfMonth(final Date date) {
        return new Date(nextMonth(date).getTime() - 1);
    }

    /**
     * 2016-11-10 07:33:23, 则返回2016-12-1 00:00:00
     */
    public static Date nextMonth(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, 1);
        c.set(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2017-1-20 07:33:23, 则返回2017-1-16 00:00:00 周一
     */
    public static Date beginOfWeek(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, 2);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2017-1-20 07:33:23, 则返回2017-1-22 23:59:59.999 周日
     */
    public static Date endOfWeek(final Date date) {
        return new Date(nextWeek(date).getTime() - 1);
    }

    /**
     * 返回下周一
     */
    public static Date nextWeek(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.WEEK_OF_MONTH, 1);
        c.set(Calendar.DAY_OF_WEEK, 2);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2016-11-10 07:33:23, 则返回2016-11-10 00:00:00
     */
    public static Date beginOfDate(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 获取今天结束时间
     * 2017-1-23 07:33:23, 则返回2017-1-23 23:59:59.999
     */
    public static Date endOfDate(final Date date) {
        return new Date(nextDate(date).getTime() - 1);
    }

    public static void main(String[] args) {
        System.out.println(endOfDate(new Date()));
    }

    /**
     * 2016-11-10 07:33:23, 则返回2016-11-11 00:00:00
     */
    public static Date nextDate(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2016-12-10 07:33:23, 则返回2016-12-10 07:00:00
     */
    public static Date beginOfHour(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2017-1-23 07:33:23, 则返回2017-1-23 07:59:59.999
     */
    public static Date endOfHour(final Date date) {
        return new Date(nextHour(date).getTime() - 1);
    }

    /**
     * 2016-12-10 07:33:23, 则返回2016-12-10 08:00:00
     */
    public static Date nextHour(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2016-12-10 07:33:23, 则返回2016-12-10 07:33:00
     */
    public static Date beginOfMinute(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 2017-1-23 07:33:23, 则返回2017-1-23 07:33:59.999
     */
    public static Date endOfMinute(final Date date) {
        return new Date(nextMinute(date).getTime() - 1);
    }

    /**
     * 2016-12-10 07:33:23, 则返回2016-12-10 07:34:00
     */
    public static Date nextMinute(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MINUTE, 1);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    ////// 闰年及每月天数///////

    /**
     * 是否闰年.
     */
    public static boolean isLeapYear(final Date date) {
        return isLeapYear(get(date, Calendar.YEAR));
    }

    /**
     * 判断是否闰年, 移植Joda Core的TimeUtil
     * <p>
     * 参数是公元计数, 如2016
     */
    public static boolean isLeapYear(int y) {
        boolean result = false;
        boolean flag1 = (y % 4) == 0;
        if (flag1 && ((y < 1582) || ((y % 100) != 0) || ((y % 400) == 0))) {
            result = true;
        }
        return result;
    }

    /**
     * 获取某个月有多少天, 考虑闰年等因数, 移植Joda Core的TimeUtil
     */
    public static int getMonthLength(final Date date) {
        int year = get(date, Calendar.YEAR);
        int month = get(date, Calendar.MONTH);
        return getMonthLength(year, month);
    }

    /**
     * 获取某个月有多少天, 考虑闰年等因数, 移植Joda Core的TimeUtil
     */
    public static int getMonthLength(int year, int month) {
        if ((month < 1) || (month > 12)) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        if (month == 2) {
            return isLeapYear(year) ? 29 : 28;
        }
        return MONTH_LENGTH[month];
    }

    public static String getPatternMonth() {
        return PATTERN_MONTH;
    }

    public static String getPatternYear() {
        return PATTERN_YEAR;
    }

    /**
     * 时间转换
     *
     * @param time    时间
     * @param pattern 格式
     * @return date
     */
    public static Date parseDate(String time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 时间转换
     *
     * @param date    时间
     * @param pattern 格式
     * @return str
     */
    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 计算间隔天数
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pattern   格式
     * @return 间隔天数
     */
    public static int getBetweenDay(String startTime, String endTime, String pattern) {
        Date startDate = DateUtils.parseDate(startTime, pattern);
        Date endDate = DateUtils.parseDate(endTime, pattern);
        return getBetweenDay(startDate, endDate);
    }

    /**
     * 计算间隔天数
     *
     * @param startDate 开始时间
     * @param endDate   结束时间,必须大于开始时间
     * @return 间隔天数
     */
    public static int getBetweenDay(Date startDate, Date endDate) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startDate);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(endDate);
        int betweenYear = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
        if (betweenYear == 0) {
            return c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);
        }
        // 获取起始年总天数
        int actualMaximum = c1.getActualMaximum(Calendar.DAY_OF_YEAR);
        // 间隔天数=尾年过了几天+首年末过天数+中间的年度每年天数
        int days = c2.get(Calendar.DAY_OF_YEAR) + actualMaximum - c1.get(Calendar.DAY_OF_YEAR);
        for (int i = 1; i < betweenYear; i++) {
            c1.add(Calendar.YEAR, 1);
            days += c1.getActualMaximum(Calendar.DAY_OF_YEAR);
        }
        return days;
    }

    /**
     * 拼接时间
     *
     * @param date 取日期
     * @param time 取时间
     * @return 日期+时间
     */
    public static Date concatDateTime(Date date, Date time) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(time);
        Calendar calendar = Calendar.getInstance();
        calendar.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DATE), c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE), c2.get(Calendar.SECOND));
        return calendar.getTime();
    }

    /**
     * 判断时间大小(只判断时分秒)
     *
     * @param time1 time1
     * @param time2 time2
     * @return time1小返回-1,大返回1,等于返回0
     */
    public static int compareTime(Date time1, Date time2) {
        int second1 = getSecondIgnoreDay(time1);
        int second2 = getSecondIgnoreDay(time2);
        return Integer.compare(second1, second2);
    }

    /**
     * 判断时间大小(只判断日期)
     *
     * @param time1 time1
     * @param time2 time2
     * @return time1小返回-1,大返回1,等于返回0
     */
    public static int compareDate(Date time1, Date time2) {
        Date date1 = beginOfDate(time1);
        Date date2 = beginOfDate(time2);
        return Long.compare(date1.getTime(), date2.getTime());
    }

    /**
     * 将时分秒转换为秒
     *
     * @param time time
     * @return 秒
     */
    private static int getSecondIgnoreDay(Date time) {
        Calendar c2 = Calendar.getInstance();
        c2.setTime(time);
        return c2.get(Calendar.HOUR_OF_DAY) * SECOND_PER_MINUTE * SECOND_PER_MINUTE + c2.get(Calendar.MINUTE) * SECOND_PER_MINUTE + c2.get(Calendar.SECOND);
    }

    /**
     * 计算当天时间差,不计算日期
     *
     * @param startTime startTime
     * @param endTime   endTime
     * @return 时间差(单位分钟)
     */
    public static long betweenMinutesIgnoreDate(Date startTime, Date endTime) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(startTime);
        int minutes1 = c1.get(Calendar.HOUR_OF_DAY) * SECOND_PER_MINUTE + c1.get(Calendar.MINUTE);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(endTime);
        int minutes2 = c2.get(Calendar.HOUR_OF_DAY) * SECOND_PER_MINUTE + c2.get(Calendar.MINUTE);
        return minutes2 - minutes1;
    }

    /**
     * 返回两个时间之间的分钟数(取整,忽略秒)
     *
     * @param startTime startTime 格式:yyyy-MM-dd HH:mm:ss
     * @param endTime   endTime 格式:yyyy-MM-dd HH:mm:ss
     * @return long(单位 : 分钟)
     */
    public static long countMinTimes(String startTime, String endTime) {
        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_SECOND);
        try {
            Date fromTime = formatter.parse(startTime);
            Date toTime = formatter.parse(endTime);
            long ms = (toTime.getTime() - fromTime.getTime()) / 1000;
            return ms / 60;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Date convert(String stringDate) {
        SimpleDateFormat simpleDateFormat;
        if (stringDate.length() == 10) {
            simpleDateFormat = new SimpleDateFormat(PATTERN_DAY);
        } else if (stringDate.length() > 10) {
            simpleDateFormat = new SimpleDateFormat(PATTERN_SECOND);
        } else {
            return null;
        }
        try {
            return simpleDateFormat.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获得某天最小时间 2017-11-30 00:00:00
    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }
}