package com.thabat.quran.reference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "quran_page_ref")
public class QuranPageRef {

    @Id
    @Column(name = "page_number")
    private int pageNumber;

    @Column(name = "juz_number", nullable = false)
    private int juzNumber;

    @Column(name = "hizb_number", nullable = false)
    private int hizbNumber;
}
