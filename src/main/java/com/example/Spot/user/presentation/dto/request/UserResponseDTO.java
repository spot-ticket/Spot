<<<<<<<< HEAD:src/main/java/com/example/Spot/user/presentation/dto/request/UserResponseDTO.java
package com.example.Spot.user.presentation.dto.request;
========
package com.example.Spot.domain.user.presentation.dto.request;
>>>>>>>> 7404372 (chore(#0): 작업 중 상태 저장):src/main/java/com/example/Spot/domain/user/presentation/dto/request/UserResponseDTO.java

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDTO {
    private int id;
    private String username;
    private String role;
}
