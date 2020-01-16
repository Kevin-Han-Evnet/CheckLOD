package lib.netmania.ble.model;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Vector;

public class BeaconDataModelNew implements Serializable {
	public static final String LOG_TAG = "CheckLODBeacon";

	public static final String NT = "CL";

	// AD2 User Type ADV
	public static final int PROTOCOL_OFFSET 	= 	7;

	// define ble becon logger item size

	// define becon item ptr
	public static final int BCN_COMPANY_NAME_PTR	= PROTOCOL_OFFSET;
	public static final int BCN_RESERVE_PTR			= 2 + PROTOCOL_OFFSET;
	public static final int BCN_SEQ_PTR				= 3 + PROTOCOL_OFFSET;
	public static final int BCN_PROBE_TEMP_PTR		= 7 + PROTOCOL_OFFSET;
	public static final int BCN_PROBE_HUM_PTR		= 9 + PROTOCOL_OFFSET;
	public static final int BCN_CHIPSET_TEMP_PTR	= 11 + PROTOCOL_OFFSET;
	public static final int BCN_CHIPSET_HUM_PTR		= 13 + PROTOCOL_OFFSET;
	public static final int BLE_STATUS_PTR			= 15 + PROTOCOL_OFFSET;
	public static final int BLE_BAT_VOL_PTR			= 16 + PROTOCOL_OFFSET;



	//beacon status (ready or run)
	public static final int BCN_STATUS_OFF = -1;
	public static final int BCN_STATUS_READY = 0;
	public static final int BCN_STATUS_RUN = 1;

	//sensor disconnected
	public static final int CONNECTED = 0;
	public static final int DISCONNECTED = 1;


	public boolean isSlection = false;
	public String beaconSeq;

	public List<Double> dListTemp = new Vector<Double>();

	public long	  iSum=0;
	public double dTemp=0;
	public int dvBat=0;
	public double dvBatVol = 0.0;
	public double dTempOnChip=0;
	//
	public double dTempInitial=-999;
	public double dTempMin=999;
	public double dTempMax=-999;

	public double dHum=0;
	public double dHumOnChip=0;
	public int sensor_disconnected = 0;
	public int hasNext = 0;


	// Current RSSI
	public int rssi;

	// timestamp when this beacon was last time scanned
	public long timestamp;
	public String strDate, strTime, strItem;

	// ID of the becon, in case of Android it will be Bluetooth MAC address
	public String idMacAddr;


	//ready or run
	public int bcnReserved = BCN_STATUS_OFF;

	public boolean isWorkLogging() {
		return workLogging;
	}

	public void setWorkLogging(boolean workLogging) {
		this.workLogging = workLogging;
	}

	private boolean workLogging = false;

	public String name_tag;


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



	public static void updateFrom(BeaconDataModelNew bcn, int rssi, final byte[] adv) {


		byte[] tmp = {adv[BCN_COMPANY_NAME_PTR], adv[BCN_COMPANY_NAME_PTR + 1]};
		bcn.name_tag = new String(tmp);

		bcn.bcnReserved = adv[BCN_RESERVE_PTR];
		bcn.beaconSeq = String.valueOf (convertSeq (adv, BCN_SEQ_PTR));//bytesToHex (adv);

		bcn.dTemp = convertTemp(adv, BCN_PROBE_TEMP_PTR);
		bcn.dHum = convertHum(adv, BCN_PROBE_HUM_PTR);

		bcn.dListTemp.add (bcn.dTemp);
		bcn.dTempOnChip = convertTemp(adv, BCN_CHIPSET_TEMP_PTR);
		bcn.dHumOnChip = convertHum(adv, BCN_CHIPSET_HUM_PTR);

		int tmpK = adv[BLE_STATUS_PTR];
		switch (tmpK) {
			case 0x07 :
				bcn.dvBat = 1;
				bcn.sensor_disconnected = 0;
				bcn.hasNext = 1;
				break;

			case 0x06 :
				bcn.dvBat = 1;
				bcn.sensor_disconnected = 0;
				bcn.hasNext = 1;
				break;

			case 0x05 :
				bcn.dvBat = 0;
				bcn.sensor_disconnected = 0;
				bcn.hasNext = 1;
				break;

			case 0x03 :
				bcn.dvBat = 1;
				bcn.sensor_disconnected = 0;
				bcn.hasNext = 0;
				break;

			case 0x02 :
				bcn.dvBat = 0;
				bcn.sensor_disconnected = 0;
				bcn.hasNext = 0;
				break;

			case 0x01 :
				bcn.dvBat = 1;
				bcn.sensor_disconnected = 1;
				bcn.hasNext = 0;
				break;

			case 0x00 :
			default :
				bcn.dvBat = 0;
				bcn.sensor_disconnected = 1;
				bcn.hasNext = 0;

		}

		bcn.iSum++;
		bcn.dvBatVol = (double) adv[BLE_BAT_VOL_PTR] / 20.0;

		bcn.rssi = rssi;

		bcn.timestamp = new java.util.Date().getTime();

		// date and time
		bcn.strTime = DefineFinal.getTime(bcn.timestamp);
		bcn.strDate = DefineFinal.getDate(bcn.timestamp);

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
				", Temp:" 		+ dTemp 		+
				"℃, Hum:" 		+ dHum 		+
				"%, Temp2:" 		+ dTempOnChip 		+
				"℃, Hum2:" 		+ dHumOnChip 		+
				"%, Battery:" 		+ dvBat +
				", rssi:" 		+ rssi +
				", Time:"	+ strDate + strTime +
				'}';
	}

	public void addFrom(final BluetoothDevice device,
						final int rssi,
						final byte[] adv) {

		this.idMacAddr = device.getAddress();
		this.updateFrom(this, rssi, adv);
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


	// my index
	public static Boolean isBeaconBleLogger(final byte[] data, String idMacAddr) {
		byte[] tmp = {data[BCN_COMPANY_NAME_PTR], data[BCN_COMPANY_NAME_PTR + 1]};
		String name_tag = new String(tmp);

		Log.i ("FUCK", idMacAddr + " ----> " + name_tag + " ====> " + new String(data));

		return NT.equals(name_tag);
	}


	public static byte[] toBytes(int i) {
		byte[] result = new byte[4];

		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i /*>> 0*/);

		return result;
	}

}