package lib.netmania.dcamera.photo_slider;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import lib.netmania.dcamera.DCamera;
import lib.netmania.dcamera.R;
import lib.netmania.dcamera.gallery.GalleryChildActivity;
import lib.netmania.dcamera.util.BitmapUtils;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by KevinHan on 2016. 4. 19..
 */
public class FragmentPhotoSlideItem extends Fragment implements CompoundButton.OnCheckedChangeListener {


    //상수


    //UI
    private View mRootView;
    private ImageView mPhotoContainer;
    private CheckBox mCheckPhotoSelect;


    //데이타
    private int mTotal;
    private int mPosition;
    private String mImage;
    private int DENSITY = -1;


    //객체
    private Activity mActivity;
    private Picasso dPicasso;
    private GalleryChildActivity mGalleryChildActivity;
    private DisplayMetrics mDisplayMetrics;
    private PhotoViewAttacher mAttacher;


    public FragmentPhotoSlideItem () {
        //nothing yet;
    }


    /** 생성자
     *
     * @param total
     * @param position
     * @param image
     * @return
     */
    public static FragmentPhotoSlideItem newInstance (int total, int position, String image) {
        FragmentPhotoSlideItem fragment = new FragmentPhotoSlideItem();

        Bundle bundle = new Bundle();
        bundle.putInt("TOTAL", total);
        bundle.putInt("POSITION", position);
        bundle.putString("IMAGE", image);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            mTotal = getArguments().getInt("TOTAL");
            mPosition = getArguments().getInt("POSITION");
            mImage = getArguments().getString ("IMAGE");
        }

        mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        if(DENSITY == -1) {
            DENSITY = mDisplayMetrics.densityDpi;
        }

        mGalleryChildActivity = GalleryChildActivity.getInstance();

        dPicasso = DCamera.getPicasso();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.item_photo_slider, null);

        setLayout();
        initData ();

        return mRootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();


    }


    /** 레이아웃 셋팅
     *
     */
    private void setLayout () {
        mPhotoContainer = (ImageView) mRootView.findViewById (R.id.photo_container);
        mCheckPhotoSelect = (CheckBox) mRootView.findViewById (R.id.check_photo_select);
        if ("".equals (mImage) || mImage.startsWith ("http://")) {
            mCheckPhotoSelect.setVisibility (View.GONE);
        } else {
            mCheckPhotoSelect.setOnCheckedChangeListener(this);
        }

        if (mImage.startsWith("http://")) mPhotoContainer.setOnLongClickListener ((ActivityPhotoSlider) getActivity ());
    }


    /** 데이타 이니셜라이징
     *
     */
    private void initData () {

        Log.i (getClass().getSimpleName(), "이미지 로드 --> " + mImage);

        if("".equals(mImage)) {
            //doNothing;
        } else if (mImage.startsWith("http://")) {

            mPhotoContainer.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            mAttacher = new PhotoViewAttacher(mPhotoContainer);
            mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mAttacher.setOnLongClickListener((ActivityPhotoSlider) getActivity ());

            dPicasso.with(mActivity)
                    .load(mImage)
                    .resize(mDisplayMetrics.widthPixels, 0)
                    .error(R.drawable.sys_img_none)
                    .into(mPhotoContainer, new Callback() {
                        @Override
                        public void onSuccess() {
                            mAttacher.setScale(mAttacher.getMinimumScale());
                        }

                        @Override
                        public void onError() {
                            //nothing yet;
                        }
                    });
        } else {

            File tf = new File (mImage);

            mPhotoContainer.setScaleType(ImageView.ScaleType.FIT_CENTER);

            int[] sizes = BitmapUtils.getBitmapSizeFromFile (Uri.fromFile(tf));
            int width = Math.round (mDisplayMetrics.widthPixels * DCamera.IMG_SCALED);
            int height = Math.round ((width * ((long) (sizes[1] / sizes[0]))) * DCamera.IMG_SCALED);

            dPicasso.with(mActivity)
                    .load(tf)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(R.drawable.sys_img_none)
                    .resize(width, 0)
                    .into(mPhotoContainer);

            mCheckPhotoSelect.setOnCheckedChangeListener (null);
            if (mGalleryChildActivity.getSelections().contains(String.valueOf (mPosition))) {
                mCheckPhotoSelect.setChecked(true);
            } else {
                mCheckPhotoSelect.setChecked (false);
            }
            mCheckPhotoSelect.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //오직하나라서 분기느 하지 않음.
        if (isChecked && mGalleryChildActivity.getSelections().size() >= mGalleryChildActivity.getAttachMaxCount()) {
            ((CheckBox) buttonView).setOnCheckedChangeListener(null);
            ((CheckBox) buttonView).setChecked(false);
            ((CheckBox) buttonView).setOnCheckedChangeListener(FragmentPhotoSlideItem.this);
            return;
        }

        mGalleryChildActivity.updateAdapterView (mPosition);
        ((ActivityPhotoSlider) getActivity ()).updateHeader();
    }


    /** 홍홍홍
     *
     * @return
     */
    public String getImageURL () {
        return mImage;
    }
}









