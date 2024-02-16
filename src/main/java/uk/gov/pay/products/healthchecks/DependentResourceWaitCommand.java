package uk.gov.pay.products.healthchecks;

import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.service.payments.commons.utils.startup.ApplicationStartupDependentResourceChecker;
import uk.gov.service.payments.commons.utils.startup.DatabaseStartupResource;

public class DependentResourceWaitCommand extends ConfiguredCommand<ProductsConfiguration> {
    public DependentResourceWaitCommand() {
        super("waitOnDependencies", "Waits for dependent resources to become available");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
    }

    @Override
    protected void run(Bootstrap<ProductsConfiguration> bs, Namespace ns, ProductsConfiguration conf) {
        new ApplicationStartupDependentResourceChecker(new DatabaseStartupResource(conf.getDataSourceFactory()))
                .checkAndWaitForResource();
    }
}
