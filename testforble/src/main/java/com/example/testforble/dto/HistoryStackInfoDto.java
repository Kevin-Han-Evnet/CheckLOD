package com.example.testforble.dto;

import android.app.Activity;

import java.io.Serializable;

import lib.netmania.data.dtos.BaseDto;


public class HistoryStackInfoDto extends BaseDto implements Serializable {
	public boolean is_open;
	public boolean is_same_room;
	public int stack_idx;
	public Activity activity_instance;
	
	public void HistoryStackInfoDto () {
		is_open = false;
		is_same_room = false;
		stack_idx = -1;
		activity_instance = null;
	}
}
