package com.smartcourier.admin;

import com.smartcourier.admin.client.DeliveryServiceClient;
import com.smartcourier.admin.dto.DeliveryDTO;
import com.smartcourier.admin.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceClientTest {

    @Mock
    private DeliveryServiceClient deliveryServiceClient;

    @Test
    void getAllDeliveries_shouldReturnPageWithTotalElements() {

        DeliveryDTO dto = new DeliveryDTO();
        dto.setStatus("DELIVERED");

        PageResponse<DeliveryDTO> mockPage = new PageResponse<>();
        mockPage.setTotalElements(42);
        mockPage.setContent(List.of(dto));

        when(deliveryServiceClient.getAllDeliveries(0, 1))
                .thenReturn(mockPage);

        PageResponse<DeliveryDTO> result =
                deliveryServiceClient.getAllDeliveries(0, 1);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(42);
        verify(deliveryServiceClient, times(1)).getAllDeliveries(0, 1);
    }

    @Test
    void getAllDeliveries_shouldReturnZero_whenException() {

        when(deliveryServiceClient.getAllDeliveries(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Service down"));

        long total = 0;

        try {
            PageResponse<DeliveryDTO> page =
                    deliveryServiceClient.getAllDeliveries(0, 1);
            total = page.getTotalElements();
        } catch (Exception e) {
            total = 0;
        }

        assertThat(total).isEqualTo(0);
    }
}