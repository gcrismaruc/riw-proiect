package p1;

import java.util.HashMap;
import java.util.Map;

public class TextParser {
	private Map<String, Integer> aparitii;


	public TextParser(){
		aparitii = new HashMap<String, Integer>();
	}

	public Map<String, Integer> getAparitii() {
		return aparitii;
	}

	public static Map<String, MyPair> getParsedWords(String text, String fileName) {

		StringBuilder word = new StringBuilder();
		Map<String, MyPair> words = new HashMap<>();

		for(int i = 0; i < text.length(); i++) {
			if(!Character.isLetter(text.charAt(i)) && text.charAt(i) != '\''){
				if(!word.toString().equals("")) {
					String canonicalForm = getCanonicalForm(word.toString());
					if (words.containsKey(canonicalForm)) {
						MyPair pair = words.get(canonicalForm);
						pair.setValue(pair.getValue() + 1);
						words.put(canonicalForm, pair);
					} else {
						if (!word.toString().equals("")) {
							if (!StopWords.stopWords.contains(word.toString())) {
								if (ExceptionWords.exceptionWords.contains(word.toString())) {
									MyPair pair = new MyPair();
									pair.setKey(fileName);
									pair.setValue(1);
									words.put(word.toString(), pair);
								} else {
									MyPair pair = new MyPair();
									pair.setKey(fileName);
									pair.setValue(1);
									words.put(getCanonicalForm(word.toString()), pair);
								}
							}
						}
					}
				}
				word = word.delete(0, word.length());
			}else {
				word.append(text.charAt(i));
			}
		}

		return words;
	}


	private static String getCanonicalForm(String word){
		Porter porter = new Porter();

		return porter.stripAffixes(word);
	}

	public void getWords (String text) {

		StringBuilder word = new StringBuilder();

		for(int i = 0; i < text.length(); i++) {

			if(!Character.isLetter(text.charAt(i)) && text.charAt(i) != '\''){
				if(aparitii.containsKey(word.toString())) {
					int count = aparitii.get(word.toString());
					aparitii.put(word.toString(), count + 1);
				}else {
					if (!word.toString().equals("")) {
						aparitii.put(word.toString(), 1);
					}
				}
				word = word.delete(0, word.length());
			}else {
				word.append(text.charAt(i));
			}
		}
	}



	public void printWords(String text) {
		getWords(text);
		for(Map.Entry entry : aparitii.entrySet()){
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
	
}
