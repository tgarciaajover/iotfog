package com.advicetec.displayadapter;

import javax.xml.bind.DatatypeConverter;

public class Display{
 
	public static final String START  = "01";
	public static final String ADDRESS = "02";

	public static final String DATA_PREFIX_OUT = "55" + "a7";
	public static final String DATA_PREFIX_IN = "55" + "a8";
	public static final String DST_ADDR = "01" + "01";
	
	public static final String HEAD = "51" + "5A" + "30" + "30" + "53" + "41" + "58";
	public static final String EOF = "04";
	public static final String NEW_FRAME = "0c";
	public static final String LINE_FEED = "0d";
	public static final String Halfspace = "82";


	public static class Flash{
		private final static String com = "07";
		public final static String ON = com + "1";
		public final static String OFF = com + "0";
	}

	public static class LineSpacing{
		private final static String com = "08";

		public final static String LS0 = com + "30";
		public final static String LS1 = com + "31";
		public final static String LS2 = com + "32";
		public final static String LS3 = com + "33";
		public final static String LS4 = com + "34";
		public final static String LS5 = com + "35";
		public final static String LS6 = com + "36";
		public final static String LS7 = com + "37";
		public final static String LS8 = com + "38";
		public final static String LS9 = com + "39";
	}

	public static class PatternControl {
		private final static String com = "0A";
		private final static String I = "49";
		private final static String O = "4F";

		public final static String I_RANDOM = com + I + "2F";
		public final static String O_RANDOM = com + O + "2F";
		public final static String I_JUMP_OUT = com + I + "30";
		public final static String O_JUMP_OUT = com + O + "30";
		public final static String I_MOVE_LEFT = com + I + "31";
		public final static String O_MOVE_LEFT = com + O + "31";
		public final static String I_MOVE_RIGHT = com + I + "32";
		public final static String O_MOVE_RIGHT =  com + O + "32";
		public final static String I_SCROLL_LEFT = com + I + "33";
		public final static String O_SCROLL_LEFT = com + O + "33";
		public final static String I_SCROLL_RIGHT = com + I + "34";
		public final static String O_SCROLL_RIGHT = com + O + "34";
		public final static String I_MOVE_UP = com + I + "35";
		public final static String O_MOVE_UP = com + O + "35";
		public final static String I_MOVE_DOWN = com + I + "36";
		public final static String O_MOVE_DOWM = com + O + "36";
		public final static String I_SCROLL_LR = com + I + "37";
		public final static String O_SCROLL_LR = com + O + "37";
		public final static String I_SCROLL_UP = com + I + "38";
		public final static String O_SCROLL_UP = com + O + "38";
		public final static String I_SCROLL_DOWN = com + I + "39";
		public final static String O_SCROLL_DOWN = com + O + "39";
		public final static String I_FOLD_LR = com + I + "3A";
		public final static String O_FOLD_LR = com + O + "3A";
		public final static String I_FOLD_UD = com + I + "3B";
		public final static String O_FOLD_UD = com + O + "3B";
		public final static String I_SCROLL_UD = com + I + "3C";
		public final static String O_SCROLL_UD = com + O + "3C";
		public final static String I_SUTTLE_LR = com + I + "3D";
		public final static String O_SUTTLE_LR = com + O + "3D";
		public final static String I_SUTTLE_UD = com + I + "3E";
		public final static String O_SUTTLE_UD = com + O + "3E";
		public final static String I_PEEL_OFF_L = com + I + "3F";
		public final static String O_PEEL_OFF_L = com + O + "3F";
		public final static String I_PEEL_OFF_R = com + I + "40";
		public final static String O_PEEL_OFF_R = com + O + "40";

		public final static String I_RAINDROPS = com + I + "43";
		public final static String O_RAINDROPS = com + O + "43";
		public final static String I_RANDOM_MOSAIC = com + I + "44";
		public final static String O_RANDOM_MOSAIC = com + O + "44";
		public final static String I_TWINKLE_STARS = com + I + "45";
		public final static String O_TWINKLE_STARS = com + O + "45";
		public final static String I_HIP_HOP = com + I + "46";
		public final static String O_HIP_HOP = com + O + "46";
		public final static String I_RADAR = com + I + "47";
		public final static String O_RADAR = com + O + "47";
	}

	public static class Pause{
		private final static String com = "0E";

		public final static String SEC_00 = com + "30";
		public final static String MIL_SEC_00 = com + "31";
		public final static String SEC_0000 = com + "32";
		public final static String MILSEC_0000 = com + "33";
	}

	public static class Speed{
		private final static String com = "0F";

		public final static String VERY_FAST = com + "30";
		public final static String FAST = com + "31";
		public final static String MED_FAST = com + "32";
		public final static String MEDIUM = com + "33";
		public final static String MED_SLOW = com + "34";
		public final static String SLOW = com + "35";
		public final static String VERY_SLOW = com + "36";
	}

	public static class FontSize{
		private final static String com = "1A";

		public final static String EN_5X5 = com + "30";
		public final static String EN_7X6 = com + "31";
		public final static String EN_14X8 = com + "32";
		public final static String EN_15X9 = com + "33";
		public final static String EN_16X9 = com + "34";
		public final static String EN_24X16 = com + "36";
		public final static String EN_32X18 = com + "38";
	}

	public static class DisposalMode{
		static final String CONSTRAINT = "1b" + "30" + "61";
		static final String DEFAULT = "1b" + "30" + "62";
	}

	public static class TextColor{
		private final static String com = "1C";

		public final static String BLACK = com + "30";
		public final static String RED = com + "31";
		public final static String GREEN = com + "32";
		public final static String AMBER = com + "33";
		public final static String MIX_PAL1 = com + "34";
		public final static String MIX_PAL2 = com + "35";
		public final static String MIX_PAL3 = com + "36";
		public final static String MIX_PAL4 = com + "37";
	}

	public static class Background{
		private final static String com = "1D";

		public final static String BLACK = com + "30";
		public final static String RED = com + "31";
		public final static String GREEN = com + "32";
		public final static String AMBER = com + "33";
	}

	public static class HorizontalAlign{
		private final static String com = "1E";

		public final static String CENTER = com + "30";
		public final static String LEFT = com + "31";
		public final static String RIGHT = com + "32";
	}

	public static class VerticalAlign{
		private final static String com = "1F";

		public final static String CENTER = com + "30";
		public final static String TOP = com + "1";
		public final static String BOTTOM = com + "2";
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