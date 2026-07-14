package com.thabat.quran.reference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuranPageSurahRangeRepository extends JpaRepository<QuranPageSurahRange, Long> {

    List<QuranPageSurahRange> findBySurahNumberOrderByPageNumberAsc(int surahNumber);

    List<QuranPageSurahRange> findAllByOrderBySurahNumberAscPageNumberAsc();
}
