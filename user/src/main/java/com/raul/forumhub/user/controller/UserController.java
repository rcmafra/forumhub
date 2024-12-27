package com.raul.forumhub.user.controller;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import com.raul.forumhub.user.dto.request.UserUpdateDTO;
import com.raul.forumhub.user.dto.response.HttpMessageDefault;
import com.raul.forumhub.user.dto.response.UserDetailedInfo;
import com.raul.forumhub.user.dto.response.UserSummaryInfo;
import com.raul.forumhub.user.exception.MalFormatedParamUserException;
import com.raul.forumhub.user.security.IsAuthenticated;
import com.raul.forumhub.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api-forum/v1/forumhub/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<HttpMessageDefault> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {

        this.userService.createUser(userCreateDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('MOD', 'ADM') or hasAuthority('SCOPE_myuser:read')")
    @GetMapping("/detailed-info")
    public ResponseEntity<UserDetailedInfo> getDetailedInfoUser(@RequestParam(required = false) Long user_id, @AuthenticationPrincipal Jwt jwt) {

        Profile.ProfileName claimUserRole =
                Enum.valueOf(Profile.ProfileName.class, jwt.getClaim("authority").toString().substring(5));

        Long claimUserId = Long.parseLong(jwt.getClaim("user_id"));

        boolean isADM = claimUserRole.equals(Profile.ProfileName.ADM);
        boolean isMOD = claimUserRole.equals(Profile.ProfileName.MOD);
        boolean isBASIC = claimUserRole.equals(Profile.ProfileName.BASIC);

        if (isADM || isMOD) {
            return ResponseEntity.ok(new UserDetailedInfo(this.userService.getDetailedInfoUser(Objects.requireNonNullElse(user_id, claimUserId))));
        } else if (isBASIC && Objects.isNull(user_id)) {
            return ResponseEntity.ok(new UserDetailedInfo(this.userService.getDetailedInfoUser(claimUserId)));
        }
        throw new MalFormatedParamUserException("Parâmetros fornecidos não esperado");
    }

    @IsAuthenticated
    @GetMapping("/summary-info")
    public ResponseEntity<UserSummaryInfo> getSummaryInfoUser(@RequestParam Long user_id) {
        return ResponseEntity.ok(new UserSummaryInfo(this.userService.getDetailedInfoUser(user_id)));
    }


    @PreAuthorize("hasAnyRole('MOD','ADM') and hasAuthority('SCOPE_user:readAll')")
    @GetMapping("/listAll")
    public PagedModel<EntityModel<UserSummaryInfo>> usersList(@PageableDefault Pageable pageable,
                                                              PagedResourcesAssembler<UserSummaryInfo> assembler) {

        return assembler.toModel(userService.usersList(pageable));
    }


    @PreAuthorize("hasRole('ADM') or hasAuthority('SCOPE_myuser:edit')")
    @PutMapping("/update")
    public ResponseEntity<UserDetailedInfo> updateUser(@RequestParam(required = false) Long user_id, @Valid @RequestBody UserUpdateDTO userUpdateDTO,
                                                       @AuthenticationPrincipal Jwt jwt) {

        Profile.ProfileName claimUserRole =
                Enum.valueOf(Profile.ProfileName.class, jwt.getClaim("authority").toString().substring(5));

        Long claimUserId = Long.parseLong(jwt.getClaim("user_id"));

        boolean isADM = claimUserRole.equals(Profile.ProfileName.ADM);
        boolean isMOD = claimUserRole.equals(Profile.ProfileName.MOD);
        boolean isBASIC = claimUserRole.equals(Profile.ProfileName.BASIC);


        if (isADM) {
            return ResponseEntity.ok(this.userService.updateUser(Objects.requireNonNullElse(user_id, claimUserId), claimUserRole, userUpdateDTO));
        } else if ((isBASIC || isMOD) && Objects.isNull(user_id)) {
            return ResponseEntity.ok(this.userService.updateUser(claimUserId, claimUserRole, userUpdateDTO));
        }
        throw new MalFormatedParamUserException("Parâmetros fornecidos não esperado");
    }


    @PreAuthorize("hasRole('ADM') or hasAuthority('SCOPE_myuser:delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<HttpMessageDefault> deleteUser(@RequestParam(required = false) Long user_id, @AuthenticationPrincipal Jwt jwt) {

        Profile.ProfileName claimUserRole =
                Enum.valueOf(Profile.ProfileName.class, jwt.getClaim("authority")
                        .toString().substring(5));

        Long claimUserId = Long.parseLong(jwt.getClaim("user_id"));

        boolean isBASIC = claimUserRole.equals(Profile.ProfileName.BASIC);
        boolean isMOD = claimUserRole.equals(Profile.ProfileName.MOD);
        boolean isADM = claimUserRole.equals(Profile.ProfileName.ADM);


        if (isADM) {
            this.userService.deleteUser(Objects.requireNonNullElse(user_id, claimUserId));
            return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
        } else if ((isBASIC || isMOD) && Objects.isNull(user_id)) {
            this.userService.deleteUser(claimUserId);
            return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
        }
        throw new MalFormatedParamUserException("Parâmetros fornecidos não esperado");


    }


}
