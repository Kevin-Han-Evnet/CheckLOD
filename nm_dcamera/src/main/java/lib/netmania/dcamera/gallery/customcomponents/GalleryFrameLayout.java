package lib.netmania.dcamera.gallery.customcomponents;

import lib.netmania.dcamera.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryFrameLayout extends FrameLayout
{
	
	private boolean mChecked = false;
	private TextView idxField;
	private ImageView selectedRect;
	private Context mContext;
	
	public GalleryFrameLayout(Context context)
	{
		super(context);
		this.mContext = context;
	}
	
	public GalleryFrameLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.mContext = context;
	}
	
	public GalleryFrameLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		this.mContext = context;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		double density = getResources().getDisplayMetrics().density;
		double referenceWidth = 159 * density;
		double ratio = (widthSize / referenceWidth);
		
		heightSize = (int) (heightSize * ratio);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
		
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
	
	public void setChecked(boolean checked, int idx)
	{
		
		mChecked = checked;
		if (checked) {
			//this.setForeground(getResources().getDrawable(R.drawable.attach_select));

			if (selectedRect == null) {
				selectedRect = new ImageView (mContext);
				selectedRect.setBackgroundResource(R.drawable.attach_select);

				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				selectedRect.setLayoutParams (lp);
			}

			//TextView 생성
			if (idxField == null) {
				idxField = new TextView(mContext);
				idxField.setTextSize(12);
				idxField.setTextColor(Color.WHITE);

				//layout_width, layout_height, gravity 설정
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				lp.gravity = Gravity.LEFT | Gravity.TOP;
				idxField.setLayoutParams(lp);
				idxField.setPadding (15,10,5,5);
			}

			//부모 뷰에 추가
			try {
				addView(selectedRect);

				idxField.setText(String.valueOf(idx));
				addView(idxField);
			} catch (Exception e) {
				//nothing yet;
			}

		} else {
			this.setForeground(getResources().getDrawable(R.drawable.gridview_selector));
			try {
				removeView(idxField);
				removeView(selectedRect);
			} catch (Exception e) {
				//nothing yet;
			}
		}
		
	}
	
	public boolean isChecked()
	{
		return mChecked;
	}
	
}
