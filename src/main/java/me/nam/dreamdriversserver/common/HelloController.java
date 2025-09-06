// src/main/java/me/nam/dreamdriversserver/common/HelloController.java
package me.nam.dreamdriversserver.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // 루트(/)로 들어와도 200 주기
    @GetMapping("/")
    public String root() {
        return "OK";
    }
}