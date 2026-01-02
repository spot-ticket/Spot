package com.example.Spot.menu.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OriginEntityTest {

    @Test
    @DisplayName("예외: 빌더를 통해 객체를 생성할 때 둘 다 빈 값이면 에러가 발생한다")
    void 생성자_정상_테스트() {

        assertThatThrownBy(() -> OriginEntity.builder()
                .originName(null)
                .ingredientName("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("적어도 하나는 입력되어야 합니다.");
    }

    @Test
    @DisplayName("성공: 재료 이름만 넣고 원산지는 null이어도 객체 생성이 가능하다")
    void 재료만_입력_정상_테스트() {

        OriginEntity origin = OriginEntity.builder()
                .ingredientName("닭고기")
                .build();

        assertThat(origin.getIngredientName()).isEqualTo("닭고기");
        assertThat(origin.getOriginName()).isNull();
    }

}
