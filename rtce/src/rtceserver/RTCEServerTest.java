package rtceserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RTCEServerTest {
	
	private int version;
	private ArrayList<String> edits = new ArrayList<String>();
	
	
	
	private ArrayList<Object> getTokens(String delta) {
		int numSpaces = 4; // total # of spaces separating tokens
		int last_version_number = 0;
		int startIndex = 0;
		int length = 0;
		String replacement = "";
		String buffer = "";
		for (int i = 0; i < delta.length(); i++) {
			if (numSpaces == 0) {
				replacement = delta.substring(i);
				break;
			}
			if (delta.substring(i, i + 1).equals(" ")) {
				numSpaces += -1;
				if (numSpaces == 2) {
					last_version_number = Integer.parseInt(buffer);
				} else if (numSpaces == 1) {
					startIndex = Integer.parseInt(buffer);
				} else if (numSpaces == 0) {
					length = Integer.parseInt(buffer);
				}
				buffer = "";
				continue;
			}
			buffer += delta.substring(i, i + 1);
		}
		ArrayList<Object> tokens = new ArrayList<Object>();
		tokens.add(last_version_number);
		tokens.add(startIndex);
		tokens.add(length);
		tokens.add(replacement);
		return tokens;
	}
	
	private String handleMerge(String delta) {
		
		ArrayList<Object> tokens = getTokens(delta);
		int last_version_number = (Integer) tokens.get(0); 
		int startIndex = (Integer) tokens.get(1); 
		int length = (Integer) tokens.get(2); 
		String replacement = (String) tokens.get(3); 
		int difference = version - last_version_number;
		for (int i = 0; i < difference; i++) {
			String currentEdit = edits.get(last_version_number + i);
			ArrayList<Object> tempTokens = getTokens(currentEdit);
			int tempStartIndex = (Integer) tempTokens.get(1);
			int tempLength = (Integer) tempTokens.get(2);
			String tempReplacement = (String) tempTokens.get(3);
			System.out.println("tempstart + templength: " + (tempStartIndex + tempLength));
			if (tempStartIndex <= startIndex) {
				if (tempStartIndex + tempLength > startIndex) {
					System.out.println("branch taken");
					startIndex = tempStartIndex;
				}
				else {
					startIndex += tempReplacement.length() - tempLength;
				}
			}
		}
		last_version_number += difference + 1;
		
		return ("push " + last_version_number + " " + startIndex + " " + length + " " + replacement);
	}
    @Test
    public void testTokenize() {
        String string = "pull 1 0 2  ";
        String[] tokens = string.split("\\s");
        for (String token: tokens){
            System.out.println("This is a token:" + token);
        }
    }
    
    @Test
    public void testHandleMergeInsertingBehindEdits() {
    	edits.add("push 0 0 0 a");
    	edits.add("push 1 1 0 b");
    	edits.add("push 2 2 0 c");
    	edits.add("push 3 3 0 d");
    	this.version = 3;
    	assertEquals(handleMerge("push 2 1 0 1"),"push 4 1 0 1");
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    }
    
    @Test
    public void testHandleMergeInsertingAheadEdits() {
    	edits.add("push 0 0 0 d");
    	edits.add("push 1 0 0 c");
    	edits.add("push 2 0 0 b");
    	edits.add("push 3 0 0 a");
    	this.version = 3;
    	assertEquals(handleMerge("push 0 1 0 1"),"push 4 4 0 1");
    	System.out.println(edits);
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    }
    
    @Test
    public void testHandleMergeReplaced() {
    	edits.add("push 0 0 0 d");
    	edits.add("push 1 0 0 c");
    	edits.add("push 2 0 0 b");
    	edits.add("push 3 0 0 a");
    	edits.add("push 4 0 0 z");
    	edits.add("push 5 1 3 ");
    	this.version = 5;
    	assertEquals(handleMerge("push 4 2 0 123"),"push 6 3 0 123");
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    	edits.remove(0);
    }
    
    @Test
    public void test() {
    	assertEquals(1,"\u0015".length());
    	edits.add("push 0 0 0 b");
    	edits.add("push 1 0 0 a");
    	this.version = 1;
    	assertEquals(handleMerge("push 0 0 0 \u0015"),"push 2 1 0 \u0015");
    	String a = "ab\u0015";
  
    	a.replaceAll("\u0015", "\n");
 
    	System.out.println("this is a: " + a);
    	assertEquals(a, "ab\n");
    	}

}
