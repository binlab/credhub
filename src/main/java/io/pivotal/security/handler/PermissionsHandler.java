package io.pivotal.security.handler;

import io.pivotal.security.auth.UserContext;
import io.pivotal.security.data.CredentialDataService;
import io.pivotal.security.entity.Credential;
import io.pivotal.security.exceptions.EntryNotFoundException;
import io.pivotal.security.exceptions.InvalidAclOperationException;
import io.pivotal.security.request.PermissionEntry;
import io.pivotal.security.service.PermissionCheckingService;
import io.pivotal.security.service.PermissionService;
import io.pivotal.security.view.PermissionsView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionsHandler {

  private final PermissionService permissionService;
  private final PermissionCheckingService permissionCheckingService;
  private final CredentialDataService credentialDataService;

  @Autowired
  PermissionsHandler(
      PermissionService permissionService,
      PermissionCheckingService permissionCheckingService,
      CredentialDataService credentialDataService
  ) {
    this.permissionService = permissionService;
    this.permissionCheckingService = permissionCheckingService;
    this.credentialDataService = credentialDataService;
  }

  public PermissionsView getPermissions(String name, UserContext userContext) {
    final Credential credential = credentialDataService.findOrThrow(name);

    return new PermissionsView(
        credential.getName(),
        permissionService.getAccessControlList(userContext, credential)
    );
  }

  public PermissionsView setPermissions(
      String name,
      UserContext userContext,
      List<PermissionEntry> permissionEntryList
  ) {
    final Credential credential = credentialDataService.find(name);

    // We need to verify that the credential exists in case ACL enforcement is off
    if (credential == null) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }

    for (PermissionEntry permissionEntry : permissionEntryList) {
      if (!permissionCheckingService.userAllowedToOperateOnActor(userContext, permissionEntry.getActor())) {
        throw new InvalidAclOperationException("error.acl.invalid_update_operation");
      }
    }

    permissionService.saveAccessControlEntries(userContext, credential, permissionEntryList);

    return new PermissionsView(
        credential.getName(),
        permissionService.getAccessControlList(userContext, credential)
    );
  }

  public void deletePermissionEntry(UserContext userContext,
      String credentialName, String actor) {

    boolean successfullyDeleted = permissionService
        .deleteAccessControlEntry(userContext, credentialName, actor);

    if (!successfullyDeleted) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }
  }
}
