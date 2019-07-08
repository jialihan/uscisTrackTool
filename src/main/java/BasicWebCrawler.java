import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class BasicWebCrawler {
      private Map<String,String> postData;
      private Map<String, Integer> summary;
      public BasicWebCrawler() {
          postData = new HashMap<>();
          summary = new HashMap<>();
          summary.put("received",0);
          summary.put("produced", 0);
          summary.put("approved",0);
      }

    public void getCaseStatus(String URL) {
            try{
                Connection.Response resp = Jsoup.connect(URL)
                        .followRedirects(true)
                        .method(Connection.Method.GET)
                        .execute();
                Document doc = resp.parse();

                for (Element input : doc.select("input[type=hidden]")) {
                    postData.put(input.attr("name"),  input.attr("value"));
                }

                final String properiesName = "myconfig.properties";
                Properties prop = new Properties();
                String receiptNum = null;
                String range = null;
                try {

                    InputStream in = new BufferedInputStream(new FileInputStream(properiesName));
                    prop.load(in);
                    receiptNum = prop.getProperty("start");
                    range = prop.getProperty("range");

                } catch (IOException e) {
                    e.printStackTrace();
                }

                int upperRange = Integer.parseInt(range);
                for(int i = 0; i<upperRange; i++) {
                    int cur = Integer.parseInt(receiptNum) + i;
                    // you can define your certain block here
                    postData.put("appReceiptNum", "YSC1990178" +cur);
                    String action = doc.select("form[method=post]").get(0).attr("action");
                    resp = Jsoup.connect("https://egov.uscis.gov" + action)
                            .data(postData)
                            .method(Connection.Method.POST)
                            .execute();
                    doc = resp.parse();
                    String res = doc.select("h1").first().text().toString();
                    if(res.contains("Received"))
                        summary.put("received", summary.get("received")+1);
                    if(res.contains("Approved"))
                        summary.put("approved", summary.get("approved")+1);
                    if(res.contains("Produced"))
                        summary.put("produced", summary.get("produced")+1);

                    System.out.println(cur +": "+ res);
                }


            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }

            // print total summary
            System.out.println("Case was Received: " + summary.get("received"));
            System.out.println("Case was Approved:" + summary.get("approved"));
            System.out.println("new card produced: " + summary.get("produced"));
        }


    public static void main(String[] args) {
        //1. Pick a URL from the frontier
        new BasicWebCrawler().getCaseStatus("https://egov.uscis.gov/casestatus/landing.do");
    }

}
