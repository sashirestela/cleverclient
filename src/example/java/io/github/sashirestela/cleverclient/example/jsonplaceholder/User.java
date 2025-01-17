package io.github.sashirestela.cleverclient.example.jsonplaceholder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User {

    private Integer id;
    private String name;
    private String username;
    private String email;
    private String address;
    private String phone;
    private String website;
    private String company;

}
