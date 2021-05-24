package guru.springframework.msscbrewery.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.services.CustomerService;
import guru.springframework.msscbrewery.web.model.CustomerDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @MockBean
    CustomerService customerService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    CustomerDto validCustomer;

    private final String API_BASE_PATH = "/api/v1/customer/";
    private final String ERROR_MSG_NAME_SIZE = "[\"name : size must be between 3 and 100\"]";
    private final String ERROR_MSG_NAME_BLANK = "[\"name : must not be blank\"]";

    @Before
    public void setUp() {
        validCustomer = CustomerDto.builder().id(UUID.randomUUID())
            .name("Customer1")
            .build();
    }

    @Test
    public void getCustomer() throws Exception {
        given(customerService.getCustomerById(any(UUID.class))).willReturn(validCustomer);

        mockMvc.perform(get(API_BASE_PATH + validCustomer.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(validCustomer.getId().toString())))
            .andExpect(jsonPath("$.name", is(validCustomer.getName())));
    }

    @Test
    public void handlePost() throws Exception {
        //given
        CustomerDto customer = validCustomer;
        CustomerDto savedDto = CustomerDto.builder().id(UUID.randomUUID()).name("New Customer").build();
        String customerDtoJson = objectMapper.writeValueAsString(customer);

        given(customerService.saveNewCustomer(any())).willReturn(savedDto);

        mockMvc.perform(post(API_BASE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerDtoJson))
            .andExpect(status().isCreated());
    }

    @Test
    public void handleUpdate() throws Exception {
        //given
        CustomerDto customer = validCustomer;
        customer.setId(null);
        String customerDtoJson = objectMapper.writeValueAsString(customer);

        //when
        mockMvc.perform(put(API_BASE_PATH + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerDtoJson))
            .andExpect(status().isNoContent());

        then(customerService).should().updateCustomer(any(), any());
    }

    @Test
    public void handleDelete() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        then(customerService).should().deleteById(any());
    }

    @Test
    public void givenNullNameWhenHandlePostThenThrowException() throws Exception {
        //given
        CustomerDto customer = validCustomer;
        customer.setName(null);
        String customerDtoJson = objectMapper.writeValueAsString(customer);

        mockMvc.perform(post(API_BASE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerDtoJson))
            .andExpect(content().string(ERROR_MSG_NAME_BLANK))
            .andExpect(status().isBadRequest())
            .andReturn();

        verify(customerService, times(0)).saveNewCustomer(any());
    }

    @Test
    public void givenInvalidNameSizeWhenHandlePostThenThrowException() throws Exception {
        //given
        CustomerDto customer = validCustomer;
        customer.setName("A");
        String customerDtoJson = objectMapper.writeValueAsString(customer);

        mockMvc.perform(post(API_BASE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerDtoJson))
            .andExpect(content().string(ERROR_MSG_NAME_SIZE))
            .andExpect(status().isBadRequest())
            .andReturn();

        verify(customerService, times(0)).saveNewCustomer(any());
    }


    @Test
    public void givenNullNameWhenHandleUpdateThenThrowException() throws Exception {
        //given
        CustomerDto customer = validCustomer;
        customer.setName(null);
        String customerDtoJson = objectMapper.writeValueAsString(customer);

        mockMvc.perform(put(API_BASE_PATH + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerDtoJson))
            .andExpect(content().string(ERROR_MSG_NAME_BLANK))
            .andExpect(status().isBadRequest())
            .andReturn();

        verify(customerService, times(0)).updateCustomer(any(), any());
    }

    @Test
    public void givenInvalidNameSizeWhenHandleUpdateThenThrowException() throws Exception {
        //given
        CustomerDto customer = validCustomer;
        customer.setName("A");
        String customerDtoJson = objectMapper.writeValueAsString(customer);

        mockMvc.perform(put(API_BASE_PATH + UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(customerDtoJson))
            .andExpect(content().string(ERROR_MSG_NAME_SIZE))
            .andExpect(status().isBadRequest())
            .andReturn();

        verify(customerService, times(0)).updateCustomer(any(), any());
    }
}