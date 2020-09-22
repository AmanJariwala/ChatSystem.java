import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * [Add your documentation here]
 *
 * @author Aman Jariwala and Sultan Al-Ali, Lab sec L04
 * @version April 26, 2020
 */
public class ChatFilter {

    //list of badwords stored in separate text file separated by comma.
    List<String> badwords = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {

        File file = new File(badWordsFileName);

        try {

            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                //Reading lines from badwordfile
                String lines = sc.nextLine();
                //Splitting words and storing in arraylist
                for (String word : lines.split(",")) {
                    badwords.add(word);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String filter(String msg) {

        for (String word : badwords) {
            if (msg.contains(word)) {
                int j = word.length();
                String k = "";
                for (int i = 0; i < j; i++) {
                    k += "*";
                }
                msg = msg.replaceAll("\\b" + word + "\\b", k);
                msg = msg.replace(word, k);
            }
        }
        return msg;
    }
}