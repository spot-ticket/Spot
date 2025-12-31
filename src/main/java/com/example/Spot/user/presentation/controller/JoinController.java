<<<<<<<< HEAD:src/main/java/com/example/Spot/user/presentation/controller/JoinController.java
package com.example.Spot.user.presentation.controller;

import com.example.Spot.user.presentation.dto.request.JoinDTO;
import com.example.Spot.user.application.service.JoinService;
========
package com.example.Spot.domain.user.presentation.controller;

import com.example.Spot.domain.user.presentation.dto.request.JoinDTO;
import com.example.Spot.domain.user.application.service.JoinService;
>>>>>>>> 7404372 (chore(#0): 작업 중 상태 저장):src/main/java/com/example/Spot/domain/user/presentation/controller/JoinController.java
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class JoinController {


    private final JoinService joinService;

    public JoinController(JoinService joinService) {

        this.joinService = joinService;
    }

    @PostMapping("/join")
    public String joinProcess(JoinDTO joinDTO) {

        System.out.println(joinDTO.getUsername());
        joinService.joinProcess(joinDTO);

        return "ok";
    }
}
