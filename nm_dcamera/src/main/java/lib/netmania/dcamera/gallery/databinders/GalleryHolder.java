package lib.netmania.dcamera.gallery.databinders;

import lib.netmania.dcamera.gallery.customcomponents.GalleryFrameLayout;
import lib.netmania.dcamera.gallery.customcomponents.RecyclingImageView;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

class GalleryHolder
{
	GalleryFrameLayout baseLayout = null;
	FrameLayout listBaseLayout = null;
	RecyclingImageView folderThumbnail = null;
	TextView folderName = null;
	ImageView sys_btn_zoom = null;
	int myPosition = -1;
}
