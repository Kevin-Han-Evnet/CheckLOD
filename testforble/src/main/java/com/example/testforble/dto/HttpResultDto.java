package com.example.testforble.dto;

import lib.netmania.data.dtos.BaseDto;

public class HttpResultDto extends BaseDto {
	public boolean isSuccess;
	public String resutMessage;
	public String status_code;
	public String auth_access_token;
	public String auth_signature_key;
}