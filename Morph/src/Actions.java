package com.gmail.nikhil1995.morph;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.Base64;
import android.view.Gravity;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Static methods to save room in other classes
 */

public class Actions {
	
	/**
	 * Class cannot be instantiated
	 */
	
	private Actions() {}
	
	/**
	 * Whether the bitmap is safe to be used again
	 * 
	 * @param img the bitmap to analyze
	 * @return true if the bitmap can be used again
	 */
	
	public static boolean isGood(Bitmap img) {
		if (img != null && !img.isRecycled()) {
			return true;
		} return false;
	}
	
	/**
	 * Recycles the bitmap and makes it null,
	 * so it cannot be used again
	 * 
	 * @param img the bitmap to erase
	 */
	
	public static void erase(Bitmap img) {
		img.recycle();
		img = null;
	}
	
	/**
	 * Puts the slider back in its original position,
	 * but makes it seem as if it has moved, allowing
	 * onProgressChanged() to be called
	 * 
	 * @param slider the seekbar
	 */
	
	public static void fakeMove(SeekBar slider) {
		int current = slider.getProgress();
		if (current < slider.getMax()) {
			slider.setProgress(current + 1);
		} else {
			slider.setProgress(current - 1);
		}
		slider.setProgress(current);
	}
	
	public static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
	
	public static Bitmap rescaleImg(String path) {
		return decodeSampledBitmapFromResource(path, getDims()[0], getDims()[1]);
	}
	
	public static Bitmap rescaleThumb(String path) {
		return decodeSampledBitmapFromResource(path, getDims()[0], getDims()[1]);
	}
	
	/**
	 * Creates a bitmap that is the weighted average of two bitmaps
	 * 
	 * @param i1 the background image
	 * @param i2 the foregeround image
	 * @param percentage the percent of the foreground image to overlay
	 * @return the weighted average of the two images, in a bitmap
	 */
	public static Bitmap getMorph(Bitmap i1, Bitmap i2, double percentage) {
		// rescale just in case
		i1 = rescaleImg(i1);
		i2 = rescaleImg(i2);
		
		// preventes index out of bound errors
		int width = getDims()[0];
		int height = getDims()[1];
		
		int[] pixels1 = new int[width * height];
		int[] pixels2 = new int[width * height];
		
		// puts the information from each bitmap into the pixel arrays
		i1.getPixels(pixels1, 0, width, 0, 0, width, height);
		i2.getPixels(pixels2, 0, width, 0, 0, width, height);
		int[] avgpix = new int[pixels1.length];
		
		// each pixel in the new bitmap is the average of the pixels
		// from the two original bitmaps
		for (int i = 0; i < pixels1.length; i++) {
			int pixel1 = pixels1[i];
			int pixel2 = pixels2[i];
			int avgRed = (int) (Color.red(pixel1) * (1 - percentage) + Color.red(pixel2) * percentage);
			int avgGreen = (int) (Color.green(pixel1) * (1 - percentage) + Color.green(pixel2) * percentage);			
			int avgBlue = (int) (Color.blue(pixel1) * (1 - percentage) + Color.blue(pixel2) * percentage);
			avgpix[i] = Color.rgb(avgRed, avgGreen, avgBlue);
		}
		
		Bitmap newimg = Actions.rescaleImg(Bitmap.createBitmap(avgpix, i1.getWidth(),
				i1.getHeight(), Config.ARGB_8888));	
		pixels1 = null;
		pixels2 = null;
		return newimg;
	}
	
	/**
	 * Given the bitmap, retrieves the Uri so that the bitmap
	 * can uniquely be identifed
	 * 
	 * @param inContext the activity that calls the method
	 * @param inImage the bitmap
	 * @return the Uri of the bitmap
	 */
	
	public static Uri getImageUri(Context inContext, Bitmap inImage) {
		  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		  inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		  String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		  if (path != null) {
			  return Uri.parse(path);
		  } else {
			  return null;
		  }
	}
	
	/**
	 * Initializes a text view with the desired formatting, 
	 * including text size, padding, color...
	 * 
	 * @param the textview to be formatted
	 * @param resId the string to appear on the textview
	 * @return the formatted textview
	 */
	
