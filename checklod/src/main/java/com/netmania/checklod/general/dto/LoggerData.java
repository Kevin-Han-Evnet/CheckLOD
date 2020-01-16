package com.netmania.checklod.general.dto;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by syJ_Mac on 2017. 6. 8..
 *  Cmd to communicate with the beacon logger
 * rxLogger Daata Format
 * 	typedef struct {
 *
 *  uint8_t		u8_rsp_type;						//0, 1	, 0xE0:RSP_START
 *  												, 0xE1:RSP_RECORD
 *  												, 0xE2:RSP_EMPTY
 *  												, 0xE3:RSP_CLEAR
 *  												, 0xE4:RSP_DISCON
 *  uint8_t		u8_result					//1, 1+1 = 1
 * 	uint16_t	u16_recNo_sz;				//2, 2+2 = 4
 	uint16_t	u16_seq;					//4, 4+2 = 6
 	uint8_t		u8a_rtc_bcd[NUM_OF_EPT];	//6, 6+5 = 11
 	uint8_t		u8_vbat;					//11, 11+1= 12
 	int16_t		i16_inTm;					//12, 12+2= 14
 	int16_t		i16_exTm;					//14, 14+2= 16
 	int16_t		i16_inRh;					//16, 16+2= 18
 	int16_t		i16a_exRh;					//18, 18+2= 20
 	} Logger_Record_t
 */

public class LoggerData
{
	public static final String TAG = "LiotUART";

	// Master Cmd To Logger
	public static final byte BYTE_CMD_START		= (byte)0xC0;	// Rec Start RSP:ReC Size
	public static final byte BYTE_CMD_RECORD	= (byte)0xC1;	//
	public static final byte BYTE_CMD_CLERA		= (byte)0xC2;
	public static final byte BYTE_CMD_DISCON	= (byte)0xC3;
	public static final byte BYTE_CMD_NON		= (byte)0xC4;
	public static final int  NUM_OF_BYTE_CMDS	= 4;

	// Temp Logger Rsp from Logger
	public static final int  LENGTH_OF_REC		= 20;			// 로거 한 레코드 길이
	public static final byte BYTE_RSP_START 	= (byte)0xE0;	// 데이타 보내기 위한 초기화 및 전송시작 알림
	public static final byte BYTE_RSP_RECORD	= (byte)0xE1;	// 데이타 레코드
	public static final byte BYTE_RSP_EMPTY		= (byte)0xE2;	// 더 이상의 데이타 없다.
	public static final byte BYTE_RSP_CLEAR		= (byte)0xE3;	// 데이타 삭제알림
	public static final byte BYTE_RSP_DISCON	= (byte)0xE4;	// 접속 해제 알림.
	public static final byte BYTE_RSP_NON		= (byte)0xE5;	// 해당하는 응답없다
	public static final int  NUM_OF_BYTE_RSPS	= 5;			// 응답 Record 종류 갯수

	// Rsp Byte PTR
	private static final int	PTR_RSP_TYPE		= 0;
	private static final int	PTR_RSP_RESULT		= 1;	// result of response 0(fail), 1(OK)
	private static final int	PTR_RSP_REC_NO_SZ	= 2;	// RecNo / RecSize depend on RSP Type
	private static final int	PTR_RSP_SEQ			= 4;	// Seq No
	private static final int	PTR_RSP_RTC			= 6;	// RTC[MMDDhhmmss]
	private static final int	PTR_RSP_VBAT		= 11;	// Battery Volt * 10
	private static final int	PTR_RSP_IN_TEMP		= 14;	// In Temp 	16bit * 10
	private static final int	PTR_RSP_EX_TEMP		= 12;	// Ex Temp 	16bit * 10
	private static final int	PTR_RSP_IN_RH		= 16;	// In Rh 	16bit * 10
	private static final int	PTR_RSP_EX_RH		= 18;	// Ex Rh 	16bit * 10


	//Var for Rec Items
	private byte	m_bRspResult = 0;
	public short	m_sBcnCycleSec=0;
	public short	m_sRecordSize=0;
	public short	m_sRecordNo = 0;
	public short	m_sSeqNo = 0;
	public double 	m_dInTemp = 0.0;
	public double 	m_dInRh = 0.0;
	public double 	m_dExTemp = 0.0;
	public double 	m_dExRh = 0.0;
	public double  m_dBatV = 0.0;
	public String m_strRtc = "";

