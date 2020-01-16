package lib.netmania.dcamera.photo_slider;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import lib.netmania.dcamera.DCamera;
import lib.netmania.dcamera.gallery.GalleryChildActivity;
import lib.netmania.dcamera.util.BitmapUtilsK;
import lib.netmania.dcamera.view.ViePagerFixed;

import java.io.File;
import java.util.ArrayList;

import lib.netmania.dcamera.R;

/**
 * Created by KevinHan on 2016. 4. 18..
 */
public class ActivityPhotoSlider extends FragmentActivity implements View.OnClickListener, View.OnLongClickListener {

    //상수
    public static final String IMAGE_LIST = "imsegList";
    public static final String FIRST_IDX = "firstIdx";


    //UI
    private View top_container, fragment_photo_menu_container;
    private ViePagerFixed mViewPager;
    private ImageView imgBtnAttachSubmit, imgBtnRotateImage, dsTitleImg, icon_attach_count, imgBtnBack;
    private TextView gallerySelectedCount;
    private Button fragment_photo_btn_download, fragment_photo_btn_cancel;


    //데이타
    private ViewFragmentAdapter mAdapter;
    private ArrayList<String> mItems;
    private int firstIdx;
    private int attachMaxCount = -1;
    private boolean downloadMenuOpen = false;
    private final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Doosaram/";



    //객체
    private ObjectAnimator slide_open;


    @Override
    public void onCreate (Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_photo_slide);

        mItems = (ArrayList<String>) getIntent ().getSerializableExtra(IMAGE_LIST);
        firstIdx = getIntent ().getIntExtra (FIRST_IDX, 0);

