package com.netmania.checklod.general.utils;

import android.text.InputFilter;
import android.text.Spanned;

import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.R;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String 유틸리티
 * 
 * @author jungeh
 *
 */
public class StringUtils {
	
	
	public static String DATE_DELIMITER = "-";
	

	/**
	 * 천단위 콤마
	 * 
	 * @param value
	 * @return
	 */
	public static String setComma (int value) {
		DecimalFormat format = new DecimalFormat("#,##0");
		return format.format(value);
	}
	
	public static String setComma (long value) {
		DecimalFormat format = new DecimalFormat("#,##0");
		return format.format(value);
	}
	
	public static String setComma (double value) {
		DecimalFormat format = new DecimalFormat("#,##0.00");
		return format.format(value);
	}
	
	public static String setComma (float value) {
		DecimalFormat format = new DecimalFormat("#,##0.00");
		return format.format(value);
	}

	/**
	 * <pre>
	 * 문자열 형태의 날짜를 원하는 형태로 변환합니다.
	 * 
	 * 예시)
	 * "yyyy.MM.dd G 'at' HH:mm:ss z"       2001.07.04 AD at 12:08:56 PDT
	 * "EEE, MMM d, ''yy"   Wed, Jul 4, '01
	 * "h:mm a"     12:08 PM
	 * "hh 'o''clock' a, zzzz"      12 o'clock PM, Pacific Daylight Time
	 * "K:mm a, z"  0:08 PM, PDT
	 * "yyyyy.MMMMM.dd GGG hh:mm aaa"       02001.July.04 AD 12:08 PM
	 * "EEE, d MMM yyyy HH:mm:ss Z" Wed, 4 Jul 2001 12:08:56 -0700
	 * "yyMMddHHmmssZ"      010704120856-0700
	 * "yyyy-MM-dd'T'HH:mm:ss.SSSZ" 2001-07-04T12:08:56.235-0700
	 * </pre>
	 * 
	 * @param date
	 *            변환할 날짜
	 * @param fromFormatString
	 *            변환될 포맷
	 * @param toFormatString
	 *            변환할 포맷
	 * @return 변환된 날짜 문자열
	 */
	public static String formattedDate(String date, String fromFormatString,
									   String toFormatString) {
		SimpleDateFormat fromFormat = new SimpleDateFormat(fromFormatString);
		SimpleDateFormat toFormat = new SimpleDateFormat(toFormatString);
		Date fromDate = null;

		try {
			fromDate = fromFormat.parse(date);
		} catch (ParseException e) {
			/** 이제그만~*///LogUtil.E("DateParsingError="+e.toString());
			fromDate = new Date();
		}

		return toFormat.format(fromDate);
	}

	public static String getLPad(String input, int pad_len, String pad_char) {
		int len = input.length();
		String return_value = "";

		if (len < pad_len) {
			for (int i = 0; i < pad_len - len; i++) {
				return_value = pad_char + return_value;
			}
			return (return_value + input);
		} else {
			return input.substring(len - pad_len, len);
		}
	}
	
	//버전 스트링 넘버로 리턴
	public static int getVersionNum (String strVersion) {
		
		int verNo = 0;
		
		String[] v = strVersion.split (" ");
		if (v.length > 1) { //베타나 알파 스트링이 앞에 붙은것으로 간주
			verNo = (v[0].equals("Alpha")) ? 1000 : 2000; //Alpha 버전은 1000번대 Beta 버전은 2000번대.
			verNo += Integer.parseInt(v[1].replaceAll("\\.", ""));
		} else {
			verNo = 3000; //릴리즈버전은 3000번대로 시작하자.
			verNo += Integer.parseInt(v[0].replaceAll("\\.", ""));
		}
		
		return verNo;
	}
	
