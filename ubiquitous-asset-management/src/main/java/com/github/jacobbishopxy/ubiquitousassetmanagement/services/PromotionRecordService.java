/**
 * Created by Jacob Xie on 2/15/2022.
 */

package com.github.jacobbishopxy.ubiquitousassetmanagement.services;

import java.util.List;
import java.util.Optional;

import com.github.jacobbishopxy.ubiquitousassetmanagement.dtos.PromotionRecordSearch;
import com.github.jacobbishopxy.ubiquitousassetmanagement.models.PromotionRecord;
import com.github.jacobbishopxy.ubiquitousassetmanagement.models.PromotionStatistic;
import com.github.jacobbishopxy.ubiquitousassetmanagement.repositories.PromotionRecordRepository;
import com.github.jacobbishopxy.ubiquitousassetmanagement.repositories.PromotionStatisticRepository;
import com.github.jacobbishopxy.ubiquitousassetmanagement.services.helper.PromotionCalculationHelper;
import com.github.jacobbishopxy.ubiquitousassetmanagement.services.specifications.PromotionRecordSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PromotionRecordService
 *
 * Business logic for PromotionRecord. Each time a create/update/delete
 * operation is performed, the corresponding PromotionStatistic will be updated
 * as well.
 */
@Service
public class PromotionRecordService {

  @Autowired
  private PromotionRecordRepository prRepo;

  @Autowired
  private PromotionStatisticRepository psRepo;

  public List<PromotionRecord> getPromotionRecords(int page, int size, PromotionRecordSearch searchDto) {

    if (searchDto == null) {
      return prRepo.findAll(PageRequest.of(page, size)).getContent();
    }

    PromotionRecordSpecification prs = new PromotionRecordSpecification(searchDto);

    List<Sort.Order> orders = searchDto.getOrders();
    PageRequest pr = orders.isEmpty() ? PageRequest.of(page, size) : PageRequest.of(page, size, Sort.by(orders));

    return prRepo.findAll(prs, pr).getContent();
  }

  public Optional<PromotionRecord> getPromotionRecord(int id) {
    return prRepo.findById(id);
  }

  @Transactional(rollbackFor = Exception.class)
  public PromotionRecord createPromotionRecord(PromotionRecord promotionRecord) {
    String promotionPactName = promotionRecord.getPromotionPact().getName();
    String promoterEmail = promotionRecord.getPromoter().getEmail();

    // 0. fetch promotion statistic, create one if not exist
    PromotionStatistic promotionStatistic = this.psRepo
        .findByPromotionPactNameAndPromoterEmail(promotionPactName, promoterEmail)
        .orElse(new PromotionStatistic());

    // 1. fetch all promotion records by promotion pact name and promoter email
    List<PromotionRecord> relativePromotionRecord = this.prRepo
        .findByPromotionPactNameAndPromoterEmail(promotionPactName, promoterEmail);

    // 2. calculation
    promotionStatistic = PromotionCalculationHelper.affectPromotionStatistic(
        PromotionCalculationHelper.AffectPromotionStatisticType.CREATE,
        promotionRecord,
        promotionStatistic,
        relativePromotionRecord);

    // 3. save to promotion statistic
    this.psRepo.save(promotionStatistic);

    // 4. create promotion record
    return prRepo.save(promotionRecord);
  }

  @Transactional(rollbackFor = Exception.class)
  public Optional<PromotionRecord> updatePromotionRecord(int id, PromotionRecord promotionRecord) {

    String promotionPactName = promotionRecord.getPromotionPact().getName();
    String promoterEmail = promotionRecord.getPromoter().getEmail();

    // 0. fetch promotion statistic, create one if not exist
    PromotionStatistic promotionStatistic = this.psRepo
        .findByPromotionPactNameAndPromoterEmail(promotionPactName, promoterEmail)
        .orElse(new PromotionStatistic());

    // 1. fetch all promotion records by promotion pact name and promoter email
    List<PromotionRecord> relativePromotionRecord = this.prRepo
        .findByPromotionPactNameAndPromoterEmail(promotionPactName, promoterEmail);

    // 2. calculation
    promotionStatistic = PromotionCalculationHelper.affectPromotionStatistic(
        PromotionCalculationHelper.AffectPromotionStatisticType.UPDATE,
        promotionRecord,
        promotionStatistic,
        relativePromotionRecord);

    // 3. save to promotion statistic
    this.psRepo.save(promotionStatistic);

    // 4. update promotion record
    return prRepo.findById(id).map(
        record -> {
          record.setPromoter(promotionRecord.getPromoter());
          record.setSymbol(promotionRecord.getSymbol());
          record.setAbbreviation(promotionRecord.getAbbreviation());
          record.setIndustry(promotionRecord.getIndustry());
          record.setDirection(promotionRecord.getDirection());
          record.setOpenTime(promotionRecord.getOpenTime());
          record.setOpenPrice(promotionRecord.getOpenPrice());
          record.setCloseTime(promotionRecord.getCloseTime());
          record.setClosePrice(promotionRecord.getClosePrice());
          record.setAdjustFactor(promotionRecord.getAdjustFactor());
          record.setPerformanceScore(promotionRecord.getPerformanceScore());
          record.setPromotionPact(promotionRecord.getPromotionPact());
          record.setIsArchived(promotionRecord.getIsArchived());
          // calculate earnings yield
          record.setEarningsYield();
          return prRepo.save(record);
        });
  }

  @Transactional(rollbackFor = Exception.class)
  public void deletePromotionRecord(int id) {

    PromotionRecord promotionRecord = prRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("PromotionRecord %d not found", id)));

    String promotionPactName = promotionRecord.getPromotionPact().getName();
    String promoterEmail = promotionRecord.getPromoter().getEmail();

    // 0. fetch promotion statistic, create one if not exist
    PromotionStatistic promotionStatistic = this.psRepo
        .findByPromotionPactNameAndPromoterEmail(promotionPactName, promoterEmail)
        .orElse(new PromotionStatistic());

    // 1. fetch all promotion records by promotion pact name and promoter email
    List<PromotionRecord> relativePromotionRecord = this.prRepo
        .findByPromotionPactNameAndPromoterEmail(promotionPactName, promoterEmail);

    // 2. calculation
    promotionStatistic = PromotionCalculationHelper.affectPromotionStatistic(
        PromotionCalculationHelper.AffectPromotionStatisticType.DELETE,
        promotionRecord,
        promotionStatistic,
        relativePromotionRecord);

    // 3. save to promotion statistic
    this.psRepo.save(promotionStatistic);

    // 4. delete promotion record
    prRepo.deleteById(id);
  }

  public long countPromotionRecords() {
    return prRepo.count();
  }

}
