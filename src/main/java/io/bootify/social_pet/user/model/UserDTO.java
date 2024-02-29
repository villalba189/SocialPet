package io.bootify.social_pet.user.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserDTO {

    private Integer id;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    @Size(max = 100)
    private String nombre;

    @Size(max = 20)
    private String numeroTlf;

    @NotNull
    @Size(max = 255)
    private String contraseA;

    @NotNull
    private LocalDate fechaNacimiento;



}
