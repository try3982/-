package com.example.Dividend.scheduler;

import com.example.Dividend.model.Company;
import com.example.Dividend.model.ScrapedResult;
import com.example.Dividend.persist.CompanyRepository;
import com.example.Dividend.persist.DividendRepository;
import com.example.Dividend.persist.entity.CompanyEntity;
import com.example.Dividend.persist.entity.DividendEntity;
import com.example.Dividend.scraper.Scraper;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
@EnableCaching
public class ScraperScheduler {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(ScraperScheduler.class);
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

    @CacheEvict(
            value = {"finance"},
            allEntries = true
    )
    @Scheduled(
            cron = "${scheduler.scrap.yahoo}"
    )
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        List<CompanyEntity> companies = this.companyRepository.findAll();
        Iterator var2 = companies.iterator();

        while(var2.hasNext()) {
            CompanyEntity company = (CompanyEntity)var2.next();
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(new Company(company.getTicker(), company.getName()));
            scrapedResult.getDividends().stream().map((e) -> {
                return new DividendEntity(company.getId(), e);
            }).forEach((e) -> {
                boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                if (!exists) {
                    this.dividendRepository.save(e);
                    log.info("insert new dividend -> " + e.toString());
                }

            });

            try {
                Thread.sleep(3000L);
            } catch (InterruptedException var6) {
                var6.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

    }

    @Generated
    public ScraperScheduler(final CompanyRepository companyRepository, final DividendRepository dividendRepository, final Scraper yahooFinanceScraper) {
        this.companyRepository = companyRepository;
        this.dividendRepository = dividendRepository;
        this.yahooFinanceScraper = yahooFinanceScraper;
    }
}

