package com.skillfive.backend.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 */
public class DateUtil {
    
    // 常用日期时间格式
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * 获取当前日期
     */
    public static LocalDate nowDate() {
        return LocalDate.now();
    }
    
    /**
     * 获取当前时间
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * 格式化日期为字符串
     */
    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : null;
    }
    
    /**
     * 格式化日期时间为字符串
     */
    public static String formatDateTime(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime != null ? dateTime.format(formatter) : null;
    }
    
    /**
     * 解析字符串为日期
     */
    public static LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        return dateStr != null ? LocalDate.parse(dateStr, formatter) : null;
    }
    
    /**
     * 解析字符串为日期时间
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, DateTimeFormatter formatter) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, formatter) : null;
    }
    
    /**
     * 计算两个日期之间的天数差
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    /**
     * 计算两个日期时间之间的小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }
    
    /**
     * 计算两个日期时间之间的分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }
    
    /**
     * 计算两个日期时间之间的秒差
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }
    
    /**
     * 给日期增加指定天数
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        return date.plusDays(days);
    }
    
    /**
     * 给日期时间增加指定分钟数
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }
    
    /**
     * 给日期时间增加指定秒数
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.plusSeconds(seconds);
    }
    
    /**
     * 判断是否在指定时间范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
    
    /**
     * 获取当天开始时间
     */
    public static LocalDateTime getDayStart(LocalDate date) {
        return date.atStartOfDay();
    }
    
    /**
     * 获取当天结束时间
     */
    public static LocalDateTime getDayEnd(LocalDate date) {
        return date.atTime(23, 59, 59);
    }
    
    /**
     * 将java.util.Date转换为LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * 将LocalDateTime转换为java.util.Date
     */
    public static Date localDateTimeToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取时间戳（毫秒）
     */
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 根据时间戳创建LocalDateTime
     */
    public static LocalDateTime ofTimestamp(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    /**
     * 获取友好的时间显示
     * 例如：刚刚、5分钟前、1小时前、昨天、2天前
     */
    public static String getFriendlyTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);
        
        if (seconds < 60) {
            return "刚刚";
        }
        
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 60) {
            return minutes + "分钟前";
        }
        
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + "小时前";
        }
        
        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days == 1) {
            return "昨天";
        }
        
        if (days < 30) {
            return days + "天前";
        }
        
        // 超过30天显示具体日期
        return formatDateTime(dateTime, YYYY_MM_DD);
    }
}