package com.smartcourier.tracking;

import com.smartcourier.tracking.client.DeliveryServiceClient;
import com.smartcourier.tracking.dto.DeliveryResponseDto;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceClientTest {

    @Mock
    private DeliveryServiceClient deliveryServiceClient;

    @Test
    void getDeliveryById_shouldReturnDelivery_whenExists() {
        DeliveryResponseDto dto = new DeliveryResponseDto();
        dto.setId(1L);
        dto.setTrackingNumber("SC-ABCDE12345");
        dto.setStatus("BOOKED");

        when(deliveryServiceClient.getDeliveryById(1L)).thenReturn(dto);

        DeliveryResponseDto result = deliveryServiceClient.getDeliveryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTrackingNumber()).isEqualTo("SC-ABCDE12345");
        verify(deliveryServiceClient, times(1)).getDeliveryById(1L);
    }

    @Test
    void getDeliveryById_shouldThrowFeignException_whenDeliveryNotFound() {
        when(deliveryServiceClient.getDeliveryById(999L))
                .thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> deliveryServiceClient.getDeliveryById(999L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    void getDeliveryById_shouldBeCalledOnce_perEventAddition() {
        DeliveryResponseDto dto = new DeliveryResponseDto();
        dto.setId(5L);
        when(deliveryServiceClient.getDeliveryById(5L)).thenReturn(dto);

        deliveryServiceClient.getDeliveryById(5L);

        verify(deliveryServiceClient, times(1)).getDeliveryById(5L);
        verifyNoMoreInteractions(deliveryServiceClient);
    }
}
