package lib.netmania.data.dtos;

import java.io.Serializable;
import java.lang.reflect.Field;

public class BaseDto implements Serializable {

	/**
     * auto generated serial id
     */
    private static final long serialVersionUID = -4773154135868456081L;
    /** do not modify above **/
    
    
    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Class<?> cls = this.getClass();
		Field[] fs = cls.getFields();
		boolean firstCheck = true;
		for(Field f : fs) {
			try {
				sb.append(firstCheck ? "" : ", ").append(f.getName()).append("=").append(f.get(this));
			} catch (Exception e) {;}
			if(firstCheck) firstCheck = false;
		}
		return sb.toString();
	}
    
    //파라미터 리스트
    public Field[] params () {
		Class<?> cls = this.getClass();
    	Field[] fs = cls.getFields();
    	
    	return fs;
    	
    }
    
    //흐흐흐흐
    public void setValue (String key, Object value) {
		Class<?> cls = this.getClass();
		Field f1 = null;
		
		try {
			f1 = cls.getField(key);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		try {
			f1.set (this, value);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//흐흐흐흐
	public void setIntValue (String key, int value) {
		Class<?> cls = this.getClass();
		Field f1 = null;

		try {
			f1 = cls.getField(key);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			f1.set (this, value);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//흐흐흐흐
	public void setBooleanValue (String key, boolean value) {
		Class<?> cls = this.getClass();
		Field f1 = null;

		try {
			f1 = cls.getField(key);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			f1.set (this, value);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
