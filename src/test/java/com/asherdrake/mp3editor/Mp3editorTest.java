package com.asherdrake.mp3editor;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
/*import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;*/
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.Before;
//import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
//import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v22Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class Mp3editorTest {
	@InjectMocks
    private Mp3editor editor = new Mp3editor();
 	
	String systemProperty = System.getProperty("user.dir");
	
	
	@Before
	public void setUp() {
		System.setProperty("user.dir", "test");
	}
	
	@After
	public void tearDown() {
		System.setProperty("user.dir", systemProperty);
	}
	
	
	@Test
	public void parseArgs_DefaultPaths() {
		String[] testArgs = {"testCommand"};
		String[] expectedPaths = new String[2];
		expectedPaths[0] = System.getProperty("user.dir");
		expectedPaths[1] = System.getProperty("user.dir") + "/mp3tags.csv";
		String[] returnedPaths = editor.parseArgs(testArgs);
		assertTrue(Arrays.equals(expectedPaths, returnedPaths));
	}
	
	@Test
	public void parseArgs_OnlyUserMp3Path() {
		String[] testArgs = {"testCommand", "-m", "testMp3Path"};
		String[] expectedPaths = new String[2];
		expectedPaths[0] = "testMp3Path";
		expectedPaths[1] = System.getProperty("user.dir") + "/mp3tags.csv";
		String[] returnedPaths = editor.parseArgs(testArgs);
		assertTrue(Arrays.equals(expectedPaths, returnedPaths));
	}
	
	@Test
	public void parseArgs_OnlyUserCsvPath() {
		String[] testArgs = {"testCommand", "-c", "testCsvPath"};
		String[] expectedPaths = new String[2];
		expectedPaths[0] = System.getProperty("user.dir");
		expectedPaths[1] = "testCsvPath";
		String[] returnedPaths = editor.parseArgs(testArgs);
		assertTrue(Arrays.equals(expectedPaths, returnedPaths));
	}
	
	@Test
	public void parseArgs_BothUserPaths_Mp3First() {
		String[] testArgs = {"testCommand", "-m", "testMp3Path", "-c", "testCsvPath"};
		String[] expectedPaths = new String[2];
		expectedPaths[0] = "testMp3Path";
		expectedPaths[1] = "testCsvPath";
		String[] returnedPaths = editor.parseArgs(testArgs);
		assertTrue(Arrays.equals(expectedPaths, returnedPaths));
	}
	
	@Test
	public void parseArgs_BothUserPaths_CsvFirst() {
		String[] testArgs = {"testCommand", "-c", "testCsvPath", "-m", "testMp3Path"};
		String[] expectedPaths = new String[2];
		expectedPaths[0] = "testMp3Path";
		expectedPaths[1] = "testCsvPath";
		String[] returnedPaths = editor.parseArgs(testArgs);
		assertTrue(Arrays.equals(expectedPaths, returnedPaths));
	}

	@Test
	public void parseArgs_TwoFlags_InvalidArgs() {
		String[] testArgs = {"testCommand", "-c", "testCsvPath", "-m"};
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			editor.parseArgs(testArgs);
		});
		
		String expectedMessage = "Two flags were entered, but the # of arguments is invalid.";
		String actualMessage = exception.getMessage();
		
		assertTrue(actualMessage.equals(expectedMessage));
	}
	
	@Test
	public void parseArgs_OneFlag_InvalidArgs() {
		String[] testArgs = {"testCommand", "-m"};
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			editor.parseArgs(testArgs);
		});
		
		String expectedMessage = "One flag was entered, but the # of arguments is invalid.";
		String actualMessage = exception.getMessage();
		
		assertTrue(actualMessage.equals(expectedMessage));
	}
	
	@Test
	public void parseArgs_NoFlags_InvalidArgs() {
		String[] testArgs = {"testCommand", "testCsvPath"};
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			editor.parseArgs(testArgs);
		});
		
		String expectedMessage = "No flags were specified, but the # of arguments is invalid.";
		String actualMessage = exception.getMessage();
		
		assertTrue(actualMessage.equals(expectedMessage));
	}
	
	@Test
	public void getMp3Info_Happy() throws InvalidDataException, UnsupportedTagException, IOException {
		try (MockedConstruction<Mp3File> mockMp3File = Mockito.mockConstruction(Mp3File.class, 
				(mock, context) -> {
					//System.out.println(context.arguments().get(0).toString());
					if (context.arguments().get(0).toString().matches(".*test1.mp3")) {
						when(mock.hasId3v1Tag()).thenReturn(true);
						when(mock.getId3v1Tag()).thenReturn(new ID3v1Tag());
					} else if (context.arguments().get(0).toString().matches(".*test2.mp3")) {
						when(mock.hasId3v2Tag()).thenReturn(true);
						when(mock.getId3v2Tag()).thenReturn(new ID3v22Tag());
					} else {
						when(mock.hasId3v2Tag()).thenReturn(false);
						when(mock.getId3v1Tag()).thenReturn(new ID3v1Tag());
					}
			      })){
			Mp3editor editr = new Mp3editor();
			
			List<String> files = Arrays.asList("test1.mp3", "test2.mp3", "test3.mp3");
			List<String> expectedPaths = files.stream()
					.map(f -> new File(f).getAbsolutePath())
					.collect(Collectors.toList());
					
			       
			File testFile1 = new File("test1.mp3");
			File testFile2 = new File("test2.mp3");
			File testFile3 = new File("test3.mp3");
			File[] arr = {testFile1, testFile2, testFile3};
			File ff = Mockito.mock(File.class);
			when(ff.listFiles(any(FilenameFilter.class))).thenReturn(arr);
			
			ID3v1 tag = new ID3v1Tag();
			ID3v1 tag2 = new ID3v22Tag();
			ArrayList<Mp3Info> expectedList = new ArrayList<Mp3Info>();
			expectedList.add(new Mp3Info(tag, testFile1.getAbsolutePath()));
			expectedList.add(new Mp3Info(tag2, testFile2.getAbsolutePath()));
			expectedList.add(new Mp3Info(tag, testFile3.getAbsolutePath()));
				
			ArrayList<Mp3Info> actualList = editr.getMp3Info(ff);
			
			assertEquals(expectedList.size(), actualList.size());
			
			List<String> resultPaths = actualList
					.stream()
					.map(k -> k.getPath())
					.collect(Collectors.toList());

			//System.out.println(resultPaths + "\n" + expectedPaths);
			assertEquals(resultPaths, expectedPaths);
			assertEquals(actualList.get(0).getTag() instanceof ID3v1Tag, true);
			assertEquals(actualList.get(1).getTag() instanceof ID3v22Tag, true);
			assertEquals(actualList.get(2).getTag() instanceof ID3v1Tag, true);
		}
	}
	
	@Test
	public void getWriter_Happy() throws IOException {
		try (MockedConstruction<FileWriter> mockWriter = Mockito.mockConstruction(FileWriter.class)){
			assertEquals(true, editor.getWriter("testFileWriter") instanceof Writer);
		}
	}
}
