package edu.purdue.cs252.lab6.userapp;

import java.util.HashMap;

import edu.purdue.cs252.lab6.*;
import android.app.Application;
import android.content.SharedPreferences;

public class VoipApp extends Application {
	private User user;
	private DirectoryClient directoryClient;
	private VoicePlayerServer voicePlayerServer;
	private VoiceCaptureClient voiceCaptureClient;
	//private SharedPreferences prefs;
	//private SharedPreferences.Editor editor;
	public HashMap userMap;
/*
	public VoipApp() {
		super();
		prefs = getSharedPreferences("lab6",0);
		editor = prefs.edit();
	}*/
	public void setUser(User user) {
		this.user = user;
	}
	public User getUser() {
		return user;
	}
	public void setDirectoryClient(DirectoryClient dc) {
		this.directoryClient = dc;
	}
	public DirectoryClient getDirectoryClient() {
		return directoryClient;
	}
	public void setVoicePlayerServer(VoicePlayerServer vps) {
		this.voicePlayerServer = vps;
	}
	public VoicePlayerServer getVoicePlayerServer() {
		return voicePlayerServer;
	}
	public void setVoiceCaptureClient(VoiceCaptureClient vcc) {
		this.voiceCaptureClient = vcc;
	}
	public VoiceCaptureClient getVoiceCaptureClient() {
		return voiceCaptureClient;
	}
}
