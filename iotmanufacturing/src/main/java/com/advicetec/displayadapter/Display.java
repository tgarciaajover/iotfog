package com.advicetec.displayadapter;

import javax.xml.bind.DatatypeConverter;

public class Display{

	public static final char[] START  = {0x01};
	public static final char[] ADDRESS = {0x02};

	public static final String DATA_PREFIX_OUT = "55" + "a7";
	public static final char[] DATA_PREFIX_IN = {0x55,0xa8};
	public static final char[] DST_ADDR = {0x01,0x01};
	
	public static final char[] HEAD = {'Q','Z','0','0','S','A','X'};
	public static final char[] EOF = {0x4};
	public static final char[] NEW_FRAME = {0x0c};
	public static final char[] LINE_FEED = {0x0d};
	public static final char[] Halfspace = {0x82};


	public static class Flash{
		private final static char com = 0x07;
		public final static char[] ON = {com,'1'};
		public final static char[] OFF = {com,'0'};
	}

	public static class LineSpacing{
		private final static char com = 0x08;

		public final static char[] LS0 = {com,'0'};
		public final static char[] LS1 = {com,'1'};
		public final static char[] LS2 = {com,'2'};
		public final static char[] LS3 = {com,'3'};
		public final static char[] LS4 = {com,'4'};
		public final static char[] LS5 = {com,'5'};
		public final static char[] LS6 = {com,'6'};
		public final static char[] LS7 = {com,'7'};
		public final static char[] LS8 = {com,'8'};
		public final static char[] LS9 = {com,'9'};
	}

	public static class PatternControl {
		private final static char com = 0x0A;
		private final static char I = 'I';
		private final static char O = 'O';

		public final static char[] I_RANDOM = {com,I,0x2F};
		public final static char[] O_RANDOM = {com,O,0x2F};
		public final static char[] I_JUMP_OUT = {com,I,0x30};
		public final static char[] O_JUMP_OUT = {com,O,0x30};
		public final static char[] I_MOVE_LEFT = {com,I,0x31};
		public final static char[] O_MOVE_LEFT = {com,O,0x31};
		public final static char[] I_MOVE_RIGHT = {com,I,0x32};
		public final static char[] O_MOVE_RIGHT = {com,O,0x32};
		public final static char[] I_SCROLL_LEFT = {com,I,0x33};
		public final static char[] O_SCROLL_LEFT = {com,O,0x33};
		public final static char[] I_SCROLL_RIGHT = {com,I,0x34};
		public final static char[] O_SCROLL_RIGHT = {com,O,0x34};
		public final static char[] I_MOVE_UP = {com,I,0x35};
		public final static char[] O_MOVE_UP = {com,O,0x35};
		public final static char[] I_MOVE_DOWN = {com,I,0x36};
		public final static char[] O_MOVE_DOWM = {com,O,0x36};
		public final static char[] I_SCROLL_LR = {com,I,0x37};
		public final static char[] O_SCROLL_LR = {com,O,0x37};
		public final static char[] I_SCROLL_UP = {com,I,0x38};
		public final static char[] O_SCROLL_UP = {com,O,0x38};
		public final static char[] I_SCROLL_DOWN = {com,I,0x39};
		public final static char[] O_SCROLL_DOWN = {com,O,0x39};
		public final static char[] I_FOLD_LR = {com,I,0x3A};
		public final static char[] O_FOLD_LR = {com,O,0x3A};
		public final static char[] I_FOLD_UD = {com,I,0x3B};
		public final static char[] O_FOLD_UD = {com,O,0x3B};
		public final static char[] I_SCROLL_UD = {com,I,0x3C};
		public final static char[] O_SCROLL_UD = {com,O,0x3C};
		public final static char[] I_SUTTLE_LR = {com,I,0x3D};
		public final static char[] O_SUTTLE_LR = {com,O,0x3D};
		public final static char[] I_SUTTLE_UD = {com,I,0x3E};
		public final static char[] O_SUTTLE_UD = {com,O,0x3E};
		public final static char[] I_PEEL_OFF_L = {com,I,0x3F};
		public final static char[] O_PEEL_OFF_L = {com,O,0x3F};
		public final static char[] I_PEEL_OFF_R = {com,I,0x40};
		public final static char[] O_PEEL_OFF_R = {com,O,0x40};

