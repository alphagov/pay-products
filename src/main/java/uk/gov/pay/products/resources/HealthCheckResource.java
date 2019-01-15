package uk.gov.pay.products.resources;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Path("/")
public class HealthCheckResource {
    private static final String HEALTHCHECK = "healthcheck";
    private static final String HEALTHY = "healthy";
    private static final String MESSAGE = "message";

    private static Logger logger = LoggerFactory.getLogger(HealthCheckResource.class);
    private static ObjectMapper mapper = new ObjectMapper();

    private Environment environment;

    @Inject
    public HealthCheckResource(Environment environment) {
        this.environment = environment;
    }

    @GET
    @Path(HEALTHCHECK)
    @Produces(APPLICATION_JSON)
    public Response healthCheck() throws JsonProcessingException {
        SortedMap<String, HealthCheck.Result> results = environment.healthChecks().runHealthChecks();

        Map<String, Map<String, Object>> response = getResponse(results);

        boolean healthy = results.size() == results.values()
                .stream()
                .filter(HealthCheck.Result::isHealthy)
                .count();

        if (healthy) {
            logger.info("Healthcheck OK: {}", mapper.writeValueAsString(response));
            return Response.ok().build();
        }

        logger.error("Healthcheck ERROR: {}", mapper.writeValueAsString(response));
        return status(503).entity(response).build();
    }

    private Map<String, Map<String, Object>> getResponse(SortedMap<String, HealthCheck.Result> results) {
        Map<String, Map<String, Object>> response = new HashMap<>();
        for (SortedMap.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
            response.put(entry.getKey(), ImmutableMap.of(
                    HEALTHY, entry.getValue().isHealthy(),
                    MESSAGE, defaultIfBlank(entry.getValue().getMessage(), "Healthy")));
        }
        return response;
    }
}
