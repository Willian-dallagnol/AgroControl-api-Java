package dev.willian.agrocontrol.service;

import dev.willian.agrocontrol.domain.Farm;
import dev.willian.agrocontrol.domain.Field;
import dev.willian.agrocontrol.domain.Role;
import dev.willian.agrocontrol.domain.User;
import dev.willian.agrocontrol.dto.field.FieldRequest;
import dev.willian.agrocontrol.dto.field.FieldResponse;
import dev.willian.agrocontrol.exception.BusinessRuleException;
import dev.willian.agrocontrol.exception.ResourceNotFoundException;
import dev.willian.agrocontrol.mapper.FieldMapper;
import dev.willian.agrocontrol.repository.FarmRepository;
import dev.willian.agrocontrol.repository.FieldRepository;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldServiceTest {

    @Mock
    private FieldRepository fieldRepository;
    @Mock
    private FarmRepository farmRepository;
    @Mock
    private AuthenticatedUserProvider currentUser;

    private final FieldMapper fieldMapper = Mappers.getMapper(FieldMapper.class);

    private FieldService fieldService;
    private Farm farm;

    @BeforeEach
    void setUp() {
        fieldService = new FieldService(fieldRepository, farmRepository, fieldMapper, currentUser);
        User owner = User.builder().id(1L).name("Dono").email("d@x.dev").role(Role.MANAGER).active(true).build();
        farm = Farm.builder().id(7L).name("Fazenda").totalAreaHa(new BigDecimal("100.00")).user(owner).build();
    }

    @Test
    void create_whenAreaExceedsFarmCapacity_shouldThrowBusinessRule() {
        FieldRequest request = new FieldRequest("Talhao X", new BigDecimal("30.00"), "Argiloso", 7L);
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(farmRepository.findByIdAndUser_Id(7L, 1L)).thenReturn(Optional.of(farm));
        when(fieldRepository.sumAreaByFarmId(eq(7L), isNull())).thenReturn(new BigDecimal("80.00"));

        assertThatThrownBy(() -> fieldService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Capacidade da fazenda excedida");

        verify(fieldRepository, never()).save(any());
    }

    @Test
    void create_whenWithinCapacity_shouldPersistField() {
        FieldRequest request = new FieldRequest("Talhao Y", new BigDecimal("20.00"), "Argiloso", 7L);
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(farmRepository.findByIdAndUser_Id(7L, 1L)).thenReturn(Optional.of(farm));
        when(fieldRepository.sumAreaByFarmId(eq(7L), isNull())).thenReturn(new BigDecimal("80.00"));
        when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> {
            Field f = inv.getArgument(0);
            f.setId(50L);
            return f;
        });

        FieldResponse response = fieldService.create(request);

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.farmId()).isEqualTo(7L);
        assertThat(response.areaHa()).isEqualByComparingTo("20.00");
    }

    @Test
    void create_whenFarmNotOwned_shouldThrowResourceNotFound() {
        FieldRequest request = new FieldRequest("Talhao Z", new BigDecimal("10.00"), null, 999L);
        when(currentUser.getCurrentUserId()).thenReturn(1L);
        when(farmRepository.findByIdAndUser_Id(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fieldService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Fazenda");
    }
}