	// Return Type of RSP Index
	public  static byte scanResponse(LoggerData ld, byte[] rsp)
	{
		//Log.d(TAG,"scanResponse:80:RSP_Type=" + String.format("%02X", rsp[PTR_RSP_TYPE] ) );
		for( byte rspType = BYTE_RSP_START; rspType<BYTE_RSP_NON; rspType++)
		{
			//Log.d(TAG,"scanResponse:83:Type=[" + rspIdx + "]"+ STR_TLGR_RSPS[rspIdx] );
			if( rsp[PTR_RSP_TYPE] == rspType )
			{
				ld.m_bRspResult = rsp[PTR_RSP_RESULT];
				//Log.d(TAG,"scanResponse:86:match" );
				if( rspType == BYTE_RSP_START )
				{	//Get Total num of Records
					byte[] b = {rsp[PTR_RSP_REC_NO_SZ],rsp[PTR_RSP_REC_NO_SZ+1] };
					ld.m_sRecordSize = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
					// Get Beacon Fly Cycle Second
					b[0] = rsp[PTR_RSP_SEQ];
					b[1] = rsp[PTR_RSP_SEQ+1];
					ld.m_sBcnCycleSec = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
					//Log.d(TAG,"scanResponse:94:RecSz=" + ld.m_sRecordSize );
				}
				return rspType;
			}
		}
		Log.d(TAG,"errRspScan:None");
		return BYTE_RSP_NON;
	}

	// Update Record Item
	public  void updateRecord(byte[] rsp)
	{
		byte[] b = {0,0};
		short s;
		int	 i;


		// Rec No.
		b[0] = rsp[PTR_RSP_REC_NO_SZ];
		b[1] = rsp[PTR_RSP_REC_NO_SZ+1];
		m_sRecordNo = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
		// Rec Seq
		b[0] = rsp[PTR_RSP_SEQ];
		b[1] = rsp[PTR_RSP_SEQ+1];
		m_sSeqNo = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
		// rec In Temp;
		b[0] = rsp[PTR_RSP_IN_TEMP];
		b[1] = rsp[PTR_RSP_IN_TEMP+1];
		s = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
		i = (int)s;
		m_dInTemp = (double)(i/10.0);

		// rec In Rh;
		b[0] = rsp[PTR_RSP_IN_RH];
		b[1] = rsp[PTR_RSP_IN_RH+1];
		s = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
		i = (int)s;
		m_dInRh = (double)(i/10.0);

		// rec Ex Temp;
		b[0] = rsp[PTR_RSP_EX_TEMP];
		b[1] = rsp[PTR_RSP_EX_TEMP+1];
		s = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
		i = (int)s;
		m_dExTemp = (double)(i/10.0);

		// rec Ex Rh;
		b[0] = rsp[PTR_RSP_EX_RH];
		b[1] = rsp[PTR_RSP_EX_RH+1];
		s = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
		i = (int)s;
		m_dExRh = (double)(i/10.0);

		m_dBatV = (double)(( rsp[PTR_RSP_VBAT] & 0xFF) / 10.0);
		m_strRtc = String.format("00-%02X-%02X", rsp[PTR_RSP_RTC],rsp[PTR_RSP_RTC+1]);
		m_strRtc += String.format("%02X:%02X:%02X", rsp[PTR_RSP_RTC+2],rsp[PTR_RSP_RTC+3],rsp[PTR_RSP_RTC+4] );
	}

	public boolean getRspResult()
	{
		if( m_bRspResult == 0x00 )
			return false;

		return true;
	}

	public short getBcnCycleSec()
	{
		return m_sBcnCycleSec;
	}

	public short getRecordSize()
	{
		return m_sRecordSize;
	}

	public short getRecordNo()
	{
		return m_sRecordNo;
	}

	@Override
	public String toString()
	{
		return "R{" +
				m_sRecordNo +
				"," + m_sSeqNo +
				"," + m_strRtc +
				"," + m_dInTemp + "℃" +
				"," + m_dInRh + "%" +
				"," + m_dExTemp +  "℃" +
				"," + m_dExRh + "%" +
				"," + m_dBatV + "v" +
				'}';
	}
}
