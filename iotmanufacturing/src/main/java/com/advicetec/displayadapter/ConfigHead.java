package com.advicetec.displayadapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.advicetec.utils.UdpUtils;

public class ConfigHead {

	static Logger logger = LogManager.getLogger(ConfigHead.class.getName());

	String screenWidth ; // screen width
	String screenHeight; // screen height
	String priority ; // priority
	String language ; // language
	String baudRate1; // baud rate
	String baudRate2 ; // baud rate 2
	String tcpTimeOut; // tcp time out
	String dhcpEnable ; // dhcp on / off

	String cardSize; // card size
	String fpgaType; // fpga type
	String fpgaMode; // fpga mode
	String fpgaGrade; // fpga gray scale
	String fpgaLight; // fpga light
	String fpgaLength; // fpga length
	String fpgaStartLine; // start line
	String sameset; // same set? same=1 different=0
	String ledbin; // led bin
	String group; // group address
	String unit; // unit address
	String poweronmessage; // power on message=0 , f0= no power on message
	String broadcastIpEnable; // reserved
	String ipAddress; // ip address
	String MacAddress; // mac address
	String softSecVer; // soft sectoin version number
	String softver; // soft version
	String hardver; // hardware version
	String powertimeUser; // power time is_user, true = valid
	String powertimeOffHour; // power time off hour
	String powertimeOffMin; // power time off min
	String powertimeOnHour; // power time on hour
	String powertimeOnMin; // power time on min

	String timeZone; //time zone
	String remote;   // remote control = 0, no remote control =1
	String playMode; // process file immediately=0, delayed=1
	String daylight; // saving time
	
	String halflightTimeUser; 	// user
	String halflightTimeOffHour;// 
	String halflightTimeOffMin;
	String halflightTimeOnHour;
	String halflightTimeOnMin;
	
	String systemStat; // starting state of a system, 0= schedule mode
	String uselogin; // log in, default=no log in.
	String slaveNum; // number of slave board
	String tempFlag; // temp flag, 0=celcious, 1=fahrenheit
	String newLineWidth; // new line width
	String newLineHeigth; // new line height
	String offlineX; // offline x
	String offlineY; // offline y
	String tempValue; // temp value
	String serial; // serial number
	String dateTime; // datetime renewing files
	String ledName; // led name
	String brightCtrl; // brigth control

