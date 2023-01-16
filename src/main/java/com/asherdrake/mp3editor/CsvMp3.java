package com.asherdrake.mp3editor;
import java.io.*;
import java.util.ArrayList;
import com.mpatric.mp3agic.*;

public class CsvMp3 {
	private ArrayList<Mp3Info> mp3Info;
	private String csvOutPath;
	private String csvInPath;
	private String mp3OutPath;
	private Writer writer;
	
	public CsvMp3(ArrayList<Mp3Info> mp3Info, String outputPath, Writer writer) {
		this.mp3Info = mp3Info;
		csvOutPath = outputPath;
		this.writer = writer;
	}
	
	public CsvMp3(String csvInPath, String mp3OutPath) {
		this.csvInPath = csvInPath;
		this.mp3OutPath = mp3OutPath;
	}
	
	public void writeCsv() {
		try {
			writer.append("Track, Album, Artist, File Name, File Path\n");
			
			for (int i = 0; i < mp3Info.size(); i++) {
				ID3v1 tag = mp3Info.get(i).getTag();
				
				writer.append(tag.getTrack() + "," + 
								  tag.getAlbum() + "," + 
								  tag.getArtist() + "," + 
								  tag.getTitle() + "," + 
								  tag.getTitle() + "," + 
								  mp3Info.get(i).getPath() + "\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				writer.flush();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void readCsv() {
		BufferedReader reader = null;
		
		try {
			String line = "";
			reader = new BufferedReader(new FileReader(csvInPath));
			reader.readLine();
			
			while ((line = reader.readLine()) != null) {
				String[] id3Tags = line.split(",");
				
				if (id3Tags.length > 0) {
					Mp3File mp3 = new Mp3File(id3Tags[4]);
					
					ID3v1 tag = null;
					if (mp3.hasId3v1Tag()) {
						tag = mp3.getId3v1Tag();			
					} else if (mp3.hasId3v2Tag()) {
						tag = mp3.getId3v2Tag();
					}

					tag.setTrack(id3Tags[0]);
					tag.setAlbum(id3Tags[1]);
					tag.setArtist(id3Tags[2]);

					mp3.save(mp3OutPath + "/" + id3Tags[3] + ".mp3");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
 