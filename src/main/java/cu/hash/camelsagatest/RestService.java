package cu.hash.camelsagatest;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class RestService extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        rest("/saga")
            .bindingMode(RestBindingMode.off)
            .post("/test")
            .to("direct:buy");

        from("direct:buy")
                .saga()
                .to("direct:newOrder")
                .to("direct:reserveCredit")
                .end();

        from("direct:newOrder")
                .saga()
                .propagation(SagaPropagation.SUPPORTS)
                .compensation("direct:cancelOrder")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .process(exc->{
                    String rs = "";
                })
                .end();

        from("direct:cancelOrder")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("Order cancelled");

        from("direct:reserveCredit")
                .saga()
                .propagation(SagaPropagation.SUPPORTS)
                .compensation("direct:refundCredit")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .setHeader("amount",constant(5))
                .log("Credit ${header.amount} reserved in action ${body}")
                .process(exchange -> {
                    throw new Exception("to saga");
                })
                .end()
;
        from("direct:refundCredit")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("Credit for action refunded");

    }
}
