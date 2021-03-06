package org.nrg.xnatx.plugins.pixi.rest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.models.PDX;
import org.nrg.xnatx.plugins.pixi.services.PDXService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PDXApiTest {

    private PDXService pdxService;
    private UserManagementServiceI userManagementService;
    private RoleHolder roleHolder;
    private PDXApi pdxApi;
    private static String username = "PDXUser";

    @BeforeAll
    public static void beforeAll() {
        UserI userI = mock(UserI.class);
        when(userI.getUsername()).thenReturn(username);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userI);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @BeforeEach
    public void beforeEach() {
        pdxService = mock(PDXService.class);
        userManagementService = mock(UserManagementServiceI.class);
        roleHolder = mock(RoleHolder.class);
        pdxApi = new PDXApi(userManagementService, roleHolder, pdxService);
    }

    @Test
    public void testGetPDXs() {
        PDX pdx1 = new PDX();
        PDX pdx2 = new PDX();
        pdx1.setPdxID("WUXNAT01");
        pdx2.setPdxID("WUXNAT02");

        List<PDX> pdxs = new ArrayList<>();
        pdxs.add(pdx1);
        pdxs.add(pdx2);

        when(pdxService.getAllPDX()).thenReturn(pdxs);

        assertEquals(pdxApi.getAllPDX(), pdxs);
    }

    @Test
    public void testCreatePDX() {
        PDX pdx = new PDX();

        try {
            pdxApi.createPDX(pdx);
            verify(pdxService).createPDX(pdx);
            assertEquals(username, pdx.getCreatedBy());
        } catch (ResourceAlreadyExistsException e) {
            fail("Exception should not be thrown. Resource does not already exist");
        }
    }

    @Test
    public void testGetKnownPDX() {
        String pdxID = "WUXNAT01";
        PDX pdx = PDX.builder().pdxID(pdxID).build();

        when(pdxService.getPDX(pdxID)).thenReturn(Optional.of(pdx));

        try {
            assertEquals(pdx, pdxApi.getPDX(pdxID));
        } catch (NotFoundException e) {
            fail("Exception should not be thrown. PDX should be found.");
        }
    }

    @Test
    public void testGetUnknownPDX() {
        String pdxID = "WUXNAT01";

        when(pdxService.getPDX(pdxID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pdxApi.getPDX(pdxID));
    }

    @Test
    public void testPutPDXWithIDMismatch() {
        PDX pdx = PDX.builder().pdxID("junk").build();
        String pdxID = "WUXNAT01";
        assertThrows(DataFormatException.class, () -> pdxApi.createOrUpdatePDX(pdxID, pdx));
    }

    @Test
    public void testPutPDXCreation() {
        String pdxID = "WUXNAT01";
        PDX pdx = PDX.builder().pdxID(pdxID).build();

        try {
            pdxApi.createOrUpdatePDX(pdxID, pdx);
            verify(pdxService).createPDX(pdx);
            verify(pdxService, never()).updatePDX(pdx);
        } catch (DataFormatException | NotFoundException | ResourceAlreadyExistsException e) {
            fail("Exception should not be thrown.");
        }
    }

    @Test
    public void testPutPDXUpdate() {
        String pdxID = "WUXNAT01";
        PDX pdx = PDX.builder().pdxID(pdxID).build();

        try {
            doThrow(ResourceAlreadyExistsException.class).when(pdxService).createPDX(any());
            pdxApi.createOrUpdatePDX(pdxID, pdx);
            verify(pdxService).createPDX(pdx);
            verify(pdxService).updatePDX(pdx);
        } catch (DataFormatException | NotFoundException | ResourceAlreadyExistsException e) {
            fail("Exception should not be thrown.");
        }
    }

    @Test
    public void testDeletePDX() {
        String pdxID = "WUXNAT01";

        try {
            pdxApi.deletePDX(pdxID);
            verify(pdxService).deletePDX(pdxID);
        } catch (NotFoundException e) {
            fail("Exception should not be thrown.");
        }
    }
}