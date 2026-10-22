package ru.stickoroch.mineshop.web.minecraft;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MinecraftProfileServiceTest {

    @Test
    void findsExistingProfile() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mojang.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MinecraftProfileService service = new MinecraftProfileService(builder.build(), true);

        server.expect(requestTo("https://api.mojang.com/users/profiles/minecraft/Notch"))
                .andRespond(withSuccess(
                        "{\"id\":\"069a79f444e94726a5befca90e38aaf5\",\"name\":\"Notch\"}",
                        MediaType.APPLICATION_JSON
                ));

        Optional<MinecraftProfile> profile = service.findProfile("Notch");

        assertThat(profile).isPresent();
        assertThat(profile.get().name()).isEqualTo("Notch");
        server.verify();
    }

    @Test
    void treatsNoContentAsUnknownProfile() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mojang.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MinecraftProfileService service = new MinecraftProfileService(builder.build(), true);

        server.expect(requestTo("https://api.mojang.com/users/profiles/minecraft/Nobody"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertThat(service.findProfile("Nobody")).isEmpty();
        server.verify();
    }

    @Test
    void treatsNotFoundAsUnknownProfile() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mojang.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MinecraftProfileService service = new MinecraftProfileService(builder.build(), true);

        server.expect(requestTo("https://api.mojang.com/users/profiles/minecraft/Nobody"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(service.findProfile("Nobody")).isEmpty();
        server.verify();
    }
}
