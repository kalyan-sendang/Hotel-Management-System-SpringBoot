package com.java.hospitalmanagementsystem.repositories;

import com.java.hospitalmanagementsystem.models.MenuType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for MenuType entities. Extends JpaRepository to facilitate database
 * operations for menu types. Includes a method to find a MenuType by its name.
 */
public interface MenuTypeRepository extends JpaRepository<MenuType, Integer> {
  MenuType findByName(MenuType.MenuEnum menuTypeName);
}