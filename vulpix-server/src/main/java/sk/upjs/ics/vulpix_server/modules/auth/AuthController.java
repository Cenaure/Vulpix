package sk.upjs.ics.vulpix_server.modules.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/")
    public String getUsers() {
        return "some user";
    }

}
