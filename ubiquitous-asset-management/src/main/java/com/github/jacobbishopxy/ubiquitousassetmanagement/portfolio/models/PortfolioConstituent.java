/**
 * Created by Jacob Xie on 2/27/2022.
 */

package com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.models;

import java.time.LocalDate;

import com.github.jacobbishopxy.ubiquitousassetmanagement.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "portfolio_constituent")
public class PortfolioConstituent {
  // =======================================================================
  // Fields
  // =======================================================================

  @Id
  @Column(columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "portfolio_adjustment_record_id")
  @NotEmpty
  @Schema(description = "This portfolio record belongs to a specific portfolio pact's adjustment record.", required = true)
  private PortfolioAdjustmentRecord portfolioAdjustmentRecord;

  @JsonFormat(pattern = Constants.DATE_FORMAT)
  @Column(nullable = false, columnDefinition = "DATE")
  @NotEmpty
  @Schema(description = "The date of adjustment.", required = true)
  private LocalDate adjustDate;

  @Column(nullable = false)
  @NotEmpty
  @Schema(description = "The symbol code.", required = true)
  private String symbol;

  @Schema(description = "The symbol abbreviation.", required = true)
  private String abbreviation;

  @Column(nullable = false)
  @NotEmpty
  private Float adjustDatePrice;

  @Column(nullable = false)
  @NotEmpty
  private Float currentPrice;

  @Column(nullable = false)
  @NotEmpty
  private Float adjustDateFactor;

  @Column(nullable = false)
  @NotEmpty
  private Float currentFactor;

  @Column(nullable = false)
  @NotEmpty
  @Schema(description = "The weight of this portfolio record.", required = true)
  private Float adjustDateWeight;

  @Column(nullable = false)
  @NotEmpty
  @Schema(description = "The weight of this portfolio record.", required = true)
  private Float currentWeight;

  @Column(nullable = false)
  @NotEmpty
  private Float pbpe;

  @Column(nullable = false)
  @NotEmpty
  private Float marketValue;

  @Column(nullable = false)
  @NotEmpty
  private Float earningsYield;

  // =======================================================================
  // Constructors
  // =======================================================================

  public PortfolioConstituent() {
  }

  public PortfolioConstituent(
      PortfolioAdjustmentRecord portfolioAdjustmentRecord,
      LocalDate adjustDate,
      String symbol,
      String abbreviation,
      Float adjustDatePrice,
      Float currentPrice,
      Float adjustDateFactor,
      Float currentFactor,
      Float adjustDateWeight,
      Float currentWeight,
      Float pbpe,
      Float marketValue,
      Float earningsYield,
      int version) {
    super();
    this.portfolioAdjustmentRecord = portfolioAdjustmentRecord;
    this.adjustDate = adjustDate;
    this.symbol = symbol;
    this.abbreviation = abbreviation;
    this.adjustDatePrice = adjustDatePrice;
    this.currentPrice = currentPrice;
    this.adjustDateFactor = adjustDateFactor;
    this.currentFactor = currentFactor;
    this.adjustDateWeight = adjustDateWeight;
    this.currentWeight = currentWeight;
    this.pbpe = pbpe;
    this.marketValue = marketValue;
    this.earningsYield = earningsYield;
  }

  // =======================================================================
  // Accessors
  // =======================================================================

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PortfolioAdjustmentRecord getPortfolioAdjustmentRecord() {
    return portfolioAdjustmentRecord;
  }

  public void setPortfolioAdjustmentRecord(PortfolioAdjustmentRecord portfolioAdjustmentRecord) {
    this.portfolioAdjustmentRecord = portfolioAdjustmentRecord;
  }

  public LocalDate getAdjustDate() {
    return adjustDate;
  }

  public void setAdjustDate(LocalDate adjustDate) {
    this.adjustDate = adjustDate;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  public Float getAdjustDatePrice() {
    return adjustDatePrice;
  }

  public void setAdjustDatePrice(Float adjustDatePrice) {
    this.adjustDatePrice = adjustDatePrice;
  }

  public Float getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(Float currentPrice) {
    this.currentPrice = currentPrice;
  }

  public Float getAdjustDateFactor() {
    return adjustDateFactor;
  }

  public void setAdjustDateFactor(Float adjustDateFactor) {
    this.adjustDateFactor = adjustDateFactor;
  }

  public Float getCurrentFactor() {
    return currentFactor;
  }

  public void setCurrentFactor(Float currentFactor) {
    this.currentFactor = currentFactor;
  }

  public Float getAdjustDateWeight() {
    return adjustDateWeight;
  }

  public void setAdjustDateWeight(Float adjustDateWeight) {
    this.adjustDateWeight = adjustDateWeight;
  }

  public Float getCurrentWeight() {
    return currentWeight;
  }

  public void setCurrentWeight(Float currentWeight) {
    this.currentWeight = currentWeight;
  }

  public Float getPbpe() {
    return pbpe;
  }

  public void setPbpe(Float pbpe) {
    this.pbpe = pbpe;
  }

  public Float getMarketValue() {
    return marketValue;
  }

  public void setMarketValue(Float marketValue) {
    this.marketValue = marketValue;
  }

  public Float getEarningsYield() {
    return earningsYield;
  }

  public void setEarningsYield(Float earningsYield) {
    this.earningsYield = earningsYield;
  }

}
