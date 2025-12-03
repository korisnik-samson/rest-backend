package com.samson.restbackend.services;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @PreAuthorize("hasRole('ADMIN')")
    public void rebuildSearchIndex() {
        // admin-only maintenance task
    }

}
