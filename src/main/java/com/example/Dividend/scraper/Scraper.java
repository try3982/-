package com.example.Dividend.scraper;

import com.example.Dividend.model.Company;
import com.example.Dividend.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);

    ScrapedResult scrap(Company company);
}
