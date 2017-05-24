package com.advicetec.displayadapter;

import com.advicetec.utils.UdpUtils;

public class DateTimeStruct {
	
	private String year;
	private String month;
	private String day;
	private String hour;
	private String min;
	private String dateUseFlag;
	private String timeUseFlag;
	
	public DateTimeStruct(int year, int month, int day){
		this.year = UdpUtils.swap(Integer.toString(year)); // 2 bytes
		this.month = UdpUtils.int2HexString(month, 1); // 1 byte
		this.day = UdpUtils.int2HexString(day, 1); // 1 byte
		this.hour = UdpUtils.int2HexString(1, 1);
		this.min = UdpUtils.int2HexString(1, 1);
		dateUseFlag = UdpUtils.int2HexString(1, 1);
		timeUseFlag = UdpUtils.int2HexString(1, 1);
	}

	public String toHexString(){
		return year.concat(month).concat(day).concat(hour).concat(min).concat(dateUseFlag).concat(timeUseFlag);
	}
}
