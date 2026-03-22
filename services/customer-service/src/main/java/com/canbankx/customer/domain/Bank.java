package com.canbankx.customer.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bnks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nm;

    @Column(nullable = false)
    private String aUrl;

    @Column(nullable = false)
    private String k;

    @Column(nullable = false)
    private String st;

    private String ce;

    public Bank(Integer id, String nm, String aUrl, String k) {
        this.id = id;
        this.nm = nm;
        this.aUrl = aUrl;
        this.k = k;
        this.st = "ACTIVE";
    }
}
