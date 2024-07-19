package com.example.IotController.domain.Device.model;

import com.example.IotController.domain.User.model.Users;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor
public class Plugs {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private Users user;

    private Boolean status; // 지금 장치가 사용중인지 아닌지

    private Boolean mode; // 자동 제어 모드

    private Long time; // 자동 제어 기준 시간

    @Builder
    public Plugs(String name, Users user, Long time) {
        this.name = name;
        this.user = user;
        this.status = false;
        this.mode = false;
        this.time = time;
    }
}