	String gatewayIp; // gateway ip
	String maskAddr; // mask address
	String cfglist1; // sigma 3000 config select
	String cfglist2; // sigma 3000 config select 2
	String functionlist1; // firmware function list
	String functionlist2; // firmware function list
	// 188 = 0xbc

	
	public ConfigHead(String data, int expectedSize){
		if(!UdpUtils.getFields(data, 0, 2).equalsIgnoreCase("aa55") || data.length() != expectedSize*2 || data.length() < 160){ // 216 0xd8
			logger.error(" Format does not match: "+ data);
		}else{
			
			screenWidth = UdpUtils.getFields(data, 2, 2); // screen width
			screenHeight = UdpUtils.getFields(data, 4, 2); // screen height
			priority = UdpUtils.getFields(data, 6, 1); // priority
			language = UdpUtils.getFields(data, 7, 1); // language
			baudRate1 = UdpUtils.getFields(data, 8, 2); // baud rate
			baudRate2 = UdpUtils.getFields(data, 10, 2); // baud rate 2
			tcpTimeOut = UdpUtils.getFields(data, 12, 2); // tcp time out
			dhcpEnable = UdpUtils.getFields(data, 14, 1); // dhcp on / off
			UdpUtils.getFields(data, 15, 1); // reserved
			cardSize = UdpUtils.getFields(data, 16, 2); // card size
			fpgaType = UdpUtils.getFields(data, 18, 2); // fpga type
			fpgaMode = UdpUtils.getFields(data, 20, 2); // fpga mode
			fpgaGrade = UdpUtils.getFields(data, 22, 2); // fpga gray scale
			fpgaLight = UdpUtils.getFields(data, 24, 2); // fpga light
			fpgaLength = UdpUtils.getFields(data, 26, 2); // fpga length
			fpgaStartLine = UdpUtils.getFields(data, 28, 2); // start line
			sameset = UdpUtils.getFields(data, 30, 1); // same set? same=1 different=0
			ledbin = UdpUtils.getFields(data, 31, 1); // led bin
			group = UdpUtils.getFields(data, 32, 1); // group address
			unit = UdpUtils.getFields(data, 33, 1); // unit address
			poweronmessage = UdpUtils.getFields(data, 34, 1); // power on message=0 , f0= no power on message
			broadcastIpEnable = UdpUtils.getFields(data, 35, 1); // reserved
			ipAddress = UdpUtils.getFields(data, 36, 4); // ip address
			MacAddress = UdpUtils.getFields(data, 40, 6); // mac address
			softSecVer = UdpUtils.getFields(data, 46, 2); // soft sectoin version number
			softver = UdpUtils.getFields(data, 48, 2); // soft version
			hardver = UdpUtils.getFields(data, 50, 2); // hardware version
			powertimeUser = UdpUtils.getFields(data, 52, 1); // power time is_user, true = valid
			powertimeOffHour = UdpUtils.getFields(data, 53, 1); // power time off hour
			powertimeOffMin = UdpUtils.getFields(data, 54, 1); // power time off min
			powertimeOnHour = UdpUtils.getFields(data, 55, 1); // power time on hour
			powertimeOnMin = UdpUtils.getFields(data, 56, 1); // power time on min
			UdpUtils.getFields(data, 57, 3); // rev
			
			timeZone = UdpUtils.getFields(data, 60, 1); //time zone
			remote = UdpUtils.getFields(data, 61, 1);   // remote control = 0, no remote control =1
			playMode = UdpUtils.getFields(data, 62, 1); // process file immediately=0, delayed=1
			daylight = UdpUtils.getFields(data, 63, 1); // saving time
			
			halflightTimeUser = UdpUtils.getFields(data, 64, 1); 	// user
			halflightTimeOffHour = UdpUtils.getFields(data, 65, 1);// 
			halflightTimeOffMin = UdpUtils.getFields(data, 66, 1);
			halflightTimeOnHour = UdpUtils.getFields(data, 67, 1);
			halflightTimeOnMin = UdpUtils.getFields(data, 68, 1);
			UdpUtils.getFields(data, 69, 3); // rev
			
			systemStat = UdpUtils.getFields(data, 72, 1); // starting state of a system, 0= schedule mode
			uselogin = UdpUtils.getFields(data, 73, 1); // log in, default=no log in.
			slaveNum = UdpUtils.getFields(data, 74, 1); // number of slave board
			tempFlag = UdpUtils.getFields(data, 75, 1); // temp flag, 0=celcious, 1=fahrenheit
			newLineWidth = UdpUtils.getFields(data, 76, 2); // new line width
			newLineHeigth = UdpUtils.getFields(data, 78, 2); // new line height
			offlineX = UdpUtils.getFields(data, 80, 2); // offline x
			offlineY = UdpUtils.getFields(data, 82, 2); // offline y
			tempValue = UdpUtils.getFields(data, 84, 1); // temp value
			serial = UdpUtils.getFields(data, 85, 13); // serial number
			dateTime = UdpUtils.getFields(data, 98, 8); // datetime renewing files
			ledName = UdpUtils.getFields(data, 106, 11); // led name
			brightCtrl = UdpUtils.getFields(data, 17, 18); // brigth control
			UdpUtils.getFields(data, 135, 1); // rev
			gatewayIp = UdpUtils.getFields(data, 136, 4); // gateway ip
			maskAddr = UdpUtils.getFields(data, 140, 4); // mask address
			cfglist1 = UdpUtils.getFields(data, 144, 4); // sigma 3000 config select
			cfglist2 = UdpUtils.getFields(data, 148, 4); // sigma 3000 config select 2
			functionlist1 = UdpUtils.getFields(data, 152, 4); // firmware function list
			functionlist2 = UdpUtils.getFields(data, 156, 4); // firmware function list
			UdpUtils.getFields(data, 160, 40); // rev
			// 188 = 0xbc
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public String getScreenWidth() {
		return screenWidth;
	}

	public String getScreenHeight() {
		return screenHeight;
	}

	public String getPriority() {
		return priority;
	}

	public String getLanguage() {
		return language;
	}

	public String getBaudRate1() {
		return baudRate1;
	}

	public String getBaudRate2() {
		return baudRate2;
	}

	public String getTcpTimeOut() {
		return tcpTimeOut;
	}

	public String getDhcpEnable() {
		return dhcpEnable;
	}

	public String getCardSize() {
		return cardSize;
	}

	public String getFpgaType() {
		return fpgaType;
	}

	public String getFpgaMode() {
		return fpgaMode;
	}

	public String getFpgaGrade() {
		return fpgaGrade;
	}

	public String getFpgaLight() {
		return fpgaLight;
	}

	public String getFpgaLength() {
		return fpgaLength;
	}

	public String getFpgaStartLine() {
		return fpgaStartLine;
	}

	public String getSameset() {
		return sameset;
	}

	public String getLedbin() {
		return ledbin;
	}

	public String getGroup() {
		return group;
	}

	public String getUnit() {
		return unit;
	}

	public String getPoweronmessage() {
		return poweronmessage;
	}

	public String getBroadcastIpEnable() {
		return broadcastIpEnable;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getMacAddress() {
		return MacAddress;
	}

	public String getSoftSecVer() {
		return softSecVer;
	}

	public String getSoftver() {
		return softver;
	}

	public String getHardver() {
		return hardver;
	}

	public String getPowertime() {
		return powertimeUser;
	}

	public String getOffHour() {
		return powertimeOffHour;
	}

	public String getOffMin() {
		return powertimeOffMin;
	}

	public String getOnHour() {
		return powertimeOnHour;
	}

	public String getOnMin() {
		return powertimeOnMin;
	}

	public String getSystemStat() {
		return systemStat;
	}

	public String getUselogin() {
		return uselogin;
	}

	public String getSlaveNum() {
		return slaveNum;
	}

	public String getTempFlag() {
		return tempFlag;
	}

	public String getNewLineWidth() {
		return newLineWidth;
	}

	public String getNewLineHeigth() {
		return newLineHeigth;
	}

	public String getOfflineX() {
		return offlineX;
	}

	public String getOfflineY() {
		return offlineY;
	}

	public String getTempValue() {
		return tempValue;
	}

	public String getSerial() {
		return serial;
	}

	public String getDateTime() {
		return dateTime;
	}

	public String getLedName() {
		return ledName;
	}

	public String getBrightCtrl() {
		return brightCtrl;
	}

	public String getGatewayIp() {
		return gatewayIp;
	}

	public String getMaskAddr() {
		return maskAddr;
	}

	public String getCfglist1() {
		return cfglist1;
	}

	public String getCfglist2() {
		return cfglist2;
	}

	public String getFunctionlist1() {
		return functionlist1;
	}

	public String getFunctionlist2() {
		return functionlist2;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(screenWidth ) // screen width
		.append(screenHeight) // screen height
		.append(priority ) // priority
		.append(language ) // language
		.append(baudRate1) // baud rate
		.append(baudRate2 ) // baud rate 2
		.append(tcpTimeOut) // tcp time out
		.append(dhcpEnable ) // dhcp on / off

		.append(cardSize) // card size
		.append(fpgaType) // fpga type
		.append(fpgaMode) // fpga mode
		.append(fpgaGrade) // fpga gray scale
		.append(fpgaLight) // fpga light
		.append(fpgaLength) // fpga length
		.append(fpgaStartLine) // start line
		.append(sameset) // same set? same=1 different=0
		.append(ledbin) // led bin
		.append(group) // group address
		.append(unit) // unit address
		.append(poweronmessage) // power on message=0 , f0= no power on message
		.append(broadcastIpEnable) // reserved
		.append(ipAddress) // ip address
		.append(MacAddress) // mac address
		.append(softSecVer) // soft sectoin version number
		.append(softver) // soft version
		.append(hardver) // hardware version
		.append(powertimeUser) // power time is_user, true = valid
		.append(powertimeOffHour) // power time off hour
		.append(powertimeOffMin) // power time off min
		.append(powertimeOnHour) // power time on hour
		.append(powertimeOnMin) // power time on min

		.append(timeZone)
		.append(remote)
		.append(playMode)
		.append(daylight)
		
		.append(halflightTimeUser) // power time is_user, true = valid
		.append(halflightTimeOffHour) // power time off hour
		.append(halflightTimeOffMin) // power time off min
		.append(halflightTimeOnHour) // power time on hour
		.append(halflightTimeOnMin)
		
		.append(systemStat) // starting state of a system, 0= schedule mode
		.append(uselogin) // log in, default=no log in.
		.append(slaveNum) // number of slave board
		.append(tempFlag) // temp flag, 0=celcious, 1=fahrenheit
		.append(newLineWidth) // new line width
		.append(newLineHeigth) // new line height
		.append(offlineX) // offline x
		.append(offlineY) // offline y
		.append(tempValue) // temp value
		.append(serial) // serial number
		.append(dateTime) // datetime renewing files
		.append(ledName) // led name
		.append(brightCtrl) // brigth control

		.append(gatewayIp) // gateway ip
		.append(maskAddr) // mask address
		.append(cfglist1) // sigma 3000 config select
		.append(cfglist2) // sigma 3000 config select 2
		.append(functionlist1) // firmware function list
		.append(functionlist2);
		return sb.toString();
	}
}
