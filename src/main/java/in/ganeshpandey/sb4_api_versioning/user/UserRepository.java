package in.ganeshpandey.sb4_api_versioning.user;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository {
    private final List<User> users = new ArrayList<>();

    @PostConstruct
    public void init() {
        users.add(new User(UUID.randomUUID(), "Alice Smith", 28, BloodGroup.O_POSITIVE, "alice@example.com"));
        users.add(new User(UUID.randomUUID(), "Bob Jones", 34, BloodGroup.A_POSITIVE, "bob@example.com"));
    }

    public List<User> getAll() {
        return new ArrayList<>(users);
    }
}
