package io.elasticore.demo.crm.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The screens are one page that routes in the browser: a reload or a shared link to
 * /account/show/12 gets the page, which then shows the record. Paths with a dot (files)
 * and the API are not matched.
 */
@Controller
public class ScreenRoutes {

    @GetMapping({"/", "/login", "/signup", "/pipeline", "/assistant", "/board/{resource}", "/{resource:[a-zA-Z]+}",
            "/{resource:[a-zA-Z]+}/show/{id}"})
    public String screen() {
        return "forward:/index.html";
    }
}
