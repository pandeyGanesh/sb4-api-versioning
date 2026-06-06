package in.ganeshpandey.sb4_api_versioning.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(version = "1.0")
    public List<UserV1> getUsers() {
        return userRepository.getAll().stream()
                .map(User::toV1)
                .collect(Collectors.toList());
    }

    @GetMapping(version = "2.0")
    public List<UserV2> getUsersV2() {
        return userRepository.getAll().stream()
                .map(User::toV2)
                .collect(Collectors.toList());
    }
}
