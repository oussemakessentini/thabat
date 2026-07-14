package com.thabat.quran.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "quran_page_surah_range")
public class QuranPageSurahRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Column(name = "surah_number", nullable = false)
    private int surahNumber;

    @Column(name = "start_ayah", nullable = false)
    private int startAyah;

    @Column(name = "end_ayah", nullable = false)
    private int endAyah;
}
