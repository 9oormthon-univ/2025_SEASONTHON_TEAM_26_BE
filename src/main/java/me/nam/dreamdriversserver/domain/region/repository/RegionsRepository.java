package me.nam.dreamdriversserver.domain.region.repository;

import me.nam.dreamdriversserver.domain.region.entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionsRepository extends JpaRepository<Regions, Long> {
}
