package in.ganeshpandey.sb4_api_versioning.user;

import java.util.UUID;

public record UserV1(
    String fullName,
    int age,
    BloodGroup bloodGroup,
    String email
) {
    public User toUser() {
        return new User(UUID.randomUUID(), fullName, age, bloodGroup, email);
    }

    public User toUser(UUID id) {
        return new User(id, fullName, age, bloodGroup, email);
    }
}
