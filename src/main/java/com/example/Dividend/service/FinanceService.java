package com.example.Dividend.service;

import com.example.Dividend.exception.impl.NoCompanyException;
import com.example.Dividend.model.Company;
import com.example.Dividend.model.Dividend;
import com.example.Dividend.model.ScrapedResult;
import com.example.Dividend.persist.CompanyRepository;
import com.example.Dividend.persist.DividendRepository;
import com.example.Dividend.persist.entity.CompanyEntity;
import com.example.Dividend.persist.entity.DividendEntity;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FinanceService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(FinanceService.class);
    public final CompanyRepository companyRepository;
    public final DividendRepository dividendRepository;

    @Cacheable(
            key = "#companyName",
            value = {"finance"}
    )
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        CompanyEntity company = (CompanyEntity)this.companyRepository.findByName(companyName).orElseThrow(() -> {
            return new NoCompanyException();
        });
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());
        List<Dividend> dividends = (List)dividendEntities.stream().map((e) -> {
            return new Dividend(e.getDate(), e.getDividend());
        }).collect(Collectors.toList());
        return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
    }

    @Generated
    public FinanceService(final CompanyRepository companyRepository, final DividendRepository dividendRepository) {
        this.companyRepository = companyRepository;
        this.dividendRepository = dividendRepository;
    }
}