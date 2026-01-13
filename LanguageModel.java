import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
        String corpus = in.readAll(); 

        for (int i = 0; i <= corpus.length() - windowLength - 1; i++) {
            String window = corpus.substring(i, i + windowLength);
            char nextChar = corpus.charAt(i + windowLength);
            
            if (!CharDataMap.containsKey(window)) {
                CharDataMap.put(window, new List());
            }
            List charList = CharDataMap.get(window);
            charList.update(nextChar);
        }
        for (List list : CharDataMap.values()) {
            calculateProbabilities(list);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
        int totalCount = 0;

        for (int i = 0; i < probs.getSize(); i++) {
            totalCount += probs.get(i).count;
        }
        double cumulativeProb = 0.0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            cd.p = (double) cd.count / totalCount;
            cumulativeProb += cd.p;
            cd.cp = cumulativeProb;
        }
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble();
        for (int i = 0; i < probs.getSize(); i++) {
            CharData cd = probs.get(i);
            if (r <= cd.cp) {
                return cd.chr;
            }
        }
        return '\0'; // Should never reach here if probs is not empty
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) {
            return initialText; // Not enough initial text to form a window
        }
        StringBuilder result = new StringBuilder(initialText);
        String currentWindow = initialText.substring(initialText.length() - windowLength);

        for (int i = 0; i < textLength; i++) {
            List probs = CharDataMap.get(currentWindow);
            if (probs == null) {
                break;
            }
            char nextChar = getRandomChar(probs);
            result.append(nextChar);
            currentWindow = currentWindow.substring(1) + nextChar;
        }
        return result.toString();
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialtext = args[1];
        int textLength = Integer.parseInt(args[2]);
        String mode = args[3];
        String filename = args[4];

        LanguageModel model;
        if (mode.equals("fixed")) {
            model = new LanguageModel(windowLength, 20);
        } else {
            model = new LanguageModel(windowLength);
        }

        model.train(filename);

        String generatedText = model.generate(initialtext, textLength);
        System.out.println(generatedText);
    }
}
