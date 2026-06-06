package in.ganeshpandey.sb4_api_versioning.user;

import java.util.UUID;

public record User(
    UUID id,
    String fullName,
    int age,
    BloodGroup bloodGroup,
    String email
) {
    public UserV1 toV1() {
        return new UserV1(fullName, age, bloodGroup, email);
    }
}
