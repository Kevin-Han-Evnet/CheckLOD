package com.netmania.checklod.general.dto;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LoggerDataNew {
	public static final String TAG = "LiotUART";

	// Master Cmd To Logger
	public static final byte BYTE_CMD_START		= (byte)0xC0;	// Rec Start RSP:ReC Size
	public static final byte BYTE_CMD_RECORD	= (byte)0xC1;	//
	public static final byte BYTE_CMD_CLERA		= (byte)0xC2;
	public static final byte BYTE_CMD_DISCON	= (byte)0xC3;



	public static final int BCN_DATA_OFFSET = 0;

	public static final int BCN_COMPANY_NAME_PTR	= 0 + BCN_DATA_OFFSET;
	public static final int BCN_RESERVE_PTR			= 2 + BCN_DATA_OFFSET;
	public static final int BCN_SEQ_PTR				= 3 + BCN_DATA_OFFSET;
	public static final int BCN_PROBE_TEMP_PTR		= 7 + BCN_DATA_OFFSET;
	public static final int BCN_PROBE_HUM_PTR		= 9 + BCN_DATA_OFFSET;
	public static final int BCN_CHIPSET_TEMP_PTR	= 11 + BCN_DATA_OFFSET;
	public static final int BCN_CHIPSET_HUM_PTR		= 13 + BCN_DATA_OFFSET;
	public static final int BLE_STATUS_PTR			= 15 + BCN_DATA_OFFSET;
	public static final int BLE_BAT_VOL_PTR			= 16 + BCN_DATA_OFFSET;


	//Var for Rec Items
	public String m_sSeqNo = "0";




	public double dTemp=0;
	public int dvBat=0;
	public double dvBatVol = 0.0;
	public double dTempOnChip=0;

	public double dHum=0;
	public double dHumOnChip=0;
	public int hasNext = 0;
	public int sensor_disconnected = 0;



	// Update Record Item
	public  void updateRecord(byte[] rsp)
	{
		byte[] b = {0,0};
		short s;
		int	 i;


		m_sSeqNo = String.valueOf (convertSeq (rsp, BCN_SEQ_PTR));//bytesToHex (adv);
		dTemp = convertTemp(rsp, BCN_PROBE_TEMP_PTR);
		dHum = convertHum(rsp, BCN_PROBE_HUM_PTR);

		dTempOnChip = convertTemp(rsp, BCN_CHIPSET_TEMP_PTR);
		dHumOnChip = convertHum(rsp, BCN_CHIPSET_HUM_PTR);

		int tmpK = rsp[BLE_STATUS_PTR];
		switch (tmpK) {
			case 0x07 :
				dvBat = 1;
				sensor_disconnected = 0;
				hasNext = 1;
				break;

			case 0x06 :
				dvBat = 1;
				sensor_disconnected = 0;
				hasNext = 1;
				break;

			case 0x05 :
				dvBat = 0;
				sensor_disconnected = 0;
				hasNext = 1;
				break;

			case 0x03 :
				dvBat = 1;
				sensor_disconnected = 0;
				hasNext = 0;
				break;

			case 0x02 :
				dvBat = 0;
				sensor_disconnected = 0;
				hasNext = 0;
				break;

			case 0x01 :
				dvBat = 1;
				sensor_disconnected = 1;
				hasNext = 0;
				break;

			case 0x00 :
			default :
				dvBat = 0;
				sensor_disconnected = 1;
				hasNext = 0;

		}

		dvBatVol = (double) rsp[BLE_BAT_VOL_PTR] / 20.0;
	}



	@Override
	public String toString()
	{

		return "R{seq:" + m_sSeqNo +
				",probe_temp:" + dTemp + "℃" +
				",probe_hum:" + dHum + "%" +
				",chipset_temp:" + dTempOnChip +  "℃" +
				",chipset_hum:" + dHumOnChip + "%" +
				",battery:" + dvBatVol + "v" +
				",has_next:" + hasNext +
				'}';
	}




	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}




	/** 시퀀스 넘버
	 *
	 * @param adv
	 * @param bcnSeqPtr
	 * @return
	 */
	private static int convertSeq(byte[] adv, int bcnSeqPtr) {
		byte[] bytes = {adv[bcnSeqPtr], adv[bcnSeqPtr+1], adv[bcnSeqPtr+2], adv[bcnSeqPtr+3]};
		int s = (int) ByteBuffer.wrap(bytes).getInt();

		return s;
	}

	private static double convertTemp(byte[] adv, int bcnExTempPtr)
	{
		byte[] bytes = {adv[bcnExTempPtr], adv[bcnExTempPtr + 1]};
		short s = ByteBuffer.wrap(bytes).getShort();

		int iTemp = (int)s;
		return (double)(iTemp / 100.0);
	}

	private static double convertHum(byte[] adv, int humPtr) {
		byte[] bytes = {adv[humPtr], adv[humPtr+1]};
		short s = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();

		int iTemp = (int)s;
		return (double)(iTemp / 100.0);
	}
}

