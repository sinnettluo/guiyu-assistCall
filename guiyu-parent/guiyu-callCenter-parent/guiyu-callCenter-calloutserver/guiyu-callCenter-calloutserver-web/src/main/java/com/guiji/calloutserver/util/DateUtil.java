package com.guiji.calloutserver.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DateUtil.class);
    /**
     * 日期格式为 年-月-日 时:分:秒
     */
    public static String FORMAT_YEARMONTHDAY_HOURMINSEC = "yyyy-MM-dd HH:mm:ss";
    public static String FORMAT_YEARMONTHDAY = "yyyy-MM-dd";

    /**
     * 获取此时到凌晨的秒数
     * @return
     */
    public static long getSecondsBeforeDawn(){

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endTime = cal.getTime().getTime();

        return (endTime - System.currentTimeMillis())/1000;
    }


    /**
     * 获取当前的日期， 年月日，如20121218
     *
     * @return
     */
    public static String getCurrentDate() {
        return getCurrentDateByFormat("yyyyMMdd");
    }

    /**
     * 获取当前的日期时间， 年月日 时分秒 如2013-05-24 14:39:43
     *
     * @return
     */
    public static String getCurrentDateTime() {
        return getCurrentDateByFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 按照指定格式返回当前日期和时间
     *
     * @param format
     *            时间格式，如yyyyMMdd 或 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentDateByFormat(String format) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(format);
        String currDate = df.format(date);
        return currDate;
    }

    /**
     * 将字符串按指定的格式转化为日期类型
     *
     * @param date
     *            日期字符串
     * @param formatString
     *            格式字符串， 如yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Calendar getCalendarByDateAndFormat(String date, String formatString) {
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        Calendar calendar = Calendar.getInstance();
        try {
            Date dt = format.parse(date);
            calendar.setTime(dt);
        } catch (ParseException e) {
            calendar = null;
        }

        return calendar;
    }

    public static Date getDateByDateAndFormat(String date, String formatString) {
//        SimpleDateFormat format = new SimpleDateFormat(formatString);
        Date calendar = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formatString);
            sdf.setLenient(false);//是否严格匹配
            calendar =  sdf.parse(date);
        } catch (ParseException e) {
            calendar = null;
        }
        return calendar;
    }

    /**
     * 比较两个日期 cal1 - cal2，返回两个日期的差，以秒值返回
     *
     * @param cal1
     * @param cal2
     * @return
     */
    public static Integer CompareTwoCalendar(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            Long lon = (cal1.getTimeInMillis() - cal2.getTimeInMillis()) / 1000;
            return lon.intValue();
        } else {
            return null;
        }
    }

    /**
     * 获取日期字符串
     * @param date
     * @param strFormat
     * @return
     */
    public static String getStrDate(Date date,String strFormat)
    {
        String strDate = "";
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        strDate = format.format(date);
        return  strDate;
    }

    /**
     * 将不规则的时间格式转成规则的时间格式
     * @param date
     * @return
     */
    public static String complianceDate(String date){
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YEARMONTHDAY_HOURMINSEC);
        try {
            return sdf.format(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }



    public static void main(String[] args) {
//        String ff = "yyyy-MM-dd HH:mm:ss";
//        System.out.println("currdate: " + getCurrentDateByFormat(ff));
        System.out.println(getSecondsBeforeDawn());
    }

    /**
     * 将标准的日期时间格式(yyyy-MM-dd HH:mm:ss)，转成时间戳
     * @param dateTime
     * @return
     */
    public static Long toEpoch(String dateTime){
        if(Strings.isNullOrEmpty(dateTime)){
            return null;
        }

        Long epoch = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.UTC).getEpochSecond();
        return epoch;
    }

    public static String timeStampToDate(Long epoch){

        Timestamp timestamp1 = new Timestamp(epoch);
        Date date = new Date(timestamp1.getTime());
        return getStrDate(date, FORMAT_YEARMONTHDAY_HOURMINSEC);
    }


    public static long dateDiffTime(Date startTime, Date endTime) {
        //获得两个时间的毫秒时间差异
        long diff = endTime.getTime() - startTime.getTime();
        return diff;//计算差多少小时
    }

    public static String toString(Calendar calendar,String formate)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        String dateStr = sdf.format(calendar.getTime());
        return  dateStr;
    }

    public static String toString(Date calendar,String formate)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(formate);
        String dateStr = sdf.format(calendar.getTime());
        return  dateStr;
    }

    public static Timestamp toTimeStamp(String dateTime){
        if(Strings.isNullOrEmpty(dateTime)){
            return null;
        }

        Timestamp myStamp = null;
        try {
            myStamp = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime).getTime());
        } catch (ParseException e) {
        }

        return myStamp;
    }

    public static Date timestampToDate(long timeStamp) {
        Timestamp ts = new Timestamp(timeStamp);
        try {
            Date date = ts;
            return  date;
        } catch (Exception e) {
            log.error("timestampToDate异常",e);
            return null;
        }
    }
}

