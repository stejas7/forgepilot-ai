package io.forgepilot.enterprise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "forgepilot.data-dir=${java.io.tmpdir}/forgepilot-enterprise-test")
@AutoConfigureMockMvc
class EnterpriseAcceptanceControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void enterpriseControlPlaneAndAcceptanceSurfacesAreAvailable() throws Exception {
        mvc.perform(get("/api/enterprise/identity")).andExpect(status().isOk()).andExpect(jsonPath("$.profiles").isArray());
        mvc.perform(get("/api/enterprise/core/insights")).andExpect(status().isOk()).andExpect(jsonPath("$.totalApps").isNumber());
        mvc.perform(get("/api/enterprise/runtime/reliability")).andExpect(status().isOk()).andExpect(jsonPath("$.incidents").isArray());
        mvc.perform(get("/api/enterprise/acceptance")).andExpect(status().isOk()).andExpect(jsonPath("$.status").isString()).andExpect(jsonPath("$.checks").isArray());
    }
}
