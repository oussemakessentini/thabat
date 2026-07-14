package com.thabat.quran.reference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuranJuzRepository extends JpaRepository<QuranJuz, Integer> {

    List<QuranJuz> findAllByOrderByJuzNumberAsc();
}
