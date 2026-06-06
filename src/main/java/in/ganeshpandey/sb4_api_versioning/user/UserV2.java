package in.ganeshpandey.sb4_api_versioning.user;

import java.util.UUID;

public record UserV2(
    String firstName,
    String lastName,
    BloodGroup bloodGroup,
    String email,
    UUID id
) {
    public User toUser() {
        String fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
        return new User(id != null ? id : UUID.randomUUID(), fullName, 0, bloodGroup, email);
    }

    public User toUser(int age) {
        String fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
        return new User(id != null ? id : UUID.randomUUID(), fullName, age, bloodGroup, email);
    }
}