	public static TextView createMessage(TextView message, int resId) {
		message.setText(resId);
		message.setGravity(Gravity.CENTER_HORIZONTAL);
		message.setPadding(10, 10, 10, 10);
		message.setTextSize(24);
		message.setTextColor(0xFF33b5e5);
		return message;
	}
	
	/**
	 * Shares the image via social media
	 * 
	 * @param img the bitmap to be shared
	 * @param path the path of the image
	 * @return the intent to share the image
	 */
	
	public static Intent shareImage(Bitmap img, String path) {
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		img.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		Uri screenshotUri = Uri.parse(path);
		sharingIntent.setType("image/png");
		sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
		return sharingIntent;
	}
	
	/**
	 * When switching the background and foreground, switch the dimensions also
	 */
	
	public static void switchDims() {
		int tempWidth = MainActivity.owidth2;
		int tempHeight = MainActivity.oheight2;
		MainActivity.owidth2 = MainActivity.owidth1;
		MainActivity.oheight2 = MainActivity.oheight1;
		MainActivity.owidth1 = tempWidth;
		MainActivity.oheight1 = tempHeight;
	}
	
	/**
	 * @return true if the background is set
	 */
	
	public static boolean hasBackground() {
		if (MainActivity.owidth1 != 0 && MainActivity.oheight1 != 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return true if the foreground is set
	 */
	
	public static boolean hasForeground() {
		if (MainActivity.owidth2 != 0 && MainActivity.oheight2 != 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the ideal dimensions of a morph based on the original dimensions of the background
	 * 
	 * @return an array containing the ideal width and ideal height, in that order
	 */
	
	public static int[] getDims() {
		double ratio = 1.0;
		int idealWidth = 512;
		int idealHeight = 512;
		if (hasBackground()) {
			ratio = 1.0 * MainActivity.owidth1 / MainActivity.oheight1;
		} else if (hasForeground()) {
			ratio = 1.0 * MainActivity.owidth2 / MainActivity.oheight2;
		}
		if (ratio > 1) {
			idealHeight = (int) (idealWidth / ratio);
		} else if (ratio < 1) {
			idealWidth = (int) (idealHeight * ratio);
		}
		int[] dims = {idealWidth, idealHeight};
		return dims;
	}
	
	/**
	 * Rescales the image to ideal with and height
	 * 
	 * @param img the original image
	 * @return the rescaled image
	 */
	
	public static Bitmap rescaleImg(Bitmap img) {
		Bitmap imgz = Bitmap.createScaledBitmap(img, getDims()[0], getDims()[1], true);
		return imgz;
	}
	
	/**
	 * Rescales the image to fit on a thumbnail
	 * 
	 * @param img the original image
	 * @return the rescaled image
	 */
	
	public static Bitmap rescaleThumb(Bitmap img) {
		Bitmap imgz = Bitmap.createScaledBitmap(img, 32, 32, true);
		return imgz;
	}
	
	/**
	 * Send email feedback to morphapp@gmail.com
	 */
	
	public static Intent getEmail() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("message/rfc822");
        Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "morphapp@gmail.com" });
        Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Morph");
        Email.putExtra(Intent.EXTRA_TEXT, "\n\n\n\nAndroid Version:\nDevice:" + "");
        return Email;
	}
	
	/**
	 * Converts a bitmap to an equivalent string
	 * 
	 * @param bitmap the bitmap to be converted
	 * @return the encoded string
	 */
	
	public static String BitMapToString(Bitmap bitmap) {
		if (bitmap != null && bitmap.isRecycled() == false) {
			ByteArrayOutputStream baos=new  ByteArrayOutputStream();
	        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
	        byte [] b=baos.toByteArray();
	        String temp=Base64.encodeToString(b, Base64.DEFAULT);
	        return temp;
		}  
		return null;
     }
	
    /**
     * @param encodedString the string to be converted
     * @return bitmap (from given string)
     */
	
    public static Bitmap StringToBitMap(String encodedString) {
    	try {
    		byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
    		Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
    		return bitmap;
    		} catch (Exception e) {
    			e.getMessage();
    			return null;
    		}
    }
}
