package uk.gov.pay.products.resources;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.dropwizard.core.setup.Environment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Path("/")
public class HealthCheckResource {
    private final Environment environment;

    @Inject
    public HealthCheckResource(Environment environment) {
        this.environment = environment;
    }

    @GET
    @Path("healthcheck")
    @Produces(APPLICATION_JSON)
    @Operation(
            tags = "Other",
            summary = "Healthcheck endpoint for products. Check database, deadlocks and ping",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(example = "{" +
                            "    \"database\": {" +
                            "        \"healthy\": true," +
                            "        \"message\": \"Healthy\"" +
                            "    }," +
                            "    \"ping\": {" +
                            "        \"healthy\": true," +
                            "        \"message\": \"Healthy\"" +
                            "    }," +
                            "    \"deadlocks\": {" +
                            "        \"healthy\": true," +
                            "        \"message\": \"Healthy\"" +
                            "    }" +
                            "}")), description = "OK"),
                    @ApiResponse(responseCode = "503", description = "Service unavailable. If any healthchecks fail")
            }
    )
    public Response healthCheck() {
        SortedMap<String, HealthCheck.Result> results = environment.healthChecks().runHealthChecks();

        Map<String, Map<String, Object>> response = results.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        healthCheck -> ImmutableMap.of(
                                "healthy", healthCheck.getValue().isHealthy(),
                                "message", defaultIfBlank(healthCheck.getValue().getMessage(), "Healthy"))));

        Response.Status status = allHealthy(results.values()) ? OK : SERVICE_UNAVAILABLE;

        return Response.status(status).entity(response).build();
    }

    private boolean allHealthy(Collection<HealthCheck.Result> results) {
        return results.stream().allMatch(HealthCheck.Result::isHealthy);
    }
}
