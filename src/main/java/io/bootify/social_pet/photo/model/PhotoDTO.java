package io.bootify.social_pet.photo.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


@Getter
@Setter
public class PhotoDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    private String photoUrl;

    private Integer user;

}
