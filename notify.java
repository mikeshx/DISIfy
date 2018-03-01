import org.apache.commons.io.FileUtils;
import org.gnome.gtk.Gtk;
import org.gnome.notify.Notification;
import org.gnome.notify.Notify;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class notify {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Starts the scraping process
        checkForUpdates();
    }

    // Sends the notification to gnome
    public static void sendNotification(String header, String notificationText) throws InterruptedException {

        String[] args = null;

        Gtk.init(args);                           // initialize Gtk
        Notify.init("DISIM News");  // initialize the notification system

        // Create the notification
        Notification myNotification = new Notification(header, notificationText, "dialog-information");

        myNotification.show();
        TimeUnit.SECONDS.sleep(1);

    }

    // Parse data fom the DISIM website
    public static ArrayList<List<String>> parseData() throws IOException {

        ArrayList<List<String>> arrayL = new ArrayList<>();

        // Connect to the website and get data
        Document doc = Jsoup.connect("http://www.disim.univaq.it/main/news.php").get();

        Elements headers = doc.select("div.post_item_list>h3");
        Elements posts = doc.select("div.post_item_list>p.post_description");

        List<String> tempHeaders = new ArrayList<String>();
        List<String> tempPosts = new ArrayList<String>();

        // Scan every header to save it into a list
        for (Element header : headers) {
            tempHeaders.add(header.text());
        }

        // Add the list to an arrayList
        arrayL.add(tempHeaders);

        for (Element post : posts) {
            tempPosts.add(post.text());
        }
        arrayL.add(tempPosts);

        return arrayL;
    }

    // Check if there are more recent news
    public static void checkForUpdates() throws IOException, InterruptedException {

        // Update the list of news
        ArrayList<List<String>> test = parseData();

        // Check if the file exists
        File f = new File("tempFile.txt");
        if(!f.exists()) {
            FileUtils.writeStringToFile(new File("tempFile.txt"), "different");
        }

        // We check if the first line of the new information is
        // Different from the one written in the file
        // If so, send a notification and update the txt file
        if (!test.get(0).get(0).equals(readFromFile("tempFile.txt"))) {
            sendNotification(test.get(0).get(0), test.get(1).get(0));

            // Delete the previous file
            FileUtils.deleteQuietly(new File("tempFile.txt"));
            FileUtils.writeStringToFile(new File("tempFile.txt"), test.get(0).get(0));
        }
    }

    // Read a file into a string and return it
    public static String readFromFile(String fileName) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
        return content;
    }
}
