package io.puzzlebox.jigsaw.data;

import android.os.Environment;
import android.util.Log;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;

import java.io.File;

public class ProfileManagerUser {
	public static ProfileManagerUser instance;
	public String userName;
	public static ProfileManagerUser shareInstance()
	{
		if(instance==null)
		{
			instance=new ProfileManagerUser();
		}
		return instance;
	}
	public void saveUserProfile(int userId)
	{
		 	String _profileLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EmoProFile/";
		 	File profileFolder = new File(_profileLocation);
		     if (!profileFolder.exists()) {
		       profileFolder.mkdirs();
		     }
		     Log.e("profile", "save " + IEdk.IEE_SaveUserProfile(userId, _profileLocation + userName + ".emu") + " " + userName);

	}
	public boolean loadUserProfile(int userId)
	{
		
		String _profileLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EmoProFile/";
	 	File profileFolder = new File(_profileLocation);
	 	if(!profileFolder.exists())
	 		return false;
	 	if ( IEdk.IEE_LoadUserProfile(userId, _profileLocation + userName + ".emu") == IEdkErrorCode.EDK_OK.ToInt()){
			   return true;
		   }
	 	saveUserProfile(userId);
	 	 if ( IEdk.IEE_LoadUserProfile(userId, _profileLocation + userName + ".emu") == IEdkErrorCode.EDK_OK.ToInt()){
			   return true;
		   }
	 	return false;
	}
}
