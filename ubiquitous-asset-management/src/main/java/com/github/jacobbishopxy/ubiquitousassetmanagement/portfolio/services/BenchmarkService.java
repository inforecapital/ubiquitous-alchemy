/**
 * Created by Jacob Xie on 3/11/2022.
 */

package com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.models.AdjustmentRecord;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.models.Benchmark;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.models.Performance;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.repositories.BenchmarkRepository;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.repositories.PerformanceRepository;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.services.helper.PortfolioCalculationHelper;
import com.github.jacobbishopxy.ubiquitousassetmanagement.portfolio.services.helper.PortfolioCalculationHelper.BenchmarksResult;
import com.google.common.collect.Sets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BenchmarkService {

  @Autowired
  private BenchmarkRepository bRepo;

  @Autowired
  private PerformanceRepository pRepo;

  // =======================================================================
  // Query methods
  //
  // expose to controller
  // =======================================================================

  public List<Benchmark> getBenchmarksByAdjustmentRecordId(Long adjustmentRecordId) {
    return bRepo.findByAdjustmentRecordId(adjustmentRecordId);
  }

  public List<Benchmark> getBenchmarksByAdjustmentRecordIds(List<Long> adjustmentRecordIds) {
    return bRepo.findByAdjustmentRecordIdIn(adjustmentRecordIds);
  }

  public Optional<Benchmark> getBenchmarkById(Long id) {
    return bRepo.findById(id);
  }

  // =======================================================================
  // Mutation methods
  //
  // called by internal services
  // =======================================================================

  @Transactional(rollbackFor = Exception.class)
  private void rawMutation(List<Benchmark> benchmarks) {
    // 0. benchmarks cannot be empty
    if (benchmarks.isEmpty()) {
      throw new IllegalArgumentException("Benchmarks cannot be empty");
    }

    // 1. get adjustment record id
    Long adjustmentRecordId = benchmarks.get(0).getAdjRecordId();

    // 2. recalculate all benchmarks and their related performance
    BenchmarksResult res = PortfolioCalculationHelper
        .modifyBenchmarksAndCalculateBenchmarkEarningsYield(benchmarks);

    // 3. update all benchmarks' dynamic weight
    bRepo.saveAll(res.benchmarks());

    // 4. update or create performance
    Performance performance = pRepo
        .findByAdjustmentRecordId(adjustmentRecordId)
        .orElse(new Performance());
    performance.setAdjustmentRecord(new AdjustmentRecord(adjustmentRecordId));
    performance.setBenchmarkEarningsYield(res.earningsYield());
    performance.setAlpha(performance.getPortfolioEarningsYield() - res.earningsYield());
    pRepo.save(performance);
  }

  // common mutations is a wrapper of raw mutation, which only needs adjustment
  // record id as input
  @Transactional(rollbackFor = Exception.class)
  private void commonMutation(Long adjustmentRecordId) {
    // find all benchmarks in the portfolio
    List<Benchmark> benchmarks = getBenchmarksByAdjustmentRecordId(adjustmentRecordId);

    rawMutation(benchmarks);
  }

  @Transactional(rollbackFor = Exception.class)
  public Benchmark createBenchmark(Benchmark benchmark) {
    // 0. validate benchmark
    // IMPORTANT: benchmark's adjustmentRecord id cannot be null. In other words,
    // it must have an adjustmentRecord to create a benchmark.
    Long adjustmentRecordId = benchmark.getAdjRecordId();

    // 1. save benchmark.
    Benchmark newB = bRepo.save(benchmark);

    // 2. common mutation
    commonMutation(adjustmentRecordId);

    return bRepo
        .findById(newB.getId())
        .orElseThrow(() -> new RuntimeException(
            String.format("Constituent %d not found", newB.getId())));
  }

  @Transactional(rollbackFor = Exception.class)
  public List<Benchmark> createBenchmarks(List<Benchmark> benchmarks) {
    // 0. validate benchmarks
    List<Long> adjustmentRecordIds = benchmarks
        .stream()
        .map(Benchmark::getAdjRecordId)
        .collect(Collectors.toList());

    Set<Long> uniqueARIds = Sets.newHashSet(adjustmentRecordIds);
    if (uniqueARIds.size() != 1) {
      throw new IllegalArgumentException("All benchmarks must have the same adjustment record id");
    }

    // 1. save benchmarks
    List<Benchmark> newBs = bRepo.saveAll(benchmarks);

    // 2. raw mutation
    rawMutation(newBs);

    return newBs;
  }

  @Transactional(rollbackFor = Exception.class)
  public Optional<Benchmark> updateBenchmark(Long id, Benchmark benchmark) {
    // 0. validate benchmark
    Long adjustmentRecordId = benchmark.getAdjRecordId();

    // 1. update benchmark
    bRepo
        .findById(id)
        .map(
            record -> {
              record.setBenchmarkName(benchmark.getBenchmarkName());
              record.setSymbol(benchmark.getSymbol());
              record.setPercentageChange(benchmark.getPercentageChange());
              record.setStaticWeight(benchmark.getStaticWeight());
              return bRepo.save(record);
            })
        .orElseThrow(() -> new RuntimeException(
            String.format("Benchmark %d not found", id)));

    // 2. common mutation
    commonMutation(adjustmentRecordId);

    return bRepo.findById(id);
  }

  @Transactional(rollbackFor = Exception.class)
  public List<Benchmark> updateBenchmarks(List<Benchmark> benchmarks) {
    // 0. make sure all benchmarks' id are valid
    List<Long> adjustmentRecordIds = benchmarks
        .stream()
        .map(Benchmark::getAdjRecordId)
        .collect(Collectors.toList());

    Set<Long> uniqueARIds = Sets.newHashSet(adjustmentRecordIds);
    if (uniqueARIds.size() != 1) {
      throw new IllegalArgumentException("All benchmarks must have the same adjustment record id");
    }

    // 1. only modify benchmarks that are in the database
    List<Long> ids = benchmarks
        .stream()
        .map(Benchmark::getId)
        .collect(Collectors.toList());

    List<Benchmark> newBms = bRepo
        .findAllById(ids)
        .stream()
        .map(b -> {
          // update benchmark
          b.setBenchmarkName(benchmarks.get(ids.indexOf(b.getId())).getBenchmarkName());
          b.setPercentageChange(benchmarks.get(ids.indexOf(b.getId())).getPercentageChange());
          b.setStaticWeight(benchmarks.get(ids.indexOf(b.getId())).getStaticWeight());
          return b;
        })
        .collect(Collectors.toList());

    // 2. raw mutation
    rawMutation(newBms);

    return newBms;
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteBenchmark(Long id) {
    // 0. validate benchmark
    Benchmark b = bRepo
        .findById(id)
        .orElseThrow(() -> new RuntimeException(String.format("Benchmark %d not found", id)));
    Long adjustmentRecordId = b.getAdjRecordId();

    // 1. delete benchmark
    bRepo.deleteById(id);

    // 2. common mutation
    commonMutation(adjustmentRecordId);
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteBenchmarks(List<Long> ids) {
    // 0. make sure all benchmarks' id are valid
    List<Benchmark> bms = bRepo.findAllById(ids);
    List<Long> adjustmentRecordIds = bms
        .stream()
        .map(Benchmark::getAdjRecordId)
        .collect(Collectors.toList());

    Set<Long> uniqueARIds = Sets.newHashSet(adjustmentRecordIds);
    if (uniqueARIds.size() != 1) {
      throw new IllegalArgumentException("All benchmarks must have the same adjustment record id");
    }

    // 1. delete benchmarks
    bRepo.deleteAll(bms);

    // 2. common mutation
    commonMutation(adjustmentRecordIds.get(0));
  }

  // delete all benchmarks in the portfolio
  // IMPORTANT: adjustRecord is not deleted
  @Transactional(rollbackFor = Exception.class)
  public void deleteBenchmarksByAdjustmentRecordId(Long adjustmentRecordId) {
    bRepo.deleteByAdjustmentRecordId(adjustmentRecordId);

    pRepo.deleteByAdjustmentRecordId(adjustmentRecordId);
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteBenchmarksByAdjustmentRecordIds(List<Long> adjustmentRecordIds) {
    bRepo.deleteByAdjustmentRecordIdIn(adjustmentRecordIds);

    pRepo.deleteByAdjustmentRecordIdIn(adjustmentRecordIds);
  }
}
