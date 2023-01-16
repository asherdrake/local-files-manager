package com.asherdrake.mp3editor;

import com.mpatric.mp3agic.*;
import java.util.*;
import java.io.*;

public class Mp3editor {
	public static void main(String[] args) throws InvalidDataException, UnsupportedTagException, IOException, NotSupportedException{
		String mp3InputPath = null;
		String csvOutputPath = null;
		String csvInputPath = null;
		String mp3OutputPath = null;
		
		String help = "Bulk read or update metadata of given MP3 files.\n"
					+ "\n"
					+ "Usage:\n"
					+ "   mp3editor command [-m mp3dir] [-c csv]\n"
					+ "\n"
					+ "Where:\n"
					+ "   'command' is any of the following:\n"
					+ "		makecsv - Read MP3 files and generate a CSV file.\n"
					+ "		editmp3 - Update MP3 files with metadata from given CSV file.\n"
					+ "\n"
					+ "   makecsv options:\n"
					+ "		mp3dir  - Location of mp3 files to read, otherwise it reads from current folder.\n"
					+ "		csv     - CSV file to be created to save metadata to, otherwise it outputs to current folder as mp3tags.csv.\n"
					+ "\n"    
					+ "   editmp3 options:\n"
					+ "		mp3dir  - Directory to save updated mp3 files, otherwise mp3editor creates a subfolder named 'output' relative to current folder.\n"
					+ "		csv     - CSV file input, otherwise it reads 'mp3tags.csv' from current folder.\n"
					+ "\n"      
					+ "Examples:\n"
					+ "   mp3editor makecsv -m /Users/user/mp3folder -c /Users/user/abc.csv\n"
					+ "   mp3editor editmp3 -m /Users/user/editedmp3s -c /Users/user/mp3metadata.csv\n";
		
		try {
			Mp3editor editor = new Mp3editor();
			String command = args[0].toLowerCase();
			if (args.length != 0 && (command.equals("makecsv") || command.equals("editmp3s"))) {
				if (command.equals("makecsv")) { //parses arguments then calls the makeCsv method
					String[] paths = editor.parseArgs(args);
					Writer fileWriter = editor.getWriter(paths[1]);
					editor.makeCsv(paths[0], paths[1], fileWriter);
				} else if (command.equals("editmp3s")) { //parses arguments then calls the editMp3s method
					String[] paths = editor.parseArgs(args);
					editor.editMp3s(paths[0], paths[1]);
				}
			} else { //throws an IllegalArgumentException if no command or the incorrect command was passed
				throw new IllegalArgumentException("Please specify a command.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(help);
		}
	}
	
	public String[] parseArgs(String[] args) throws IllegalArgumentException{
		//default paths
		String mp3Path = System.getProperty("user.dir");
		String csvPath = System.getProperty("user.dir") + "/mp3tags.csv";
		String[] paths = new String[2];
		
		//throws an IllegalArgumentException if the incorrect arguments or # of arguments were passed
		List <String> argsList = Arrays.asList(args);
		boolean containsM = argsList.contains("-m");
		boolean containsC = argsList.contains("-c");
		if (containsM && containsC) {
			if (args.length != 5) {
				throw new IllegalArgumentException("Two flags were entered, but the # of arguments is invalid.");
			}
		} else if (containsM || containsC) {
			if (args.length != 3) {
				throw new IllegalArgumentException("One flag was entered, but the # of arguments is invalid.");
			}
		} else {
			if (args.length != 1) {
				throw new IllegalArgumentException("No flags were specified, but the # of arguments is invalid.");
			}
		}
		
		for (int i = 1; i < args.length; i++) {
			String arg = args[i].toLowerCase();
			if (arg.equals("-m")) {
				mp3Path = args[i + 1];
			} else if (arg.equals("-c")) {
				csvPath = args[i + 1];
			}
		}
		
		paths[0] = mp3Path;
		paths[1] = csvPath;
		return paths;
	}
	
	public void makeCsv(String mp3FolderPath, String csvOutputPath, Writer writer) throws IllegalArgumentException, InvalidDataException, UnsupportedTagException, IOException { 
		//validates paths
		File mp3Folder = new File(mp3FolderPath);
		File csvOutput = new File(csvOutputPath);
		
		if (!mp3Folder.exists()) {
			throw new IllegalArgumentException("Mp3 folder path does not exist.");
		}
		
		if (!mp3Folder.isDirectory()) {
			throw new IllegalArgumentException("Mp3 folder path does not lead to a folder");
		}
		
		if (csvOutput.isDirectory()) {
			throw new IllegalArgumentException("Csv file path leads to a directory, not a file.");
		}
		
		//gets Id3 tags and file paths from the mp3 files
		ArrayList<Mp3Info> mp3Info = getMp3Info(mp3Folder);
		
		//calls the writeCsv method
		CsvMp3 csv = new CsvMp3(mp3Info, csvOutputPath, writer);
		csv.writeCsv();
		System.out.println("Csv file complete.");
	}
	
	public Writer getWriter(String csvOut) throws IllegalArgumentException, IOException {
		File csvOutput = new File(csvOut);
		
		if (csvOutput.exists()) {
			throw new IllegalArgumentException("Csv File already exists.");
		}
		
		FileWriter fileWriter = new FileWriter(csvOut);
		
		return fileWriter;
	}
	
	public ArrayList<Mp3Info> getMp3Info(File mp3Folder) throws InvalidDataException, UnsupportedTagException, IOException {
		File[] mp3Files = mp3Folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".mp3");
			}
		});	
		
		ArrayList<Mp3Info> mp3Info = new ArrayList<Mp3Info>();
		
		try { 
			for (int i  = 0; i < mp3Files.length; i++) {
				String filePath = mp3Files[i].getAbsolutePath();
				Mp3File mp3file = new Mp3File(filePath);
				ID3v1 tag = null;
				if (mp3file.hasId3v1Tag()) {
					tag = mp3file.getId3v1Tag();
				} else if (mp3file.hasId3v2Tag()) {
					tag = mp3file.getId3v2Tag();
				} else {
					tag = new ID3v1Tag();
				}
				mp3Info.add(new Mp3Info(tag, filePath));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Invalid MP3 File");
		}
		
		return mp3Info;
	}
	
	public void editMp3s(String mp3OutputPath, String csvInputPath) {
		//validates paths
		File csvInputFile = new File(csvInputPath);
		File mp3OutputFolder = new File(mp3OutputPath);

		if (!mp3OutputFolder.exists()) {
			throw new IllegalArgumentException("Mp3 output folder path does not exist.");
		}
		
		if (!mp3OutputFolder.isDirectory()) {
			throw new IllegalArgumentException("Mp3 output folder path does not lead to a folder");
		}
		
		if (!csvInputFile.exists()) {
			throw new IllegalArgumentException("Csv input file path does not exist.");
		}
		
		if (csvInputFile.isDirectory()) {
			throw new IllegalArgumentException("Csv file path does not lead to a file.");
		}
		
		//calls the readCsv method
		CsvMp3 csv = new CsvMp3(csvInputPath, mp3OutputPath);
		csv.readCsv();
		System.out.println("Mp3 files complete.");
	}
}
