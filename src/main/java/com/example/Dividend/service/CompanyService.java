package com.example.Dividend.service;

import com.example.Dividend.exception.impl.NoCompanyException;
import com.example.Dividend.model.Company;
import com.example.Dividend.model.ScrapedResult;
import com.example.Dividend.persist.CompanyRepository;
import com.example.Dividend.persist.DividendRepository;
import com.example.Dividend.persist.entity.CompanyEntity;
import com.example.Dividend.persist.entity.DividendEntity;
import com.example.Dividend.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // ticker 존재 유무 확인
    public Company save(String ticker) {
        boolean exists = this.companyRepository.existsByTicker(ticker);

        // 이미 존재하는 ticker
        if (exists) {
            throw new RuntimeException("already exists ticker -> " + ticker);
        }
        // 존재하지 않는 ticker
        return this.storeCompanyAndDividend(ticker);
    }

    // 페이징 & 회사 조회
    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    // 회사 & 배당금 저장
    private Company storeCompanyAndDividend(String ticker) {
        // ticker 를 기준으로 회사를 스크래핑
        Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) { // company 정보가 없다면 에러 메세지 발생 & 메소드 종료
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(company);

        // 스크래핑 결과 저장
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .limit(10)
                .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);

        return company;
    }


    // trie에 회사명 저장
    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null); // apache에서 구현된 trie는 이런저런 기능들을 덧붙여 응용된 형태로 구현되어 있기 때문에 key,value를 함께 저장하도록 되어있음, 자동완성 기능만 구현할 것이기 때문에 value는 null로 입력 처리
    }

    // trie에서 회사명 조회
    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                .limit(10) // 데이터 갯수 제한
                .collect(Collectors.toList());
    }

    // trie에 저장된 키워드 삭제
    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    // 회사 & 배당금 삭제
    public String deleteCompany(String ticker) {
        CompanyEntity company = (CompanyEntity)this.companyRepository.findByTicker(ticker).orElseThrow(() -> {
            return new NoCompanyException();
        });
        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);
        this.deleteAutocompleteKeyword(company.getName());
        return company.getName();
    }

    @Generated
    public CompanyService(final Trie trie, final Scraper yahooFinanceScraper, final CompanyRepository companyRepository, final DividendRepository dividendRepository) {
        this.trie = trie;
        this.yahooFinanceScraper = yahooFinanceScraper;
        this.companyRepository = companyRepository;
        this.dividendRepository = dividendRepository;
    }
}