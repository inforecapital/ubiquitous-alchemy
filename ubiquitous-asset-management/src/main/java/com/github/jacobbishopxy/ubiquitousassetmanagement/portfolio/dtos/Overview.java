/**
 * Created by Jacob Xie on 3/17/2022.
 */

package com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.dtos;

import java.time.LocalDate;

import com.github.jacobbishopxy.ubiquitousassetmanagement.Constants;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.models.AdjustmentRecord;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.models.Pact;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.models.Performance;

import com.fasterxml.jackson.annotation.JsonFormat;

public record Overview(
		int adjustmentRecordId,
		String industryName,
		String promoterName,
		String alias,
		Float portfolioEarningsYield,
		Float benchmarkEarningsYield,
		Float alpha,
		@JsonFormat(pattern = Constants.DATE_FORMAT) LocalDate adjustDate,
		int adjustVersion) {

	public static Overview fromPactAndPerformance(
			Pact pact,
			Performance performance) {

		AdjustmentRecord ar = performance.getAdjustmentRecord();

		return new Overview(
				ar.getId(),
				pact.getIndustryInfo().getName(),
				pact.getPromoter().getNickname(),
				pact.getAlias(),
				performance.getPortfolioEarningsYield(),
				performance.getBenchmarkEarningsYield(),
				performance.getAlpha(),
				ar.getAdjustDate(),
				ar.getAdjustVersion());
	}

}