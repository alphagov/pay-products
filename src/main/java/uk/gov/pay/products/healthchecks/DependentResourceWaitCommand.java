package uk.gov.pay.products.healthchecks;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import uk.gov.pay.products.config.ProductsConfiguration;

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
        ApplicationStartupDependentResourceChecker applicationStartupDependentResourceChecker = new ApplicationStartupDependentResourceChecker(new ApplicationStartupDependentResource(conf.getDataSourceFactory()));
        applicationStartupDependentResourceChecker.checkAndWaitForResources();
    }
}
