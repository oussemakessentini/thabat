package com.thabat.quran.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "quran_hizb")
public class QuranHizb {

    @Id
    @Column(name = "hizb_number")
    private int hizbNumber;

    @Column(name = "juz_number", nullable = false)
    private int juzNumber;

    @Column(name = "start_page", nullable = false)
    private int startPage;

    @Column(name = "end_page", nullable = false)
    private int endPage;
}
