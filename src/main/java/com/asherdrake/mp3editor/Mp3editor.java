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
			String command = args[0].toLowerCase();
			if (args.length != 0 && (command.equals("makecsv") || command.equals("editmp3s"))) {
				if (command.equals("makecsv")) { //parses arguments then calls the makeCsv method
					String[] paths = parseArgs(args);
					makeCsv(paths[0], paths[1]);
				} else if (command.equals("editmp3s")) { //parses arguments then calls the editMp3s method
					String[] paths = parseArgs(args);
					editMp3s(paths[0], paths[1]);
				}
			} else { //throws an IllegalArgumentException if no command or the incorrect command was passed
				throw new IllegalArgumentException("Please specify a command.");
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.out.println(help);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.println(help);
		}
	}
	
	public static String[] parseArgs(String[] args) {
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
				throw new IllegalArgumentException("Please specify the correct arguments or # of arguments.");
			}
		} else if (containsM || containsC) {
			if (args.length != 3) {
				throw new IllegalArgumentException("Please specify the correct arguments or # of arguments.");
			}
		} else if (!(containsM || containsC)) {
			if (args.length != 1) {
				throw new IllegalArgumentException("Please specify the correct arguments or # of arguments.");
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
	
	public static void makeCsv(String mp3FolderPath, String csvOutputPath) throws IllegalArgumentException, InvalidDataException, UnsupportedTagException, IOException { 
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
		
		//if the specified csv file already exists, the path is updated to include the number of copies at the end. Ex: mp3data (1).csv
		if (csvOutput.exists()) {
			int copies = 1;
			String suffix = csvOutputPath.substring(csvOutputPath.indexOf(".csv"));
			int suffixIndex = csvOutputPath.indexOf(".csv");
			String csvOutputCopy = null;
			while (csvOutput.exists()) {
				csvOutputCopy = csvOutputPath.substring(0, suffixIndex) + " (" + copies + ")" + suffix;
				csvOutput = new File(csvOutputCopy);
				copies++;
			}
			csvOutputPath = csvOutputCopy;
		}
		
		//gets Id3 tags and file paths from the mp3 files
		ArrayList<ID3v1> tags = new ArrayList<ID3v1>();
		ArrayList<String> mp3Paths = new ArrayList<String>();
		File[] mp3FolderFiles = mp3Folder.listFiles();
		
		for (int i  = 0; i < mp3FolderFiles.length; i++) {
			if (mp3FolderFiles[i].getAbsolutePath().contains(".mp3")) {
				String filePath = mp3FolderFiles[i].getAbsolutePath();
				Mp3File mp3file = new Mp3File(filePath);
				ID3v1 tag = null;
				if (mp3file.hasId3v1Tag()) {
					tag = mp3file.getId3v1Tag();
				} else if (mp3file.hasId3v2Tag()) {
					tag = mp3file.getId3v2Tag();
				} else {
					tag = new ID3v1Tag();
				}
				tags.add(tag);
				mp3Paths.add(filePath);
			}
		}
		
		//calls the writeCsv method
		CsvMp3 csv = new CsvMp3(tags, csvOutputPath, mp3Paths);
		csv.writeCsv();
		System.out.println("Csv file complete.");
	}
	
	public static void editMp3s(String mp3OutputPath, String csvInputPath) {
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
