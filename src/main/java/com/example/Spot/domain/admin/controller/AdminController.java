<<<<<<<< HEAD:src/main/java/com/example/Spot/domain/user/controller/AdminController.java
package com.example.Spot.domain.user.controller;
========
package com.example.Spot.domain.admin.controller;
>>>>>>>> e85835b (refactor: login/user 도메인 분리 및 인증 구조 정리):src/main/java/com/example/Spot/domain/admin/controller/AdminController.java

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class AdminController {

    @GetMapping("/admin")
    public String adminP() {

        return "admin Controller";
    }
}
