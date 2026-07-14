package com.thabat.quran.reference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuranSurahRepository extends JpaRepository<QuranSurah, Integer> {

    List<QuranSurah> findAllByOrderBySurahNumberAsc();
}
