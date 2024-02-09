package com.gallery.galleryapplication.util;

import com.gallery.galleryapplication.security.PersonDetails;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
@Component
public class ModelPreparatorForPages {
    public Model prepareModelForIndexAndFanArtPage(Model model, Page<?> pages, String currentPageUrl, int PAGESIZE){
        PersonDetails personDetails = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ANONYMOUS")) {
            personDetails = (PersonDetails) authentication.getPrincipal();
        }
        model.addAttribute("personDetails", personDetails);
        model.addAttribute("images", pages.get().toList());
        model.addAttribute("currentPage", pages.getNumber() + 1);
        model.addAttribute("totalItems", pages.getTotalElements());
        model.addAttribute("totalPages", pages.getTotalPages());
        model.addAttribute("pageSize", PAGESIZE);
        model.addAttribute("pageTitle", "home"); // to delete
        model.addAttribute("currentPageUrl",currentPageUrl);
        model.addAttribute("apiType",currentPageUrl.substring(1));
        return model;
    }
}
