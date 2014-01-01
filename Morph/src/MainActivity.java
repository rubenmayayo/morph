package com.gmail.nikhil1995.morph;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.aviary.android.feather.FeatherActivity;
import com.flurry.android.FlurryAgent;

/**
 * The user selects two images from either the gallery, or by taking new photos.
 * A slider allows the user the blend the images together as he or she wishes.
 */

public class MainActivity extends SherlockActivity implements SeekBar.OnSeekBarChangeListener, OnLongClickListener {
		
	// constants for the first and second pictures, respectively
	public static final int PICTURE_1_CAMERA = 200;
	public static final int PICTURE_1_GALLERY = 201;
	public static final int PICTURE_2_CAMERA = 300;
	public static final int PICTURE_2_GALLERY = 301;
	
	// editing the background or foreground
	public static final int ACTION_REQUEST_FEATHER_1 = 100;
	public static final int ACTION_REQUEST_FEATHER_2 = 101;
	
	// name to save user progress
	public static final String PREFS_NAME = "preferences";
	
	// bitmaps containing information on the two images
	private Bitmap img1;
	private Bitmap img2;
	private Bitmap cimg;
	
	// thumbnails show the original images, iv shows the current blend, slider sets the blend percentage
	private ImageView thumbnail1, thumbnail2;
	private CustomImageView iv;
	private SeekBar slider;
	
	// preferences
	private int newImgSrc;
	private int changeImgSrc;
	private int editImgSrc;
	private boolean askMerge;
	private boolean askDelete;
	
	// toast messages
	private Toast toast;
	private Toast percentage;
	
	// used for maintaining correct dimensions (original dimensions of the images)
	public static int owidth1 = 0, oheight1 = 0, owidth2 = 0, oheight2 = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FlurryAgent.onStartSession(this, "T7Y8YC9RZPJ3RGV3B9PQ");
		
		setContentView(R.layout.activity_main);
		// allows us to refer to each view programmatically
		iv = (CustomImageView) findViewById(R.id.image);
		thumbnail1 = (ImageView) findViewById(R.id.thumbnail_1);
		thumbnail1.setOnLongClickListener(this);
		thumbnail2 = (ImageView) findViewById(R.id.thumbnail_2);
		thumbnail2.setOnLongClickListener(this);
		slider = (SeekBar) findViewById(R.id.slider);
		slider.setOnSeekBarChangeListener(this);
		
		// continue project where the user left off
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		percentage = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String i1 = settings.getString(getString(R.string.i1), null);
		String i2 = settings.getString(getString(R.string.i2), null);
		int prog = settings.getInt(getString(R.string.prog), 0);
		slider.setProgress(prog);
		Actions.fakeMove(slider);
		
		// maintain user preferences
		newImgSrc = settings.getInt(getString(R.string.imgsource), 0);
		changeImgSrc = settings.getInt(getString(R.string.changesource), 0);
		editImgSrc = settings.getInt(getString(R.string.editsource), 0);
		askMerge = settings.getBoolean(getString(R.string.askmerge), true);
		askDelete = settings.getBoolean(getString(R.string.askdelete), true);
		
		// if the user was in the middle of a project, bring that project back
		if (i1 != null) {
			img1 = Actions.rescaleImg(Actions.StringToBitMap(i1));
			thumbnail1.setImageBitmap(Actions.rescaleThumb(img1));
		}		
		if (i2 != null) {
			img2 = Actions.rescaleImg(Actions.StringToBitMap(i2));
			thumbnail2.setImageBitmap(Actions.rescaleThumb(img2));
		}		
        if (Actions.isGood(img1) && Actions.isGood(img2)) {
        	iv.setImageBitmap(img2);
        } else {
        	iv.setImageBitmap(img1);
        }	     
        if (Actions.isGood(img1) && Actions.isGood(img2)) {
        	slider.setProgress(slider.getProgress());
        	Actions.fakeMove(slider);
        }	      
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// when coming from another activity, such as settings, remember user preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		newImgSrc = settings.getInt(getString(R.string.imgsource), 0);
		changeImgSrc = settings.getInt(getString(R.string.changesource), 0);
		editImgSrc = settings.getInt(getString(R.string.editsource), 0);
		askMerge = settings.getBoolean(getString(R.string.askmerge), true);
		askDelete = settings.getBoolean(getString(R.string.askdelete), true);
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();	
		
