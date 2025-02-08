package com.example.Dividend.scraper;

import com.example.Dividend.model.Company;
import com.example.Dividend.model.Dividend;
import com.example.Dividend.model.ScrapedResult;
import com.example.Dividend.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {
    private static final String URL = "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s";
    private static final long START_TIME = 86400L;

    public YahooFinanceScraper() {
    }

    public ScrapedResult scrap(Company company) {
        ScrapedResult scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000L;
            String url = String.format("https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d", company.getTicker(), 86400L, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            Elements parsingDivs = document.getElementsByClass("table yf-j5d1ld noDl");
            Element tableEle = (Element)parsingDivs.get(0);
            Element tbody = (Element)tableEle.children().get(1);
            List<Dividend> dividends = new ArrayList();
            Iterator var12 = tbody.children().iterator();

            while(var12.hasNext()) {
                Element e = (Element)var12.next();
                String txt = e.text();
                if (txt.endsWith("Dividend")) {
                    String[] splits = txt.split(" ");
                    int month = Month.strToNumber(splits[0]);
                    int day = Integer.valueOf(splits[1].replace(",", ""));
                    int year = Integer.valueOf(splits[2]);
                    String dividend = splits[3];
                    if (month < 0) {
                        throw new RuntimeException("Unexcepted Month enum value -> " + splits[0]);
                    }

                    dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
                }
            }

            scrapResult.setDividends(dividends);
        } catch (IOException var20) {
            var20.printStackTrace();
        }

        return scrapResult;
    }

    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format("https://finance.yahoo.com/quote/%s", ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = (Element)document.getElementsByTag("h1").get(1);
            String title = titleEle.text().split(" \\(")[0].trim();
            return new Company(ticker, title);
        } catch (IOException var6) {
            var6.printStackTrace();
            return null;
        }
    }
}
