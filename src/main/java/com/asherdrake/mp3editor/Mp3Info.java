package com.asherdrake.mp3editor;

import com.mpatric.mp3agic.ID3v1;

public class Mp3Info {
	private ID3v1 tag;
	private String mp3Path;
	
	public Mp3Info(ID3v1 tag, String mp3Path) {
		this.tag = tag;
		this.mp3Path = mp3Path;
	}
	
	public ID3v1 getTag() {
		return tag;
	}
	
	public String getPath() {
		return mp3Path;
	}
}
