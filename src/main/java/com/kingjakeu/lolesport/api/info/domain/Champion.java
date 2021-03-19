package com.kingjakeu.lolesport.api.info.domain;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CHAMPION")
public class Champion {

    @Id
    @Column(name = "ID", length = 50)
    private String id;

    @Column(name = "CRAWL_KEY", length = 20)
    private String crawlKey;

    @Column(name = "NAME", length = 50)
    private String name;

    @Column(name = "PATCH_VER", length = 20)
    private String patchVersion;

    @CreationTimestamp
    @Column(name = "CREATE_DTM", nullable = false, updatable = false, columnDefinition = "timestamp")
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    @Column(name = "UPDATE_DTM", nullable = false, columnDefinition = "timestamp")
    private LocalDateTime updateDateTime;
}
