package com.raul.forumhub.user.controller;

import com.raul.forumhub.user.domain.Profile;
import com.raul.forumhub.user.dto.request.UserCreateDTO;
import com.raul.forumhub.user.dto.request.UserUpdateDTO;
import com.raul.forumhub.user.dto.response.HttpStatusMessage;
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
import java.util.Optional;

@RestController
@RequestMapping("/forumhub.io/api/v1/users")
public class UserController {

    private final UserService userService;

    private static final String USER_ID = "user_id";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserDetailedInfo> registerUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {

        UserDetailedInfo userDetailedInfo = this.userService.registerUser(userCreateDTO);

        return new ResponseEntity<>(userDetailedInfo, HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('MOD', 'ADM') or hasAuthority('SCOPE_myuser:read')")
    @GetMapping("/detailed-info")
    public ResponseEntity<UserDetailedInfo> getDetailedInfoUser(@RequestParam(required = false) Long user_id, @AuthenticationPrincipal Jwt jwt) {

        Profile.ProfileName claimUserRole = this.extractUserRoleClaim(jwt);

        Long claimUserId = Long.parseLong(jwt.getClaim(USER_ID));

        boolean isADM = claimUserRole.equals(Profile.ProfileName.ADM);
        boolean isMOD = claimUserRole.equals(Profile.ProfileName.MOD);
        boolean isBASIC = claimUserRole.equals(Profile.ProfileName.BASIC);

        Optional<UserDetailedInfo> userDetailedInfo =
                isBASIC && user_id == null ? Optional.of(this.userService.getDetailedInfoUser(claimUserId)) :
                        isADM || isMOD ? Optional.of(this.userService.getDetailedInfoUser(Objects.requireNonNullElse(user_id, claimUserId))) :
                                Optional.empty();

        if (userDetailedInfo.isPresent()) {
            return ResponseEntity.ok(userDetailedInfo.get());
        }
        throw raiseMalFormatedParamUserException();


    }

    @IsAuthenticated
    @GetMapping("/summary-info")
    public ResponseEntity<UserSummaryInfo> getSummaryInfoUser(@RequestParam Long user_id) {
        return ResponseEntity.ok(new UserSummaryInfo(this.userService.getUserById(user_id)));
    }


    @PreAuthorize("hasAnyRole('MOD','ADM') and hasAuthority('SCOPE_user:readAll')")
    @GetMapping("/listAll")
    public PagedModel<EntityModel<UserSummaryInfo>> usersList(@PageableDefault Pageable pageable,
                                                              PagedResourcesAssembler<UserSummaryInfo> assembler) {

        return assembler.toModel(userService.usersList(pageable));
    }


    @PreAuthorize("hasRole('ADM') or hasAuthority('SCOPE_myuser:edit')")
    @PutMapping("/edit")
    public ResponseEntity<UserDetailedInfo> updateUser(@RequestParam(required = false) Long user_id, @Valid @RequestBody UserUpdateDTO userUpdateDTO,
                                                       @AuthenticationPrincipal Jwt jwt) {

        Profile.ProfileName claimUserRole = this.extractUserRoleClaim(jwt);

        Long claimUserId = Long.parseLong(jwt.getClaim(USER_ID));

        boolean isADM = claimUserRole.equals(Profile.ProfileName.ADM);
        boolean isMOD = claimUserRole.equals(Profile.ProfileName.MOD);
        boolean isBASIC = claimUserRole.equals(Profile.ProfileName.BASIC);


        Optional<UserDetailedInfo> userDetailedInfo =
                (isBASIC || isMOD) && user_id == null ? Optional.of(this.userService.updateUser(claimUserId,
                        claimUserRole, userUpdateDTO)) :
                        isADM ? Optional.of(this.userService.updateUser(Objects.requireNonNullElse(user_id, claimUserId),
                                claimUserRole, userUpdateDTO)) : Optional.empty();

        if (userDetailedInfo.isPresent()) {
            return ResponseEntity.ok(userDetailedInfo.get());
        }
        throw raiseMalFormatedParamUserException();
    }


    @PreAuthorize("hasRole('ADM') or hasAuthority('SCOPE_myuser:delete')")
    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatusMessage> deleteUser(@RequestParam(required = false) Long user_id, @AuthenticationPrincipal Jwt jwt) {

        Profile.ProfileName claimUserRole = this.extractUserRoleClaim(jwt);

        Long claimUserId = Long.parseLong(jwt.getClaim(USER_ID));

        boolean isBASIC = claimUserRole.equals(Profile.ProfileName.BASIC);
        boolean isMOD = claimUserRole.equals(Profile.ProfileName.MOD);
        boolean isADM = claimUserRole.equals(Profile.ProfileName.ADM);


        if ((isBASIC || isMOD) && user_id == null) {
            this.userService.deleteUser(claimUserId);
            return ResponseEntity.ok(new HttpStatusMessage("HttpStatusCode OK"));
        } else if (isADM) {
            this.userService.deleteUser(Objects.requireNonNullElse(user_id, claimUserId));
            return ResponseEntity.ok(new HttpStatusMessage("HttpStatusCode OK"));
        }
        throw this.raiseMalFormatedParamUserException();


    }

    private Profile.ProfileName extractUserRoleClaim(Jwt jwt) {
        return Enum.valueOf(Profile.ProfileName.class, jwt.getClaim("authority").toString().substring(5));
    }

    private MalFormatedParamUserException raiseMalFormatedParamUserException() {
        return new MalFormatedParamUserException("Parâmetro 'user_id' fornecido não esperado");
    }

}
