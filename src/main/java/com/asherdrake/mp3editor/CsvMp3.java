package com.asherdrake.mp3editor;
import java.io.*;
import java.util.ArrayList;
import com.mpatric.mp3agic.*;

public class CsvMp3 {
	private ArrayList<ID3v1> tags;
	private String csvOutPath;
	private ArrayList<String> mp3Paths;
	private String csvInPath;
	private String mp3OutPath;
	
	public CsvMp3(ArrayList<ID3v1> tags, String outputPath, ArrayList<String> paths) {
		this.tags = tags;
		csvOutPath = outputPath;
		mp3Paths = paths;
	}
	
	public CsvMp3(String csvInPath, String mp3OutPath) {
		this.csvInPath = csvInPath;
		this.mp3OutPath = mp3OutPath;
	}
	
	public void writeCsv() {
		FileWriter fileWriter = null;
		try {
			File csvFile = new File(csvOutPath);
			fileWriter = new FileWriter(csvFile);
			
			fileWriter.append("Track, Album, Artist, File Name, File Path\n");
			
			for (int i = 0; i < tags.size(); i++) {
				ID3v1 tag = tags.get(i);

				fileWriter.append(tag.getTrack());
				fileWriter.append(",");
				
				fileWriter.append(tag.getAlbum());
				fileWriter.append(",");
				
				fileWriter.append(tag.getArtist());
				fileWriter.append(",");
				
				fileWriter.append(tag.getTitle());
				fileWriter.append(",");

				fileWriter.append(mp3Paths.get(i) + "\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
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
 