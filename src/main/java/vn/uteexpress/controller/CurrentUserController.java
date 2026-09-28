package vn.uteexpress.controller;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
@RestController
public class CurrentUserController {
 private final UserRepository userRepository;
 public CurrentUserController(UserRepository userRepository){this.userRepository=userRepository;}
 @GetMapping("/api/me")
 public Map<String,Object> me(Authentication authentication){
  if(authentication==null||!authentication.isAuthenticated()) throw new IllegalArgumentException("Chưa đăng nhập");
  User u=userRepository.findByUsername(authentication.getName()).orElseThrow(()->new IllegalArgumentException("Không tìm thấy tài khoản"));
  Map<String,Object> r=new LinkedHashMap<>(); r.put("id",u.getId()); r.put("username",u.getUsername()); r.put("email",u.getEmail()); r.put("fullName",u.getFullName()); r.put("phone",u.getPhone()); r.put("enabled",u.isEnabled()); if(u.getRole()!=null){Map<String,Object> role=new LinkedHashMap<>();role.put("id",u.getRole().getId());role.put("name",u.getRole().getName());r.put("role",role);} return r;
 }
}
