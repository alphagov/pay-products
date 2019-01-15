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
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

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

        Map<String, Map<String, Object>> response = results.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        healthCheck -> ImmutableMap.of(
                                HEALTHY, healthCheck.getValue().isHealthy(),
                                MESSAGE, defaultIfBlank(healthCheck.getValue().getMessage(), "Healthy"))));

        boolean healthy = results.values()
                .stream()
                .allMatch(HealthCheck.Result::isHealthy);

        if (healthy) {
            logger.info("Healthcheck OK: {}", mapper.writeValueAsString(response));
            return Response.ok().build();
        }

        logger.error("Healthcheck ERROR: {}", mapper.writeValueAsString(response));
        return status(503).entity(response).build();
    }
}
