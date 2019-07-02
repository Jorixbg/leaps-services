package com.leaps.Leaps;

import com.leaps.LeapsApplication;
import com.leaps.payment.BraintreeGatewayFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LeapsApplication.class)
@WebAppConfiguration
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeClass
    public static void setupConfig() {
        File configFile = new File("config.properties");
        try {
            if(configFile.exists() && !configFile.isDirectory()) {
                LeapsApplication.gateway = BraintreeGatewayFactory.fromConfigFile(configFile);
            } else {
                LeapsApplication.gateway = BraintreeGatewayFactory.fromConfigMapping(System.getenv());
            }
        } catch (NullPointerException e) {
            System.err.println("Could not load Braintree configuration from config file or system environment.");
        }
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void checkoutReturnsOK() throws Exception {
        mockMvc.perform(get("/payments/checkouts"))
                .andExpect(status().isOk());
    }

    @Test
    @Ignore
    public void rendersNewView() throws Exception {
        mockMvc.perform(get("/payments/checkouts"))
                .andExpect(view().name("checkouts/new"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("clientToken"));
    }

    @Test
    @Ignore
    public void rendersErrorsOnTransactionFailure() throws Exception {
        mockMvc.perform(post("/payments/checkouts")
                .param("payment_method_nonce", "fake-valid-nonce")
                .param("amount", "2000.00"))
                .andExpect(status().isFound());
    }

    @Test
    @Ignore
    public void rendersErrorsOnInvalidAmount() throws Exception {
        mockMvc.perform(post("/payments/checkouts")
                .param("payment_method_nonce", "fake-valid-nonce")
                .param("amount", "-1.00"))
                .andExpect(status().isFound())
                .andExpect(flash().attributeExists("errorDetails"));

        mockMvc.perform(post("/payments/checkouts")
                .param("payment_method_nonce", "fake-valid-nonce")
                .param("amount", "not_a_valid_amount"))
                .andExpect(status().isFound())
                .andExpect(flash().attributeExists("errorDetails"));
    }

    @Test
    @Ignore
    public void redirectsOnTransactionNotFound() throws Exception {
        mockMvc.perform(post("/payments/checkouts/invalid-transaction"))
                .andExpect(status().isFound());
    }

    @Test
    @Ignore
    public void redirectsRootToNew() throws Exception {
        mockMvc.perform(get("/payments/"))
                .andExpect(status().isFound());
    }
}
