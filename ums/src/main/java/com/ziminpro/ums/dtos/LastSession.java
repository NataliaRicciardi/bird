package com.ziminpro.ums.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "last_visit")
public class LastSession {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private byte[] id;

    @Column(name = "in")
    int lastLoginTimeStamp;
    @Column(name = "out")
    int lastLogoutTimeStamp;

    public LastSession(int lastLoginTimeStamp, int lastLogoutTimeStamp) {
        this.lastLoginTimeStamp = lastLoginTimeStamp;
        this.lastLogoutTimeStamp = lastLogoutTimeStamp;
    }
}
