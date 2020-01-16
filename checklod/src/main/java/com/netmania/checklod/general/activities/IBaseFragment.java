package com.netmania.checklod.general.activities;

import android.content.Intent;

public interface IBaseFragment {
	void initFragmentData();
	boolean onBackPressed();
	void moveScrollToTop();
	void onActivityResult(int requestCode, int resultCode, Intent data);
}
