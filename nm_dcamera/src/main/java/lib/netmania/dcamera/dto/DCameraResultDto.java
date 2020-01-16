package lib.netmania.dcamera.dto;

import java.io.File;
import java.io.Serializable;

public class DCameraResultDto implements Serializable
{
	public static final int RESULT_CANCELED = 0;
	public static final int RESULT_OK = -1;
	public static final int RESULT_EXCEPTION = -2;
	
	public File resultFile;
	public int resultCode;
	public Exception exception;
	public String message;
}
