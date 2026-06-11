package dev.willian.agrocontrol.service;

import dev.willian.agrocontrol.domain.Farm;
import dev.willian.agrocontrol.domain.Role;
import dev.willian.agrocontrol.domain.User;
import dev.willian.agrocontrol.dto.farm.FarmRequest;
import dev.willian.agrocontrol.dto.farm.FarmResponse;
import dev.willian.agrocontrol.exception.ResourceNotFoundException;
import dev.willian.agrocontrol.mapper.FarmMapper;
import dev.willian.agrocontrol.repository.FarmRepository;
import dev.willian.agrocontrol.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;
    @Mock
    private AuthenticatedUserProvider currentUser;

    private final FarmMapper farmMapper = Mappers.getMapper(FarmMapper.class);

    private FarmService farmService;

    private User owner;

    @BeforeEach
    void setUp() {
        farmService = new FarmService(farmRepository, farmMapper, currentUser);
        owner = User.builder().id(1L).name("Dono").email("dono@x.dev").role(Role.MANAGER).active(true).build();
    }

    @Test
    void create_shouldPersistFarmOwnedByCurrentUser() {
        FarmRequest request = new FarmRequest("Fazenda A", new BigDecimal("100.00"), "Toledo", "PR");
        when(currentUser.getCurrentUser()).thenReturn(owner);
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> {
            Farm f = inv.getArgument(0);
            f.setId(10L);
            return f;
        });

        FarmResponse response = farmService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Fazenda A");
        assertThat(response.totalAreaHa()).isEqualByComparingTo("100.00");
    }

    @Test
    void getById_whenFarmNotFound_shouldThrowResourceNotFound() {
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(farmRepository.findByIdAndUser_Id(eq(99L), eq(1L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fazenda");
    }

    @Test
    void getById_whenFound_shouldReturnMappedResponse() {
        Farm farm = Farm.builder()
                .id(5L).name("Fazenda B").totalAreaHa(new BigDecimal("250.00"))
                .city("Cascavel").state("PR").user(owner).build();
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(farmRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.of(farm));

        FarmResponse response = farmService.getById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.city()).isEqualTo("Cascavel");
    }
}
