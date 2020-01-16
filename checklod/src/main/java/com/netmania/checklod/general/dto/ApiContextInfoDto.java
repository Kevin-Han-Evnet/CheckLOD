package com.netmania.checklod.general.dto;



import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.activities.BaseActivity;

import lib.netmania.data.dtos.BaseDto;

public class ApiContextInfoDto extends BaseDto {
	public BaseActivity mActivity = null;
	public BaseApplication mApp = null;
}
