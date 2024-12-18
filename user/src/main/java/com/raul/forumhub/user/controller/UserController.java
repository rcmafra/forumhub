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
        return new ResponseEntity<>(new HttpMessageDefault("HttpStatusCode OK"), HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('MOD', 'ADM') or hasAuthority('SCOPE_myuser:read')")
    @GetMapping("/detailed-info")
    public ResponseEntity<UserDetailedInfo> getDetailedInfoUser(@RequestParam(required = false) Long user_id, @AuthenticationPrincipal Jwt jwt) {

        String claimUserRole = jwt.getClaim("authority").toString().substring(5);
        Long claimUserId = Long.parseLong(jwt.getClaim("user_id"));

        boolean isADM = claimUserRole.equals(Profile.ProfileName.ADM.name());
        boolean isMOD = claimUserRole.equals(Profile.ProfileName.MOD.name());
        boolean isBASIC = claimUserRole.equals(Profile.ProfileName.BASIC.name());

        if (isADM || isMOD) {
            return ResponseEntity.ok(new UserDetailedInfo(this.userService.getInfoUser(Objects.requireNonNullElse(user_id, claimUserId))));
        } else if (isBASIC && Objects.isNull(user_id)) {
            return ResponseEntity.ok(new UserDetailedInfo(this.userService.getInfoUser(claimUserId)));
        } else {
            throw new MalFormatedParamUserException("Parâmetros fornecidos não esperado");
        }
    }

    @IsAuthenticated
    @GetMapping("/summary-info")
    public ResponseEntity<UserSummaryInfo> getSummaryInfoUser(@RequestParam Long user_id) {
        return ResponseEntity.ok(new UserSummaryInfo(this.userService.getInfoUser(user_id)));
    }


    @PreAuthorize("hasAnyRole('MOD','ADM') and hasAuthority('SCOPE_user:readAll')")
    @GetMapping("/listAll")
    public PagedModel<EntityModel<UserDetailedInfo>> usersList(@PageableDefault Pageable pageable,
                                                               PagedResourcesAssembler<UserDetailedInfo> assembler) {

        return assembler.toModel(userService.usersList(pageable));
    }


    @PreAuthorize("hasRole('ADM') or hasAuthority('SCOPE_myuser:edit')")
    @PutMapping("/update")
    public ResponseEntity<UserDetailedInfo> updateUser(@RequestParam(required = false) Long user_id, @Valid @RequestBody UserUpdateDTO userUpdateDTO,
                                                       @AuthenticationPrincipal Jwt jwt) {

        String claimUserRole = jwt.getClaim("authority").toString().substring(5);
        Long claimUserId = Long.parseLong(jwt.getClaim("user_id"));

        String myUserEditScope = Objects.isNull(jwt.getClaim("scope")) ? "" :
                jwt.getClaimAsStringList("scope").stream()
                        .filter(s -> s.equals("myuser:edit")).findFirst().orElse("");


        if (myUserEditScope.equals("myuser:edit") && Objects.isNull(user_id)) {
            return ResponseEntity.ok(this.userService.updateUser(claimUserId, claimUserRole, userUpdateDTO));
        } else if (claimUserRole.equals(Profile.ProfileName.ADM.name()) && Objects.nonNull(user_id)) {
            return ResponseEntity.ok(this.userService.updateUser(user_id, claimUserRole, userUpdateDTO));
        } else {
            throw new MalFormatedParamUserException("Parâmetros fornecidos não esperado");
        }
    }


    @PreAuthorize("hasRole('ADM') or hasAuthority('SCOPE_myuser:delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<HttpMessageDefault> deleteUser(@RequestParam(required = false) Long user_id, @AuthenticationPrincipal Jwt jwt) {

        String claimUserRole = jwt.getClaim("authority").toString().substring(5);
        Long claimUserId = Long.parseLong(jwt.getClaim("user_id"));
        String myUserDeleteScope = jwt.getClaimAsStringList("scope").stream()
                .filter(s -> s.equals("myuser:delete")).findFirst().orElse("");

        if (myUserDeleteScope.equals("myuser:delete") && Objects.isNull(user_id)) {
            this.userService.deleteUser(claimUserId);
        } else if (claimUserRole.equals(Profile.ProfileName.ADM.name()) && Objects.nonNull(user_id)) {
            this.userService.deleteUser(user_id);
        } else {
            throw new MalFormatedParamUserException("Parâmetros fornecidos não esperado");
        }

        return ResponseEntity.ok(new HttpMessageDefault("HttpStatusCode OK"));
    }


}
