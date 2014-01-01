package com.gmail.nikhil1995.morph;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

import com.flurry.android.FlurryAgent;

/**
 * Settings activity, where the user can save preferences including:
 *  - whether adding an image is from the camera or from the gallery
 * 	- whether adding a new image changes the background or the foreground
 * 	- which image will be edited by default
 *  - whether the user should be asked to confirm merging the images
 *  - whether the user should be asked to confirm deleting the images
 */

public class Settings extends Activity implements OnItemSelectedListener, OnCheckedChangeListener {
	
	private Spinner newImgSrc, changeImgSrc, editImgSrc;
	private CheckBox askMerge, askDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FlurryAgent.onStartSession(this, "T7Y8YC9RZPJ3RGV3B9PQ");
		
		setContentView(R.layout.activity_settings);
		
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);		
		
		// whether the default images should come from the camera or from the gallery
		newImgSrc = (Spinner) findViewById(R.id.srcselect);
		newImgSrc.setOnItemSelectedListener(this);		
		newImgSrc.setSelection(settings.getInt(getString(R.string.imgsource), 0));
		
		// whether adding new images should change the background or the foreground
		changeImgSrc = (Spinner) findViewById(R.id.changeselect);
		changeImgSrc.setOnItemSelectedListener(this);
		changeImgSrc.setSelection(settings.getInt(getString(R.string.changesource), 0));
		
		// whether editing should edit the background or the foreground
		editImgSrc = (Spinner) findViewById(R.id.editselect);
		editImgSrc.setOnItemSelectedListener(this);
		editImgSrc.setSelection(settings.getInt(getString(R.string.editsource), 0));
		
		// whether the user should be asked to confirm a merge
		askMerge = (CheckBox) findViewById(R.id.mergebut);
		askMerge.setOnCheckedChangeListener(this);
		askMerge.setChecked(settings.getBoolean(getString(R.string.askmerge), true));
		
		// whether the user should be asked to confirm a delete
		askDelete = (CheckBox) findViewById(R.id.deletebut);
		askDelete.setOnCheckedChangeListener(this);
		askDelete.setChecked(settings.getBoolean(getString(R.string.askdelete), true));
	}
	
	/**
	 * Restores the default settings:
	 *  - the user will be asked to select a source
	 *  - the user will be asked which image to change
	 *  - the user will be asked which image to edit
	 *  - the user will be asked before merging images
	 *  - the user will be asked before deleting images
	 *  
	 * @param v the restore defaults button
	 */
	
	public void resetdefaults(View v) {
		newImgSrc.setSelection(0);
		changeImgSrc.setSelection(0);
		editImgSrc.setSelection(0);
		askMerge.setChecked(true);
		askDelete.setChecked(true);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> items, View v, int position, long id) {
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    
	    if (items.equals(newImgSrc)) {
	    	// camera, gallery, or ask
			editor.putInt(getString(R.string.imgsource), position);
			editor.commit();
	    } else if (items.equals(changeImgSrc)) {
	    	// change background, foreground, or ask
	    	editor.putInt(getString(R.string.changesource), position);
	    	editor.commit();
	    } else if (items.equals(editImgSrc)) {
	    	// edit background, foreground, or ask
	    	editor.putInt(getString(R.string.editsource), position);
	    	editor.commit();
	    }
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}

	@Override
	public void onCheckedChanged(CompoundButton c, boolean value) {
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    
		if (c.equals(askMerge)) {
			// whether to ask before merge
			editor.putBoolean(getString(R.string.askmerge), value);
			editor.commit();
		} else if (c.equals(askDelete)) {
			// whether to ask before delete
			editor.putBoolean(getString(R.string.askdelete), value);
			editor.commit();
		}
	}
	
	
	
}
