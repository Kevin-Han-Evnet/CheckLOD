package com.example.testforble.dto;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Vector;

import lib.netmania.ble.model.DefineFinal;

/*
import static com.tkselab.tksbeacon.DefineFinal.BCN_UHF_EPC_PTR;
import static com.tkselab.tksbeacon.DefineFinal.BCN_UHF_EPC_SIZE;
import static com.tkselab.tksbeacon.DefineFinal.BCN_UHF_PC_PTR;
import static com.tkselab.tksbeacon.DefineFinal.BCN_UHF_PC_SIZE;
import static com.tkselab.tksbeacon.DefineFinal.BCN_UHF_UD_PTR;
*/

/**
 * Created by syJ_Mac on 16. 3. 30..
 *
 *   iPad estomote virtual Beacon
 *   04-04 22:40:23.843 4440-4440/? D/TksBeacon: RAW Len = 62
	 02011A1A : FF4C0002 :
	 158492E7 : 5F4FD646 :
	 9DB13204 : 3FE94921 :
	 D831E039 : 52C80000 :
	 00000000 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 0000

 	 iPhone estomote virtual Beacon
	 4-04 22:43:37.843 4440-4440/? D/TksBeacon: RAW Len = 62
	 02011A1A : FF4C0002 :
	 158492E7 : 5F4FD646 :
	 9DB13204 : 3FE94921 :
	 D811AA4A : 1FC70000 :
	 00000000 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 0000


	 04-05 15:40:40.163 9832-9832/? D/TksBeacon: RAW Len = 62
	 0201060F : FF351200 :
	 0EC575D8 : 0017012D :
	 00001F00 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 00000000 :
	 00000000 : 0000
 */
public class BeaconDataModelNew implements Serializable
{
	public static final String LOG_TAG = "CheckLODBeacon";

	// AD2 User Type ADV
	public static final int PROTOCOL_OFFSET 	= 	3;

	// define ble becon logger item size
	public static final int BCN_RESERVED_SIZE	=	1;
	public static final int BCN_SEQ_SIZE		=	3;

	// define becon item ptr
	public static final int BCN_RESERVED_PTR	= 1;
	public static final int BCN_SEQ_PTR			= 2;
	public static final int BCN_IN_TEMP_PTR		= 5;

	public static final int BCN_AD_LENGTH_PTR	= 3;
	public static final int BCN_COMPANY_ID_PTR	= 5;
	public static final int BCN_LOG_YEAR_PTR	= 10;
	public static final int BCN_LOG_MONTH_PTR	= 11;
	public static final int BCN_LOG_DATE_PTR	= 12;
	public static final int BCN_LOG_HOUR_PTR	= 13;
	public static final int BCN_LOG_MIN_PTR		= 14;
	public static final int BCN_LOG_SEC_PTR		= 15;
	public static final int BCN_IN_RH_PTR		= 20;
	public static final int BCN_EX_TEMP_PTR		= 22;
	public static final int BCN_EX_RH_PTR		= 24;
	public static final int BLE_BAT_PTR			= 28;
	public static final int BLE_TXPWR_PTR		= 29;

	// define fix value
	public static final int BCN_AD_LENGTH_VALUE 	= 0x1A;		//30-4=26=0x1a
	public static final int BCN_AD_TYPE_VALUE 		= 0xFF;		//AD Type = Manufacturer Specific Data
	public static final int BCN_COMPANY_ID_VALUE 	= 0x1236;	//LSB:0x36, MSB:0x12
	public static final int BCN_UBEACON_ID_VALUE 	= 0xCDEF;	//LSB:0xCD, MSB:0xEF


	//beacon status (ready or run)
	public static final int BCN_STATUS_OFF = -1;
	public static final int BCN_STATUS_READY = 0;
	public static final int BCN_STATUS_RUN = 1;


	// UUID of beacon
	//public String
	//public String companyID;
	//public String beconID;

	public boolean isSlection = false;
	public String beaconSeq;
	public byte  bSeq = 0x00;
	public String beaconPcEpc = "";

	public List<Double> dListTemp = new Vector<Double>();

	public long	  iSum=0;
	public double dTemp=0;
	public double dvBat=0;
	public double dTempOnChip=0;
	//
	public double dTempInitial=-999;
	public double dTempMin=999;
	public double dTempMax=-999;

	public double dHum=0;
	public double dHumOnChip=0;


	public String RTC_YEAR = "0";
	public String RTC_MONTH = "0";
	public String RTC_DATE = "0";
	public String RTC_HOUR = "0";
	public String RTC_MIN = "0";
	public String RTC_SEC = "0";



