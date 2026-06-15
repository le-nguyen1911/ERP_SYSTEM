package com.ERP_SYSTEM.auth.mapper;

import com.ERP_SYSTEM.auth.dto.request.UpdateUserRequest;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.entity.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy  = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(source = "roles", target = "roles",qualifiedByName = "rolesToStrings")
    UserInfoResponse toUserInfoResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
      if (roles == null) {
        return Set.of();
      }
     return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }
}
