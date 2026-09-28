package vn.uteexpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/product/list")
    public String productList() {
        return "product/list";
    }

    @GetMapping("/product/detail")
    public String productDetail(@RequestParam(required = false) Long id) {
        return "product/detail";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart/cart";
    }

    @GetMapping("/order/checkout")
    public String checkout() {
        return "order/checkout";
    }

    @GetMapping("/order/history")
    public String orderHistory() {
        return "order/history";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile/profile";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
<<<<<<< HEAD
=======

    @GetMapping("/tracking")
    public String tracking() {
        return "logistics/tracking";
    }

    @GetMapping("/shipper/dashboard")
    public String shipperDashboard() {
        return "shipper/dashboard";
    }

    @GetMapping("/admin/shipments")
    public String adminShipments() {
        return "admin/shipments";
    }

    @GetMapping("/manager/shipments")
    public String managerShipments() {
        return "admin/shipments";
    }
>>>>>>> 2b622bd (Them 1 vai frontend va khoi tao lai git do xoa nham file)
}