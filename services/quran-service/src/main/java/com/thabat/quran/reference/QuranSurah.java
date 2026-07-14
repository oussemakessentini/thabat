package com.thabat.quran.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "quran_surah")
public class QuranSurah {

    @Id
    @Column(name = "surah_number")
    private int surahNumber;

    @Column(name = "name_arabic", nullable = false, length = 100)
    private String nameArabic;

    @Column(name = "name_english", nullable = false, length = 120)
    private String nameEnglish;

    @Column(name = "transliteration", nullable = false, length = 120)
    private String transliteration;

    @Column(name = "ayah_count", nullable = false)
    private int ayahCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "revelation_type", nullable = false, length = 16)
    private QuranRevelationType revelationType;

    @Column(name = "start_page", nullable = false)
    private int startPage;

    @Column(name = "end_page", nullable = false)
    private int endPage;
}