		// get rid of toasts
		toast.cancel();
		percentage.cancel();
		
		// stop collecting data
		FlurryAgent.onEndSession(this);
		
		// save the user's project
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    String i1 = null, i2 = null;
	    if (Actions.isGood(img1)) {
	    	i1 = Actions.BitMapToString(img1);
	    }
	    if (Actions.isGood(img2)) {
	    	i2 = Actions.BitMapToString(img2);
	    }	
	    editor.putString(getString(R.string.i1), i1);
	    editor.commit();
	    editor.putString(getString(R.string.i2), i2);
	    editor.commit();
	    editor.putInt(getString(R.string.prog), slider.getProgress());
	    editor.commit();
	}
	
	@Override
	 public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		// the imageview will be a square
	  	iv.getLayoutParams().height = iv.getWidth();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	    	case R.id.add_icon:
	    		showAddDialog();
	    		return true;
	    	case R.id.edit_icon:
	    		showEditDialog();
	    		return true;
	    	case R.id.merge_icon:
	    		showMergeDialog();
	    		return true;
	        case R.id.delete_icon:
	        	showDeleteDialog();
	        	return true;
	        case R.id.save_icon:
	        	saveImage();
	        	return true;
	        case R.id.switch_icon:
	        	switchImages();
	        	return true;
	        case R.id.share_icon:
	        	shareImage();
	        	return true;
	        case R.id.about_icon:
	        	showAboutDialog();
	        	return true;
	        case R.id.rate_icon:
	        	rateApp();
	        	return true;
	        case R.id.feedback_icon:
	        	sendFeedback();
	        	return true;
	        case R.id.settings_icon:
	        	launchSettings();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Adding a new image
	 */
	
	public void showAddDialog() {
		if (!Actions.isGood(img1)) {
			thumbnail1.performClick();
		} else if (!Actions.isGood(img2)) {
			thumbnail2.performClick();
		} else if (changeImgSrc == 1) {
			thumbnail1.performClick();
		} else if (changeImgSrc == 2) {
			thumbnail2.performClick();
		} else {
			// asks the user which image to change if both images are not null
			final String items[] = {"Change Background", "Change Foreground"};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			TextView message = new TextView(this);
			message = Actions.createMessage(message, R.string.select_image);
			builder.setCustomTitle(message);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int choice) {
					if (choice == 0) {
						thumbnail1.performClick();
					}
					else if (choice == 1) {
						thumbnail2.performClick();
					}
				}
			});
			builder.create().show();
		}
	}
	
	/**
	 * Select which image to edit
	 */
	
	public void showEditDialog() {
		if (!Actions.isGood(img1) && Actions.isGood(img2)) {
			editImage(ACTION_REQUEST_FEATHER_2);
		} else if (!Actions.isGood(img2) && Actions.isGood(img1)) {
			editImage(ACTION_REQUEST_FEATHER_1);
		} else if (Actions.isGood(img1) && Actions.isGood(img2)) {
			if (editImgSrc == 1) {
				editImage(ACTION_REQUEST_FEATHER_1);
			} else if (editImgSrc == 2) {
				editImage(ACTION_REQUEST_FEATHER_2);
			} else {
				// asks the user which image to edit if both the images are not null
				final String items[] = {"Edit Background", "Edit Foreground"};
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				TextView message = new TextView(this);
				message = Actions.createMessage(message, R.string.select_image);
				builder.setCustomTitle(message);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int choice) {
						if (choice == 0) {
							editImage(ACTION_REQUEST_FEATHER_1);
						}
						else if (choice == 1) {
							editImage(ACTION_REQUEST_FEATHER_2);
						}
					}
				});
				builder.create().show();
			}	
		} else {
			toast.setText(R.string.request_select);
			toast.show();
		}
	}
	
	/**
	 * Adds the aviary editing feature
	 */
	
	public void editImage(int requestCode) {
		Uri image = null;
		if (requestCode == ACTION_REQUEST_FEATHER_1 && Actions.isGood(img1)) {
			image = Actions.getImageUri(this, img1);
		} else if (requestCode == ACTION_REQUEST_FEATHER_2 && Actions.isGood(img2)) {
			image = Actions.getImageUri(this, img2);
		}
		Intent newIntent = new Intent( this, FeatherActivity.class );
		// set the source image uri
		if (image != null) {
			newIntent.setData(image);
			startActivityForResult(newIntent, requestCode);
		} else {
			toast.setText(R.string.editfail);
			toast.show();
		}
	}
	
	/**
	 * Asks the user if they really want to merge the images
	 */
	public void showMergeDialog() {
		if (Actions.isGood(img1) && Actions.isGood(img2)) {
			if (askMerge) {
				// asks if user has it in their settings
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				TextView message = new TextView(this);
				message = Actions.createMessage(message, R.string.merge_confirm);
				builder.setCustomTitle(message)
				.setPositiveButton(R.string.merge_affirm, new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mergeImages();
					}
				})
				.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();				
					}
				});
				builder.create().show();
			} else {
				mergeImages();
			}
		} else {
			toast.setText(R.string.request_select);
			toast.show();
		}
	}
	
	/**
	 * Takes the current morph and sets it as the background,
	 * allowing the user to morph another image to this blend
	 */
	
	public void mergeImages() {
		if (Actions.isGood(cimg)) {
			Bitmap img = Bitmap.createBitmap(cimg);
			Actions.erase(img2);
			img1 = img;
			thumbnail1.setImageBitmap(Actions.rescaleThumb(img1));
			thumbnail2.setImageBitmap(null);
			toast.setText(R.string.mergesuccess);
			toast.show();
		} 
	}
	
	/**
	 * Creates a dialog that asks if the user really
	 * wants to delete all current images
	 */
	
	public void showDeleteDialog() {
		if (Actions.isGood(img1) || Actions.isGood(img2)) {
			if (askDelete) {
				// if the user wants to be asked in their settings, asks
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				TextView message = new TextView(this);
				message = Actions.createMessage(message, R.string.delete_confirm);
				builder.setCustomTitle(message)
				.setPositiveButton(R.string.delete_affirm, new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteImages();
					}
				})
				.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();				
					}
				});
				builder.create().show();
			} else {
				deleteImages();
			}
		} else {
			toast.setText(R.string.request_select);
			toast.show();
		}
	}
	
	/**
	 * Deletes all current images
	 */
	
	public void deleteImages() {
		// recycle to help garbage collection
		if (Actions.isGood(img1)) {
			Actions.erase(img1);
		}
		if (Actions.isGood(img2)) {
			Actions.erase(img2);
		}
		owidth1 = 0; oheight1 = 0; owidth2 = 0; oheight2 = 0;
		thumbnail1.setImageBitmap(null);
		thumbnail2.setImageBitmap(null);
		iv.setImageBitmap(null);
	}
	
	/**
	 * Saves the current morph to the gallery
	 */
	
	public void saveImage() {
		toast.setText(R.string.savesuccess);
		if (Actions.isGood(cimg)) {
			MediaStore.Images.Media.insertImage(getContentResolver(), cimg, "morph", "image");
			toast.show();
		} else if (Actions.isGood(img1)) {
			MediaStore.Images.Media.insertImage(getContentResolver(), img1, "morph", "image");
			toast.show();
		} else if (Actions.isGood(img2)) {
			MediaStore.Images.Media.insertImage(getContentResolver(), img2, "morph", "image");
			toast.show();
		} else {
			toast.setText(R.string.request_select);
			toast.show();
		}
	}
	
	/**
	 * Allows the user to share the morph via social media
	 */
	
	public void shareImage() {
		Bitmap img = null;
		if (Actions.isGood(cimg)) {
			img = cimg;
		} else if (Actions.isGood(img1)) {
			img = img1;
		} else if (Actions.isGood(img2)) {
			img = img2;
		}
		if (Actions.isGood(img)) {
			String path = Images.Media.insertImage(this.getContentResolver(), img, "Title", null);
			Intent sharingIntent = Actions.shareImage(img, path);
			startActivity(Intent.createChooser(sharingIntent, "Share image using:"));
		} else {
			toast.setText(R.string.request_select);
			toast.show();
		}
	}
	
	/**
	 * Sets the new image and adjusts the thumbnails
	 * based on whether the image was selected to be used
	 * as the foreground or as the background
	 * 
	 * @param view imageview to set the image into
	 */
	
	public void setImage(View view) {
		final View v = view;
		if (newImgSrc == 0) {
			// asks the user where they want to select an image from
			AlertDialog.Builder getImageFrom = new AlertDialog.Builder(this);
			TextView message = new TextView(this);
			message = Actions.createMessage(message, R.string.image_source);
	        getImageFrom.setCustomTitle(message);
	        final CharSequence[] opsChars = {getResources().getString(R.string.takepic), getResources().getString(R.string.opengallery)};
	        getImageFrom.setItems(opsChars, new android.content.DialogInterface.OnClickListener(){
	
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                if(which == 0){
	                    takeNewPic(v);
	                } else if(which == 1) {
	                	selectFromGallery(v);
	                }
	                dialog.dismiss();
	            }
	        });
	        getImageFrom.create().show();
		} else if (newImgSrc == 1) {
			// settings say user wants to choose from gallery
			selectFromGallery(v);
		} else if (newImgSrc == 2) {
			// settings say user wants to take a new picture
			takeNewPic(v);
		}
	}
	
	/**
	 * Takes a new picture to add to the morph
	 * 
	 * @param v the thumbnail that was clicked
	 */
	
	public void takeNewPic(View v) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		if (v.getId() == R.id.thumbnail_1)
			startActivityForResult(Intent.createChooser(intent, "First Picture"), PICTURE_1_CAMERA);
		else if (v.getId() == R.id.thumbnail_2)
			startActivityForResult(Intent.createChooser(intent, "Second Picture"), PICTURE_2_CAMERA);
	}
	
	/**
	 * Selects a picture from the user's gallery to add to the morph
	 * 
	 * @param v the thumbnail that was clicked
	 */
	
	public void selectFromGallery(View v) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		if (v.getId() == R.id.thumbnail_1)
			startActivityForResult(Intent.createChooser(intent, "First Picture"), PICTURE_1_GALLERY);
		else if (v.getId() == R.id.thumbnail_2)
			startActivityForResult(Intent.createChooser(intent, "Second Picture"), PICTURE_2_GALLERY);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        
        if (resultCode == RESULT_CANCELED) {
        	return;
        }
		
        if (resultCode == RESULT_OK && null != imageReturnedIntent && (requestCode == PICTURE_1_GALLERY || requestCode == PICTURE_2_GALLERY)) {
            Uri selectedImage = imageReturnedIntent.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            
            Bitmap image = Actions.rescaleImg(picturePath);
            Bitmap thumb = Actions.rescaleThumb(picturePath);
            
            // handles images retrieved from the gallery
            if (requestCode == PICTURE_1_GALLERY) {
            	if (image != null) {
            		owidth1 = image.getWidth();
            		oheight1 = image.getHeight();
	            	img1 = image;
	            	thumbnail1.setImageBitmap(thumb);
            	}	
            } else if (requestCode == PICTURE_2_GALLERY) {
            	if (image != null) {
            		owidth2 = image.getWidth();
            		oheight2 = image.getHeight();
	            	img2 = image;
	            	thumbnail2.setImageBitmap(thumb);
            	}
            }
            // handles images retreived from taking a new photo
        } else if (resultCode == RESULT_OK && null != imageReturnedIntent && (requestCode == PICTURE_1_CAMERA || requestCode == PICTURE_2_CAMERA)) {
            Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
            
            if (requestCode == PICTURE_1_CAMERA) {
            	owidth1 = photo.getWidth();
            	oheight1 = photo.getHeight();
            	img1 = Actions.rescaleImg(photo);
            	thumbnail1.setImageBitmap(Actions.rescaleThumb(img1));
            } else if (requestCode == PICTURE_2_CAMERA) {
            	owidth2 = photo.getWidth();
            	oheight2 = photo.getHeight();
            	img2 = Actions.rescaleImg(photo);
            	thumbnail2.setImageBitmap(Actions.rescaleThumb(img2));
            }	
            Actions.erase(photo);
            // handles images taken from aviary's editing
        } else if (resultCode == RESULT_OK && (requestCode == ACTION_REQUEST_FEATHER_1 || requestCode == ACTION_REQUEST_FEATHER_2)) {
            // modified preview image
            Uri mImageUri = imageReturnedIntent.getData();
            Bitmap image = null;
            try {
            	image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
            } catch (IOException e) {
            	toast.setText(R.string.loadfail);
            	toast.show();
            }
            if (Actions.isGood(image)) {
            	if (requestCode == ACTION_REQUEST_FEATHER_1) {
            		owidth1 = image.getWidth();
            		oheight1 = image.getHeight();
	            	img1 = Actions.rescaleImg(image);
	            	thumbnail1.setImageBitmap(Actions.rescaleThumb(img1));
            	} else if (requestCode == ACTION_REQUEST_FEATHER_2) {
            		owidth2 = image.getWidth();
            		oheight2 = image.getHeight();
            		img2 = Actions.rescaleImg(image);
            		thumbnail2.setImageBitmap(Actions.rescaleThumb(img2));
            	}
            	iv.setImageBitmap(img1);
            }
            Actions.erase(image);
        }
        
        // sets the images and morphs if both are not null
        if (!Actions.isGood(img1) && Actions.isGood(img2)) {
        	iv.setImageBitmap(img2);
        } else if (Actions.isGood(img1) && !Actions.isGood(img2)) {
        	iv.setImageBitmap(img1);
        } else if (Actions.isGood(img1) && Actions.isGood(img2)) {
        	Actions.fakeMove(slider);
        }	
	} 
	
	/**
	 * Switches the background and foreground
	 */
	
	public void switchImages() {
		Actions.switchDims();
		if (Actions.isGood(img1) && !Actions.isGood(img2)) {
			img2 = img1;
			thumbnail2.setImageBitmap(Actions.rescaleThumb(img2));
			Actions.erase(img1);
			thumbnail1.setImageBitmap(null);
		} else if (!Actions.isGood(img1) && Actions.isGood(img2)) {
			img1 = img2;
			thumbnail1.setImageBitmap(Actions.rescaleThumb(img1));
			Actions.erase(img2);
			thumbnail2.setImageBitmap(null);
		} else if (Actions.isGood(img1) && Actions.isGood(img2)) {
			Bitmap temp = img1;
			img1 = img2;
			thumbnail1.setImageBitmap(Actions.rescaleThumb(img1));
			img2 = temp;
			thumbnail2.setImageBitmap(Actions.rescaleThumb(img2));
			slider.setProgress(slider.getProgress());
			Actions.fakeMove(slider);
		} else {
			toast.setText(R.string.request_select);
			toast.show();
		}
	}
	
	/**
	 * Help for the app
	 */
	
	public void showAboutDialog() {
		TextView message = new TextView(this);
		message = Actions.createMessage(message, R.string.about);
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setIcon(R.drawable.icon_about);
		ad.setCustomTitle(message);
		ad.setCancelable(true);
	 	ad.setView(LayoutInflater.from(this).inflate(R.layout.about, null));   
	 	ad.create().show();
	}
	
	/**
	 * Sends the user to the play store to rate the app
	 */
	
	public void rateApp() {
		Uri uri = Uri.parse("market://details?id=" + getPackageName());
	    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
	    try {
	        startActivity(goToMarket);
	    } catch (ActivityNotFoundException e) {
	    	toast.setText(R.string.marketfail);
			toast.show();
	    }
	}
	
	/**
	 * Send email feedback to morphapp@gmail.com
	 */
	
	public void sendFeedback() {
		Intent Email = Actions.getEmail();
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
	}
	
	/**
	 * Opens settings menu
	 */
	
	public void launchSettings() {
	    Intent intent = new Intent(this, Settings.class);
	    startActivity(intent);
	}

	@Override
	public void onProgressChanged(SeekBar slider, int progress, boolean fromUser) {
		int percent = slider.getProgress();
		int len = slider.getRight() - slider.getLeft();
		int dist = (int) (slider.getLeft() + percent / 100.0 * len);
		percentage.setText(Integer.toString(percent) + "%");
		percentage.setGravity(Gravity.TOP|Gravity.LEFT, dist, 0);
		percentage.show();
		if (iv != null && Actions.isGood(img1) && Actions.isGood(img2)) {
			if (Actions.isGood(cimg) && !(cimg == img1 || cimg == img2)) {
				Actions.erase(cimg);
			}	
			cimg = Actions.getMorph(img1, img2, percent / 100.0);
			iv.setImageBitmap(cimg);
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar slider) {}

	@Override
	public void onStopTrackingTouch(SeekBar slider) {}

	@Override
	public boolean onLongClick(View v) {
		if (v.equals(thumbnail1) && Actions.isGood(img1)) {
			editImage(ACTION_REQUEST_FEATHER_1);
		} else if (v.equals(thumbnail2) && Actions.isGood(img2)) {
			editImage(ACTION_REQUEST_FEATHER_2);
		}
		return false;
	}
}