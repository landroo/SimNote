package org.landroo.simnote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsScreen extends PreferenceActivity 
{
	private static final String TAG = "SettingsScreen";
	private String fileName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference button = (Preference)findPreference("save_as");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() 
		{
			@Override
			public boolean onPreferenceClick(Preference arg0) 
			{ 
				String oldName = fileName;
				updateVars();
				copyFile(oldName, fileName);
			    return true;
			}
		});
		
		updateVars();
	}
	
	private void updateVars()
	{
		SharedPreferences settings = getSharedPreferences("org.landroo.simnote_preferences", MODE_PRIVATE);
		String str = settings.getString("fileName", "");
		this.fileName = str;

		return;
	}
	
	private void copyFile(String fromName, String toName)
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			File inFile = new File(fromName);
			in = new FileInputStream(inFile);
			File outFile = new File(toName);
			if (!outFile.exists())
			{
				out = new FileOutputStream(outFile);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
		}
		catch (Exception ex)
		{
			Log.i(TAG, "Error in copyFile:" + ex);
		}
	}
	
	// copy file
	private void copyFile(InputStream fin, OutputStream fout) throws IOException
	{
		byte[] b = new byte[65536];
		int noOfBytes = 0;

		// read bytes from source file and write to destination file
		while ((noOfBytes = fin.read(b)) != -1)
			fout.write(b, 0, noOfBytes);
	}
	
}