package minhacestinha.api.persistence.mapper;

import minhacestinha.api.dto.response.UserResponse;
import minhacestinha.api.persistence.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getNome(), user.getEmail(), Boolean.TRUE.equals(user.getCompartilharPrecos()));
    }
}
