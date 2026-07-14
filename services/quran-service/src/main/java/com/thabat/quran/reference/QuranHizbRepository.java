package com.thabat.quran.reference;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuranHizbRepository extends JpaRepository<QuranHizb, Integer> {

    List<QuranHizb> findAllByOrderByHizbNumberAsc();
}