		public final static char[] I_RAINDROPS = {com,I,0x43};
		public final static char[] O_RAINDROPS = {com,O,0x43};
		public final static char[] I_RANDOM_MOSAIC = {com,I,0x44};
		public final static char[] O_RANDOM_MOSAIC = {com,O,0x44};
		public final static char[] I_TWINKLE_STARS = {com,I,0x45};
		public final static char[] O_TWINKLE_STARS = {com,O,0x45};
		public final static char[] I_HIP_HOP = {com,I,0x46};
		public final static char[] O_HIP_HOP = {com,O,0x46};
		public final static char[] I_RADAR = {com,I,0x47};
		public final static char[] O_RADAR = {com,O,0x47};
	}

	public static class Pause{
		private final static char com = 0x0E;

		public final static char[] SEC_00 = {com,'0'};
		public final static char[] MIL_SEC_00 = {com,'1'};
		public final static char[] SEC_0000 = {com,'2'};
		public final static char[] MILSEC_0000 = {com,'3'};
	}

	public static class Speed{
		private final static char com = 0x0F;

		public final static char[] VERY_FAST = {com,'0'};
		public final static char[] FAST = {com,'1'};
		public final static char[] MED_FAST = {com,'2'};
		public final static char[] MEDIUM = {com,'3'};
		public final static char[] VERY_SLOW = {com,'6'};
		public final static char[] SLOW = {com,'5'};
		public final static char[] MED_SLOW = {com,'4'};
	}

	public static class FontSize{
		private final static char com = 0x1A;

		public final static char[] EN_5X5 = {com,'0'};
		public final static char[] EN_7X6 = {com,'1'};
		public final static char[] EN_14X8 = {com,'2'};
		public final static char[] EN_15X9 = {com,'3'};
		public final static char[] EN_16X9 = {com,'4'};
		public final static char[] EN_24X16 = {com,'6'};
		public final static char[] EN_32X18 = {com,'8'};
	}

	public static class DisposalMode{
		static final char[] CONSTRAINT = {0x1b,'0','a'};
		static final char[] DEFAULT = {0x1b,'0','b'};
	}

	public static class TextColor{
		private final static char com = 0x1C;

		public final static char[] BLACK = {com,'0'};
		public final static char[] RED = {com,'1'};
		public final static char[] GREEN = {com,'2'};
		public final static char[] AMBER = {com,'3'};
		public final static char[] MIX_PAL1 = {com,'4'};
		public final static char[] MIX_PAL2 = {com,'5'};
		public final static char[] MIX_PAL3 = {com,'6'};
		public final static char[] MIX_PAL4 = {com,'7'};
	}

	public static class Background{
		private final static char com = 0x1D;

		public final static char[] BLACK = {com,'0'};
		public final static char[] RED = {com,'1'};
		public final static char[] GREEN = {com,'2'};
		public final static char[] AMBER = {com,'3'};
	}

	public static class HorizontalAlign{
		private final static char com = 0x1E;

		public final static char[] CENTER = {com,'0'};
		public final static char[] LEFT = {com,'1'};
		public final static char[] RIGHT = {com,'2'};
	}

	public static class VerticalAlign{
		private final static char com = 0x1F;

		public final static char[] CENTER = {com,'0'};
		public final static char[] TOP = {com,'1'};
		public final static char[] BOTTOM = {com,'2'};
	}

	public static class TestCommand{
		private final static String com = "03";
		
		public final static String CONX_TEST = com + "01";
		public final static String AUTO_TEST = com + "02";
		public final static String ALL_BRIGTH_TEST = com + "03";
		
		public final static String END_TEST = com + "09";
	}
	
	public static String checksum(final byte[] bytes){
		int checksum = 0;
		
		for(byte b: bytes){
			checksum += 0xff & b;
		}
				
		String s = String.format("%04X", checksum);
		
		// put it on little endian.
		s = s.substring(2,4) + s.substring(0,2);
		
		return s;
	}
}