        setLayout();
        initData();
    }


    /** 레이아웃 셋팅
     *
     */
    private void setLayout () {

        top_container = (View) findViewById(R.id.top_container);
        if (mItems.get(0).startsWith("http://")) top_container.setVisibility (View.GONE);
        else top_container.setVisibility(View.VISIBLE);

        mViewPager = (ViePagerFixed) findViewById (R.id.viewPager);
        mAdapter = new ViewFragmentAdapter (getSupportFragmentManager ());
        mViewPager.setAdapter(mAdapter);

        if (top_container.getVisibility() == View.VISIBLE) {

            gallerySelectedCount = (TextView) findViewById (R.id.gallerySelectedCount);

            imgBtnAttachSubmit = (ImageView) findViewById (R.id.imgBtnAttachSubmit);
            imgBtnAttachSubmit.setOnClickListener(this);

            imgBtnRotateImage = (ImageView) findViewById(R.id.imgBtnRotateImage);
            imgBtnRotateImage.setVisibility(View.GONE);

            dsTitleImg = (ImageView) findViewById (R.id.dsTitleImg);
            icon_attach_count = (ImageView) findViewById(R.id.icon_attach_count);

            int max = attachMaxCount = GalleryChildActivity.getInstance ().getAttachMaxCount();
            int count = GalleryChildActivity.getInstance ().getSelections().size();
            gallerySelectedCount.setText(count + "/" + max);

            if (count > 0) {
                imgBtnAttachSubmit.setEnabled (true);
            } else {
                imgBtnAttachSubmit.setEnabled(false);
            }


            //탑메뉴 제어
            if (GalleryChildActivity.getInstance().getGalleryStyle () == DCamera.PICK_CROP_FROM_SINGLE_FILE) {
                gallerySelectedCount.setVisibility(View.GONE);
                icon_attach_count.setVisibility (View.GONE);
                dsTitleImg.setVisibility (View.VISIBLE);
                imgBtnAttachSubmit.setVisibility (View.GONE);
            } else {
                gallerySelectedCount.setVisibility(View.VISIBLE);
                icon_attach_count.setVisibility (View.VISIBLE);
                dsTitleImg.setVisibility (View.GONE);
                imgBtnAttachSubmit.setVisibility (View.VISIBLE);
            }

        } else {
            //nothing;

        }

        imgBtnBack = (ImageView) findViewById (R.id.imgBtnBack);
        imgBtnBack.setOnClickListener(kevinClickListener);

        fragment_photo_menu_container = (View) findViewById (R.id.fragment_photo_menu_container);

        fragment_photo_btn_download = (Button) findViewById (R.id.fragment_photo_btn_download);
        fragment_photo_btn_download.setTag (0);
        fragment_photo_btn_download.setOnClickListener(kevinClickListener2);

        fragment_photo_btn_cancel = (Button) findViewById (R.id.fragment_photo_btn_cancel);
        fragment_photo_btn_cancel.setTag (1);
        fragment_photo_btn_cancel.setOnClickListener (kevinClickListener2);
    }


    /** 데이타 이니셜라이징.
     *
     */
    private void initData () {
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(firstIdx);
    }

    /** 업데이트 고고
     *
     */
    public void updateHeader () {
        if (top_container.getVisibility() == View.VISIBLE) {
            int max = attachMaxCount = GalleryChildActivity.getInstance ().getAttachMaxCount();
            int count = GalleryChildActivity.getInstance ().getSelections().size();
            gallerySelectedCount.setText(count + "/" + max);

            if (count > 0) {
                imgBtnAttachSubmit.setEnabled (true);
            } else {
                imgBtnAttachSubmit.setEnabled(false);
            }

            if (count >= 1 && GalleryChildActivity.getInstance ().getGalleryStyle () == DCamera.PICK_CROP_FROM_SINGLE_FILE) {
                GalleryChildActivity.getInstance ().gotoResult ();
                finish ();
            }
        }
    }


    /** 온클릭
     *
     * @param src
     */
    @Override
    public void onClick(View src) {
        GalleryChildActivity.getInstance ().gotoResult ();
        finish();
    }







    @Override
    public boolean onLongClick (View src) {
        downloadMenuOpen = false;
        openImageDownLoadMenu();

        return false;
    }

    //이미지 다운로드 메뉴셋 열기
    private void openImageDownLoadMenu () {


        if (downloadMenuOpen) return; //이미 열렸다면 패스
        downloadMenuOpen = true;


        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        float targetY = mDisplayMetrics.heightPixels - fragment_photo_menu_container.getHeight();
        float startY = mDisplayMetrics.heightPixels;

        slide_open = ObjectAnimator.ofFloat(fragment_photo_menu_container, View.Y, startY, targetY);
        slide_open.setDuration(500);
        slide_open.start();

        fragment_photo_menu_container.setVisibility(View.VISIBLE);
    }

    //이미지 다운로드 메뉴셋 닫기
    private void closeImageDownLoadMenu () {

        if (!downloadMenuOpen) return; //이미 닫혔다면 패스

        downloadMenuOpen = false;

        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        float targetY = mDisplayMetrics.heightPixels;
        float startY = fragment_photo_menu_container.getY();
        slide_open = ObjectAnimator.ofFloat(fragment_photo_menu_container, View.Y, startY, targetY);
        slide_open.setDuration(500);
        slide_open.start();

        fragment_photo_menu_container.setVisibility(View.VISIBLE);
    }




    @Override
    public void onBackPressed () {

        if (downloadMenuOpen) {
            closeImageDownLoadMenu ();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        //Runtime.getRuntime().gc();
    }


    /**
     *
     */
    public class ViewFragmentAdapter extends FragmentStatePagerAdapter {

        public ViewFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentPhotoSlideItem.newInstance(getCount(), position, (String) mItems.get(position));
        }

        @Override
        public int getItemPosition (Object item) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }


    }


    public void reloadMediaFiles (Context context, String path) {
        MediaScannerConnection.scanFile (context.getApplicationContext(), new String[]
                {path}, null, new MediaScannerConnection.OnScanCompletedListener() {

            public void onScanCompleted(String path, Uri uri) {
                // nothing yet;
            }

        });
    }


    //리스너 ------------------------------------------------------------------------------------------------
    private View.OnClickListener kevinClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View src) {
            //하나니까 분기하지 말자.
            ActivityPhotoSlider.this.onBackPressed();
        }
    };


    private View.OnClickListener kevinClickListener2 = new View.OnClickListener() {

        @Override
        public void onClick(View src) {


            int t = (int) src.getTag ();

            switch (t) {
                case 0 :


                    String imgURL = mAdapter.getItem (mViewPager.getCurrentItem()).getArguments().getString ("IMAGE");   //((FragmentPhotoSlideItem) mAdapter.getItem (mViewPager.getCurrentItem())).getImageURL();
                    BitmapUtilsK btUtil = new BitmapUtilsK();

                    Log.i ("FUCK", "이미지 주소 --> " + imgURL);


                    File dir = new File (SAVE_PATH);

                    if(!dir.exists()) dir.mkdirs();

                    String filePath = SAVE_PATH + "p_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    Bitmap bitmap = btUtil.getBitmapFromURL(imgURL, false);
                    BitmapUtilsK.saveBitmap(ActivityPhotoSlider.this, filePath, bitmap);

                    //MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, String.valueOf(System.currentTimeMillis()), "");
                    reloadMediaFiles(getApplicationContext(), filePath);


                    closeImageDownLoadMenu();

                    Toast.makeText (ActivityPhotoSlider.this, "이미지가 갤러리에 저장 되었습니다.", Toast.LENGTH_SHORT).show ();

                    break;
                case 1 :
                    closeImageDownLoadMenu();
                    break;
            }
        }
    };

}
