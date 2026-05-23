package com.booknest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByCustomerId(int customerId);
    List<Address> findByCity(String city);
    void deleteByCustomerId(int customerId);
}
