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

    public UserV2 toV2() {
        String[] parts = fullName != null ? fullName.split(" ", 2) : new String[]{"", ""};
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        return new UserV2(firstName, lastName, bloodGroup, email, id);
    }

    public UserV2 toV2(UUID customId) {
        String[] parts = fullName != null ? fullName.split(" ", 2) : new String[]{"", ""};
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        return new UserV2(firstName, lastName, bloodGroup, email, customId);
    }

}
