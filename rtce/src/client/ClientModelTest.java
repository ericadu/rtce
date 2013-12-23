package client;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;

import org.junit.Test;

public class ClientModelTest {
	
	public int updateCaretPosition(int caretPosition, int startIndex, int length, String replacement) {
		if (startIndex <= caretPosition) {
			if (startIndex + length > caretPosition) {
				caretPosition = startIndex;
			}
			else {
				caretPosition += replacement.length() - length;
			}
		}
		return caretPosition;
	}
	
	public String handleEdit(String document, int startIndex, int length, String replacement) {
		if (startIndex >= document.length()) { //startIndex should never be greater
			document = document + replacement;
		}
		else {
			document = document.substring(0,startIndex) + replacement + document.substring(startIndex + length);
			//assumes that given a string "abc" startIndex of 1 means inserting/replacing starting after "a"
		}
		return document;
	}
	
	public ArrayList<Object> ClientRunnableRunMethod(String delta) {
		int numSpaces = 3; //total # of spaces separating tokens
		int last_version_number = 0; int startIndex = 0;
		int length = 0; String replacement = "";
		String buffer = ""; //
		for (int i = 0; i < delta.length(); i++) {
			if (numSpaces == 0) {
				replacement = delta.substring(i);
				break;
			}
			if (delta.substring(i,i+1).equals(" ")) {
				numSpaces += -1;
				if (numSpaces == 2) { 
					last_version_number = Integer.parseInt(buffer);
				}
				else if (numSpaces == 1) {
					startIndex = Integer.parseInt(buffer);
				}
				else if (numSpaces == 0) {
					length = Integer.parseInt(buffer);
				}
				buffer = "";
				continue;
			}
			buffer += delta.substring(i,i+1);
		}
		ArrayList<Object> tokens = new ArrayList<Object>();
		tokens.add(last_version_number);tokens.add(startIndex);
		tokens.add(length);tokens.add(replacement);
		return tokens;
	}
	
	@Test
	public void testHandleEditAtEndWithReplacement() {
		assertTrue(handleEdit("abcdefghijklmnopq",17,5,"abcde").equals("abcdefghijklmnopqabcde"));
	}
	
	@Test
	public void testHandleEditAtBeginningWithReplacement() {
		assertTrue(handleEdit("abcde",0,5,"abcde").equals("abcde"));
	}
	
	@Test
	public void testHandleEditInMiddleWithReplacement() {
		assertTrue(handleEdit("abcdefghijklmnopq",5,5,"11111").equals("abcde11111klmnopq"));
	}
	
	@Test
	public void testHandleEditAtBeginingWithInsertion() {
		assertTrue(handleEdit("abcde",0,0,"abcde").equals("abcdeabcde"));
	}
	
	@Test
	public void testHandleEditAtMiddleWithInsertion() {
		assertTrue(handleEdit("abcde",3,0,"abcde").equals("abcabcdede"));
	}
	
	@Test
	public void testHandleEditAtEndWithInsertion() {
		assertTrue(handleEdit("abcde",5,0,"abcde").equals("abcdeabcde"));
	}
	
	@Test
	public void testRunMethodWithSpaceAsReplacement() {
		ArrayList<Object> tokens = ClientRunnableRunMethod("0 1 2  ");
		assertTrue(((Integer) (tokens.get(0))).equals(0));
		assertTrue(((Integer) (tokens.get(1))).equals(1));
		assertTrue(((Integer) (tokens.get(2))).equals(2));
		assertTrue(((String) (tokens.get(3))).equals(" "));
	}
	
	@Test
	public void testRunMethodWithDelete() {
		assertTrue(handleEdit("abcde",3,1,"").equals("abce"));
	}
	
	@Test
	public void testRunMethodWithBigStringAsReplacement() {
		ArrayList<Object> tokens = ClientRunnableRunMethod("0 1 2 asdfasdfkjaksjdfklasdfkladsfkjln");
		assertTrue(((Integer) (tokens.get(0))).equals(0));
		assertTrue(((Integer) (tokens.get(1))).equals(1));
		assertTrue(((Integer) (tokens.get(2))).equals(2));
		assertTrue(((String) (tokens.get(3))).equals("asdfasdfkjaksjdfklasdfkladsfkjln"));
	}
	
	@Test
	public void testRunMethodWithBigIntegers() {
		ArrayList<Object> tokens = ClientRunnableRunMethod("123 456 789 asdfasdfkjaksjdfklasdfkladsfkjln");
		assertTrue(((Integer) (tokens.get(0))).equals(123));
		assertTrue(((Integer) (tokens.get(1))).equals(456));
		assertTrue(((Integer) (tokens.get(2))).equals(789));
		assertTrue(((String) (tokens.get(3))).equals("asdfasdfkjaksjdfklasdfkladsfkjln"));
	}
	
	@Test
	public void testCaretPositionBefore() {
		assertEquals(1,updateCaretPosition(0,0,0,"a"));
	}
	
	@Test
	public void testCaretPositionAfter() {
		assertEquals(6,updateCaretPosition(5,0,0,"a"));
	}
	
	@Test
	public void testCaretPositionOverlap() {
		assertEquals(4,updateCaretPosition(5,4,2,"a"));
	}
}
