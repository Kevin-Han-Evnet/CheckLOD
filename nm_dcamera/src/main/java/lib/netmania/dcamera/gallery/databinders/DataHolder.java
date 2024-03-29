package lib.netmania.dcamera.gallery.databinders;


/**
 * VO class for holding thumbnail data
 * @author Sam Rajan
 *
 */
public class DataHolder {
	
	//file path of the media
	private String mMediaPath;
	//thumbnail id
	private String mThumbnailData;

	private GalleryHolder myHolder;
	
	public DataHolder(){}
	
	/**
	 * function for getting media path
	 * @return media path
	 */
	public String getMediaPath(){
		return mMediaPath;
	}
	
	/**
	 * function for getting media id
	 * @return thumbnail id
	 */
	public String getThumbnailData(){
		return mThumbnailData;
	}
	
	/**
	 * function for setting media path
	 * @param mediaPath
	 */
	public void setMediapath(String mediaPath){
		mMediaPath = mediaPath; 
	}
	
	/**
	 * function for setting thumbnail id
	 * @param thumbnailData
	 */
	public void setThumbnailData(String thumbnailData){
		mThumbnailData = thumbnailData;
	}



	public void setHolder (GalleryHolder myHolder) {
		this.myHolder = myHolder;
	}

	public GalleryHolder getHolder () {
		return this.myHolder;
	}

}
