package lk.kolitha.dana.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisteredDonorDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String nic;
    private String password;
}