	//QA버전넘
	public static int getQAVersionNum (String str) {
		if ( str == null ) return 0;
		
		StringBuffer sb = new StringBuffer();
		
		if (str.substring (0, 1).equals ("A")) sb.append ("1");
		else if (str.substring (0, 1).equals ("B")) sb.append ("2");
		else sb.append ("3");
		
		for(int i = 0; i < str.length(); i++){
			
			if( Character.isDigit(str.charAt(i)) ) { //숫자를 만나면 버퍼에 쌓자.
				sb.append( str.charAt(i) );
			}
		}
		return Integer.valueOf(sb.toString()); //끝까지 봤으니 뽑은 숫자 리턴
	}
	
	
	//문자열에서 보이는 숫자만 뽑자.
	public static String onlyNum (String str) {
		if ( str == null ) return "";
		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < str.length(); i++){
			
			if( Character.isDigit(str.charAt(i)) ) { //숫자를 만나면 버퍼에 쌓자.
				sb.append( str.charAt(i) );
			}
		}
		return sb.toString(); //끝까지 봤으니 뽑은 숫자 리턴
	}
	
	
	
	// 영문만 허용하는 필터
	public static InputFilter inputFilterEnglishOnly = new InputFilter() {
		
		@Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    		Pattern ps = Pattern.compile("^[a-zA-Z]+$");
    		if (!ps.matcher(source).matches()) {
    			return "";
            } 

            return null;
        }

	}; 
	
	
	//영문/숫자만 허용하는 필터
	public static InputFilter inputFilterNoKorean = new InputFilter() {
		
		@Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    		Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
    		if (!ps.matcher(source).matches()) {
    			return "";
            } 

            return null;
        }

	}; 
	
	//한글만 허용
	public InputFilter inputFilterKorean = new InputFilter() {
		
		@Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			Pattern ps = Pattern.compile("^[ㄱ-가-힣]+$");
    		if (!ps.matcher(source).matches()) {
    			return "";
            } 

            return null;
        }
	}; 



	//이메일 형식 체크
	public static boolean checkEmail (String email) {
		 String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
		  Pattern p = Pattern.compile(regex);
		 Matcher m = p.matcher(email);
		 boolean isNormal = m.matches();
		 return isNormal;
	}
	
	//영문 숫자 체크
	public static boolean checkEngNumber (String str) {
		 String regex = "^[a-zA-Z0-9]+$";
		  Pattern p = Pattern.compile(regex);
		 Matcher m = p.matcher(str);
		 boolean isNormal = m.matches();
		 return isNormal;
	}
	
	
	//빈문자열 체크
	public static boolean isEmpty (String str) {
		return ( null == str || str.isEmpty() || 1 > str.replaceAll(" ", "").length() || "null".equals (str));
	}
	
	//문자열 바이트 체크합시다.
	public static int getBytesLength (String str) {
		byte[] strBytes = null;
		try {
			strBytes = str.getBytes ("euc-kr");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		return strBytes.length;
	}
	
	//넘버앞에 0붙이기
	public static String getDecimalNumberString (String decimalFormat, int source) {
		NumberFormat nf = new DecimalFormat(decimalFormat);
		String result = nf.format(source);
		
		return result;
	}
	
	//동네 이름에서 구 혹은 동 뭐 암튼 그거 빼 보아요.
	public static String getPureNameFromAddress (String source) {
		String result = "";
		
		if (!isEmpty (source)) {
			result = source.replaceAll("\\d","");
			result = result.substring (0, result.length () - 1);
		}
		
		return result;
	}
	
	//언제 올라온 글인지 확인
	public static String getRegDateMMdd (Date regDate) {
        Calendar cal = Calendar.getInstance();
        
        try {
        	cal.setTime (regDate);
        } catch (Exception e) {
        	return "";
        }
        
        String firstPostfix = null;
        String lastPostfix = null;
        
        Integer firstField = null;
        Integer lastField = null;
        
        Integer gabday = 1;
        
        Date today = new Date();
        
        if( isSameDate (regDate, today)){
            firstPostfix = "시";
            lastPostfix = "분";
            
            firstField = Calendar.HOUR_OF_DAY;
            lastField = Calendar.MINUTE;
            gabday = 0;

        } else {
            firstPostfix = "월";
            lastPostfix = "일";

            firstField = Calendar.MONTH;
            lastField = Calendar.DAY_OF_MONTH;
        }
        
        return (cal.get(firstField) + gabday) + firstPostfix + " "
                + cal.get(lastField) + lastPostfix;
    }
	
	//날짜 정규표현식 변경
	public static String convertToStringYYYMMDD(Date input ) {
        return new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(input);
    }

	//날짜 정규표현식 변경
	public static String convertToStringYYYY_MM_DD(Date input ) {
		return new SimpleDateFormat("yyyy-MM-dd H:m:s", Locale.KOREA).format(input);
	}
	
	//날짜 요일까지 반환
	public static String convertToDateString (Date input ) {
        return new SimpleDateFormat("yyyy년 MM월 dd일 E요일", Locale.KOREA).format(input);
    }
	
	//날짜 시간만 반환
	public static String convertToTimeString (Date input ) {
        return new SimpleDateFormat("a h시 m분", Locale.KOREA).format(input);
    }
	
	//같은날이냐?
	public static Boolean isSameDate(Date input1, Date input2)  {
        return (convertToStringYYYMMDD(input1).hashCode() == convertToStringYYYMMDD(input2).hashCode());
    }
	
	//booean을 Y/N으로 변환
	public static String convertBooleanToYn (boolean value) {
		return (value == true) ? "Y" : "N";
	}
	
	//Y/N을 boolean으로 변환
	public static boolean convertYnToBoolean (String value) {
		return ("Y".equals (value.toUpperCase())) ? true : false;
	}

	//문자열 true, false boolean으로 변환
	public static boolean convertStrimgToBoolean (String value) {
		return ("true".equals (value.toLowerCase())) ? true : false;
	}
	
	public static boolean isNewNotice(String from)
	{
		Calendar toDay = Calendar.getInstance();

		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date fromDay = null;
		try
		{
			fromDay = transFormat.parse(from);
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long diffSec = (toDay.getTimeInMillis() - fromDay.getTime()) / 1000; // 초
		long diffDay = diffSec / (60 * 60 * 24); // 날

		return (diffDay >= 1) ? false : true;
	}
	
	public static String GetUTF8String(String str)
	{
		if(str.trim().length() <= 0)
			return "#";
		
		String[] cho = { "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", " ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ" };
		String[] eng = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "L", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

		String returnText = "";

		int code = str.codePointAt(0);
		
		if (code >= 44032 && code <= 55203)
		{
			int unicode = code - 44032;
			int choindex = unicode / 21 / 28;

			returnText += cho[choindex];
		}
		// 영대
		else if (code >= 65 && code <= 90)
		{
			int numcode = code - 65;
			returnText += eng[numcode];
		}
		// 영소
		else if (code >= 97 && code <= 122)
		{
			int numcode = code - 97;
			returnText += eng[numcode];

		}
		else
		{
			returnText = "#";
		}

		
		return returnText;
	}


	/** 그대는 숫자인가?
	 *
	 * @param tag
	 * @return
	 */
	public static boolean isNan (String tag) {
		return !Pattern.matches("^[0-9]+$", tag);
	}


	/** 몇시간 몇분 몇초.
	 *
	 * @param timeInMillies
	 * @return
	 */
	public static String getMilliSecondsToTimeString (long timeInMillies) {


		return String.format(BaseApplication.getInstance().getResources().getString(R.string.time_utils_something),
				TimeUnit.MILLISECONDS.toHours(timeInMillies),
				TimeUnit.MILLISECONDS.toMinutes(timeInMillies),
				TimeUnit.MILLISECONDS.toSeconds(timeInMillies) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillies))
		);




	}
	
}






