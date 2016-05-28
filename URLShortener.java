import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import com.cinchapi.concourse.Concourse;
import com.cinchapi.concourse.thrift.Operator;

/**
 * 
 * @author nishit.shivhre
 * 
 *Class URLShortener that shortens the given URLs using method shorten
 */
public class URLShortener {
	// key-url dictionary
	private HashMap<String, String> keyDict; 
	
	//dictionary to check if a key already exists
	private HashMap<String, String> valueDict;
	
	//default domain name for generating shortened URLs
	private String domainName;
	
	// character array that is used in generating shortened URLs
	private char[] chars = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	private Random rand; 
	private int keyLength = 8;

	// Default Constructor
	URLShortener() {
		keyDict = new HashMap<String, String>();
		valueDict = new HashMap<String, String>();
		rand = new Random();
		domainName = "http://bit.ly";
	}

	/**
     * method to shorten the {@code expandedURL}

     * @param original or expanded URL that needs to be shortened
     * @return {@code shoretenedURL} short version of original URL
     */
	public String shorten(String expandedURL) {
		String shortenedURL = "";
		if (isValid(expandedURL)) {
			expandedURL = cleanURL(expandedURL);
			if (valueDict.containsKey(expandedURL)) {
				shortenedURL = domainName + "/" + valueDict.get(expandedURL);
			} else {
				shortenedURL = domainName + "/" + getNewKey(expandedURL);
			}
		}
		return shortenedURL;
	}

	/**
     * method to expand the {@code shortenedURL}

     * @param shortened URL that needs to be expanded
     * @return {@code expanededURL} for shortened URL
     */
	public String expand(String shortenedURL) {
		String expandedURL = "";
		String key = shortenedURL.substring(domainName.length() + 1);
		expandedURL = keyDict.get(key);
		return expandedURL;
	}

	boolean isValid(String url) {
		return true;
	}
	
	/**
     * method to clean the {@code urlString}

     * @param URL string that is to be cleaned
     * @return {@code urlStringL} cleaned version of original URL
     */
	String cleanURL(String urlString) {
		if (urlString.substring(0, 7).equals("http://"))
			urlString = urlString.substring(7);

		if (urlString.substring(0, 8).equals("https://"))
			urlString = urlString.substring(8);

		if (urlString.charAt(urlString.length() - 1) == '/')
			urlString = urlString.substring(0, urlString.length() - 1);
		return urlString;
	}

	/**
	 * Get New Key method that generates a new key
	 **/
	private String getNewKey(String expandedURL) {
		//String key;
		//key = generateKey();
		String key = "";
		boolean flag = true;
		while (flag) {
			key = "";
			for (int i = 0; i <= keyLength; i++) {
				key += chars[rand.nextInt(62)];
			}
			if (!keyDict.containsKey(key)) {
				flag = false;
			}
		}
		keyDict.put(key, expandedURL);
		valueDict.put(expandedURL, key);
		return key;
	}


	@SuppressWarnings("resource")
	public static void main(String args[]) {
		URLShortener u = new URLShortener();//(5, "www.tinyurl.com/");
		Concourse concourse = Concourse.connect();
		String flag = "y";
		int id;
		String inputURL[] = { "https://github.com/cinchapi/concourse/blob/develop/concourse-driver-java/README.md#find", "www.google.com/", "www.google.com",
				"http://wiki.cinchapi.com/display/OSS/Concourse+Developer+Setup#ConcourseDeveloperSetup-UseGitBash", "https://mail.google.com/mail/u/1/#inbox/154d38b949bdb89c", "www.amazon.com",
				"www.amazon.com/page1abcgdlkasdklaksj.php", "https://www.youtube.com/channel/UCCq1xDJMBRF61kiOgU90_kw",
				"www.linkedIn.com", "https://www.netflix.com/browse", "https://www.coursera.org/",
				"www.techcrunch.com", "http://www.espncricinfo.com/ci/content/match/fixtures_futures.html", "https://www.facebook.com/nishit.shivhre" };
		
		Set<String> keys = new HashSet<String>();
		keys.add("expanded");
		Set<Long> records = new HashSet<Long>();
		
		//add URLs to the database
		for (int i = 0; i < inputURL.length; i++) {
			concourse.add("expanded", inputURL[i], i+1);
			records.add((long) i+1);
		}
		

		System.out.println(records);
		System.out.println(inputURL.length);
		System.out.println(records.size());
		
		//Add shortened URL's to database
		for (int i = 0; i < inputURL.length; i++) {
			concourse.add("shortened", u.shorten(inputURL[i]), i+1);
		}
		keys.add("shortened");
		Map<Long, Map<String, Set<Object>>> data1 = concourse.get(keys, records);
		System.out.println("Current DB:");
		System.out.println(data1);
		
		id = inputURL.length+1;
		
		while(flag.equalsIgnoreCase("y")){
		System.out.println("1. Enter new URL");
		System.out.println("2. Search for existing URL:");
		System.out.println("3. Exit: ");
		Scanner input = new Scanner(System.in);
		int choice = input.nextInt();
		
		if (choice == 1){
			System.out.print("Enter URL: ");
			Scanner input1 = new Scanner(System.in);
			String url = input1.nextLine();
		    concourse.add("expanded", url, id);
		    concourse.add("shortened", u.shorten(url), id);
		    Set<Long> newRecords = new HashSet<Long>();
		    newRecords.add((long) id);
		    Map<Long, Map<String, Set<Object>>> data2 = concourse.get(keys, newRecords);
		    System.out.println("Added Record:");
		    System.out.println(data2);
		    id = id+1;
		}
		
		else if (choice == 2){
			System.out.println("1. Enter record id");
			System.out.println("2. Enter URL");
			Scanner input2 = new Scanner(System.in);
			int choice1 = input2.nextInt();
			if (choice1 == 1){
				System.out.println("Enter record id:");
				Scanner input3 = new Scanner(System.in);
				int rid = input3.nextInt();
				Set<Long> searchRecords = new HashSet<Long>();
				searchRecords.add((long) rid);
				Map<Long, Map<String, Set<Object>>> data3 = concourse.get(keys, searchRecords);
				System.out.println(data3);
			}
			else if(choice1 == 2){
				System.out.println("Enter URL");
				Scanner input3 = new Scanner(System.in);
				String url = input3.nextLine();
				Set<Long> searchRecords = new HashSet<Long>();
				searchRecords.addAll(concourse.find("expanded", Operator.EQUALS, url));
				Map<Long, Map<String, Set<Object>>> data3 = concourse.get(keys, searchRecords);
				System.out.println(data3);
			}
		}
		
		else{
			flag = "n";
			break;
		}
		
		System.out.println("Do you want to continue(y/n): ");
		Scanner input4 = new Scanner(System.in);
		flag = input4.nextLine();
	}
	
		//concourse.clear(keys, records);
		System.out.println("Exit");
	}
}