	// reference tx power
	public int txPower;

	// Current RSSI
	public int rssi;

	// timestamp when this beacon was last time scanned
	public long timestamp;
	public String strDate, strTime, strItem;

	// ID of the becon, in case of Android it will be Bluetooth MAC address
	public String idMacAddr;


	//ready or run
	public int bcnReserved = BCN_STATUS_OFF;


	// my index
	public int  mySerial;
	public int  cntPastAway;
	public static Boolean isBeaconBleLogger(final byte[] data)
	{

		try {
			// check ad len
			if ((data[BCN_AD_LENGTH_PTR] & 0xFF) != BCN_AD_LENGTH_VALUE) {
				//LogUtil.D(LOG_TAG, "BCN_AD_LENGTH_VALUE != "+
				//				String.format("%02X", data[BCN_AD_LENGTH_PTR]) );
				return false;
			}

			// check comapny ID  LSB:MSB 0x36:0x12
			final int cid = ((data[BCN_COMPANY_ID_PTR + 1] << 8) & 0x0000FF00) | (data[BCN_COMPANY_ID_PTR]);
			if (cid != BCN_COMPANY_ID_VALUE) {
				//LogUtil.D(LOG_TAG, "BCN_COMPANY_ID_VALUE != "+String.format("%04X",cid ));
				return false;
			}

			// check beacon ID for Tks LSB:MSB 0xEF:0xCD
			final int bid = ((data[BCN_COMPANY_ID_PTR + 1] << 8) & 0x0000FF00) | (data[BCN_COMPANY_ID_PTR]);
			if (bid != BCN_COMPANY_ID_VALUE) {
				//LogUtil.D(LOG_TAG, "BCN_COMPANY_ID_VALUE != "+ String.format("%04X", bid) );
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isWorkLogging() {
		return workLogging;
	}

	public void setWorkLogging(boolean workLogging) {
		this.workLogging = workLogging;
	}

	private boolean workLogging = false;


	/** 시퀀스 넘버
	 *
	 * @param adv
	 * @param bcnSeqPtr
	 * @return
	 */
	private static int convertSeq(byte[] adv, int bcnSeqPtr) {
		byte[] bytes = {adv[bcnSeqPtr], adv[bcnSeqPtr + BCN_SEQ_SIZE - 1]};
		short s = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();

		int fuck = (int)s;
		return fuck;
	}

	private static double convertTemp(byte[] adv, int bcnExTempPtr)
	{
		byte[] bytes = {adv[bcnExTempPtr], adv[bcnExTempPtr+1]};
		short s = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();

		int iTemp = (int)s;
		return (double)(iTemp/10.0);
	}

	private static double convertHum(byte[] adv, int humPtr) {
		byte[] bytes = {adv[humPtr], adv[humPtr+1]};
		short s = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();

		int iTemp = (int)s;
		return (double)(iTemp/10.0);
	}


	/** 니미
	 *
	 * @param adv
	 * @param ptr
	 * @return
	 */
	private static String getStrDate (byte[] adv, int ptr)
	{
		/*byte[] bytes = {adv[ptr], adv[ptr+length]};
		short s = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();

		int fuck = (int)s;
		return fuck;*/


		byte bcd = adv[ptr];
		return BCDtoString (bcd);
	}


	public static String BCDtoString(byte bcd) {
		StringBuffer sb = new StringBuffer();

		byte high = (byte) (bcd & 0xf0);
		high >>>= (byte) 4;
		high = (byte) (high & 0x0f);
		byte low = (byte) (bcd & 0x0f);

		sb.append(high);
		sb.append(low);

		return sb.toString();
	}



	/*
		If you value is a signed 16-bit you want a short and int is 32-bit which can also hold the same values but not so naturally.

		It appears you wants a signed little endian 16-bit value.

		byte[] bytes =
		short s = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
		or

		short s = (short) ((bytes[0] & 0xff) | (bytes[1] << 8));
		BTW: You can use an int but its not so simple.

		// to get a sign extension.
		int i = ((bytes[0] & 0xff) | (bytes[1] << 8)) << 16 >> 16;
		or

		int i = (bytes[0] & 0xff) | (short) (bytes[1] << 8));
	 */
	public static void updateFrom(BeaconDataModelNew bcn, int rssi, final byte[] adv) {


		bcn.bcnReserved = adv[BCN_RESERVED_PTR];
		bcn.bSeq = adv[BCN_SEQ_PTR];
		bcn.beaconSeq = String.valueOf (convertSeq (adv, BCN_SEQ_PTR));

		bcn.dTemp = convertTemp(adv, BCN_EX_TEMP_PTR);
		bcn.dHum = convertHum(adv, BCN_EX_RH_PTR);

		bcn.RTC_YEAR = getStrDate(adv, BCN_LOG_YEAR_PTR);
		bcn.RTC_MONTH = getStrDate(adv, BCN_LOG_MONTH_PTR);
		bcn.RTC_DATE = getStrDate(adv, BCN_LOG_DATE_PTR);
		bcn.RTC_HOUR = getStrDate(adv, BCN_LOG_HOUR_PTR);
		bcn.RTC_MIN = getStrDate(adv, BCN_LOG_MIN_PTR);
		bcn.RTC_SEC = getStrDate(adv, BCN_LOG_SEC_PTR);

		bcn.dListTemp.add (bcn.dTemp);
		bcn.dTempOnChip = convertTemp(adv, BCN_IN_TEMP_PTR);
		bcn.dHumOnChip = convertHum(adv, BCN_IN_RH_PTR);

		bcn.iSum++;

		// set list limit 50
		/** 갯수 제한 하지 않는다 **/
		if( bcn.dListTemp.size() > DefineFinal.LIST_TEMP_MAX )
		{
			bcn.dListTemp.remove(0);
			bcn.cntPastAway++;
		}

		bcn.dvBat  = (double)((adv[BLE_BAT_PTR] & 0xff)/10.0);

		bcn.rssi = rssi;

		bcn.timestamp = new java.util.Date().getTime();

		// date and time
		bcn.strTime = DefineFinal.getTime(bcn.timestamp);
		bcn.strDate = DefineFinal.getDate(bcn.timestamp);

		bcn.txPower = (int)adv[BLE_TXPWR_PTR];

		//LogUtil.D(DefineFinal.LOG_TAG, "updt:"+ bcn.idMacAddr + ">" + bcn.dateTime + ",Sel:" + bcn.isSlection);
		// LogUtil.D(DefineFinal.LOG_TAG, bcn.toString() );

		// status : initial, min, max
		if(bcn.dTempInitial == -999) bcn.dTempInitial = bcn.dTemp;
		if(bcn.dTempMin > bcn.dTemp) bcn.dTempMin = bcn.dTemp;
		if(bcn.dTempMax < bcn.dTemp) bcn.dTempMax = bcn.dTemp;
	}

	@Override
	public String toString() {
		return "BeaconDataModel{"	+
				", Selection:" + isSlection	+
				", idMacAddr:" + idMacAddr 	+
				", Seq:" 		+ beaconSeq 	+
				//",PcEpc=" 		+ beaconPcEpc 	+
				", Temp:" 		+ dTemp 		+
				//"\n List=" 		+ dListTemp 	+
				//"\n dvBat=" 	+ dvBat 		+
				//", txPower=" 	+ txPower +
				", rssi:" 		+ rssi +
				", Time:"	+ strDate + strTime +
				", initial:"	+ dTempInitial +
				", minimum:"	+ dTempMin +
				", maximum:"	+ dTempMax +
				'}';
	}

	public void addFrom(final BluetoothDevice device,
						final int rssi,
						final byte[] adv) {

		// Seq Value
		// oops signed use & 0xff
		//this.beaconSeq = Integer.valueOf( advertisement[BCN_SEQ_PTR]).toString();
		//int nSeq = adv[BCN_SEQ_PTR] & 0xff;
		//this.beaconSeq = Integer.valueOf(nSeq).toString();

		/*
		int i;
		// UHF PC+EPC
		StringBuilder stringBuilderPcEpc = new StringBuilder();
		for( i=0; i<BCN_UHF_PC_SIZE; i++)
		{
			stringBuilderPcEpc.append( String.format("%02X", adv[BCN_UHF_PC_PTR+i]));
		}
		stringBuilderPcEpc.append(" ");

		for( i=0; i<BCN_UHF_EPC_SIZE; i++)
		{
			stringBuilderPcEpc.append( String.format("%02X", adv[BCN_UHF_EPC_PTR+i]));
		}
		this.beaconPcEpc = stringBuilderPcEpc.toString();
		*/

		this.cntPastAway = 0;
		this.idMacAddr = device.getAddress();
		this.updateFrom(this, rssi, adv);
	}

	public boolean isOverlapSeq( byte bSeq ) {
		if( this.bSeq == bSeq )
			return true;

		return false;
	}
} //public class BeaconDataModel